package mekanism.api.recipes.ingredients;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for how Mekanism handle's ItemStack Ingredients.
 *
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()}.
 */
public abstract class ItemStackIngredient implements InputIngredient<@NotNull ItemStack> {
}