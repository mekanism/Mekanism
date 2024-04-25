package mekanism.common.network.to_client.transmitter;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketTransporterSync(BlockPos pos, int stackId, byte[] rawStack) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketTransporterSync> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("transporter_sync"));
    public static final StreamCodec<ByteBuf, PacketTransporterSync> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketTransporterSync::pos,
          ByteBufCodecs.INT, PacketTransporterSync::stackId,
          ByteBufCodecs.BYTE_ARRAY, PacketTransporterSync::rawStack,
          PacketTransporterSync::new
    );

    public PacketTransporterSync(RegistryAccess registryAccess, BlockPos pos, int stackId, TransporterStack stack) {
        //TODO - 1.20.4: SP: Figure out if there is a better way for us to handle not leaking the instance than just forcing a write and read
        this(pos, stackId, FriendlyByteBufUtil.writeCustomData(buffer -> stack.write(buffer, pos), registryAccess));
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketTransporterSync> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (PacketUtils.blockEntity(context, pos) instanceof TileEntityLogisticalTransporterBase tile) {
            LogisticalTransporterBase transporter = tile.getTransmitter();
            TransporterStack stack = PacketUtils.read(context.player().level().registryAccess(), rawStack, TransporterStack::readFromPacket);
            transporter.addStack(stackId, stack);
        }
    }
}