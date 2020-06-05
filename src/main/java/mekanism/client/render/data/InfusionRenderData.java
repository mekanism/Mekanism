package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.api.chemical.infuse.InfusionStack;

public class InfusionRenderData extends ChemicalRenderData<InfusionStack> {

    public InfusionRenderData(@Nonnull InfusionStack chemicalType) {
        super(chemicalType);
    }

    @Override
    public boolean isGaseous() {
        return false;
    }

    @Override
    public boolean equals(Object data) {
        return super.equals(data) && data instanceof InfusionRenderData && chemicalType.isTypeEqual(((InfusionRenderData) data).chemicalType);
    }
}