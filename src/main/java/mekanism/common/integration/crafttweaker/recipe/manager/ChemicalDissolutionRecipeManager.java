package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_DISSOLUTION)
public class ChemicalDissolutionRecipeManager extends MekanismRecipeManager<SingleItemChemicalRecipeInput, ChemicalDissolutionRecipe> {

    public static final ChemicalDissolutionRecipeManager INSTANCE = new ChemicalDissolutionRecipeManager();

    private ChemicalDissolutionRecipeManager() {
        super(MekanismRecipeType.DISSOLUTION);
    }

    /**
     * Adds a chemical dissolution recipe that converts an item and a chemical into a chemical. Chemical Dissolution Chambers can process this recipe type.
     *
     * @param name          Name of the new recipe.
     * @param itemInput     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param chemicalInput {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param output        {@link ICrTChemicalStack} representing the output of the recipe.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ICrTChemicalStack output, boolean perTickUsage) {
        addRecipe(name, makeRecipe(itemInput, chemicalInput, output, perTickUsage));
    }

    /**
     * Makes a chemical dissolution recipe that converts an item and a chemical into a chemical.
     *
     * @param itemInput     {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param chemicalInput {@link ChemicalStackIngredient} representing the chemical input of the recipe.
     * @param output        {@link ICrTChemicalStack} representing the output of the recipe. Will be validated as not empty.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public final BasicChemicalDissolutionRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient chemicalInput, ICrTChemicalStack output,
          boolean perTickUsage) {
        return new BasicChemicalDissolutionRecipe(CrTUtils.fromCrT(itemInput), chemicalInput, getAndValidateNotEmpty(output), perTickUsage);
    }

    @Override
    protected String describeOutputs(ChemicalDissolutionRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), stack -> {
            ICrTChemicalStack output = new CrTChemicalStack(stack);
            return output.toString();
        });
    }
}