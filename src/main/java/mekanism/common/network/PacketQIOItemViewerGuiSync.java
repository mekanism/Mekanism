package mekanism.common.network;

import java.util.Map;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketQIOItemViewerGuiSync {

    private Type type;
    private Map<HashedItem, Long> itemMap;
    private long countCapacity;
    private int typeCapacity;

    private PacketQIOItemViewerGuiSync(Type type, Map<HashedItem, Long> itemMap, long countCapacity, int typeCapacity) {
        this.type = type;
        this.countCapacity = countCapacity;
        this.typeCapacity = typeCapacity;
        itemMap = new Object2ObjectOpenHashMap<>();
    }

    public static PacketQIOItemViewerGuiSync batch(Map<HashedItem, Long> itemMap, long countCapacity, int typeCapacity) {
        return new PacketQIOItemViewerGuiSync(Type.BATCH, itemMap, countCapacity, typeCapacity);
    }

    public static PacketQIOItemViewerGuiSync update(Map<HashedItem, Long> itemMap, long countCapacity, int typeCapacity) {
        return new PacketQIOItemViewerGuiSync(Type.UPDATE, itemMap, countCapacity, typeCapacity);
    }

    public static void handle(PacketQIOItemViewerGuiSync message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (player.openContainer instanceof QIOItemViewerContainer) {
                QIOItemViewerContainer container = (QIOItemViewerContainer) player.openContainer;
                switch (message.type) {
                    case BATCH:
                        container.handleBatchUpdate(message.itemMap, message.countCapacity, message.typeCapacity);
                        break;
                    case UPDATE:
                        container.handleUpdate(message.itemMap, message.countCapacity, message.typeCapacity);
                        break;
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketQIOItemViewerGuiSync pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.type);
        buf.writeVarLong(pkt.countCapacity);
        buf.writeVarInt(pkt.typeCapacity);
        pkt.itemMap.entrySet().forEach(e -> {
            buf.writeItemStack(e.getKey().getStack());
            buf.writeLong(e.getValue());
        });
    }

    public static PacketQIOItemViewerGuiSync decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        long countCapacity = buf.readVarLong();
        int typeCapacity = buf.readVarInt();
        Map<HashedItem, Long> map = new Object2ObjectOpenHashMap<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            map.put(new HashedItem(buf.readItemStack()), buf.readLong());
        }
        return new PacketQIOItemViewerGuiSync(type, map, countCapacity, typeCapacity);
    }

    public enum Type {
        BATCH,
        UPDATE;
    }
}
