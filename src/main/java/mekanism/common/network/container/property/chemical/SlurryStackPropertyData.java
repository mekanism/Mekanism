package mekanism.common.network.container.property.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.network.container.chemical.PacketUpdateContainerSlurryStack;
import mekanism.common.network.container.property.PropertyType;

public class SlurryStackPropertyData extends ChemicalStackPropertyData<SlurryStack> {

    public SlurryStackPropertyData(short property, @Nonnull SlurryStack value) {
        super(PropertyType.SLURRY_STACK, property, value);
    }

    @Override
    public PacketUpdateContainerSlurryStack getSinglePacket(short windowId) {
        return new PacketUpdateContainerSlurryStack(windowId, getProperty(), value);
    }
}