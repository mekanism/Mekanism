package com.jaquadro.minecraft.storagedrawers.api.registry;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Ingredient handlers are used to get ItemStacks from ingredients in custom IRecipe implementations.  If you have
 * registered an IRecipeHandler that returns lists of objects that aren't ItemStacks, then you will need to
 * implement an ingredient handler for those objects.
 */
public interface IIngredientHandler
{
    /**
     * Gets an ItemStack from an object that represents an ingredient in an IRecipe.
     *
     * @param object An ingredient object.
     * @return An ItemStack for the given ingredient.
     */
    @Nonnull
    ItemStack getItemStack (Object object);
}
