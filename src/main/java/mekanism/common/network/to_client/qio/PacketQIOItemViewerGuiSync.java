package mekanism.common.network.to_client.qio;

import com.mojang.datafixers.util.Function3;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

//TODO - 1.19: Split implementations of this packet as it is possible for it to technically become too large and cause a crash
// Also ideally we only would sync the hashed item for types we haven't sent a given client yet so that then
// we can also send a smaller packet to each client until they disconnect and then we clear what packets they know
public abstract class PacketQIOItemViewerGuiSync implements IMekanismPacket {

    protected static <PKT extends PacketQIOItemViewerGuiSync> StreamCodec<RegistryFriendlyByteBuf, PKT> streamCodec(
          Function3<Long, Integer, Object2LongMap<UUIDAwareHashedItem>, PKT> constructor) {
        return StreamCodec.composite(
              ByteBufCodecs.VAR_LONG, pkt -> pkt.countCapacity,
              ByteBufCodecs.VAR_INT, pkt -> pkt.typeCapacity,
              StreamCodec.of(
                    (buffer, itemMap) -> buffer.writeMap(itemMap, (buf, item) -> {
                        ItemStack.STREAM_CODEC.encode(buffer, item.getInternalStack());
                        //Shouldn't be null unless something failed, but if it does try to handle it relatively gracefully
                        buf.writeNullable(item.getUUID(), (b, u) -> b.writeUUID(u));
                    }, FriendlyByteBuf::writeVarLong),
                    buffer -> buffer.readMap(Object2LongOpenHashMap::new, buf -> new UUIDAwareHashedItem(ItemStack.STREAM_CODEC.decode(buffer),
                          buf.readNullable(b -> b.readUUID())), FriendlyByteBuf::readVarLong)
              ), pkt -> pkt.itemMap,
              constructor
        );
    }

    protected final Object2LongMap<UUIDAwareHashedItem> itemMap;
    protected final long countCapacity;
    protected final int typeCapacity;

    protected PacketQIOItemViewerGuiSync(long countCapacity, int typeCapacity, Object2LongMap<UUIDAwareHashedItem> itemMap) {
        this.itemMap = itemMap;
        this.countCapacity = countCapacity;
        this.typeCapacity = typeCapacity;
    }
}
