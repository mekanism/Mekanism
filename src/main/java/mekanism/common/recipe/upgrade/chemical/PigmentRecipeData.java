package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.pigment.AttachedPigments;

@NothingNullByDefault
public class PigmentRecipeData extends ChemicalRecipeData<Pigment, PigmentStack, IPigmentTank> {

    public PigmentRecipeData(List<IPigmentTank> tanks) {
        super(tanks);
    }

    @Override
    protected PigmentRecipeData create(List<IPigmentTank> tanks) {
        return new PigmentRecipeData(tanks);
    }

    @Override
    protected ContainerType<IPigmentTank, AttachedPigments, ? extends IMekanismPigmentHandler> getContainerType() {
        return ContainerType.PIGMENT;
    }
}