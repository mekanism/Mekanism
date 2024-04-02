package mekanism.common.network.to_server.configuration_update;

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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSideData(BlockPos pos, MekClickType clickType, RelativeSide inputSide, TransmissionType transmission) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("side_data");

    public PacketSideData(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(MekClickType.class), buffer.readEnum(RelativeSide.class), buffer.readEnum(TransmissionType.class));
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

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(clickType);
        buffer.writeEnum(inputSide);
        buffer.writeEnum(transmission);
    }
}