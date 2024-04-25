package mekanism.common.network.to_server.configuration_update;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketEjectConfiguration(BlockPos pos, TransmissionType transmission) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketEjectConfiguration> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("eject_configuration"));
    public static final StreamCodec<ByteBuf, PacketEjectConfiguration> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketEjectConfiguration::pos,
          TransmissionType.STREAM_CODEC, PacketEjectConfiguration::transmission,
          PacketEjectConfiguration::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketEjectConfiguration> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        TileComponentConfig configComponent = PacketUtils.config(context, pos);
        if (configComponent != null) {
            ConfigInfo info = configComponent.getConfig(transmission);
            if (info != null) {
                info.setEjecting(!info.isEjecting());
                configComponent.tile.markForSave();
            }
        }
    }
}