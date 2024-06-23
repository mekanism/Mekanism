package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_FLUID_TO_FLUID)
public abstract class FluidToFluidRecipeManager extends MekanismRecipeManager<SingleFluidRecipeInput, FluidToFluidRecipe> {

    protected FluidToFluidRecipeManager(IMekanismRecipeTypeProvider<SingleFluidRecipeInput, FluidToFluidRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that converts a fluid into another fluid.
     * <br>
     * If this is called from the evaporating recipe manager, this will be an evaporating recipe and able to be processed in a thermal evaporation plant.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link CTFluidIngredient} representing the input of the recipe.
     * @param output {@link IFluidStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient input, IFluidStack output) {
        addRecipe(name, makeRecipe(input, output));
    }

    /**
     * Creates a recipe that converts a fluid into another fluid.
     *
     * @param input  {@link CTFluidIngredient} representing the input of the recipe.
     * @param output {@link IFluidStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final BasicFluidToFluidRecipe makeRecipe(CTFluidIngredient input, IFluidStack output) {
        return makeRecipe(input, getAndValidateNotEmpty(output));
    }

    protected abstract BasicFluidToFluidRecipe makeRecipe(CTFluidIngredient input, FluidStack output);

    @Override
    protected String describeOutputs(FluidToFluidRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), IFluidStack::of);
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_EVAPORATING)
    public static class EvaporatingRecipeManager extends FluidToFluidRecipeManager {

        public static final EvaporatingRecipeManager INSTANCE = new EvaporatingRecipeManager();

        private EvaporatingRecipeManager() {
            super(MekanismRecipeType.EVAPORATING);
        }

        @Override
        protected BasicFluidToFluidRecipe makeRecipe(CTFluidIngredient input, FluidStack output) {
            return new BasicFluidToFluidRecipe(CrTUtils.fromCrT(input), output);
        }
    }
}