package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
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

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient itemInput, CrTGasStackIngredient gasInput, ICrTChemicalStack<?, ?, ?, ?> output) {
        addRecipe(new ChemicalDissolutionIRecipe(getAndValidateName(name), itemInput.getInternal(), gasInput.getInternal(), getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ChemicalDissolutionRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                ICrTChemicalStack<?, ?, ?, ?> output = CrTUtils.fromBoxedStack(getRecipe().getOutputDefinition());
                if (output == null) {
                    return "unknown chemical output";
                }
                return output.toString();
            }
        };
    }
}