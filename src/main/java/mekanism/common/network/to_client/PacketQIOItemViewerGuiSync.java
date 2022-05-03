package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof QIOItemViewerContainer container) {
            switch (type) {
                case BATCH -> container.handleBatchUpdate(itemMap, countCapacity, typeCapacity);
                case UPDATE -> container.handleUpdate(itemMap, countCapacity, typeCapacity);
                case KILL -> container.handleKill();
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        if (type == Type.BATCH || type == Type.UPDATE) {
            buffer.writeVarLong(countCapacity);
            buffer.writeVarInt(typeCapacity);
            BasePacketHandler.writeMap(buffer, itemMap, (key, value, buf) -> {
                buf.writeItem(key.getStack());
                //Shouldn't be null unless something failed, but if it does try to handle it relatively gracefully
                BasePacketHandler.writeOptional(buf, key.getUUID(), FriendlyByteBuf::writeUUID);
                buf.writeVarLong(value);
            });
        }
    }

    public static PacketQIOItemViewerGuiSync decode(FriendlyByteBuf buffer) {
        Type type = buffer.readEnum(Type.class);
        long countCapacity = 0;
        int typeCapacity = 0;
        Object2LongMap<UUIDAwareHashedItem> map = null;
        if (type == Type.BATCH || type == Type.UPDATE) {
            countCapacity = buffer.readVarLong();
            typeCapacity = buffer.readVarInt();
            map = BasePacketHandler.readMap(buffer, Object2LongOpenHashMap::new,
                  buf -> new UUIDAwareHashedItem(buf.readItem(), BasePacketHandler.readOptional(buf, FriendlyByteBuf::readUUID)), FriendlyByteBuf::readVarLong);
        }
        return new PacketQIOItemViewerGuiSync(type, map, countCapacity, typeCapacity);
    }

    public enum Type {
        BATCH,
        UPDATE,
        KILL;
    }
}
