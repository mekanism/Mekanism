package mekanism.common.network.to_server.configuration_update;

import mekanism.api.RelativeSide;
import mekanism.common.Mekanism;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketConfigurationClearAll(BlockPos pos, TransmissionType transmission) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("clear_configuration");

    public PacketConfigurationClearAll(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(TransmissionType.class));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        PacketUtils.config(context, pos).ifPresent(configComponent -> {
            ConfigInfo info = configComponent.getConfig(transmission);
            if (info != null) {
                for (RelativeSide side : EnumUtils.SIDES) {
                    if (info.isSideEnabled(side) && info.getDataType(side) != DataType.NONE) {
                        info.setDataType(DataType.NONE, side);
                        configComponent.sideChanged(transmission, side);
                    }
                }
            }
        });
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(transmission);
    }
}