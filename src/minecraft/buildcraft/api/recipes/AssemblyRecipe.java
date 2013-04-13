package buildcraft.api.recipes;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

public class AssemblyRecipe {

	public static LinkedList<AssemblyRecipe> assemblyRecipes = new LinkedList<AssemblyRecipe>();

	public final ItemStack[] input;
	public final ItemStack output;
	public final float energy;

	public AssemblyRecipe(ItemStack[] input, int energy, ItemStack output) {
		this.input = input;
		this.output = output;
		this.energy = energy;
	}

	public boolean canBeDone(ItemStack[] items) {

		for (ItemStack in : input) {

			if (in == null) {
				continue;
			}

			int found = 0; // Amount of ingredient found in inventory

			for (ItemStack item : items) {
				if (item == null) {
					continue;
				}

				if (item.isItemEqual(in)) {
					found += item.stackSize; // Adds quantity of stack to amount
												// found
				}
			}

			if (found < in.stackSize)
				return false; // Return false if the amount of ingredient found
								// is not enough
		}

		return true;
	}
}
