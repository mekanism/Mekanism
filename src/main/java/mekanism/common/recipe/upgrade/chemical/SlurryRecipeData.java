package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedSlurryTanks;
import mekanism.common.attachments.containers.ContainerType;

@NothingNullByDefault
public class SlurryRecipeData extends ChemicalRecipeData<Slurry, SlurryStack, ISlurryTank> {

    public SlurryRecipeData(AttachedSlurryTanks attachment) {
        super(attachment);
    }

    private SlurryRecipeData(List<ISlurryTank> tanks) {
        super(tanks);
    }

    @Override
    protected SlurryRecipeData create(List<ISlurryTank> tanks) {
        return new SlurryRecipeData(tanks);
    }

    @Override
    protected ContainerType<ISlurryTank, AttachedSlurryTanks, ISlurryHandler> getContainerType() {
        return ContainerType.SLURRY;
    }
}