package mekanism.common.network.to_server;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public record PacketTransferMekanismRecipe(Int2ObjectMap<ResourceStack<ItemResource>> itemsTransferred) implements IMekanismPacket {

    public static final Type<PacketTransferMekanismRecipe> TYPE = new Type<>(Mekanism.rl("transfer_recipe"));
    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<ResourceStack<ItemResource>>> ITEMS_TRANSFERRED_CODEC = ByteBufCodecs.map(
          Int2ObjectOpenHashMap::new, ByteBufCodecs.VAR_INT, StreamCodec.composite(
                ItemResource.STREAM_CODEC, ResourceStack::resource,
                ByteBufCodecs.VAR_INT, ResourceStack::amount,
                ResourceStack::new
          ));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTransferMekanismRecipe> STREAM_CODEC = StreamCodec.composite(
          ITEMS_TRANSFERRED_CODEC, PacketTransferMekanismRecipe::itemsTransferred,
          PacketTransferMekanismRecipe::new
    );

    @Override
    public Type<PacketTransferMekanismRecipe> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (player.containerMenu instanceof MekanismContainer container) {
            RecipeContainers recipeContainers = container.getRecipeContainers();
            if (recipeContainers == null) {
                Mekanism.logger.warn("Received transfer request from: {}, but the container did not have any recipe containers.", player);
            } else {
                tryTransfer(player, recipeContainers.inputs());
            }
        }
    }

    private void tryTransfer(Player player, List<? extends IResourceContainer<?>> inputContainers) {
        ResourceHandler<ItemResource> playerInventory = PlayerInventoryWrapper.of(player).getMainSlots();
        try (Transaction transaction = Transaction.openRoot()) {
            Object2IntMap<ItemResource> resourcesToReturn = new Object2IntOpenHashMap<>();
            //Extract from the slots if necessary
            for (ObjectIterator<Int2ObjectMap.Entry<ResourceStack<ItemResource>>> iterator = Int2ObjectMaps.fastIterator(itemsTransferred); iterator.hasNext(); ) {
                Int2ObjectMap.Entry<ResourceStack<ItemResource>> entry = iterator.next();
                int slotUsedFor = entry.getIntKey();
                if (slotUsedFor < 0 || slotUsedFor >= inputContainers.size()) {
                    Mekanism.logger.warn("Received transfer request from: {}, with invalid slot index: {}.", player, slotUsedFor);
                    return;
                }
                if (!(inputContainers.get(slotUsedFor) instanceof IInventorySlot slot)) {
                    Mekanism.logger.warn("Received transfer request from: {}, with container index: {}, but that container is not a slot.", player, slotUsedFor);
                    return;
                }
                ItemResource resource = entry.getValue().resource();
                ItemResource currentType = slot.resource();
                if (!currentType.isEmpty() && !currentType.equals(resource)) {
                    //Resource is different, we have to start by trying to extract it
                    int stored = slot.amountAsInt();
                    if (slot.extract(currentType, stored, transaction, AutomationType.MANUAL) < stored || !slot.isEmpty()) {
                        //Bail, couldn't remove input item
                        Mekanism.logger.warn("Received transfer request from: {}, with slot index: {}, but could not extract contents from that slot.", player, slotUsedFor);
                        return;
                    }
                    resourcesToReturn.mergeInt(currentType, stored, Integer::sum);
                }
            }
            //Insert into the slots
            for (ObjectIterator<Int2ObjectMap.Entry<ResourceStack<ItemResource>>> iterator = Int2ObjectMaps.fastIterator(itemsTransferred); iterator.hasNext(); ) {
                Int2ObjectMap.Entry<ResourceStack<ItemResource>> entry = iterator.next();
                int slotUsedFor = entry.getIntKey();
                IInventorySlot slot = (IInventorySlot) inputContainers.get(slotUsedFor);
                ItemResource resource = entry.getValue().resource();
                int amountToTransfer = entry.getValue().amount();
                if (resourcesToReturn.containsKey(resource)) {
                    int inLimbo = resourcesToReturn.getInt(resource);
                    int available = Math.min(inLimbo, amountToTransfer);
                    if (slot.insert(resource, available, transaction, AutomationType.MANUAL) != available) {
                        Mekanism.logger.warn("Received transfer request from: {}, with slot index: {}, but could not move the expected items ({} of {}) from another slot to that slot.",
                              player, slotUsedFor, available, resource);
                        return;
                    } else if (amountToTransfer < inLimbo) {
                        resourcesToReturn.put(resource, inLimbo - amountToTransfer);
                        continue;
                    }
                    resourcesToReturn.removeInt(resource);
                    if (amountToTransfer == inLimbo) {
                        continue;
                    }
                    amountToTransfer -= inLimbo;
                }
                if (playerInventory.extract(resource, amountToTransfer, transaction) < amountToTransfer ||
                    slot.insert(resource, amountToTransfer, transaction, AutomationType.MANUAL) != amountToTransfer) {
                    Mekanism.logger.warn("Received transfer request from: {}, with slot index: {}, but could not transfer the expected items ({} of {}) from the player's inventory to that slot.",
                          player, slotUsedFor, amountToTransfer, resource);
                    return;
                }
            }
            for (ObjectIterator<Object2IntMap.Entry<ItemResource>> iterator = Object2IntMaps.fastIterator(resourcesToReturn); iterator.hasNext(); ) {
                Object2IntMap.Entry<ItemResource> entry = iterator.next();
                int toInsert = entry.getIntValue();
                if (ResourceHandlerUtil.insertStacking(playerInventory, entry.getKey(), toInsert, transaction) < toInsert) {
                    Mekanism.logger.warn("Received transfer request from: {}, but could not return items currently in recipe slots to the player.", player);
                    return;
                }
            }
            transaction.commit();
        }
    }
}
