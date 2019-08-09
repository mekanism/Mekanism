package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileNetwork;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketConfigurationUpdate {

    private ConfigurationPacket packetType;
    private TransmissionType transmission;
    private Direction configIndex;
    private Coord4D coord4D;
    private int inputSide;
    private int clickType;

    public PacketConfigurationUpdate(ConfigurationPacket type, Coord4D coord, int click, int extra, TransmissionType trans) {
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
            configIndex = Direction.byIndex(extra);
            transmission = trans;
        }
        if (packetType == ConfigurationPacket.INPUT_COLOR) {
            clickType = click;
            inputSide = extra;
        }
    }

    public static void handle(PacketConfigurationUpdate message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);

        PacketHandler.handlePacket(() -> {
            TileEntity tile = message.coord4D.getTileEntity(player.world);
            if (tile instanceof ISideConfiguration) {
                LazyOptionalHelper<ITileNetwork> capabilityHelper = CapabilityUtils.getCapabilityHelper(tile, Capabilities.TILE_NETWORK_CAPABILITY, null);
                ISideConfiguration config = (ISideConfiguration) tile;

                if (message.packetType == ConfigurationPacket.EJECT) {
                    config.getConfig().setEjecting(message.transmission, !config.getConfig().isEjecting(message.transmission));
                } else if (message.packetType == ConfigurationPacket.SIDE_DATA) {
                    if (message.clickType == 0) {
                        MekanismUtils.incrementOutput(config, message.transmission, message.configIndex);
                    } else if (message.clickType == 1) {
                        MekanismUtils.decrementOutput(config, message.transmission, message.configIndex);
                    } else if (message.clickType == 2) {
                        config.getConfig().getConfig(message.transmission).set(message.configIndex, (byte) 0);
                    }

                    tile.markDirty();
                    capabilityHelper.ifPresent(
                          network -> Mekanism.packetHandler.sendToAllTracking(new PacketTileEntity(message.coord4D, network.getNetworkedData()), message.coord4D)
                    );
                    //Notify the neighbor on that side our state changed
                    MekanismUtils.notifyNeighborOfChange(tile.getWorld(), message.configIndex, tile.getPos());
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
                    Direction side = Direction.byIndex(message.inputSide);
                    TileComponentEjector ejector = config.getEjector();
                    if (message.clickType == 0) {
                        ejector.setInputColor(side, TransporterUtils.increment(ejector.getInputColor(side)));
                    } else if (message.clickType == 1) {
                        ejector.setInputColor(side, TransporterUtils.decrement(ejector.getInputColor(side)));
                    } else if (message.clickType == 2) {
                        ejector.setInputColor(side, null);
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
        }, player);
    }

    public static void encode(PacketConfigurationUpdate pkt, PacketBuffer buf) {
        buf.writeInt(pkt.packetType.ordinal());
        pkt.coord4D.write(buf);

        if (pkt.packetType != ConfigurationPacket.EJECT && pkt.packetType != ConfigurationPacket.STRICT_INPUT) {
            buf.writeInt(pkt.clickType);
        }
        if (pkt.packetType == ConfigurationPacket.EJECT) {
            buf.writeEnumValue(pkt.transmission);
        }
        if (pkt.packetType == ConfigurationPacket.SIDE_DATA) {
            buf.writeEnumValue(pkt.configIndex);
            buf.writeEnumValue(pkt.transmission);
        }
        if (pkt.packetType == ConfigurationPacket.INPUT_COLOR) {
            buf.writeInt(pkt.inputSide);
        }
    }

    public static PacketConfigurationUpdate decode(PacketBuffer buf) {
        ConfigurationPacket packetType = ConfigurationPacket.values()[buf.readInt()];
        Coord4D coord4D = Coord4D.read(buf);
        int clickType = 0;
        int extra = 0;
        TransmissionType transmission = null;

        if (packetType == ConfigurationPacket.EJECT) {
            transmission = buf.readEnumValue(TransmissionType.class);
        } else if (packetType == ConfigurationPacket.SIDE_DATA) {
            clickType = buf.readInt();
            extra = buf.readEnumValue(Direction.class).ordinal();//configIndex
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