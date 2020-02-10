package mekanism.common.network.container;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for fluid stacks
 */
public class PacketUpdateContainerFluidStack extends PacketUpdateContainer<PacketUpdateContainerFluidStack> {

    @Nonnull
    private final FluidStack value;

    public PacketUpdateContainerFluidStack(short windowId, short property, @Nonnull FluidStack value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerFluidStack(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readFluidStack();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeFluidStack(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerFluidStack message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerFluidStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerFluidStack(buf);
    }
}