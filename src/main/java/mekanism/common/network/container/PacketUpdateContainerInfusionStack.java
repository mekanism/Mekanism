package mekanism.common.network.container;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfusionStack;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for infusion stacks
 */
public class PacketUpdateContainerInfusionStack extends PacketUpdateContainer<PacketUpdateContainerInfusionStack> {

    @Nonnull
    private final InfusionStack value;

    public PacketUpdateContainerInfusionStack(short windowId, short property, @Nonnull InfusionStack value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerInfusionStack(PacketBuffer buffer) {
        super(buffer);
        this.value = PacketHandler.readInfusionStack(buffer);
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        PacketHandler.writeChemicalStack(buffer, value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerInfusionStack message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerInfusionStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerInfusionStack(buf);
    }
}