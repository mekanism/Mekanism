package mekanism.api.recipes.ingredients;

import mekanism.api.annotations.NonNull;
import net.minecraftforge.fluids.FluidStack;

/**
 * Base implementation for how Mekanism handle's FluidStack Ingredients.
 *
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()}.
 */
public abstract class FluidStackIngredient implements InputIngredient<@NonNull FluidStack> {
}