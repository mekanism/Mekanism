package com.jaquadro.minecraft.storagedrawers.api.registry;

import net.minecraft.item.crafting.IRecipe;

import java.util.List;

/**
 * Recipe handlers are used by compacting drawers to find more-compressed forms of blocks and items.  If your recipe
 * to craft compressed items is a custom IRecipe implementation, you will need to register a handler for it.
 */
public interface IRecipeHandler
{
    /**
     * Get the recipe ingredient list as an array of objects (usually used for shaped recipes).
     * If your array does not contain ItemStack objects, you will need to register an {@link IIngredientHandler} to
     * get an ItemStack from them.
     *
     * If you would prefer to return a List, return null in this method and implement {@link #getInputAsList}.
     *
     * @param recipe An instance of a custom {@link IRecipe}.
     * @return An array of ItemStacks or objects with a registered {@link IIngredientHandler}.
     */
    Object[] getInputAsArray (IRecipe recipe);

    /**
     * Get the recipe ingredient list as a list of objects (usually used for shapeless recipes).
     * If your list does not contain ItemStack objects, you will need to register an {@link IIngredientHandler} to
     * get an ItemStack from them.
     *
     * If you would prefer to return an array, return null in this method and implement {@link #getInputAsArray}.
     *
     * @param recipe An instance of a custom {@link IRecipe}.
     * @return A list of ItemStacks or objects with a registered {@link IIngredientHandler}.
     */
    List getInputAsList (IRecipe recipe);
}
