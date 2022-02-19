package mekanism.common.network.to_client.container.property.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.network.to_client.container.property.PropertyType;

public class PigmentStackPropertyData extends ChemicalStackPropertyData<PigmentStack> {

    public PigmentStackPropertyData(short property, @Nonnull PigmentStack value) {
        super(PropertyType.PIGMENT_STACK, property, value);
    }
}