package mekanism.common.network.to_client.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketTransporterBatch(BlockPos pos, IntSet deletes, byte[] rawUpdates) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketTransporterBatch> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("transporter_batch"));
    public static final StreamCodec<FriendlyByteBuf, PacketTransporterBatch> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketTransporterBatch::pos,
          ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.collection(IntOpenHashSet::new)), PacketTransporterBatch::deletes,
          ByteBufCodecs.BYTE_ARRAY, PacketTransporterBatch::rawUpdates,
          PacketTransporterBatch::new
    );

    public PacketTransporterBatch(RegistryAccess registryAccess, BlockPos pos, IntSet deletes, Int2ObjectMap<TransporterStack> updates) {
        //TODO - 1.20.4: SP: Figure out if there is a better way for us to handle not leaking the instance than just forcing a write and read
        // Also validate that we can actually just directly use deletes without copying it or anything
        this(pos, deletes, FriendlyByteBufUtil.writeCustomData(buffer -> buffer.writeMap(updates, FriendlyByteBuf::writeVarInt, (buf, stack) -> stack.write(buffer, pos)), registryAccess));
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
            Int2ObjectMap<TransporterStack> updates = PacketUtils.read(context.player().level().registryAccess(), rawUpdates, buffer ->
                  buffer.readMap(Int2ObjectOpenHashMap::new, ByteBufCodecs.VAR_INT, buf -> TransporterStack.readFromPacket(buffer)));
            for (Int2ObjectMap.Entry<TransporterStack> entry : updates.int2ObjectEntrySet()) {
                transporter.addStack(entry.getIntKey(), entry.getValue());
            }
            for (int toDelete : deletes) {
                transporter.deleteStack(toDelete);
            }
        }
    }
}