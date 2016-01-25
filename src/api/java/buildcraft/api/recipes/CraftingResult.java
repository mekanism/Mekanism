/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.recipes;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CraftingResult<T> {

    public T crafted = null;
    public ArrayList<ItemStack> usedItems = new ArrayList<ItemStack>();
    public ArrayList<FluidStack> usedFluids = new ArrayList<FluidStack>();
    public int energyCost = 0;
    public long craftingTime = 0;
    public IFlexibleRecipe<T> recipe;
}
