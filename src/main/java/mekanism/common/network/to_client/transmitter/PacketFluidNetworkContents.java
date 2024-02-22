package mekanism.common.network.to_client.transmitter;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketFluidNetworkContents(UUID networkID, FluidStack fluid) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("fluid_network");

    public PacketFluidNetworkContents(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), FluidStack.readFromPacket(buffer));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        //Note: We set the information even if opaque transmitters is true in case the client turns the config setting off
        // so that they will have the proper information to then render
        DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(networkID);
        if (clientNetwork instanceof FluidNetwork network) {
            network.setLastFluid(fluid);
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeUUID(networkID);
        fluid.writeToPacket(buffer);
        PacketUtils.log("Sending type '{}' update message for fluid network with id {}", RegistryUtils.getName(fluid.getFluid()), networkID);
    }
}