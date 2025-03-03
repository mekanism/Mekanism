package mekanism.common.network.to_client.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketTransporterBatch(long pos, IntSet deletes, Int2ObjectMap<TransporterStack> updates) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketTransporterBatch> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("transporter_batch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTransporterBatch> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, PacketTransporterBatch::pos,
          ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.collection(IntOpenHashSet::new)), PacketTransporterBatch::deletes,
          ByteBufCodecs.map(Int2ObjectOpenHashMap::new, ByteBufCodecs.VAR_INT, TransporterStack.STREAM_CODEC), PacketTransporterBatch::updates,
          PacketTransporterBatch::new
    );

    public static PacketTransporterBatch create(long pos, IntSet deletes, Int2ObjectMap<TransporterStack> updates) {
        for (TransporterStack stack : updates.values()) {
            stack.updateForPos(pos);
        }
        return new PacketTransporterBatch(pos, deletes, updates);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketTransporterBatch> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (PacketUtils.blockEntity(context, pos) instanceof TileEntityLogisticalTransporterBase tile) {
            LogisticalTransporterBase transporter = tile.getTransmitter();
            for (ObjectIterator<Int2ObjectMap.Entry<TransporterStack>> iterator = Int2ObjectMaps.fastIterator(updates); iterator.hasNext(); ) {
                Int2ObjectMap.Entry<TransporterStack> entry = iterator.next();
                transporter.addStack(entry.getIntKey(), entry.getValue());
            }
            for (int toDelete : deletes) {
                transporter.deleteStack(toDelete);
            }
        }
    }
}