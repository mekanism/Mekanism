package mekanism.common.network.to_server.configuration_update;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.MekClickType;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketEjectColor(BlockPos pos, MekClickType clickType) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketEjectColor> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("eject_color"));
    public static final StreamCodec<ByteBuf, PacketEjectColor> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketEjectColor::pos,
          MekClickType.STREAM_CODEC, PacketEjectColor::clickType,
          PacketEjectColor::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketEjectColor> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        TileComponentEjector ejector = PacketUtils.ejector(context, pos);
        if (ejector != null) {
            ejector.setOutputColor(switch (clickType) {
                case LEFT -> TransporterUtils.increment(ejector.getOutputColor());
                case RIGHT -> TransporterUtils.decrement(ejector.getOutputColor());
                case SHIFT_LEFT -> null;
            });
        }
    }
}