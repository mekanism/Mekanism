package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedInfusionTanks;
import mekanism.common.attachments.containers.ContainerType;

@NothingNullByDefault
public class InfusionRecipeData extends ChemicalRecipeData<InfuseType, InfusionStack, IInfusionTank> {

    public InfusionRecipeData(List<IInfusionTank> tanks) {
        super(tanks);
    }

    @Override
    protected InfusionRecipeData create(List<IInfusionTank> tanks) {
        return new InfusionRecipeData(tanks);
    }

    @Override
    protected ContainerType<IInfusionTank, AttachedInfusionTanks, IInfusionHandler> getContainerType() {
        return ContainerType.INFUSION;
    }
}