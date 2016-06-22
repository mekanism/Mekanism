package ic2.api.recipe;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

public class RecipeInputItemStack implements IRecipeInput {
	public RecipeInputItemStack(ItemStack aInput) {
		this(aInput, aInput.stackSize);
	}

	public RecipeInputItemStack(ItemStack aInput, int aAmount) {
		if (aInput.getItem() == null) throw new IllegalArgumentException("Invalid item stack specfied");

		input = aInput.copy(); // Never forget to copy.
		amount = aAmount;
	}

	@Override
	public boolean matches(ItemStack subject) {
		return subject.getItem() == input.getItem() &&
				(subject.getItemDamage() == input.getItemDamage() || input.getItemDamage() == OreDictionary.WILDCARD_VALUE);
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public List<ItemStack> getInputs() {
		return Arrays.asList(input);
	}

	@Override
	public String toString() {
		ItemStack stack = input.copy();
		input.stackSize = amount;
		return "RInputItemStack<"+stack+">";
	}

	public final ItemStack input;
	public final int amount;
}
