package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_CHEMICAL_TO_CHEMICAL)
public abstract class ChemicalChemicalToChemicalRecipeManager extends MekanismRecipeManager<BiChemicalRecipeInput, ChemicalChemicalToChemicalRecipe> {

    protected ChemicalChemicalToChemicalRecipeManager(IMekanismRecipeTypeProvider<BiChemicalRecipeInput, ChemicalChemicalToChemicalRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that combines two chemicals of the same type into another chemical of the same type.
     * <br>
     * If this is called from the pigment mixing recipe manager, this will be a pigment mixing recipe. Pigment Mixers can process this recipe type and the chemicals can
     * be put in any order into the mixer.
     * <br>
     * If this is called from the chemical infuser recipe manager, this will be a chemical infuser recipe. Chemical Infusers can process this recipe type and the
     * chemicals can be put in any order into the infuser.
     *
     * @param name       Name of the new recipe.
     * @param leftInput  Chemical stack ingredient representing the "left" chemical input of the recipe.
     * @param rightInput Chemical stack ingredient representing the "right" chemical input of the recipe.
     * @param output     Chemical stack representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ICrTChemicalStack output) {
        addRecipe(name, makeRecipe(leftInput, rightInput, output));
    }

    /**
     * Creates a recipe that combines two chemicals of the same type into another chemical of the same type.
     *
     * @param leftInput  Chemical stack ingredient representing the "left" chemical input of the recipe.
     * @param rightInput Chemical stack ingredient representing the "right" chemical input of the recipe.
     * @param output     Chemical stack representing the output of the recipe. Will be validated as not empty.
     */
    public final ChemicalChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ICrTChemicalStack output) {
        return makeRecipe(leftInput, rightInput, getAndValidateNotEmpty(output));
    }

    protected abstract ChemicalChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStack output);

    @Override
    protected String describeOutputs(ChemicalChemicalToChemicalRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition());
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_INFUSING)
    public static class ChemicalInfuserRecipeManager extends ChemicalChemicalToChemicalRecipeManager {

        public static final ChemicalInfuserRecipeManager INSTANCE = new ChemicalInfuserRecipeManager();

        private ChemicalInfuserRecipeManager() {
            super(MekanismRecipeType.CHEMICAL_INFUSING);
        }

        @Override
        protected ChemicalChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient left, ChemicalStackIngredient right, ChemicalStack output) {
            return new BasicChemicalInfuserRecipe(left, right, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_PIGMENT_MIXING)
    public static class PigmentMixingRecipeManager extends ChemicalChemicalToChemicalRecipeManager {

        public static final PigmentMixingRecipeManager INSTANCE = new PigmentMixingRecipeManager();

        private PigmentMixingRecipeManager() {
            super(MekanismRecipeType.PIGMENT_MIXING);
        }

        @Override
        protected ChemicalChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient left, ChemicalStackIngredient right, ChemicalStack output) {
            return new BasicPigmentMixingRecipe(left, right, output);
        }
    }
}