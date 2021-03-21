package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PigmentMixingIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_PIGMENT_MIXING)
public class PigmentMixingRecipeManager extends MekanismRecipeManager<PigmentMixingRecipe> {

    public static final PigmentMixingRecipeManager INSTANCE = new PigmentMixingRecipeManager();

    private PigmentMixingRecipeManager() {
        super(MekanismRecipeType.PIGMENT_MIXING);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, ICrTPigmentStack output) {
        addRecipe(new PigmentMixingIRecipe(getAndValidateName(name), leftInput, rightInput, getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(PigmentMixingRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), CrTPigmentStack::new);
            }
        };
    }
}