package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.network.to_client.container.property.PropertyType;
import org.jetbrains.annotations.NotNull;

public class PigmentStackPropertyData extends ChemicalStackPropertyData<PigmentStack> {

    public PigmentStackPropertyData(short property, @NotNull PigmentStack value) {
        super(PropertyType.PIGMENT_STACK, property, value);
    }
}