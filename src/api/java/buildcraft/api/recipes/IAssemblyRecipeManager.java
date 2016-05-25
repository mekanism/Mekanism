/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.recipes;

import java.util.Collection;

import net.minecraft.item.ItemStack;

public interface IAssemblyRecipeManager {

    /** Add an Assembly Table recipe.
     *
     * @param input Object... containing either an ItemStack, or a paired string and integer(ex: "dyeBlue", 1)
     * @param energyCost RF cost to produce
     * @param output resulting ItemStack */
    void addRecipe(String id, int energyCost, ItemStack output, Object... input);

    void addRecipe(IFlexibleRecipe<ItemStack> recipe);

    void removeRecipe(String id);

    void removeRecipe(IFlexibleRecipe<ItemStack> recipe);

    Collection<IFlexibleRecipe<ItemStack>> getRecipes();
}
