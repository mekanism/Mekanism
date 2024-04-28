package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.gas.AttachedGases;

@NothingNullByDefault
public class GasRecipeData extends ChemicalRecipeData<Gas, GasStack, IGasTank> {

    public GasRecipeData(List<IGasTank> tanks) {
        super(tanks);
    }

    @Override
    protected GasRecipeData create(List<IGasTank> tanks) {
        return new GasRecipeData(tanks);
    }

    @Override
    protected ContainerType<IGasTank, AttachedGases, ? extends IMekanismGasHandler> getContainerType() {
        return ContainerType.GAS;
    }
}