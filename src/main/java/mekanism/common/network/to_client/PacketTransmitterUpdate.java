package mekanism.common.network.to_client;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketTransmitterUpdate implements IMekanismPacket {

    private final PacketType packetType;
    private final UUID networkID;
    private final float scale;
    @Nonnull
    private BoxedChemical chemical = BoxedChemical.EMPTY;
    @Nonnull
    private FluidStack fluidStack = FluidStack.EMPTY;

    public PacketTransmitterUpdate(EnergyNetwork network) {
        this(network, PacketType.ENERGY);
    }

    public PacketTransmitterUpdate(BoxedChemicalNetwork network, @Nonnull BoxedChemical chemical) {
        this(network, PacketType.CHEMICAL);
        this.chemical = chemical;
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

    @Override
    public void handle(NetworkEvent.Context context) {
        DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(networkID);
        if (clientNetwork != null && packetType.networkTypeMatches(clientNetwork)) {
            //Note: We set the information even if opaque transmitters is true in case the client turns the config setting off
            // so that they will have the proper information to then render
            if (packetType == PacketType.CHEMICAL) {
                ((BoxedChemicalNetwork) clientNetwork).setLastChemical(chemical);
            } else if (packetType == PacketType.FLUID) {
                ((FluidNetwork) clientNetwork).setLastFluid(fluidStack);
            }
            ((DynamicBufferedNetwork<?, ?, ?, ?>) clientNetwork).currentScale = scale;
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(packetType);
        buffer.writeUUID(networkID);
        buffer.writeFloat(scale);
        BasePacketHandler.log("Sending '{}' update message for network with id {}", packetType, networkID);
        if (packetType == PacketType.FLUID) {
            fluidStack.writeToPacket(buffer);
        } else if (packetType == PacketType.CHEMICAL) {
            chemical.write(buffer);
        }
    }

    public static PacketTransmitterUpdate decode(PacketBuffer buffer) {
        PacketTransmitterUpdate packet = new PacketTransmitterUpdate(buffer.readEnum(PacketType.class), buffer.readUUID(), buffer.readFloat());
        if (packet.packetType == PacketType.FLUID) {
            packet.fluidStack = FluidStack.readFromPacket(buffer);
        } else if (packet.packetType == PacketType.CHEMICAL) {
            packet.chemical = BoxedChemical.read(buffer);
        }
        return packet;
    }

    public enum PacketType {
        ENERGY(net -> net instanceof EnergyNetwork),
        FLUID(net -> net instanceof FluidNetwork),
        CHEMICAL(net -> net instanceof BoxedChemicalNetwork);

        private final Predicate<DynamicNetwork<?, ?, ?>> networkTypePredicate;

        PacketType(Predicate<DynamicNetwork<?, ?, ?>> networkTypePredicate) {
            this.networkTypePredicate = networkTypePredicate;
        }

        private boolean networkTypeMatches(DynamicNetwork<?, ?, ?> network) {
            return networkTypePredicate.test(network);
        }
    }
}