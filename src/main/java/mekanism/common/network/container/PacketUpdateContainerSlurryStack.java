package mekanism.common.network.container;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for slurry stacks
 */
public class PacketUpdateContainerSlurryStack extends PacketUpdateContainer<PacketUpdateContainerSlurryStack> {

    @Nonnull
    private final SlurryStack value;

    public PacketUpdateContainerSlurryStack(short windowId, short property, @Nonnull SlurryStack value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerSlurryStack(PacketBuffer buffer) {
        super(buffer);
        this.value = ChemicalUtils.readSlurryStack(buffer);
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        ChemicalUtils.writeChemicalStack(buffer, value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerSlurryStack message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerSlurryStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerSlurryStack(buf);
    }
}