package mekanism.common.network.to_client.transmitter;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketFluidNetworkContents(UUID networkID, FluidStack fluid) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketFluidNetworkContents> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("fluid_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketFluidNetworkContents> STREAM_CODEC = StreamCodec.composite(
          UUIDUtil.STREAM_CODEC, PacketFluidNetworkContents::networkID,
          FluidStack.OPTIONAL_STREAM_CODEC, PacketFluidNetworkContents::fluid,
          PacketFluidNetworkContents::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketFluidNetworkContents> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        //Note: We set the information even if opaque transmitters is true in case the client turns the config setting off
        // so that they will have the proper information to then render
        DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getClientNetwork(networkID);
        if (clientNetwork instanceof FluidNetwork network) {
            network.setLastFluid(fluid);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PacketFluidNetworkContents other = (PacketFluidNetworkContents) o;
        return networkID.equals(other.networkID) && FluidStack.matches(fluid, other.fluid);
    }

    @Override
    public int hashCode() {
        int hash = networkID.hashCode();
        hash = 31 * hash + FluidStack.hashFluidAndComponents(fluid);
        hash = 31 * hash + fluid.getAmount();
        return hash;
    }
}