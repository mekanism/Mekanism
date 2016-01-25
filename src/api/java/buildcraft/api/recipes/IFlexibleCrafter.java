/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IFlexibleCrafter {

    int getCraftingItemStackSize();

    ItemStack getCraftingItemStack(int slotid);

    ItemStack decrCraftingItemStack(int slotid, int val);

    FluidStack getCraftingFluidStack(int tankid);

    FluidStack decrCraftingFluidStack(int tankid, int val);

    int getCraftingFluidStackSize();

}
