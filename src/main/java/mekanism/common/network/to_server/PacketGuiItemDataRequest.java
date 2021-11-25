package mekanism.common.network.to_server;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
        ServerPlayerEntity player = context.getSender();
        if (player != null) {
            if (type == Type.QIO_ITEM_VIEWER) {
                if (player.containerMenu instanceof QIOItemViewerContainer) {
                    QIOItemViewerContainer container = (QIOItemViewerContainer) player.containerMenu;
                    QIOFrequency freq = container.getFrequency();
                    if (!player.level.isClientSide() && freq != null) {
                        freq.openItemViewer(player);
                    }
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
    }

    public static PacketGuiItemDataRequest decode(PacketBuffer buffer) {
        Type type = buffer.readEnum(Type.class);
        return new PacketGuiItemDataRequest(type);
    }

    private enum Type {
        QIO_ITEM_VIEWER
    }
}
