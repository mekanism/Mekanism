package mekanism.common.network.container.property;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainerInfusionStack;
import net.minecraft.network.PacketBuffer;

public class InfusionStackPropertyData extends PropertyData {

    @Nonnull
    private final InfusionStack value;

    public InfusionStackPropertyData(short property, @Nonnull InfusionStack value) {
        super(PropertyType.INFUSION_STACK, property);
        this.value = value;
    }

    @Override
    public PacketUpdateContainerInfusionStack getSinglePacket(short windowId) {
        return new PacketUpdateContainerInfusionStack(windowId, getProperty(), value);
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        ChemicalUtils.writeChemicalStack(buffer, value);
    }
}