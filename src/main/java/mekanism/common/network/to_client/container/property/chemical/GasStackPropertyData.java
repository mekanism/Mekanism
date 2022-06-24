package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.gas.GasStack;
import mekanism.common.network.to_client.container.property.PropertyType;
import org.jetbrains.annotations.NotNull;

public class GasStackPropertyData extends ChemicalStackPropertyData<GasStack> {

    public GasStackPropertyData(short property, @NotNull GasStack value) {
        super(PropertyType.GAS_STACK, property, value);
    }
}