package mekanism.common.network.to_server.configuration_update;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import mekanism.api.RelativeSide;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketBatchConfiguration(BlockPos pos, @Nullable TransmissionType transmission, DataType targetType) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketBatchConfiguration> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("batch_configuration"));
    public static final StreamCodec<ByteBuf, PacketBatchConfiguration> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketBatchConfiguration::pos,
          ByteBufCodecs.optional(TransmissionType.STREAM_CODEC), packet -> Optional.ofNullable(packet.transmission()),
          DataType.STREAM_CODEC, PacketBatchConfiguration::targetType,
          (pos, transmission, type) -> new PacketBatchConfiguration(pos, transmission.orElse(null), type)
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketBatchConfiguration> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        TileComponentConfig configComponent = PacketUtils.config(context, pos);
        if (configComponent != null) {
            if (transmission == null) {
                for (TransmissionType type : configComponent.getTransmissions()) {
                    updateAllSides(configComponent, type, configComponent.getConfig(type));
                }
                return;
            }
            ConfigInfo info = configComponent.getConfig(transmission);
            if (info != null) {
                updateAllSides(configComponent, transmission, info);
            }
        }
    }

    private void updateAllSides(TileComponentConfig configComponent, TransmissionType transmission, @Nullable ConfigInfo info) {
        if (info != null && info.supports(targetType)) {
            for (RelativeSide side : EnumUtils.SIDES) {
                if (info.setDataType(targetType, side)) {
                    configComponent.sideChanged(transmission, side);
                }
            }
        }
    }
}