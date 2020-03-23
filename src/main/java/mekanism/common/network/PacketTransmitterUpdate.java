package mekanism.common.network;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.PacketHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.transmitters.grid.EnergyNetwork;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.transmitters.grid.GasNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

//TODO: Do we want to split this into multiple packets
public class PacketTransmitterUpdate {

    private PacketType packetType;

    private Coord4D coord4D;

    private float energyScale;

    @Nonnull
    private GasStack gasStack = GasStack.EMPTY;
    private float gasScale;

    @Nonnull
    private FluidStack fluidStack = FluidStack.EMPTY;
    private float fluidScale;

    private boolean newNetwork;
    private Collection<IGridTransmitter<?, ?, ?>> transmittersAdded;
    private Collection<Coord4D> transmitterCoords;

    public PacketTransmitterUpdate(DynamicNetwork<?, ?, ?> network, boolean newNetwork, Collection<IGridTransmitter<?, ?, ?>> transmittersAdded) {
        this(network, PacketType.UPDATE);
        this.newNetwork = newNetwork;
        this.transmittersAdded = transmittersAdded;
    }

    public PacketTransmitterUpdate(EnergyNetwork network, float energyScale) {
        this(network, PacketType.ENERGY);
        this.energyScale = energyScale;
    }

    public PacketTransmitterUpdate(GasNetwork network, @Nonnull GasStack gasStack, float gasScale) {
        this(network, PacketType.GAS);
        this.gasStack = gasStack;
        this.gasScale = gasScale;
    }

    public PacketTransmitterUpdate(FluidNetwork network, @Nonnull FluidStack fluidStack, float fluidScale) {
        this(network, PacketType.FLUID);
        this.fluidStack = fluidStack;
        this.fluidScale = fluidScale;
    }

    private PacketTransmitterUpdate(DynamicNetwork<?, ?, ?> network, PacketType type) {
        this(type, network.firstTransmitter().coord());
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
            TileEntity tileEntity = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
            Optional<IGridTransmitter<?, ?, ?>> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null));
            if (capability.isPresent()) {
                //TODO: Evaluate this stuff and see if we can do it in a way that is fine for generics
                IGridTransmitter transmitter = capability.get();
                if (message.packetType == PacketType.UPDATE) {
                    DynamicNetwork<?, ?, ?> network = transmitter.hasTransmitterNetwork() && !message.newNetwork ? transmitter.getTransmitterNetwork() : transmitter.createEmptyNetwork();
                    network.register();
                    transmitter.setTransmitterNetwork(network);
                    for (Coord4D coord : message.transmitterCoords) {
                        TileEntity tile = MekanismUtils.getTileEntity(player.world, coord.getPos());
                        CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)
                              .ifPresent(gridTransmitter -> ((IGridTransmitter) gridTransmitter).setTransmitterNetwork(network));
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
                        ((EnergyNetwork) transmitter.getTransmitterNetwork()).energyScale = message.energyScale;
                    }
                } else if (message.packetType == PacketType.GAS) {
                    if (transmissionType == TransmissionType.GAS) {
                        GasNetwork net = (GasNetwork) transmitter.getTransmitterNetwork();
                        net.gasTank.setStack(message.gasStack);
                        net.gasScale = message.gasScale;
                    }
                } else if (message.packetType == PacketType.FLUID) {
                    if (transmissionType == TransmissionType.FLUID) {
                        FluidNetwork net = (FluidNetwork) transmitter.getTransmitterNetwork();
                        net.fluidTank.setStack(message.fluidStack);
                        net.fluidScale = message.fluidScale;
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTransmitterUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        pkt.coord4D.write(buf);
        PacketHandler.log("Sending '" + pkt.packetType + "' update message from coordinate " + pkt.coord4D);
        switch (pkt.packetType) {
            case UPDATE:
                buf.writeBoolean(pkt.newNetwork);
                buf.writeInt(pkt.transmittersAdded.size());
                for (IGridTransmitter<?, ?, ?> transmitter : pkt.transmittersAdded) {
                    transmitter.coord().write(buf);
                }
                break;
            case ENERGY:
                buf.writeFloat(pkt.energyScale);
                break;
            case GAS:
                pkt.gasStack.writeToPacket(buf);
                buf.writeFloat(pkt.gasScale);
                break;
            case FLUID:
                //TODO: Use FluidStack#writeToPacket in more places
                pkt.fluidStack.writeToPacket(buf);
                buf.writeFloat(pkt.fluidScale);
                break;
            default:
                break;
        }
    }

    public static PacketTransmitterUpdate decode(PacketBuffer buf) {
        PacketTransmitterUpdate packet = new PacketTransmitterUpdate(buf.readEnumValue(PacketType.class), Coord4D.read(buf));
        if (packet.packetType == PacketType.UPDATE) {
            packet.newNetwork = buf.readBoolean();
            packet.transmitterCoords = new ObjectOpenHashSet<>();
            int numTransmitters = buf.readInt();

            for (int i = 0; i < numTransmitters; i++) {
                packet.transmitterCoords.add(Coord4D.read(buf));
            }
        } else if (packet.packetType == PacketType.ENERGY) {
            packet.energyScale = buf.readFloat();
        } else if (packet.packetType == PacketType.GAS) {
            packet.gasStack = GasStack.readFromPacket(buf);
            packet.gasScale = buf.readFloat();
        } else if (packet.packetType == PacketType.FLUID) {
            packet.fluidStack = FluidStack.readFromPacket(buf);
            packet.fluidScale = buf.readFloat();
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