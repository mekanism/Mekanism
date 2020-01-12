package mekanism.common.network;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.RelativeSide;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileNetwork;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketConfigurationUpdate {

    private ConfigurationPacket packetType;
    private TransmissionType transmission;
    private Coord4D coord4D;
    private int inputSide;
    private int clickType;

    public PacketConfigurationUpdate(@Nonnull ConfigurationPacket type, Coord4D coord, int click, int extra, TransmissionType trans) {
        packetType = type;
        coord4D = coord;

        if (packetType == ConfigurationPacket.EJECT) {
            transmission = trans;
        }
        if (packetType == ConfigurationPacket.EJECT_COLOR) {
            clickType = click;
        }
        if (packetType == ConfigurationPacket.SIDE_DATA) {
            clickType = click;
            inputSide = extra;
            transmission = trans;
        }
        if (packetType == ConfigurationPacket.INPUT_COLOR) {
            clickType = click;
            inputSide = extra;
        }
    }

    public static void handle(PacketConfigurationUpdate message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            if (tile instanceof ISideConfiguration) {
                LazyOptionalHelper<ITileNetwork> capabilityHelper = CapabilityUtils.getCapabilityHelper(tile, Capabilities.TILE_NETWORK_CAPABILITY, null);
                ISideConfiguration config = (ISideConfiguration) tile;

                if (message.packetType == ConfigurationPacket.EJECT) {
                    ConfigInfo info = config.getConfig().getConfig(message.transmission);
                    if (info != null) {
                        info.setEjecting(!info.isEjecting());
                    }
                } else if (message.packetType == ConfigurationPacket.SIDE_DATA) {
                    //TODO: Re-evaluate
                    RelativeSide relativeSide = RelativeSide.byIndex(message.inputSide);
                    ConfigInfo info = config.getConfig().getConfig(message.transmission);
                    if (info != null) {
                        if (message.clickType == 0) {
                            info.incrementDataType(relativeSide);
                        } else if (message.clickType == 1) {
                            info.decrementDataType(relativeSide);
                        } else if (message.clickType == 2) {
                            info.setDataType(relativeSide, DataType.NONE);
                        }
                    }

                    tile.markDirty();
                    capabilityHelper.ifPresent(
                          network -> Mekanism.packetHandler.sendToAllTracking(new PacketTileEntity(message.coord4D, network.getNetworkedData()), tile.getWorld(),
                                message.coord4D.getPos())
                    );
                    //Notify the neighbor on that side our state changed
                    MekanismUtils.notifyNeighborOfChange(tile.getWorld(), relativeSide.getDirection(config.getOrientation()), tile.getPos());
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
                    //TODO: Re-evaluate
                    RelativeSide relativeSide = RelativeSide.byIndex(message.inputSide);
                    TileComponentEjector ejector = config.getEjector();
                    if (message.clickType == 0) {
                        ejector.setInputColor(relativeSide, TransporterUtils.increment(ejector.getInputColor(relativeSide)));
                    } else if (message.clickType == 1) {
                        ejector.setInputColor(relativeSide, TransporterUtils.decrement(ejector.getInputColor(relativeSide)));
                    } else if (message.clickType == 2) {
                        ejector.setInputColor(relativeSide, null);
                    }
                } else if (message.packetType == ConfigurationPacket.STRICT_INPUT) {
                    config.getEjector().setStrictInput(!config.getEjector().hasStrictInput());
                }
                capabilityHelper.ifPresent(network -> {
                    for (PlayerEntity p : ((TileEntityMekanism) config).playersUsing) {
                        Mekanism.packetHandler.sendTo(new PacketTileEntity(message.coord4D, network.getNetworkedData()), (ServerPlayerEntity) p);
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketConfigurationUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        pkt.coord4D.write(buf);
        if (pkt.packetType == ConfigurationPacket.EJECT) {
            buf.writeEnumValue(pkt.transmission);
        } else if (pkt.packetType == ConfigurationPacket.SIDE_DATA) {
            buf.writeInt(pkt.clickType);
            buf.writeInt(pkt.inputSide);
            buf.writeEnumValue(pkt.transmission);
        } else if (pkt.packetType == ConfigurationPacket.EJECT_COLOR) {
            buf.writeInt(pkt.clickType);
        } else if (pkt.packetType == ConfigurationPacket.INPUT_COLOR) {
            buf.writeInt(pkt.clickType);
            buf.writeInt(pkt.inputSide);
        }
    }

    public static PacketConfigurationUpdate decode(PacketBuffer buf) {
        ConfigurationPacket packetType = buf.readEnumValue(ConfigurationPacket.class);
        Coord4D coord4D = Coord4D.read(buf);
        int clickType = 0;
        int extra = 0;
        TransmissionType transmission = null;

        if (packetType == ConfigurationPacket.EJECT) {
            transmission = buf.readEnumValue(TransmissionType.class);
        } else if (packetType == ConfigurationPacket.SIDE_DATA) {
            clickType = buf.readInt();
            extra = buf.readInt();//inputSide
            transmission = buf.readEnumValue(TransmissionType.class);
        } else if (packetType == ConfigurationPacket.EJECT_COLOR) {
            clickType = buf.readInt();
        } else if (packetType == ConfigurationPacket.INPUT_COLOR) {
            clickType = buf.readInt();
            extra = buf.readInt();//inputSide
        }
        return new PacketConfigurationUpdate(packetType, coord4D, clickType, extra, transmission);
    }

    public enum ConfigurationPacket {
        EJECT,
        SIDE_DATA,
        EJECT_COLOR,
        INPUT_COLOR,
        STRICT_INPUT
    }
}