package mekanism.common.network.container.property.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.network.container.chemical.PacketUpdateContainerPigmentStack;
import mekanism.common.network.container.property.PropertyType;

public class PigmentStackPropertyData extends ChemicalStackPropertyData<PigmentStack> {

    public PigmentStackPropertyData(short property, @Nonnull PigmentStack value) {
        super(PropertyType.PIGMENT_STACK, property, value);
    }

    @Override
    public PacketUpdateContainerPigmentStack getSinglePacket(short windowId) {
        return new PacketUpdateContainerPigmentStack(windowId, getProperty(), value);
    }
}