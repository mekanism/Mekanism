package mekanism.common.network;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketConfigurationUpdate {

    private final ConfigurationPacket packetType;
    private final BlockPos pos;
    private TransmissionType transmission;
    private RelativeSide inputSide;
    private int clickType;

    public PacketConfigurationUpdate(BlockPos pos, TransmissionType trans) {
        packetType = ConfigurationPacket.EJECT;
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
        if (packetType == ConfigurationPacket.EJECT) {
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

    public static void handle(PacketConfigurationUpdate message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.pos);
            if (tile instanceof ISideConfiguration) {
                ISideConfiguration config = (ISideConfiguration) tile;
                if (message.packetType == ConfigurationPacket.EJECT) {
                    ConfigInfo info = config.getConfig().getConfig(message.transmission);
                    if (info != null) {
                        info.setEjecting(!info.isEjecting());
                    }
                } else if (message.packetType == ConfigurationPacket.SIDE_DATA) {
                    TileComponentConfig configComponent = config.getConfig();
                    ConfigInfo info = configComponent.getConfig(message.transmission);
                    if (info != null) {
                        boolean changed = true;
                        if (message.clickType == 0) {
                            info.incrementDataType(message.inputSide);
                        } else if (message.clickType == 1) {
                            info.decrementDataType(message.inputSide);
                        } else if (message.clickType == 2) {
                            if (info.getDataType(message.inputSide) == DataType.NONE) {
                                //If it was already none, we don't need to invalidate capabilities
                                changed = false;
                            }
                            info.setDataType(DataType.NONE, message.inputSide);
                        }
                        if (changed) {
                            configComponent.sideChanged(message.transmission, message.inputSide);
                        }
                    }
                } else if (message.packetType == ConfigurationPacket.EJECT_COLOR) {
                    TileComponentEjector ejector = config.getEjector();
                    if (message.clickType == 0) {
                        ejector.setOutputColor(TransporterUtils.increment(ejector.getOutputColor()));
                    } else if (message.clickType == 1) {
                        ejector.setOutputColor(TransporterUtils.decrement(ejector.getOutputColor()));
                    } else if (message.clickType == 2) {
                        ejector.setOutputColor(null);
                    }
                } else if (message.packetType == ConfigurationPacket.INPUT_COLOR) {
                    TileComponentEjector ejector = config.getEjector();
                    if (message.clickType == 0) {
                        ejector.setInputColor(message.inputSide, TransporterUtils.increment(ejector.getInputColor(message.inputSide)));
                    } else if (message.clickType == 1) {
                        ejector.setInputColor(message.inputSide, TransporterUtils.decrement(ejector.getInputColor(message.inputSide)));
                    } else if (message.clickType == 2) {
                        ejector.setInputColor(message.inputSide, null);
                    }
                } else if (message.packetType == ConfigurationPacket.STRICT_INPUT) {
                    TileComponentEjector ejector = config.getEjector();
                    ejector.setStrictInput(!ejector.hasStrictInput());
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketConfigurationUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        buf.writeBlockPos(pkt.pos);
        if (pkt.packetType == ConfigurationPacket.EJECT) {
            buf.writeEnumValue(pkt.transmission);
        } else if (pkt.packetType == ConfigurationPacket.SIDE_DATA) {
            buf.writeVarInt(pkt.clickType);
            buf.writeEnumValue(pkt.inputSide);
            buf.writeEnumValue(pkt.transmission);
        } else if (pkt.packetType == ConfigurationPacket.EJECT_COLOR) {
            buf.writeVarInt(pkt.clickType);
        } else if (pkt.packetType == ConfigurationPacket.INPUT_COLOR) {
            buf.writeVarInt(pkt.clickType);
            buf.writeEnumValue(pkt.inputSide);
        }
    }

    public static PacketConfigurationUpdate decode(PacketBuffer buf) {
        ConfigurationPacket packetType = buf.readEnumValue(ConfigurationPacket.class);
        BlockPos pos = buf.readBlockPos();
        int clickType = 0;
        RelativeSide inputSide = null;
        TransmissionType transmission = null;
        if (packetType == ConfigurationPacket.EJECT) {
            transmission = buf.readEnumValue(TransmissionType.class);
        } else if (packetType == ConfigurationPacket.SIDE_DATA) {
            clickType = buf.readVarInt();
            inputSide = buf.readEnumValue(RelativeSide.class);
            transmission = buf.readEnumValue(TransmissionType.class);
        } else if (packetType == ConfigurationPacket.EJECT_COLOR) {
            clickType = buf.readVarInt();
        } else if (packetType == ConfigurationPacket.INPUT_COLOR) {
            clickType = buf.readVarInt();
            inputSide = buf.readEnumValue(RelativeSide.class);
        }
        return new PacketConfigurationUpdate(packetType, pos, clickType, inputSide, transmission);
    }

    public enum ConfigurationPacket {
        EJECT,
        SIDE_DATA,
        EJECT_COLOR,
        INPUT_COLOR,
        STRICT_INPUT
    }
}