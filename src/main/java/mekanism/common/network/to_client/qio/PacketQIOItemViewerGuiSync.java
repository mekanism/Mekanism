package mekanism.common.network.to_client.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

//TODO - 1.19: Split implementations of this packet as it is possible for it to technically become too large and cause a crash
// Also ideally we only would sync the hashed item for types we haven't sent a given client yet so that then
// we can also send a smaller packet to each client until they disconnect and then we clear what packets they know
public abstract class PacketQIOItemViewerGuiSync implements IMekanismPacket<PlayPayloadContext> {

    protected final Object2LongMap<UUIDAwareHashedItem> itemMap;
    protected final long countCapacity;
    protected final int typeCapacity;

    protected PacketQIOItemViewerGuiSync(FriendlyByteBuf buffer) {
        this(
              buffer.readVarLong(),
              buffer.readVarInt(),
              buffer.readMap(Object2LongOpenHashMap::new, buf -> new UUIDAwareHashedItem(buf.readItem(), buf.readNullable(FriendlyByteBuf::readUUID)), FriendlyByteBuf::readVarLong)
        );
    }

    protected PacketQIOItemViewerGuiSync(long countCapacity, int typeCapacity, Object2LongMap<UUIDAwareHashedItem> itemMap) {
        this.itemMap = itemMap;
        this.countCapacity = countCapacity;
        this.typeCapacity = typeCapacity;
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVarLong(countCapacity);
        buffer.writeVarInt(typeCapacity);
        buffer.writeMap(itemMap, (buf, item) -> {
            buf.writeItem(item.getInternalStack());
            //Shouldn't be null unless something failed, but if it does try to handle it relatively gracefully
            buf.writeNullable(item.getUUID(), FriendlyByteBuf::writeUUID);
        }, FriendlyByteBuf::writeVarLong);
    }
}
