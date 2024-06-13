package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_GAS_TO_GAS)
public abstract class GasToGasRecipeManager extends MekanismRecipeManager<GasToGasRecipe> {

    protected GasToGasRecipeManager(IMekanismRecipeTypeProvider<?, GasToGasRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that converts a gas into another gas.
     * <br>
     * If this is called from the activating recipe manager, this will be an activating recipe and able to be processed in a solar neutron activator.
     * <br>
     * If this is called from the centrifuging recipe manager, this will be a centrifuging recipe and able to be processed in an isotopic centrifuge.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link GasStackIngredient} representing the input of the recipe.
     * @param output {@link ICrTGasStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, GasStackIngredient input, ICrTGasStack output) {
        addRecipe(name, makeRecipe(input, output));
    }

    /**
     * Creates a recipe that converts a gas into another gas.
     *
     * @param input  {@link GasStackIngredient} representing the input of the recipe.
     * @param output {@link ICrTGasStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final GasToGasRecipe makeRecipe(GasStackIngredient input, ICrTGasStack output) {
        return makeRecipe(input, getAndValidateNotEmpty(output));
    }

    protected abstract GasToGasRecipe makeRecipe(GasStackIngredient ingredient, GasStack output);

    @Override
    protected String describeOutputs(GasToGasRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition());
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ACTIVATING)
    public static class SolarNeutronActivatorRecipeManager extends GasToGasRecipeManager {

        public static final SolarNeutronActivatorRecipeManager INSTANCE = new SolarNeutronActivatorRecipeManager();

        private SolarNeutronActivatorRecipeManager() {
            super(MekanismRecipeType.ACTIVATING);
        }

        @Override
        protected GasToGasRecipe makeRecipe(GasStackIngredient ingredient, GasStack output) {
            return new BasicActivatingRecipe(ingredient, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CENTRIFUGING)
    public static class IsotopicCentrifugeRecipeManager extends GasToGasRecipeManager {

        public static final IsotopicCentrifugeRecipeManager INSTANCE = new IsotopicCentrifugeRecipeManager();

        private IsotopicCentrifugeRecipeManager() {
            super(MekanismRecipeType.CENTRIFUGING);
        }

        @Override
        protected GasToGasRecipe makeRecipe(GasStackIngredient ingredient, GasStack output) {
            return new BasicCentrifugingRecipe(ingredient, output);
        }
    }
}