package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.transmitters.grid.EnergyNetwork;
import mekanism.common.transmitters.grid.FluidNetwork;
import mekanism.common.transmitters.grid.GasNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketTransmitterUpdate {

    private final PacketType packetType;
    private final UUID networkID;
    private float energyScale;
    @Nonnull
    private Gas gas = MekanismAPI.EMPTY_GAS;
    private float gasScale;
    @Nonnull
    private FluidStack fluidStack = FluidStack.EMPTY;
    private float fluidScale;

    public PacketTransmitterUpdate(EnergyNetwork network, float energyScale) {
        this(network, PacketType.ENERGY);
        this.energyScale = energyScale;
    }

    public PacketTransmitterUpdate(GasNetwork network, @Nonnull Gas gas, float gasScale) {
        this(network, PacketType.GAS);
        this.gas = gas;
        this.gasScale = gasScale;
    }

    public PacketTransmitterUpdate(FluidNetwork network, @Nonnull FluidStack fluidStack, float fluidScale) {
        this(network, PacketType.FLUID);
        this.fluidStack = fluidStack;
        this.fluidScale = fluidScale;
    }

    private PacketTransmitterUpdate(DynamicNetwork<?, ?, ?> network, PacketType type) {
        this(type, network.getUUID());
    }

    private PacketTransmitterUpdate(PacketType type, UUID networkID) {
        packetType = type;
        this.networkID = networkID;
    }

    public static void handle(PacketTransmitterUpdate message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(message.networkID);
            //Note: We set the information even if opaque transmitters is true in case the client turns the config setting off
            // so that they will have the proper information to then render
            if (message.packetType == PacketType.ENERGY) {
                if (clientNetwork instanceof EnergyNetwork) {
                    ((EnergyNetwork) clientNetwork).energyScale = message.energyScale;
                }
            } else if (message.packetType == PacketType.GAS) {
                if (clientNetwork instanceof GasNetwork) {
                    GasNetwork net = (GasNetwork) clientNetwork;
                    net.setLastGas(message.gas);
                    net.gasScale = message.gasScale;
                }
            } else if (message.packetType == PacketType.FLUID) {
                if (clientNetwork instanceof FluidNetwork) {
                    FluidNetwork net = (FluidNetwork) clientNetwork;
                    net.setLastFluid(message.fluidStack);
                    net.fluidScale = message.fluidScale;
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTransmitterUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        buf.writeUniqueId(pkt.networkID);
        BasePacketHandler.log("Sending '" + pkt.packetType + "' update message for network with id " + pkt.networkID);
        switch (pkt.packetType) {
            case ENERGY:
                buf.writeFloat(pkt.energyScale);
                break;
            case GAS:
                buf.writeRegistryId(pkt.gas);
                buf.writeFloat(pkt.gasScale);
                break;
            case FLUID:
                pkt.fluidStack.writeToPacket(buf);
                buf.writeFloat(pkt.fluidScale);
                break;
            default:
                break;
        }
    }

    public static PacketTransmitterUpdate decode(PacketBuffer buf) {
        PacketTransmitterUpdate packet = new PacketTransmitterUpdate(buf.readEnumValue(PacketType.class), buf.readUniqueId());
        if (packet.packetType == PacketType.ENERGY) {
            packet.energyScale = buf.readFloat();
        } else if (packet.packetType == PacketType.GAS) {
            packet.gas = buf.readRegistryId();
            packet.gasScale = buf.readFloat();
        } else if (packet.packetType == PacketType.FLUID) {
            packet.fluidStack = FluidStack.readFromPacket(buf);
            packet.fluidScale = buf.readFloat();
        }
        return packet;
    }

    public enum PacketType {
        ENERGY,
        GAS,
        FLUID
    }
}