package mekanism.api.recipes.inputs;

import mekanism.api.annotations.NonNull;
import net.minecraftforge.fluids.FluidStack;

/**
 * Base implementation for how Mekanism handle's FluidStack Ingredients.
 *
 * Create instances of this using {@link mekanism.api.recipes.inputs.creator.IngredientCreatorAccess#fluid()}.
 */
public abstract class FluidStackIngredient implements InputIngredient<@NonNull FluidStack> {
}