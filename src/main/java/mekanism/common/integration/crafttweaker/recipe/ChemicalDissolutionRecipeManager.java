package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalDissolutionIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_DISSOLUTION)
public class ChemicalDissolutionRecipeManager extends MekanismRecipeManager<ChemicalDissolutionRecipe> {

    public static final ChemicalDissolutionRecipeManager INSTANCE = new ChemicalDissolutionRecipeManager();

    private ChemicalDissolutionRecipeManager() {
        super(MekanismRecipeType.DISSOLUTION);
    }

    /**
     * Adds a chemical dissolution recipe that converts an item and a gas into a chemical. Chemical Dissolution Chambers can process this recipe type.
     *
     * @param name      Name of the new recipe.
     * @param itemInput {@link ItemStackIngredient} representing the item input of the recipe.
     * @param gasInput  {@link GasStackIngredient} representing the gas input of the recipe.
     * @param output    {@link ICrTChemicalStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient itemInput, GasStackIngredient gasInput, ICrTChemicalStack<?, ?, ?> output) {
        addRecipe(new ChemicalDissolutionIRecipe(getAndValidateName(name), itemInput, gasInput, getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ChemicalDissolutionRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                ICrTChemicalStack<?, ?, ?> output = CrTUtils.fromBoxedStack(getRecipe().getOutputDefinition());
                if (output == null) {
                    return "unknown chemical output";
                }
                return output.toString();
            }
        };
    }
}