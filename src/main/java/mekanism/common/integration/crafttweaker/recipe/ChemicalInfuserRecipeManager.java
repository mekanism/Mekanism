package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CHEMICAL_INFUSING)
public class ChemicalInfuserRecipeManager extends MekanismRecipeManager<ChemicalInfuserRecipe> {

    public static final ChemicalInfuserRecipeManager INSTANCE = new ChemicalInfuserRecipeManager();

    private ChemicalInfuserRecipeManager() {
        super(MekanismRecipeType.CHEMICAL_INFUSING);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTGasStackIngredient leftInput, CrTGasStackIngredient rightInput, ICrTGasStack output) {
        addRecipe(new ChemicalInfuserIRecipe(getAndValidateName(name), leftInput.getInternal(), rightInput.getInternal(), getAndValidateNotEmpty(output)));
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