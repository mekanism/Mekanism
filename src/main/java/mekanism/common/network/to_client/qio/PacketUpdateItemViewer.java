package mekanism.common.network.to_client.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class PacketUpdateItemViewer implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketUpdateItemViewer> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("update_qio"));
    private static final StreamCodec<RegistryFriendlyByteBuf, Object2LongMap<UUIDAwareHashedItem>> ITEM_MAP_CODEC = ByteBufCodecs.map(Object2LongOpenHashMap::new,
          UUIDAwareHashedItem.STREAM_CODEC, ByteBufCodecs.VAR_LONG
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateItemViewer> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, pkt -> pkt.countCapacity,
          ByteBufCodecs.VAR_INT, pkt -> pkt.typeCapacity,
          ITEM_MAP_CODEC, pkt -> pkt.itemMap,
          PacketUpdateItemViewer::new
    );

    private final Object2LongMap<UUIDAwareHashedItem> itemMap;
    private final long countCapacity;
    private final int typeCapacity;

    public PacketUpdateItemViewer(long countCapacity, int typeCapacity, Object2LongMap<UUIDAwareHashedItem> itemMap) {
        this.itemMap = itemMap;
        this.countCapacity = countCapacity;
        this.typeCapacity = typeCapacity;
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketUpdateItemViewer> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof QIOItemViewerContainer container) {
            container.handleUpdate(itemMap, countCapacity, typeCapacity);
        }
    }
}