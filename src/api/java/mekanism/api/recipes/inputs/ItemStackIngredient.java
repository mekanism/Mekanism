package mekanism.api.recipes.inputs;

import mekanism.api.annotations.NonNull;
import net.minecraft.world.item.ItemStack;

/**
 * Base implementation for how Mekanism handle's ItemStack Ingredients.
 *
 * Create instances of this using {@link mekanism.api.recipes.inputs.creator.IngredientCreatorAccess#item()}.
 */
public abstract class ItemStackIngredient implements InputIngredient<@NonNull ItemStack> {
}