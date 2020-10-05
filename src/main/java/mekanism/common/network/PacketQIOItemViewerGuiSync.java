package mekanism.common.network;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.function.Supplier;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketQIOItemViewerGuiSync {

    private final Type type;
    private final Object2LongMap<UUIDAwareHashedItem> itemMap;
    private final long countCapacity;
    private final int typeCapacity;

    private PacketQIOItemViewerGuiSync(Type type, Object2LongMap<UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
        this.type = type;
        this.itemMap = itemMap;
        this.countCapacity = countCapacity;
        this.typeCapacity = typeCapacity;
    }

    public static PacketQIOItemViewerGuiSync batch(Object2LongMap<UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
        return new PacketQIOItemViewerGuiSync(Type.BATCH, itemMap, countCapacity, typeCapacity);
    }

    public static PacketQIOItemViewerGuiSync update(Object2LongMap<UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
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
            pkt.itemMap.forEach((key, value) -> {
                buf.writeItemStack(key.getStack());
                if (key.getUUID() == null) {
                    //Shouldn't be null unless something failed, but if it does try to handle it relatively gracefully
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    buf.writeUniqueId(key.getUUID());
                }
                buf.writeVarLong(value);
            });
        }
    }

    public static PacketQIOItemViewerGuiSync decode(PacketBuffer buf) {
        Type type = buf.readEnumValue(Type.class);
        long countCapacity = 0;
        int typeCapacity = 0;
        Object2LongMap<UUIDAwareHashedItem> map = null;
        if (type == Type.BATCH || type == Type.UPDATE) {
            countCapacity = buf.readVarLong();
            typeCapacity = buf.readVarInt();
            int count = buf.readVarInt();
            map = new Object2LongOpenHashMap<>(count);
            for (int i = 0; i < count; i++) {
                map.put(new UUIDAwareHashedItem(buf.readItemStack(), buf.readBoolean() ? buf.readUniqueId() : null), buf.readVarLong());
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
