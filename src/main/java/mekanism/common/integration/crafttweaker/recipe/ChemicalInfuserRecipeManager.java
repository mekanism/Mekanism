package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CHEMICAL_INFUSING)
public class ChemicalInfuserRecipeManager extends MekanismRecipeManager<ChemicalInfuserRecipe> {

    public static final ChemicalInfuserRecipeManager INSTANCE = new ChemicalInfuserRecipeManager();

    private ChemicalInfuserRecipeManager() {
        super(MekanismRecipeType.CHEMICAL_INFUSING);
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ChemicalInfuserRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), CrTGasStack::new);
            }
        };
    }
}