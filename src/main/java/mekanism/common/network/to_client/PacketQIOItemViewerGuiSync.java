package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketQIOItemViewerGuiSync implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof QIOItemViewerContainer) {
            QIOItemViewerContainer container = (QIOItemViewerContainer) player.containerMenu;
            switch (type) {
                case BATCH:
                    container.handleBatchUpdate(itemMap, countCapacity, typeCapacity);
                    break;
                case UPDATE:
                    container.handleUpdate(itemMap, countCapacity, typeCapacity);
                    break;
                case KILL:
                    container.handleKill();
                    break;
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
        if (type == Type.BATCH || type == Type.UPDATE) {
            buffer.writeVarLong(countCapacity);
            buffer.writeVarInt(typeCapacity);
            buffer.writeVarInt(itemMap.size());
            itemMap.forEach((key, value) -> {
                buffer.writeItem(key.getStack());
                if (key.getUUID() == null) {
                    //Shouldn't be null unless something failed, but if it does try to handle it relatively gracefully
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeUUID(key.getUUID());
                }
                buffer.writeVarLong(value);
            });
        }
    }

    public static PacketQIOItemViewerGuiSync decode(PacketBuffer buffer) {
        Type type = buffer.readEnum(Type.class);
        long countCapacity = 0;
        int typeCapacity = 0;
        Object2LongMap<UUIDAwareHashedItem> map = null;
        if (type == Type.BATCH || type == Type.UPDATE) {
            countCapacity = buffer.readVarLong();
            typeCapacity = buffer.readVarInt();
            int count = buffer.readVarInt();
            map = new Object2LongOpenHashMap<>(count);
            for (int i = 0; i < count; i++) {
                map.put(new UUIDAwareHashedItem(buffer.readItem(), buffer.readBoolean() ? buffer.readUUID() : null), buffer.readVarLong());
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
