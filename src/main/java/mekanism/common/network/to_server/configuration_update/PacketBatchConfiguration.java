package mekanism.common.network.to_server.configuration_update;

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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketBatchConfiguration(BlockPos pos, @Nullable TransmissionType transmission, DataType targetType) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("batch_configuration");

    public PacketBatchConfiguration(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readNullable(buf -> buf.readEnum(TransmissionType.class)), buffer.readEnum(DataType.class));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
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
                if (info.isSideEnabled(side) && info.getDataType(side) != targetType) {
                    info.setDataType(targetType, side);
                    configComponent.sideChanged(transmission, side);
                }
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNullable(transmission, FriendlyByteBuf::writeEnum);
        buffer.writeEnum(targetType);
    }
}