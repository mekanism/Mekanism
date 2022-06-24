package mekanism.common.network.to_server;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketGuiItemDataRequest implements IMekanismPacket {

    private final Type type;

    private PacketGuiItemDataRequest(Type type) {
        this.type = type;
    }

    public static PacketGuiItemDataRequest qioItemViewer() {
        return new PacketGuiItemDataRequest(Type.QIO_ITEM_VIEWER);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            if (type == Type.QIO_ITEM_VIEWER) {
                if (player.containerMenu instanceof QIOItemViewerContainer container) {
                    QIOFrequency freq = container.getFrequency();
                    if (!player.level.isClientSide() && freq != null) {
                        freq.openItemViewer(player);
                    }
                }
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
    }

    public static PacketGuiItemDataRequest decode(FriendlyByteBuf buffer) {
        return new PacketGuiItemDataRequest(buffer.readEnum(Type.class));
    }

    private enum Type {
        QIO_ITEM_VIEWER
    }
}
