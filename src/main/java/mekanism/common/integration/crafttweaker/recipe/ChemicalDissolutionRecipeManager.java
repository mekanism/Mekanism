package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_DISSOLUTION)
public class ChemicalDissolutionRecipeManager extends MekanismRecipeManager {

    public static final ChemicalDissolutionRecipeManager INSTANCE = new ChemicalDissolutionRecipeManager();

    private ChemicalDissolutionRecipeManager() {
    }

    @Override
    public IRecipeType<ChemicalDissolutionRecipe> getRecipeType() {
        return MekanismRecipeType.DISSOLUTION;
    }

    private static class ActionAddChemicalDissolutionRecipe extends ActionAddMekanismRecipe<ChemicalDissolutionRecipe> {

        protected ActionAddChemicalDissolutionRecipe(MekanismRecipeManager recipeManager, ChemicalDissolutionRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            ICrTChemicalStack<?, ?, ?, ?> output = CrTUtils.fromBoxedStack(getRecipe().getOutputDefinition());
            if (output == null) {
                return "unknown chemical output";
            }
            return output.toString();
        }
    }
}