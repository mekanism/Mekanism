package mekanism.api.chemical;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;

@NothingNullByDefault
public interface IMekanismChemicalHandler extends IMekanismResourceHandler<ChemicalResource, IChemicalTank> {

    @Override
    default ChemicalResource getEmptyResource() {
        return ChemicalResource.EMPTY;
    }
}