package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SEPARATING)
public class ElectrolysisRecipeManager extends MekanismRecipeManager {

    public static final ElectrolysisRecipeManager INSTANCE = new ElectrolysisRecipeManager();

    private ElectrolysisRecipeManager() {
    }

    @Override
    public IRecipeType<ElectrolysisRecipe> getRecipeType() {
        return MekanismRecipeType.SEPARATING;
    }

    private static class ActionAddElectrolysisRecipe extends ActionAddMekanismRecipe<ElectrolysisRecipe> {

        protected ActionAddElectrolysisRecipe(MekanismRecipeManager recipeManager, ElectrolysisRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return new CrTGasStack(getRecipe().getLeftGasOutputRepresentation()) + " and " + new CrTGasStack(getRecipe().getRightGasOutputRepresentation());
        }
    }
}