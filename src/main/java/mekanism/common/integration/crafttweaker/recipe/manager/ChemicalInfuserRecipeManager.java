package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_INFUSING)
public class ChemicalInfuserRecipeManager extends MekanismRecipeManager<ChemicalInfuserRecipe> {

    public static final ChemicalInfuserRecipeManager INSTANCE = new ChemicalInfuserRecipeManager();

    private ChemicalInfuserRecipeManager() {
        super(MekanismRecipeType.CHEMICAL_INFUSING);
    }

    /**
     * Adds a chemical infuser recipe that converts two gases into another gas. Chemical Infusers can process this recipe type and the gases can be put in any order into
     * the infuser.
     *
     * @param name       Name of the new recipe.
     * @param leftInput  {@link GasStackIngredient} representing the "left" gas input of the recipe.
     * @param rightInput {@link GasStackIngredient} representing the "right" gas input of the recipe.
     * @param output     {@link ICrTGasStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, GasStackIngredient leftInput, GasStackIngredient rightInput, ICrTGasStack output) {
        addRecipe(new ChemicalInfuserIRecipe(getAndValidateName(name), leftInput, rightInput, getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ChemicalInfuserRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), CrTGasStack::new);
            }
        };
    }
}