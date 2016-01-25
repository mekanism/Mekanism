/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.recipes;

import net.minecraft.item.ItemStack;

public interface IFlexibleRecipe<T> {

    boolean canBeCrafted(IFlexibleCrafter crafter);

    CraftingResult<T> craft(IFlexibleCrafter crafter, boolean preview);

    CraftingResult<T> canCraft(ItemStack expectedOutput);

    String getId();
}
