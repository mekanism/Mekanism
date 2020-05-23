package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;

public class GasRenderData extends ChemicalRenderData<Gas, GasStack> {

    public GasRenderData(@Nonnull GasStack chemicalType) {
        super(chemicalType);
    }

    @Override
    public boolean isGaseous() {
        return true;
    }

    @Override
    public boolean equals(Object data) {
        return super.equals(data) && data instanceof GasRenderData && chemicalType.isTypeEqual(((GasRenderData) data).chemicalType);
    }
}