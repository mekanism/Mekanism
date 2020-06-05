package mekanism.common.network.container.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for slurry stacks
 */
public abstract class PacketUpdateContainerChemicalStack<STACK extends ChemicalStack<?>> extends PacketUpdateContainer<PacketUpdateContainerChemicalStack<STACK>> {

    @Nonnull
    private final STACK value;

    protected PacketUpdateContainerChemicalStack(short windowId, short property, @Nonnull STACK value) {
        super(windowId, property);
        this.value = value;
    }

    protected PacketUpdateContainerChemicalStack(PacketBuffer buffer) {
        super(buffer);
        this.value = readStack(buffer);
    }

    protected abstract STACK readStack(PacketBuffer buffer);

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        ChemicalUtils.writeChemicalStack(buffer, value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerChemicalStack<STACK> message) {
        container.handleWindowProperty(message.property, message.value);
    }
}