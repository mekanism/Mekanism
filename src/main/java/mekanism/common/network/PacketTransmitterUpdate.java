package mekanism.common.network;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.FluidNetwork;
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

    public static void handle(PacketTransmitterUpdate message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(message.networkID);
            if (clientNetwork != null && message.packetType.networkTypeMatches(clientNetwork)) {
                //Note: We set the information even if opaque transmitters is true in case the client turns the config setting off
                // so that they will have the proper information to then render
                if (message.packetType == PacketType.CHEMICAL) {
                    ((BoxedChemicalNetwork) clientNetwork).setLastChemical(message.chemical);
                } else if (message.packetType == PacketType.FLUID) {
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
        if (pkt.packetType == PacketType.FLUID) {
            pkt.fluidStack.writeToPacket(buf);
        } else if (pkt.packetType == PacketType.CHEMICAL) {
            pkt.chemical.write(buf);
        }
    }

    public static PacketTransmitterUpdate decode(PacketBuffer buf) {
        PacketTransmitterUpdate packet = new PacketTransmitterUpdate(buf.readEnumValue(PacketType.class), buf.readUniqueId(), buf.readFloat());
        if (packet.packetType == PacketType.FLUID) {
            packet.fluidStack = FluidStack.readFromPacket(buf);
        } else if (packet.packetType == PacketType.CHEMICAL) {
            packet.chemical = BoxedChemical.read(buf);
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