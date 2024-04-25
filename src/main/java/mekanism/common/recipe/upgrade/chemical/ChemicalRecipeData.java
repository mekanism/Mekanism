package mekanism.common.recipe.upgrade.chemical;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.recipe.upgrade.RecipeUpgradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalRecipeData<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      implements RecipeUpgradeData<ChemicalRecipeData<CHEMICAL, STACK, TANK>> {

    protected final List<TANK> tanks;

    protected ChemicalRecipeData(List<TANK> tanks) {
        this.tanks = tanks;
    }

    @Nullable
    @Override
    public ChemicalRecipeData<CHEMICAL, STACK, TANK> merge(ChemicalRecipeData<CHEMICAL, STACK, TANK> other) {
        List<TANK> allTanks = new ArrayList<>(tanks);
        allTanks.addAll(other.tanks);
        return create(allTanks);
    }

    protected abstract ChemicalRecipeData<CHEMICAL, STACK, TANK> create(List<TANK> tanks);

    protected abstract ContainerType<TANK, ? extends IMekanismChemicalHandler<CHEMICAL, STACK, TANK>, ?> getContainerType();

    @Override
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
        if (this.tanks.isEmpty()) {
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of chemicals together first
        // and maybe make it try multiple slot combinations
        IMekanismChemicalHandler<CHEMICAL, STACK, TANK> outputHandler = getContainerType().getAttachment(stack);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        for (TANK tank : this.tanks) {
            if (!tank.isEmpty() && !insertManualIntoOutputContainer(outputHandler, tank.getStack()).isEmpty()) {
                //If we have a remainder something failed so bail
                return false;
            }
        }
        return true;
    }

    private STACK insertManualIntoOutputContainer(IMekanismChemicalHandler<CHEMICAL, STACK, TANK> outputHandler, STACK chemical) {
        //Insert into the output using manual as the automation type
        return ChemicalUtils.insert(chemical, null, outputHandler::getChemicalTanks, Action.EXECUTE, AutomationType.MANUAL, outputHandler.getEmptyStack());
    }
}