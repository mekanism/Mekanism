/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * This class is used whenever stacks needs to be stored as keys.
 */
public final class StackKey {
	public final ItemStack stack;
	public final FluidStack fluidStack;

	public StackKey(FluidStack fluidStack) {
		this(null, fluidStack);
	}

	public StackKey(ItemStack stack) {
		this(stack, null);
	}

	public StackKey(ItemStack stack, FluidStack fluidStack) {
		this.stack = stack;
		this.fluidStack = fluidStack;
	}

	public static StackKey stack(Item item, int amount, int damage) {
		return new StackKey(new ItemStack(item, amount, damage));
	}

	public static StackKey stack(Block block, int amount, int damage) {
		return new StackKey(new ItemStack(block, amount, damage));
	}

	public static StackKey stack(Item item) {
		return new StackKey(new ItemStack(item, 1, 0));
	}

	public static StackKey stack(Block block) {
		return new StackKey(new ItemStack(block, 1, 0));
	}

	public static StackKey stack(ItemStack itemStack) {
		return new StackKey(itemStack);
	}

	public static StackKey fluid(Fluid fluid, int amount) {
		return new StackKey(new FluidStack(fluid, amount));
	}

	public static StackKey fluid(Fluid fluid) {
		return new StackKey(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME));
	}

	public static StackKey fluid(FluidStack fluidStack) {
		return new StackKey(fluidStack);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != StackKey.class) {
			return false;
		}
		StackKey k = (StackKey) o;
		if ((stack == null ^ k.stack == null) || (fluidStack == null ^ k.fluidStack == null)) {
			return false;
		}
		if (stack != null) {
			if (stack.getItem() != k.stack.getItem() ||
					stack.getHasSubtypes() && stack.getItemDamage() != k.stack.getItemDamage() ||
					!objectsEqual(stack.getTagCompound(), k.stack.getTagCompound())) {
				return false;
			}
		}
		if (fluidStack != null) {
			if (fluidStack.fluidID != k.fluidStack.fluidID ||
					fluidStack.amount != k.fluidStack.amount ||
					!objectsEqual(fluidStack.tag, k.fluidStack.tag)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 7;
		if (stack != null) {
			result = 31 * result + stack.getItem().hashCode();
			result = 31 * result + stack.getItemDamage();
			result = 31 * result + objectHashCode(stack.getTagCompound());
		}
		result = 31 * result + 7;
		if (fluidStack != null) {
			result = 31 * result + fluidStack.fluidID;
			result = 31 * result + fluidStack.amount;
			result = 31 * result + objectHashCode(fluidStack.tag);
		}
		return result;
	}

	private boolean objectsEqual(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		} else {
			return o1.equals(o2);
		}
	}
	
	private int objectHashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	public StackKey copy() {
		return new StackKey(stack != null ? stack.copy() : null,
				fluidStack != null ? fluidStack.copy() : null);
	}
}
