package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.content.transmitter.EnergyNetwork;
import mekanism.common.content.transmitter.FluidNetwork;
import mekanism.common.content.transmitter.GasNetwork;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketTransmitterUpdate {

    private final PacketType packetType;
    private final UUID networkID;
    private final float scale;
    @Nonnull
    private Gas gas = MekanismAPI.EMPTY_GAS;
    @Nonnull
    private FluidStack fluidStack = FluidStack.EMPTY;

    public PacketTransmitterUpdate(EnergyNetwork network) {
        this(network, PacketType.ENERGY);
    }

    public PacketTransmitterUpdate(GasNetwork network, @Nonnull Gas gas) {
        this(network, PacketType.GAS);
        this.gas = gas;
    }

    public PacketTransmitterUpdate(FluidNetwork network, @Nonnull FluidStack fluidStack) {
        this(network, PacketType.FLUID);
        this.fluidStack = fluidStack;
    }

    private PacketTransmitterUpdate(DynamicBufferedNetwork<?, ?, ?, ?> network, PacketType type) {
        this(type, network.getUUID(), network.currentScale);
    }

    private PacketTransmitterUpdate(PacketType type, UUID networkID, float scale) {
        packetType = type;
        this.networkID = networkID;
        this.scale = scale;
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
            if (clientNetwork instanceof DynamicBufferedNetwork) {
                if (message.packetType == PacketType.GAS) {
                    if (clientNetwork instanceof GasNetwork) {
                        ((GasNetwork) clientNetwork).setLastGas(message.gas);
                    }
                } else if (message.packetType == PacketType.FLUID && clientNetwork instanceof FluidNetwork) {
                    ((FluidNetwork) clientNetwork).setLastFluid(message.fluidStack);
                }
                ((DynamicBufferedNetwork<?, ?, ?, ?>) clientNetwork).currentScale = message.scale;
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTransmitterUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        buf.writeUniqueId(pkt.networkID);
        buf.writeFloat(pkt.scale);
        BasePacketHandler.log("Sending '" + pkt.packetType + "' update message for network with id " + pkt.networkID);
        if (pkt.packetType == PacketType.GAS) {
            buf.writeRegistryId(pkt.gas);
        } else if (pkt.packetType == PacketType.FLUID) {
            pkt.fluidStack.writeToPacket(buf);
        }
    }

    public static PacketTransmitterUpdate decode(PacketBuffer buf) {
        PacketTransmitterUpdate packet = new PacketTransmitterUpdate(buf.readEnumValue(PacketType.class), buf.readUniqueId(), buf.readFloat());
        if (packet.packetType == PacketType.GAS) {
            packet.gas = buf.readRegistryId();
        } else if (packet.packetType == PacketType.FLUID) {
            packet.fluidStack = FluidStack.readFromPacket(buf);
        }
        return packet;
    }

    public enum PacketType {
        ENERGY,
        GAS,
        FLUID
    }
}