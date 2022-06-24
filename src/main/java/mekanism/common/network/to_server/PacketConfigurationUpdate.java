package mekanism.common.network.to_server;

import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class PacketConfigurationUpdate implements IMekanismPacket {

    private final ConfigurationPacket packetType;
    private final BlockPos pos;
    private TransmissionType transmission;
    private RelativeSide inputSide;
    private int clickType;

    public PacketConfigurationUpdate(@NotNull ConfigurationPacket type, BlockPos pos, TransmissionType trans) {
        packetType = type;
        this.pos = pos;
        transmission = trans;
    }

    public PacketConfigurationUpdate(BlockPos pos, int click) {
        packetType = ConfigurationPacket.EJECT_COLOR;
        this.pos = pos;
        clickType = click;
    }

    public PacketConfigurationUpdate(BlockPos pos) {
        packetType = ConfigurationPacket.STRICT_INPUT;
        this.pos = pos;
    }

    public PacketConfigurationUpdate(@NotNull ConfigurationPacket type, BlockPos pos, int click, RelativeSide inputSide, TransmissionType trans) {
        packetType = type;
        this.pos = pos;
        switch (packetType) {
            case EJECT, CLEAR_ALL -> transmission = trans;
            case EJECT_COLOR -> clickType = click;
            case SIDE_DATA -> {
                clickType = click;
                this.inputSide = inputSide;
                transmission = trans;
            }
            case INPUT_COLOR -> {
                clickType = click;
                this.inputSide = inputSide;
            }
        }
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity tile = WorldUtils.getTileEntity(player.level, pos);
        if (tile instanceof ISideConfiguration config) {
            switch (packetType) {
                case EJECT -> {
                    ConfigInfo info = config.getConfig().getConfig(transmission);
                    if (info != null) {
                        info.setEjecting(!info.isEjecting());
                        WorldUtils.saveChunk(tile);
                    }
                }
                case CLEAR_ALL -> {
                    TileComponentConfig configComponent = config.getConfig();
                    ConfigInfo info = configComponent.getConfig(transmission);
                    if (info != null) {
                        for (RelativeSide side : EnumUtils.SIDES) {
                            if (info.isSideEnabled(side) && info.getDataType(side) != DataType.NONE) {
                                info.setDataType(DataType.NONE, side);
                                configComponent.sideChanged(transmission, side);
                            }
                        }
                    }
                }
                case SIDE_DATA -> {
                    TileComponentConfig configComponent = config.getConfig();
                    ConfigInfo info = configComponent.getConfig(transmission);
                    if (info != null) {
                        DataType type = info.getDataType(inputSide);
                        boolean changed = false;
                        if (clickType == 0) {
                            changed = type != info.incrementDataType(inputSide);
                        } else if (clickType == 1) {
                            changed = type != info.decrementDataType(inputSide);
                        } else if (clickType == 2 && type != DataType.NONE) {
                            //We only need to update it if we are changing it to none
                            changed = true;
                            info.setDataType(DataType.NONE, inputSide);
                        }
                        if (changed) {
                            configComponent.sideChanged(transmission, inputSide);
                        }
                    }
                }
                case EJECT_COLOR -> {
                    TileComponentEjector ejector = config.getEjector();
                    switch (clickType) {
                        case 0 -> ejector.setOutputColor(TransporterUtils.increment(ejector.getOutputColor()));
                        case 1 -> ejector.setOutputColor(TransporterUtils.decrement(ejector.getOutputColor()));
                        case 2 -> ejector.setOutputColor(null);
                    }
                }
                case INPUT_COLOR -> {
                    TileComponentEjector ejector = config.getEjector();
                    switch (clickType) {
                        case 0 -> ejector.setInputColor(inputSide, TransporterUtils.increment(ejector.getInputColor(inputSide)));
                        case 1 -> ejector.setInputColor(inputSide, TransporterUtils.decrement(ejector.getInputColor(inputSide)));
                        case 2 -> ejector.setInputColor(inputSide, null);
                    }
                }
                case STRICT_INPUT -> {
                    TileComponentEjector ejector = config.getEjector();
                    ejector.setStrictInput(!ejector.hasStrictInput());
                }
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(packetType);
        buffer.writeBlockPos(pos);
        switch (packetType) {
            case EJECT, CLEAR_ALL -> buffer.writeEnum(transmission);
            case SIDE_DATA -> {
                buffer.writeVarInt(clickType);
                buffer.writeEnum(inputSide);
                buffer.writeEnum(transmission);
            }
            case EJECT_COLOR -> buffer.writeVarInt(clickType);
            case INPUT_COLOR -> {
                buffer.writeVarInt(clickType);
                buffer.writeEnum(inputSide);
            }
        }
    }

    public static PacketConfigurationUpdate decode(FriendlyByteBuf buffer) {
        ConfigurationPacket packetType = buffer.readEnum(ConfigurationPacket.class);
        BlockPos pos = buffer.readBlockPos();
        int clickType = 0;
        RelativeSide inputSide = null;
        TransmissionType transmission = null;
        switch (packetType) {
            case EJECT, CLEAR_ALL -> transmission = buffer.readEnum(TransmissionType.class);
            case SIDE_DATA -> {
                clickType = buffer.readVarInt();
                inputSide = buffer.readEnum(RelativeSide.class);
                transmission = buffer.readEnum(TransmissionType.class);
            }
            case EJECT_COLOR -> clickType = buffer.readVarInt();
            case INPUT_COLOR -> {
                clickType = buffer.readVarInt();
                inputSide = buffer.readEnum(RelativeSide.class);
            }
        }
        return new PacketConfigurationUpdate(packetType, pos, clickType, inputSide, transmission);
    }

    public enum ConfigurationPacket {
        EJECT,
        SIDE_DATA,
        EJECT_COLOR,
        INPUT_COLOR,
        STRICT_INPUT,
        CLEAR_ALL
    }
}