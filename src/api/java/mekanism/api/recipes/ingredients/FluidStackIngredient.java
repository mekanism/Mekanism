package mekanism.api.recipes.ingredients;

import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for how Mekanism handle's FluidStack Ingredients.
 *
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()}.
 */
public abstract class FluidStackIngredient implements InputIngredient<@NotNull FluidStack> {
}