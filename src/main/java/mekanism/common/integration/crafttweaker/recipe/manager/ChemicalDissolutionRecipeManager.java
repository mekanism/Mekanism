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
     * Adds a chemical dissolution recipe that converts an item and a gas into a chemical. Chemical Dissolution Chambers can process this recipe type.
     *
     * @param name      Name of the new recipe.
     * @param itemInput {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param gasInput  {@link ChemicalStackIngredient} representing the gas input of the recipe.
     * @param output    {@link ICrTChemicalStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount itemInput, ChemicalStackIngredient gasInput, ICrTChemicalStack output) {
        addRecipe(name, makeRecipe(itemInput, gasInput, output));
    }

    /**
     * Makes a chemical dissolution recipe that converts an item and a gas into a chemical.
     *
     * @param itemInput {@link IIngredientWithAmount} representing the item input of the recipe.
     * @param gasInput  {@link ChemicalStackIngredient} representing the gas input of the recipe.
     * @param output    {@link ICrTChemicalStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final BasicChemicalDissolutionRecipe makeRecipe(IIngredientWithAmount itemInput, ChemicalStackIngredient gasInput, ICrTChemicalStack output) {
        return new BasicChemicalDissolutionRecipe(CrTUtils.fromCrT(itemInput), gasInput, getAndValidateNotEmpty(output));
    }

    @Override
    protected String describeOutputs(ChemicalDissolutionRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), stack -> {
            ICrTChemicalStack output = new CrTChemicalStack(stack);
            return output.toString();
        });
    }
}