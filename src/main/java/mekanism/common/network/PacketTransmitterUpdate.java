package mekanism.common.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.PacketHandler;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.transmitters.grid.EnergyNetwork;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.transmitters.grid.GasNetwork;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketTransmitterUpdate {

    private PacketType packetType;

    private Coord4D coord4D;

    private double power;

    private GasStack gasStack;
    private Gas gasType;
    private boolean didGasTransfer;

    private FluidStack fluidStack;
    private Fluid fluidType;
    private boolean didFluidTransfer;

    private int amount;

    private boolean newNetwork;
    private Collection<IGridTransmitter> transmittersAdded;
    private Collection<Coord4D> transmitterCoords;

    public PacketTransmitterUpdate(PacketType type, Coord4D coord, Object... data) {
        this(type, coord);
        switch (packetType) {
            case UPDATE:
                newNetwork = (Boolean) data[0];
                transmittersAdded = (Collection<IGridTransmitter>) data[1];
                break;
            case ENERGY:
                power = (Double) data[0];
                break;
            case GAS:
                gasStack = (GasStack) data[0];
                didGasTransfer = (Boolean) data[1];
                break;
            case FLUID:
                fluidStack = (FluidStack) data[0];
                didFluidTransfer = (Boolean) data[1];
                break;
            default:
                break;
        }
    }

    private PacketTransmitterUpdate(PacketType type, Coord4D coord) {
        packetType = type;
        coord4D = coord;
    }

    public static void handle(PacketTransmitterUpdate message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (message.coord4D == null) {
                return;
            }
            TileEntity tileEntity = message.coord4D.getTileEntity(player.world);
            LazyOptionalHelper<IGridTransmitter> capabilityHelper = CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null);
            capabilityHelper.ifPresent(transmitter -> {
                if (message.packetType == PacketType.UPDATE) {
                    DynamicNetwork network = transmitter.hasTransmitterNetwork() && !message.newNetwork ? transmitter.getTransmitterNetwork() : transmitter.createEmptyNetwork();
                    network.register();
                    transmitter.setTransmitterNetwork(network);
                    for (Coord4D coord : message.transmitterCoords) {
                        TileEntity tile = coord.getTileEntity(player.world);
                        CapabilityUtils.getCapabilityHelper(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null).ifPresent(
                              gridTransmitter -> gridTransmitter.setTransmitterNetwork(network)
                        );
                    }
                    network.updateCapacity();
                    return;
                }
                if (MekanismConfig.client.opaqueTransmitters.get() || !transmitter.hasTransmitterNetwork()) {
                    return;
                }
                TransmissionType transmissionType = transmitter.getTransmissionType();
                if (message.packetType == PacketType.ENERGY) {
                    if (transmissionType == TransmissionType.ENERGY) {
                        ((EnergyNetwork) transmitter.getTransmitterNetwork()).clientEnergyScale = message.power;
                    }
                } else if (message.packetType == PacketType.GAS) {
                    if (transmissionType == TransmissionType.GAS) {
                        GasNetwork net = (GasNetwork) transmitter.getTransmitterNetwork();
                        if (message.gasType != null) {
                            net.refGas = message.gasType;
                        }
                        net.buffer = message.gasStack;
                        net.didTransfer = message.didGasTransfer;
                    }
                } else if (message.packetType == PacketType.FLUID) {
                    if (transmissionType == TransmissionType.FLUID) {
                        FluidNetwork net = (FluidNetwork) transmitter.getTransmitterNetwork();
                        if (message.fluidType != null) {
                            net.refFluid = message.fluidType;
                        }
                        net.buffer = message.fluidStack;
                        net.didTransfer = message.didFluidTransfer;
                    }
                }
            });
        });
    }

    public static void encode(PacketTransmitterUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        pkt.coord4D.write(buf);
        PacketHandler.log("Sending '" + pkt.packetType + "' update message from coordinate " + pkt.coord4D);
        switch (pkt.packetType) {
            case UPDATE:
                buf.writeBoolean(pkt.newNetwork);
                buf.writeInt(pkt.transmittersAdded.size());
                for (IGridTransmitter transmitter : pkt.transmittersAdded) {
                    transmitter.coord().write(buf);
                }
                break;
            case ENERGY:
                buf.writeDouble(pkt.power);
                break;
            case GAS:
                buf.writeInt(pkt.gasStack != null ? pkt.gasStack.getGas().getID() : -1);
                buf.writeInt(pkt.gasStack != null ? pkt.gasStack.amount : 0);
                buf.writeBoolean(pkt.didGasTransfer);
                break;
            case FLUID:
                if (pkt.fluidStack != null) {
                    buf.writeBoolean(true);
                    buf.writeCompoundTag(pkt.fluidStack.writeToNBT(new CompoundNBT()));
                } else {
                    buf.writeBoolean(false);
                }
                buf.writeBoolean(pkt.didFluidTransfer);
                break;
            default:
                break;
        }
    }

    public static PacketTransmitterUpdate decode(PacketBuffer buf) {
        PacketTransmitterUpdate packet = new PacketTransmitterUpdate(buf.readEnumValue(PacketType.class), Coord4D.read(buf));
        if (packet.packetType == PacketType.UPDATE) {
            packet.newNetwork = buf.readBoolean();
            packet.transmitterCoords = new HashSet<>();
            int numTransmitters = buf.readInt();

            for (int i = 0; i < numTransmitters; i++) {
                packet.transmitterCoords.add(Coord4D.read(buf));
            }
        } else if (packet.packetType == PacketType.ENERGY) {
            packet.power = buf.readDouble();
        } else if (packet.packetType == PacketType.GAS) {
            packet.gasType = GasRegistry.getGas(buf.readInt());
            packet.amount = buf.readInt();
            packet.didGasTransfer = buf.readBoolean();

            if (packet.gasType != null) {
                packet.gasStack = new GasStack(packet.gasType, packet.amount);
            }
        } else if (packet.packetType == PacketType.FLUID) {
            if (buf.readBoolean()) {
                packet.fluidStack = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
                packet.fluidType = packet.fluidStack != null ? packet.fluidStack.getFluid() : null;
            } else {
                packet.fluidType = null;
                packet.amount = 0;
                packet.fluidStack = null;
            }

            packet.didFluidTransfer = buf.readBoolean();
        }
        return packet;
    }

    public enum PacketType {
        UPDATE,
        ENERGY,
        GAS,
        FLUID
    }
}