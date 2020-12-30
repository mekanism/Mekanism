package mekanism.common.network;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularHashedItemSource;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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
            //TODO: Add warnings if things are invalid?
            if (player != null && player.openContainer instanceof QIOItemViewerContainer) {
                QIOItemViewerContainer container = (QIOItemViewerContainer) player.openContainer;
                byte selectedCraftingGrid = container.getSelectedCraftingGrid(player.getUniqueID());
                if (selectedCraftingGrid != -1) {
                    //TODO: Validate that the recipe we are trying to set exists and the items we want to set in the slots
                    // are valid for the recipe, if it isn't fail and print out a warning
                    //TODO: Transfer the stacks to the proper places
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
}