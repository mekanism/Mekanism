package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketQIOCraftingWindowSelect {

    private final byte windowIndex;

    public PacketQIOCraftingWindowSelect(byte windowIndex) {
        this.windowIndex = windowIndex;
    }

    public static void handle(PacketQIOCraftingWindowSelect message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player != null && player.openContainer instanceof QIOItemViewerContainer) {
                QIOItemViewerContainer container = (QIOItemViewerContainer) player.openContainer;
                container.setSelectedCraftingGrid(player.getUniqueID(), message.windowIndex);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketQIOCraftingWindowSelect pkt, PacketBuffer buf) {
        buf.writeByte(pkt.windowIndex);
    }

    public static PacketQIOCraftingWindowSelect decode(PacketBuffer buf) {
        return new PacketQIOCraftingWindowSelect(buf.readByte());
    }
}
