package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.cache.chemical.FluidChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraftforge.fluids.FluidStack;

/**
 * Base class to help implement handling of fluid slurry to slurry recipes.
 */
@Deprecated//TODO - 1.18: Remove this
@ParametersAreNonnullByDefault
public class FluidSlurryToSlurryCachedRecipe extends FluidChemicalToChemicalCachedRecipe<Slurry, SlurryStack, SlurryStackIngredient, FluidSlurryToSlurryRecipe> {

    /**
     * @param recipe             Recipe.
     * @param fluidInputHandler  Fluid input handler.
     * @param slurryInputHandler Slurry input handler.
     * @param outputHandler      Output handler.
     */
    public FluidSlurryToSlurryCachedRecipe(FluidSlurryToSlurryRecipe recipe, IInputHandler<@NonNull FluidStack> fluidInputHandler,
          IInputHandler<@NonNull SlurryStack> slurryInputHandler, IOutputHandler<@NonNull SlurryStack> outputHandler) {
        super(recipe, fluidInputHandler, slurryInputHandler, outputHandler);
    }
}