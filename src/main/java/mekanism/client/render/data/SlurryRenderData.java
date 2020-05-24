package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;

public class SlurryRenderData extends ChemicalRenderData<Slurry, SlurryStack> {

    public SlurryRenderData(@Nonnull SlurryStack chemicalType) {
        super(chemicalType);
    }

    @Override
    public boolean isGaseous() {
        return false;
    }

    @Override
    public boolean equals(Object data) {
        return super.equals(data) && data instanceof SlurryRenderData && chemicalType.isTypeEqual(((SlurryRenderData) data).chemicalType);
    }
}