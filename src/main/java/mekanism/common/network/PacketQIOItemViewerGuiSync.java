package mekanism.common.network;

import java.util.Map;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
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
        this.itemMap = itemMap;
        this.countCapacity = countCapacity;
        this.typeCapacity = typeCapacity;
    }

    public static PacketQIOItemViewerGuiSync batch(Map<HashedItem, Long> itemMap, long countCapacity, int typeCapacity) {
        return new PacketQIOItemViewerGuiSync(Type.BATCH, itemMap, countCapacity, typeCapacity);
    }

    public static PacketQIOItemViewerGuiSync update(Map<HashedItem, Long> itemMap, long countCapacity, int typeCapacity) {
        return new PacketQIOItemViewerGuiSync(Type.UPDATE, itemMap, countCapacity, typeCapacity);
    }

    public static PacketQIOItemViewerGuiSync kill() {
        return new PacketQIOItemViewerGuiSync(Type.UPDATE, null, 0, 0);
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
                    case KILL:
                        container.handleKill();
                        break;
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketQIOItemViewerGuiSync pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.type);
        if (pkt.type == Type.BATCH || pkt.type == Type.UPDATE) {
            buf.writeVarLong(pkt.countCapacity);
            buf.writeVarInt(pkt.typeCapacity);
            buf.writeVarInt(pkt.itemMap.size());
            pkt.itemMap.entrySet().forEach(e -> {
                buf.writeItemStack(e.getKey().getStack());
                buf.writeVarLong(e.getValue());
            });
        }
    }

    public static PacketQIOItemViewerGuiSync decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        long countCapacity = 0;
        int typeCapacity = 0;
        Map<HashedItem, Long> map = null;
        if (type == Type.BATCH || type == Type.UPDATE) {
            countCapacity = buf.readVarLong();
            typeCapacity = buf.readVarInt();
            map = new Object2ObjectOpenHashMap<>();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                map.put(new HashedItem(buf.readItemStack()), buf.readVarLong());
            }
        }
        return new PacketQIOItemViewerGuiSync(type, map, countCapacity, typeCapacity);
    }

    public enum Type {
        BATCH,
        UPDATE,
        KILL;
    }
}
