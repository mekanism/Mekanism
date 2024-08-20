package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_TO_CHEMICAL)
public abstract class ChemicalToChemicalRecipeManager extends MekanismRecipeManager<SingleChemicalRecipeInput, ChemicalToChemicalRecipe> {

    protected ChemicalToChemicalRecipeManager(IMekanismRecipeTypeProvider<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that converts a chemical into another chemical.
     * <br>
     * If this is called from the activating recipe manager, this will be an activating recipe and able to be processed in a solar neutron activator.
     * <br>
     * If this is called from the centrifuging recipe manager, this will be a centrifuging recipe and able to be processed in an isotopic centrifuge.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link ChemicalStackIngredient} representing the input of the recipe.
     * @param output {@link ICrTChemicalStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ChemicalStackIngredient input, ICrTChemicalStack output) {
        addRecipe(name, makeRecipe(input, output));
    }

    /**
     * Creates a recipe that converts a chemical into another chemical.
     *
     * @param input  {@link ChemicalStackIngredient} representing the input of the recipe.
     * @param output {@link ICrTChemicalStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final ChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient input, ICrTChemicalStack output) {
        return makeRecipe(input, getAndValidateNotEmpty(output));
    }

    protected abstract ChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient ingredient, ChemicalStack output);

    @Override
    protected String describeOutputs(ChemicalToChemicalRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition());
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ACTIVATING)
    public static class SolarNeutronActivatorRecipeManager extends ChemicalToChemicalRecipeManager {

        public static final SolarNeutronActivatorRecipeManager INSTANCE = new SolarNeutronActivatorRecipeManager();

        private SolarNeutronActivatorRecipeManager() {
            super(MekanismRecipeType.ACTIVATING);
        }

        @Override
        protected ChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient ingredient, ChemicalStack output) {
            return new BasicActivatingRecipe(ingredient, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CENTRIFUGING)
    public static class IsotopicCentrifugeRecipeManager extends ChemicalToChemicalRecipeManager {

        public static final IsotopicCentrifugeRecipeManager INSTANCE = new IsotopicCentrifugeRecipeManager();

        private IsotopicCentrifugeRecipeManager() {
            super(MekanismRecipeType.CENTRIFUGING);
        }

        @Override
        protected ChemicalToChemicalRecipe makeRecipe(ChemicalStackIngredient ingredient, ChemicalStack output) {
            return new BasicCentrifugingRecipe(ingredient, output);
        }
    }
}