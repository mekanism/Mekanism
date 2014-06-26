package ic2.api.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

public class RecipeInputOreDict implements IRecipeInput {
	public RecipeInputOreDict(String input1) {
		this(input1, 1);
	}

	public RecipeInputOreDict(String input1, int amount1) {
		this(input1, amount1, null);
	}

	public RecipeInputOreDict(String input1, int amount1, Integer meta) {
		this.input = input1;
		this.amount = amount1;
		this.meta = meta;
	}

	@Override
	public boolean matches(ItemStack subject) {
		List<ItemStack> inputs = OreDictionary.getOres(input);

		for (ItemStack input1 : inputs) {
			if (input1.getItem() == null) continue; // ignore invalid
			int metaRequired = meta == null ? input1.getItemDamage() : meta;

			if (subject.getItem() == input1.getItem() &&
					(subject.getItemDamage() == metaRequired || metaRequired == OreDictionary.WILDCARD_VALUE)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public List<ItemStack> getInputs() {
		List<ItemStack> ores = OreDictionary.getOres(input);
		List<ItemStack> ret = new ArrayList<ItemStack>(ores.size());

		for (ItemStack stack : ores) {
			if (stack.getItem() != null) ret.add(stack); // ignore invalid
		}

		return ret;
	}

	@Override
	public String toString() {
		if (meta == null) {
			return "RInputOreDict<"+amount+"x"+input+">";
		} else {
			return "RInputOreDict<"+amount+"x"+input+"@"+meta+">";
		}
	}

	public final String input;
	public final int amount;
	public final Integer meta;
}
