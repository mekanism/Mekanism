package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CHEMICAL_INFUSING)
public class ChemicalInfuserRecipeManager extends MekanismRecipeManager {

    public static final ChemicalInfuserRecipeManager INSTANCE = new ChemicalInfuserRecipeManager();

    private ChemicalInfuserRecipeManager() {
    }

    @Override
    public IRecipeType<ChemicalInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.CHEMICAL_INFUSING;
    }

    private static class ActionAddChemicalInfuserRecipe extends ActionAddMekanismRecipe<ChemicalInfuserRecipe> {

        protected ActionAddChemicalInfuserRecipe(MekanismRecipeManager recipeManager, ChemicalInfuserRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), CrTGasStack::new);
        }
    }
}