package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.network.to_client.container.property.PropertyType;
import org.jetbrains.annotations.NotNull;

public class SlurryStackPropertyData extends ChemicalStackPropertyData<SlurryStack> {

    public SlurryStackPropertyData(short property, @NotNull SlurryStack value) {
        super(PropertyType.SLURRY_STACK, property, value);
    }
}