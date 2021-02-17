package mekanism.common.network.to_server;

import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketQIOCraftingWindowSelect implements IMekanismPacket {

    private final byte windowIndex;

    public PacketQIOCraftingWindowSelect(byte windowIndex) {
        this.windowIndex = windowIndex;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null && player.openContainer instanceof QIOItemViewerContainer) {
            QIOItemViewerContainer container = (QIOItemViewerContainer) player.openContainer;
            container.setSelectedCraftingGrid(player.getUniqueID(), windowIndex);
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeByte(windowIndex);
    }

    public static PacketQIOCraftingWindowSelect decode(PacketBuffer buffer) {
        return new PacketQIOCraftingWindowSelect(buffer.readByte());
    }
}
