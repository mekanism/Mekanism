package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.impl.PigmentMixingIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_CHEMICAL_TO_CHEMICAL)
public abstract class ChemicalChemicalToChemicalRecipeManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>,
      RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> extends MekanismRecipeManager<RECIPE> {

    protected ChemicalChemicalToChemicalRecipeManager(IMekanismRecipeTypeProvider<RECIPE, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that combines two chemicals of the same type into another chemical of the same type.
     * <br>
     * If this is called from the pigment mixing recipe manager, this will be a pigment mixing recipe. Pigment Mixers can process this recipe type and the pigments can be
     * put in any order into the mixer.
     * <br>
     * If this is called from the chemical infuser recipe manager, this will be a chemical infuser recipe. Chemical Infusers can process this recipe type and the pigments
     * can be put in any order into the infuser.
     *
     * @param name       Name of the new recipe.
     * @param leftInput  Chemical stack ingredient representing the "left" chemical input of the recipe.
     * @param rightInput Chemical stack ingredient representing the "right" chemical input of the recipe.
     * @param output     Chemical stack representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, INGREDIENT leftInput, INGREDIENT rightInput, CRT_STACK output) {
        addRecipe(makeRecipe(getAndValidateName(name), leftInput, rightInput, output));
    }

    /**
     * Creates a recipe that combines two chemicals of the same type into another chemical of the same type.
     *
     * @param id         Name of the new recipe.
     * @param leftInput  Chemical stack ingredient representing the "left" chemical input of the recipe.
     * @param rightInput Chemical stack ingredient representing the "right" chemical input of the recipe.
     * @param output     Chemical stack representing the output of the recipe. Will be validated as not empty.
     */
    public final RECIPE makeRecipe(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, CRT_STACK output) {
        return makeRecipe(id, leftInput, rightInput, getAndValidateNotEmpty(output));
    }

    protected abstract RECIPE makeRecipe(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output);

    @Override
    protected ActionAddMekanismRecipe getAction(RECIPE recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(recipe.getOutputDefinition());
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_INFUSING)
    public static class ChemicalInfuserRecipeManager extends ChemicalChemicalToChemicalRecipeManager<Gas, GasStack, GasStackIngredient, ICrTGasStack, ChemicalInfuserRecipe> {

        public static final ChemicalInfuserRecipeManager INSTANCE = new ChemicalInfuserRecipeManager();

        private ChemicalInfuserRecipeManager() {
            super(MekanismRecipeType.CHEMICAL_INFUSING);
        }

        @Override
        protected ChemicalInfuserRecipe makeRecipe(ResourceLocation id, GasStackIngredient left, GasStackIngredient right, GasStack output) {
            return new ChemicalInfuserIRecipe(id, left, right, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_PIGMENT_MIXING)
    public static class PigmentMixingRecipeManager extends ChemicalChemicalToChemicalRecipeManager<Pigment, PigmentStack, PigmentStackIngredient, ICrTPigmentStack, PigmentMixingRecipe> {

        public static final PigmentMixingRecipeManager INSTANCE = new PigmentMixingRecipeManager();

        private PigmentMixingRecipeManager() {
            super(MekanismRecipeType.PIGMENT_MIXING);
        }

        @Override
        protected PigmentMixingRecipe makeRecipe(ResourceLocation id, PigmentStackIngredient left, PigmentStackIngredient right, PigmentStack output) {
            return new PigmentMixingIRecipe(id, left, right, output);
        }
    }
}