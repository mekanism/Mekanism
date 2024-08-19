package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicWashingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleFluidChemicalRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_FLUID_CHEMICAL_TO_CHEMICAL)
public abstract class FluidChemicalToChemicalRecipeManager extends MekanismRecipeManager<SingleFluidChemicalRecipeInput, FluidChemicalToChemicalRecipe> {

    protected FluidChemicalToChemicalRecipeManager(IMekanismRecipeTypeProvider<SingleFluidChemicalRecipeInput, FluidChemicalToChemicalRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that converts a fluid and chemical to another chemical.
     * <br>
     * If this is called from the washing recipe manager, this will be a washing recipe and able to be processed in a chemical washer.
     *
     * @param name          Name of the new recipe.
     * @param fluidInput    {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param chemicalInput {@link ChemicalStackIngredient} representing the slurry input of the recipe.
     * @param output        {@link ICrTChemicalStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient fluidInput, ChemicalStackIngredient chemicalInput, ICrTChemicalStack output) {
        addRecipe(name, makeRecipe(fluidInput, chemicalInput, output));
    }

    /**
     * Creates a recipe that converts a fluid and chemical to another chemical.
     *
     * @param fluidInput    {@link CTFluidIngredient} representing the fluid input of the recipe.
     * @param chemicalInput {@link ChemicalStackIngredient} representing the slurry input of the recipe.
     * @param output        {@link ICrTChemicalStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final FluidChemicalToChemicalRecipe makeRecipe(CTFluidIngredient fluidInput, ChemicalStackIngredient chemicalInput, ICrTChemicalStack output) {
        return makeRecipe(fluidInput, chemicalInput, getAndValidateNotEmpty(output));
    }

    protected abstract FluidChemicalToChemicalRecipe makeRecipe(CTFluidIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStack output);

    @Override
    protected String describeOutputs(FluidChemicalToChemicalRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition());
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_WASHING)
    public static class ChemicalWasherRecipeManager extends FluidChemicalToChemicalRecipeManager {

        public static final ChemicalWasherRecipeManager INSTANCE = new ChemicalWasherRecipeManager();

        private ChemicalWasherRecipeManager() {
            super(MekanismRecipeType.WASHING);
        }

        @Override
        protected FluidChemicalToChemicalRecipe makeRecipe(CTFluidIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStack output) {
            return new BasicWashingRecipe(CrTUtils.fromCrT(fluidInput), chemicalInput, output);
        }
    }
}