package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SEPARATING)
public class ElectrolysisRecipeManager extends MekanismRecipeManager<ElectrolysisRecipe> {

    public static final ElectrolysisRecipeManager INSTANCE = new ElectrolysisRecipeManager();

    private ElectrolysisRecipeManager() {
        super(MekanismRecipeType.SEPARATING);
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ElectrolysisRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return new CrTGasStack(getRecipe().getLeftGasOutputRepresentation()) + " and " + new CrTGasStack(getRecipe().getRightGasOutputRepresentation());
            }
        };
    }
}