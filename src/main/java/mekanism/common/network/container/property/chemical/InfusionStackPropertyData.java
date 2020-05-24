package mekanism.common.network.container.property.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.network.container.chemical.PacketUpdateContainerInfusionStack;
import mekanism.common.network.container.property.PropertyType;

public class InfusionStackPropertyData extends ChemicalStackPropertyData<InfuseType, InfusionStack> {

    public InfusionStackPropertyData(short property, @Nonnull InfusionStack value) {
        super(PropertyType.INFUSION_STACK, property, value);
    }

    @Override
    public PacketUpdateContainerInfusionStack getSinglePacket(short windowId) {
        return new PacketUpdateContainerInfusionStack(windowId, getProperty(), value);
    }
}