package mekanism.common.network;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularHashedItemSource;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketQIOFillCraftingWindow {

    private final Byte2ObjectMap<SingularHashedItemSource> sources;
    private final ResourceLocation recipeID;
    private final boolean maxTransfer;

    public PacketQIOFillCraftingWindow(ResourceLocation recipeID, boolean maxTransfer, Byte2ObjectMap<SingularHashedItemSource> sources) {
        this.recipeID = recipeID;
        this.sources = sources;
        this.maxTransfer = maxTransfer;
    }

    public static void handle(PacketQIOFillCraftingWindow message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player != null && player.openContainer instanceof QIOItemViewerContainer && player.world instanceof ServerWorld) {
                QIOItemViewerContainer container = (QIOItemViewerContainer) player.openContainer;
                byte selectedCraftingGrid = container.getSelectedCraftingGrid(player.getUniqueID());
                if (selectedCraftingGrid == -1) {
                    Mekanism.logger.warn("Received transfer request from: {}, but they do not currently have a crafting window open.", player);
                } else {
                    Optional<? extends IRecipe<?>> optionalIRecipe = player.world.getRecipeManager().getRecipe(message.recipeID);
                    if (optionalIRecipe.isPresent()) {
                        IRecipe<?> recipe = optionalIRecipe.get();
                        if (recipe instanceof ICraftingRecipe) {
                            transferRecipe(container, (ICraftingRecipe) recipe, message.sources, message.maxTransfer, selectedCraftingGrid, player);
                        } else {
                            Mekanism.logger.warn("Received transfer request from: {}, but the type ({}) of the specified recipe was not a crafting recipe.",
                                  player, recipe.getClass());
                        }
                    } else {
                        Mekanism.logger.warn("Received transfer request from: {}, but could not find specified recipe.", player);
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketQIOFillCraftingWindow pkt, PacketBuffer buf) {
        buf.writeResourceLocation(pkt.recipeID);
        buf.writeBoolean(pkt.maxTransfer);
        //Cast to byte as this should always be at most 9
        buf.writeByte((byte) pkt.sources.size());
        for (Byte2ObjectMap.Entry<SingularHashedItemSource> entry : pkt.sources.byte2ObjectEntrySet()) {
            //Target Slot
            buf.writeByte(entry.getByteKey());
            //Source slot
            SingularHashedItemSource source = entry.getValue();
            byte sourceSlot = source.getSlot();
            //We "cheat" here by just writing the source slot regardless of if we are in the crafting window, main inventory, or QIO
            // as then we can use the not a valid value as indication that we have a UUID following for QIO source, and otherwise we
            // get away with not having to write some sort of identifier for which type of data we are transferring
            buf.writeByte(sourceSlot);
            if (sourceSlot == -1) {
                //If we don't actually have a source slot, that means we need to write the UUID
                // as it is being transferred out of the QIO
                UUID qioSource = source.getQioSource();
                if (qioSource == null) {
                    throw new IllegalStateException("Invalid QIO crafting window transfer source.");
                }
                buf.writeUniqueId(qioSource);
            }
        }
    }

    public static PacketQIOFillCraftingWindow decode(PacketBuffer buf) {
        ResourceLocation recipeID = buf.readResourceLocation();
        boolean maxTransfer = buf.readBoolean();
        byte slotCount = buf.readByte();
        Byte2ObjectMap<SingularHashedItemSource> sources = new Byte2ObjectArrayMap<>(slotCount);
        for (byte slot = 0; slot < slotCount; slot++) {
            byte targetSlot = buf.readByte();
            byte sourceSlot = buf.readByte();
            if (sourceSlot == -1) {
                sources.put(targetSlot, new SingularHashedItemSource(buf.readUniqueId()));
            } else {
                sources.put(targetSlot, new SingularHashedItemSource(sourceSlot));
            }
        }
        return new PacketQIOFillCraftingWindow(recipeID, maxTransfer, sources);
    }

    private static void transferRecipe(QIOItemViewerContainer container, ICraftingRecipe recipe, Byte2ObjectMap<SingularHashedItemSource> sources, boolean maxTransfer,
          byte selectedCraftingGrid, PlayerEntity player) {
        QIOFrequency frequency = container.getFrequency();
        CraftingInventory dummy = MekanismUtils.getDummyCraftingInv();
        QIOCraftingWindow craftingWindow = container.getCraftingWindow(selectedCraftingGrid);
        for (Byte2ObjectMap.Entry<SingularHashedItemSource> entry : sources.byte2ObjectEntrySet()) {
            ItemStack stack;
            SingularHashedItemSource source = entry.getValue();
            byte slot = source.getSlot();
            if (slot == -1) {
                UUID qioSource = source.getQioSource();
                if (qioSource == null) {
                    Mekanism.logger.warn("Received transfer request from: {} with no valid source.", player);
                    return;
                }
                if (frequency == null) {
                    Mekanism.logger.warn("Received transfer request from: {}, with a QIO source but no selected frequency.", player);
                    return;
                }
                HashedItem storedItem = frequency.getTypeByUUID(qioSource);
                if (storedItem == null) {
                    Mekanism.logger.warn("Received transfer request from: {}, could not find stored item with UUID: {}.", player, qioSource);
                    return;
                }
                stack = storedItem.getStack();
            } else if (slot >= 0 && slot < 9 + PlayerInventory.getHotbarSize() + 27) {
                if (slot < 9) {
                    //Crafting Window
                    stack = craftingWindow.getInputSlot(slot).getStack();
                } else {
                    //Hotbar/main inventory
                    stack = player.inventory.mainInventory.get(slot - 9);
                }
            } else {
                Mekanism.logger.warn("Received transfer request from: {}, with an invalid slot id: {}.", player, slot);
                return;
            }
            dummy.setInventorySlotContents(entry.getByteKey(), stack);
        }
        if (!recipe.matches(dummy, player.world)) {
            Mekanism.logger.warn("Received transfer request from: {}, but source items aren't valid for the requested recipe: {}.", player, recipe.getId());
            return;
        }
        //TODO: Transfer the stacks to the proper places

    }
}