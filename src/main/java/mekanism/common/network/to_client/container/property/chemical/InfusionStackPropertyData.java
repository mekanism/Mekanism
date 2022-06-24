package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.network.to_client.container.property.PropertyType;
import org.jetbrains.annotations.NotNull;

public class InfusionStackPropertyData extends ChemicalStackPropertyData<InfusionStack> {

    public InfusionStackPropertyData(short property, @NotNull InfusionStack value) {
        super(PropertyType.INFUSION_STACK, property, value);
    }
}