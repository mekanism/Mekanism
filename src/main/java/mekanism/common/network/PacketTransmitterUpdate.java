package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.HashSet;
import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.PacketHandler;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import mekanism.common.transmitters.grid.EnergyNetwork;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.transmitters.grid.GasNetwork;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTransmitterUpdate implements IMessageHandler<TransmitterUpdateMessage, IMessage> {

    @Override
    public IMessage onMessage(TransmitterUpdateMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);

        if (player == null) {
            return null;
        }

        PacketHandler.handlePacket(() ->
        {
            if (message.packetType == PacketType.UPDATE) {
                TileEntity tileEntity = message.coord4D.getTileEntity(player.world);

                if (CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
                    IGridTransmitter transmitter = CapabilityUtils
                          .getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null);
                    DynamicNetwork network = transmitter.hasTransmitterNetwork() && !message.newNetwork ? transmitter
                          .getTransmitterNetwork() : transmitter.createEmptyNetwork();
                    network.register();
                    transmitter.setTransmitterNetwork(network);

                    for (Coord4D coord : message.transmitterCoords) {
                        TileEntity tile = coord.getTileEntity(player.world);

                        if (CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
                            CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)
                                  .setTransmitterNetwork(network);
                        }
                    }

                    network.updateCapacity();
                }
            }

            if (MekanismConfig.current().client.opaqueTransmitters.val() || message.coord4D == null || player == null) {
                return;
            }

            if (message.packetType == PacketType.ENERGY) {
                TileEntity tileEntity = message.coord4D.getTileEntity(player.world);

                if (CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
                    IGridTransmitter transmitter = CapabilityUtils
                          .getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null);

                    if (transmitter.hasTransmitterNetwork()
                          && transmitter.getTransmissionType() == TransmissionType.ENERGY) {
                        ((IGridTransmitter<EnergyAcceptorWrapper, EnergyNetwork>) transmitter)
                              .getTransmitterNetwork().clientEnergyScale = message.power;
                    }
                }
            } else if (message.packetType == PacketType.GAS) {
                TileEntity tileEntity = message.coord4D.getTileEntity(player.world);

                if (CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
                    IGridTransmitter transmitter = CapabilityUtils
                          .getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null);

                    if (transmitter.hasTransmitterNetwork()
                          && transmitter.getTransmissionType() == TransmissionType.GAS) {
                        GasNetwork net = ((IGridTransmitter<IGasHandler, GasNetwork>) transmitter)
                              .getTransmitterNetwork();

                        if (message.gasType != null) {
                            net.refGas = message.gasType;
                        }

                        net.buffer = message.gasStack;
                        net.didTransfer = message.didGasTransfer;
                    }
                }
            } else if (message.packetType == PacketType.FLUID) {
                TileEntity tileEntity = message.coord4D.getTileEntity(player.world);

                if (CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
                    IGridTransmitter transmitter = CapabilityUtils
                          .getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null);

                    if (transmitter.hasTransmitterNetwork()
                          && transmitter.getTransmissionType() == TransmissionType.FLUID) {
                        FluidNetwork net = ((IGridTransmitter<IFluidHandler, FluidNetwork>) transmitter)
                              .getTransmitterNetwork();

                        if (message.fluidType != null) {
                            net.refFluid = message.fluidType;
                        }

                        net.buffer = message.fluidStack;
                        net.didTransfer = message.didFluidTransfer;
                    }
                }
            }
        }, player);

        return null;
    }

    public enum PacketType {
        UPDATE,
        ENERGY,
        GAS,
        FLUID
    }

    public static class TransmitterUpdateMessage implements IMessage {

        public PacketType packetType;

        public Coord4D coord4D;

        public double power;

        public GasStack gasStack;
        public Gas gasType;
        public boolean didGasTransfer;

        public FluidStack fluidStack;
        public Fluid fluidType;
        public float fluidScale;
        public boolean didFluidTransfer;

        public int amount;

        public boolean newNetwork;
        public Collection<IGridTransmitter> transmittersAdded;
        public Collection<Coord4D> transmitterCoords;

        public TransmitterUpdateMessage() {
        }

        public TransmitterUpdateMessage(PacketType type, Coord4D coord, Object... data) {
            packetType = type;
            coord4D = coord;

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

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(packetType.ordinal());

            coord4D.write(dataStream);

            PacketHandler.log("Sending '" + packetType + "' update message from coordinate " + coord4D);

            switch (packetType) {
                case UPDATE:
                    dataStream.writeBoolean(newNetwork);
                    dataStream.writeInt(transmittersAdded.size());

                    for (IGridTransmitter transmitter : transmittersAdded) {
                        transmitter.coord().write(dataStream);
                    }

                    break;
                case ENERGY:
                    dataStream.writeDouble(power);

                    break;
                case GAS:
                    dataStream.writeInt(gasStack != null ? gasStack.getGas().getID() : -1);
                    dataStream.writeInt(gasStack != null ? gasStack.amount : 0);
                    dataStream.writeBoolean(didGasTransfer);

                    break;
                case FLUID:
                    if (fluidStack != null) {
                        dataStream.writeBoolean(true);
                        PacketHandler.writeNBT(dataStream, fluidStack.writeToNBT(new NBTTagCompound()));
                    } else {
                        dataStream.writeBoolean(false);
                    }

                    dataStream.writeBoolean(didFluidTransfer);

                    break;
                default:
                    break;
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            packetType = PacketType.values()[dataStream.readInt()];

            coord4D = Coord4D.read(dataStream);

            if (packetType == PacketType.UPDATE) {
                newNetwork = dataStream.readBoolean();
                transmitterCoords = new HashSet<>();
                int numTransmitters = dataStream.readInt();

                for (int i = 0; i < numTransmitters; i++) {
                    transmitterCoords.add(Coord4D.read(dataStream));
                }
            } else if (packetType == PacketType.ENERGY) {
                power = dataStream.readDouble();
            } else if (packetType == PacketType.GAS) {
                gasType = GasRegistry.getGas(dataStream.readInt());
                amount = dataStream.readInt();
                didGasTransfer = dataStream.readBoolean();

                if (gasType != null) {
                    gasStack = new GasStack(gasType, amount);
                }
            } else if (packetType == PacketType.FLUID) {
                if (dataStream.readBoolean()) {
                    fluidStack = FluidStack.loadFluidStackFromNBT(PacketHandler.readNBT(dataStream));
                    fluidType = fluidStack != null ? fluidStack.getFluid() : null;
                } else {
                    fluidType = null;
                    amount = 0;
                    fluidStack = null;
                }

                didFluidTransfer = dataStream.readBoolean();

            }
        }
    }
}
