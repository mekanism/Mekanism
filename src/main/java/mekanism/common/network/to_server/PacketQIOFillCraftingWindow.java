package mekanism.common.network.to_server;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularHashedItemSource;
import mekanism.common.content.qio.QIOServerCraftingTransferHandler;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketQIOFillCraftingWindow implements IMekanismPacket {

    private final Byte2ObjectMap<List<SingularHashedItemSource>> sources;
    private final ResourceLocation recipeID;
    private final boolean maxTransfer;

    //Note: While our logic is not dependent on knowing about maxTransfer, we make use of it for encoding and decoding
    // as when it is false we can reduce how many bytes the packet is by a good amount by making assumptions about the sizes of things
    public PacketQIOFillCraftingWindow(ResourceLocation recipeID, boolean maxTransfer, Byte2ObjectMap<List<SingularHashedItemSource>> sources) {
        this.recipeID = recipeID;
        this.sources = sources;
        this.maxTransfer = maxTransfer;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null && player.containerMenu instanceof QIOItemViewerContainer) {
            QIOItemViewerContainer container = (QIOItemViewerContainer) player.containerMenu;
            byte selectedCraftingGrid = container.getSelectedCraftingGrid(player.getUUID());
            if (selectedCraftingGrid == -1) {
                Mekanism.logger.warn("Received transfer request from: {}, but they do not currently have a crafting window open.", player);
            } else {
                Optional<? extends IRecipe<?>> optionalRecipe = player.level.getRecipeManager().byKey(recipeID);
                if (optionalRecipe.isPresent()) {
                    IRecipe<?> recipe = optionalRecipe.get();
                    if (recipe instanceof ICraftingRecipe) {
                        QIOServerCraftingTransferHandler.tryTransfer(container, selectedCraftingGrid, player, recipeID, (ICraftingRecipe) recipe, sources);
                    } else {
                        Mekanism.logger.warn("Received transfer request from: {}, but the type ({}) of the specified recipe was not a crafting recipe.",
                              player, recipe.getClass());
                    }
                } else {
                    Mekanism.logger.warn("Received transfer request from: {}, but could not find specified recipe.", player);
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeResourceLocation(recipeID);
        buffer.writeBoolean(maxTransfer);
        //Cast to byte as this should always be at most 9
        buffer.writeByte((byte) sources.size());
        for (Byte2ObjectMap.Entry<List<SingularHashedItemSource>> entry : sources.byte2ObjectEntrySet()) {
            //Target Slot
            buffer.writeByte(entry.getByteKey());
            //Source slot
            List<SingularHashedItemSource> slotSources = entry.getValue();
            if (maxTransfer) {
                //We "cheat" by only writing the list size if we are transferring as many items as possible as
                // the list will always be of size one
                buffer.writeVarInt(slotSources.size());
            }
            for (SingularHashedItemSource source : slotSources) {
                byte sourceSlot = source.getSlot();
                //We "cheat" here by just writing the source slot regardless of if we are in the crafting window, main inventory, or QIO
                // as then we can use the not a valid value as indication that we have a UUID following for QIO source, and otherwise we
                // get away with not having to write some sort of identifier for which type of data we are transferring
                buffer.writeByte(sourceSlot);
                if (maxTransfer) {
                    //We "cheat" by only writing the amount used if we are transferring as many items as possible as
                    // this will always just be one
                    buffer.writeVarInt(source.getUsed());
                }
                if (sourceSlot == -1) {
                    //If we don't actually have a source slot, that means we need to write the UUID
                    // as it is being transferred out of the QIO
                    UUID qioSource = source.getQioSource();
                    if (qioSource == null) {
                        throw new IllegalStateException("Invalid QIO crafting window transfer source.");
                    }
                    buffer.writeUUID(qioSource);
                }
            }
        }
    }

    public static PacketQIOFillCraftingWindow decode(PacketBuffer buffer) {
        ResourceLocation recipeID = buffer.readResourceLocation();
        boolean maxTransfer = buffer.readBoolean();
        byte slotCount = buffer.readByte();
        Byte2ObjectMap<List<SingularHashedItemSource>> sources = new Byte2ObjectArrayMap<>(slotCount);
        for (byte slot = 0; slot < slotCount; slot++) {
            byte targetSlot = buffer.readByte();
            int subSourceCount = maxTransfer ? buffer.readVarInt() : 1;
            List<SingularHashedItemSource> slotSources = new ArrayList<>(subSourceCount);
            sources.put(targetSlot, slotSources);
            for (int i = 0; i < subSourceCount; i++) {
                byte sourceSlot = buffer.readByte();
                int count = maxTransfer ? buffer.readVarInt() : 1;
                if (sourceSlot == -1) {
                    slotSources.add(new SingularHashedItemSource(buffer.readUUID(), count));
                } else {
                    slotSources.add(new SingularHashedItemSource(sourceSlot, count));
                }
            }
        }
        return new PacketQIOFillCraftingWindow(recipeID, maxTransfer, sources);
    }
}