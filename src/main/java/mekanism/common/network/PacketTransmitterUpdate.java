package mekanism.common.network;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.content.transmitter.ChemicalNetwork;
import mekanism.common.content.transmitter.EnergyNetwork;
import mekanism.common.content.transmitter.FluidNetwork;
import mekanism.common.content.transmitter.GasNetwork;
import mekanism.common.content.transmitter.InfusionNetwork;
import mekanism.common.content.transmitter.PigmentNetwork;
import mekanism.common.content.transmitter.SlurryNetwork;
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
    private Chemical<?> chemical = MekanismAPI.EMPTY_GAS;
    @Nonnull
    private FluidStack fluidStack = FluidStack.EMPTY;

    public PacketTransmitterUpdate(EnergyNetwork network) {
        this(network, PacketType.ENERGY);
    }

    public PacketTransmitterUpdate(GasNetwork network, @Nonnull Gas gas) {
        this(network, PacketType.GAS, gas);
    }

    public PacketTransmitterUpdate(InfusionNetwork network, @Nonnull InfuseType infuseType) {
        this(network, PacketType.INFUSION, infuseType);
    }

    public PacketTransmitterUpdate(PigmentNetwork network, @Nonnull Pigment pigment) {
        this(network, PacketType.PIGMENT, pigment);
    }

    public PacketTransmitterUpdate(SlurryNetwork network, @Nonnull Slurry slurry) {
        this(network, PacketType.SLURRY, slurry);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>> PacketTransmitterUpdate(ChemicalNetwork<CHEMICAL, ?, ?, ?, ?> network, PacketType type, @Nonnull CHEMICAL chemical) {
        this(network, type);
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
                if (message.packetType.isChemical()) {
                    ((ChemicalNetwork<?, ?, ?, ?, ?>) clientNetwork).setLastChemical(message.packetType.castChemical(message.chemical));
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
        } else if (pkt.packetType == PacketType.GAS) {
            buf.writeRegistryId((Gas) pkt.chemical);
        } else if (pkt.packetType == PacketType.INFUSION) {
            buf.writeRegistryId((InfuseType) pkt.chemical);
        } else if (pkt.packetType == PacketType.PIGMENT) {
            buf.writeRegistryId((Pigment) pkt.chemical);
        } else if (pkt.packetType == PacketType.SLURRY) {
            buf.writeRegistryId((Slurry) pkt.chemical);
        }
    }

    public static PacketTransmitterUpdate decode(PacketBuffer buf) {
        PacketTransmitterUpdate packet = new PacketTransmitterUpdate(buf.readEnumValue(PacketType.class), buf.readUniqueId(), buf.readFloat());
        if (packet.packetType.isChemical()) {
            packet.chemical = buf.readRegistryId();
        } else if (packet.packetType == PacketType.FLUID) {
            packet.fluidStack = FluidStack.readFromPacket(buf);
        }
        return packet;
    }

    public enum PacketType {
        ENERGY(net -> net instanceof EnergyNetwork, false),
        FLUID(net -> net instanceof FluidNetwork, false),
        GAS(net -> net instanceof GasNetwork, true),
        INFUSION(net -> net instanceof InfusionNetwork, true),
        PIGMENT(net -> net instanceof PigmentNetwork, true),
        SLURRY(net -> net instanceof SlurryNetwork, true);

        private final Predicate<DynamicNetwork<?, ?, ?>> networkTypePredicate;
        private final boolean isChemical;

        PacketType(Predicate<DynamicNetwork<?, ?, ?>> networkTypePredicate, boolean isChemical) {
            this.networkTypePredicate = networkTypePredicate;
            this.isChemical = isChemical;
        }

        private boolean isChemical() {
            return isChemical;
        }

        private boolean networkTypeMatches(DynamicNetwork<?, ?, ?> network) {
            return networkTypePredicate.test(network);
        }

        private <CHEMICAL extends Chemical<CHEMICAL>> CHEMICAL castChemical(Chemical<?> chemical) {
            return (CHEMICAL) chemical;
        }
    }
}