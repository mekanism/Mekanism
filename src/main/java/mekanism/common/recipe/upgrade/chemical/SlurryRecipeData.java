package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.slurry.AttachedSlurries;

@NothingNullByDefault
public class SlurryRecipeData extends ChemicalRecipeData<Slurry, SlurryStack, ISlurryTank> {

    public SlurryRecipeData(List<ISlurryTank> tanks) {
        super(tanks);
    }

    @Override
    protected SlurryRecipeData create(List<ISlurryTank> tanks) {
        return new SlurryRecipeData(tanks);
    }

    @Override
    protected ContainerType<ISlurryTank, AttachedSlurries, ? extends IMekanismSlurryHandler> getContainerType() {
        return ContainerType.SLURRY;
    }
}