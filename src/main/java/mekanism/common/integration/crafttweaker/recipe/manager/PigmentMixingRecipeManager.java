package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PigmentMixingIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_PIGMENT_MIXING)
public class PigmentMixingRecipeManager extends MekanismRecipeManager<PigmentMixingRecipe> {

    public static final PigmentMixingRecipeManager INSTANCE = new PigmentMixingRecipeManager();

    private PigmentMixingRecipeManager() {
        super(MekanismRecipeType.PIGMENT_MIXING);
    }

    /**
     * Adds a pigment mixing recipe that converts two pigments into another pigment. Pigment mixers can process this recipe type and the pigments can be put in any order
     * into the mixer.
     *
     * @param name       Name of the new recipe.
     * @param leftInput  {@link PigmentStackIngredient} representing the "left" pigment input of the recipe.
     * @param rightInput {@link PigmentStackIngredient} representing the "right" pigment input of the recipe.
     * @param output     {@link ICrTPigmentStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, ICrTPigmentStack output) {
        addRecipe(new PigmentMixingIRecipe(getAndValidateName(name), leftInput, rightInput, getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(PigmentMixingRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition(), CrTPigmentStack::new);
            }
        };
    }
}