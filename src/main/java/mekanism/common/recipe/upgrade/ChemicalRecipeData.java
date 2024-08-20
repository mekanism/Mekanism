package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalRecipeData implements RecipeUpgradeData<ChemicalRecipeData> {

    protected final List<IChemicalTank> tanks;

    public ChemicalRecipeData(List<IChemicalTank> tanks) {
        this.tanks = tanks;
    }

    @Nullable
    @Override
    public ChemicalRecipeData merge(ChemicalRecipeData other) {
        List<IChemicalTank> allTanks = new ArrayList<>(tanks);
        allTanks.addAll(other.tanks);
        return new ChemicalRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
        if (this.tanks.isEmpty()) {
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of chemicals together first
        // and maybe make it try multiple slot combinations
        IMekanismChemicalHandler outputHandler = ContainerType.CHEMICAL.createHandler(stack);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        for (IChemicalTank tank : this.tanks) {
            if (!tank.isEmpty() && !insertManualIntoOutputContainer(outputHandler, tank.getStack()).isEmpty()) {
                //If we have a remainder something failed so bail
                return false;
            }
        }
        return true;
    }

    private ChemicalStack insertManualIntoOutputContainer(IMekanismChemicalHandler outputHandler, ChemicalStack chemical) {
        //Insert into the output using manual as the automation type
        return ChemicalUtils.insert(chemical, null, outputHandler::getChemicalTanks, Action.EXECUTE, AutomationType.MANUAL);
    }
}