package mekanism.common.network.to_server;

import javax.annotation.Nonnull;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketConfigurationUpdate implements IMekanismPacket {

    private final ConfigurationPacket packetType;
    private final BlockPos pos;
    private TransmissionType transmission;
    private RelativeSide inputSide;
    private int clickType;

    public PacketConfigurationUpdate(@Nonnull ConfigurationPacket type, BlockPos pos, TransmissionType trans) {
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

    public PacketConfigurationUpdate(@Nonnull ConfigurationPacket type, BlockPos pos, int click, RelativeSide inputSide, TransmissionType trans) {
        packetType = type;
        this.pos = pos;
        if (packetType == ConfigurationPacket.EJECT || packetType == ConfigurationPacket.CLEAR_ALL) {
            transmission = trans;
        } else if (packetType == ConfigurationPacket.EJECT_COLOR) {
            clickType = click;
        } else if (packetType == ConfigurationPacket.SIDE_DATA) {
            clickType = click;
            this.inputSide = inputSide;
            transmission = trans;
        } else if (packetType == ConfigurationPacket.INPUT_COLOR) {
            clickType = click;
            this.inputSide = inputSide;
        }
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        TileEntity tile = WorldUtils.getTileEntity(player.level, pos);
        if (tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            if (packetType == ConfigurationPacket.EJECT) {
                ConfigInfo info = config.getConfig().getConfig(transmission);
                if (info != null) {
                    info.setEjecting(!info.isEjecting());
                    WorldUtils.saveChunk(tile);
                }
            } else if (packetType == ConfigurationPacket.CLEAR_ALL) {
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
            } else if (packetType == ConfigurationPacket.SIDE_DATA) {
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
            } else if (packetType == ConfigurationPacket.EJECT_COLOR) {
                TileComponentEjector ejector = config.getEjector();
                if (clickType == 0) {
                    ejector.setOutputColor(TransporterUtils.increment(ejector.getOutputColor()));
                } else if (clickType == 1) {
                    ejector.setOutputColor(TransporterUtils.decrement(ejector.getOutputColor()));
                } else if (clickType == 2) {
                    ejector.setOutputColor(null);
                }
            } else if (packetType == ConfigurationPacket.INPUT_COLOR) {
                TileComponentEjector ejector = config.getEjector();
                if (clickType == 0) {
                    ejector.setInputColor(inputSide, TransporterUtils.increment(ejector.getInputColor(inputSide)));
                } else if (clickType == 1) {
                    ejector.setInputColor(inputSide, TransporterUtils.decrement(ejector.getInputColor(inputSide)));
                } else if (clickType == 2) {
                    ejector.setInputColor(inputSide, null);
                }
            } else if (packetType == ConfigurationPacket.STRICT_INPUT) {
                TileComponentEjector ejector = config.getEjector();
                ejector.setStrictInput(!ejector.hasStrictInput());
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(packetType);
        buffer.writeBlockPos(pos);
        if (packetType == ConfigurationPacket.EJECT || packetType == ConfigurationPacket.CLEAR_ALL) {
            buffer.writeEnum(transmission);
        } else if (packetType == ConfigurationPacket.SIDE_DATA) {
            buffer.writeVarInt(clickType);
            buffer.writeEnum(inputSide);
            buffer.writeEnum(transmission);
        } else if (packetType == ConfigurationPacket.EJECT_COLOR) {
            buffer.writeVarInt(clickType);
        } else if (packetType == ConfigurationPacket.INPUT_COLOR) {
            buffer.writeVarInt(clickType);
            buffer.writeEnum(inputSide);
        }
    }

    public static PacketConfigurationUpdate decode(PacketBuffer buffer) {
        ConfigurationPacket packetType = buffer.readEnum(ConfigurationPacket.class);
        BlockPos pos = buffer.readBlockPos();
        int clickType = 0;
        RelativeSide inputSide = null;
        TransmissionType transmission = null;
        if (packetType == ConfigurationPacket.EJECT || packetType == ConfigurationPacket.CLEAR_ALL) {
            transmission = buffer.readEnum(TransmissionType.class);
        } else if (packetType == ConfigurationPacket.SIDE_DATA) {
            clickType = buffer.readVarInt();
            inputSide = buffer.readEnum(RelativeSide.class);
            transmission = buffer.readEnum(TransmissionType.class);
        } else if (packetType == ConfigurationPacket.EJECT_COLOR) {
            clickType = buffer.readVarInt();
        } else if (packetType == ConfigurationPacket.INPUT_COLOR) {
            clickType = buffer.readVarInt();
            inputSide = buffer.readEnum(RelativeSide.class);
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