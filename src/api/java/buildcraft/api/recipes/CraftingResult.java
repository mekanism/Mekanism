/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.recipes;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

public class CraftingResult<T> {

	public T crafted = null;
	public ArrayList<ItemStack> usedItems = new ArrayList<ItemStack>();
	public ArrayList<FluidStack> usedFluids = new ArrayList<FluidStack>();
	public double energyCost = 0;
	public long craftingTime = 0;
	public IFlexibleRecipe<?> recipe;
}
