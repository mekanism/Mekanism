package mekanism.common.network.to_server.configuration_update;

import io.netty.buffer.ByteBuf;
import mekanism.api.RelativeSide;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.MekClickType;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSideData(BlockPos pos, MekClickType clickType, RelativeSide inputSide, TransmissionType transmission) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSideData> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("side_data"));
    public static final StreamCodec<ByteBuf, PacketSideData> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketSideData::pos,
          MekClickType.STREAM_CODEC, PacketSideData::clickType,
          RelativeSide.STREAM_CODEC, PacketSideData::inputSide,
          TransmissionType.STREAM_CODEC, PacketSideData::transmission,
          PacketSideData::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSideData> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        TileComponentConfig configComponent = PacketUtils.config(context, pos);
        if (configComponent != null) {
            ConfigInfo info = configComponent.getConfig(transmission);
            if (info != null) {
                DataType type = info.getDataType(inputSide);
                boolean changed = type != switch (clickType) {
                    case LEFT -> info.incrementDataType(inputSide);
                    case RIGHT -> info.decrementDataType(inputSide);
                    case SHIFT_LEFT -> {
                        //We only need to update it if we are changing it to none
                        if (type != DataType.NONE) {
                            info.setDataType(DataType.NONE, inputSide);
                        }
                        yield DataType.NONE;
                    }
                };
                if (changed) {
                    configComponent.sideChanged(transmission, inputSide);
                }
            }
        }
    }
}