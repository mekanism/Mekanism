package ic2.api.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.Item;
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
		List<ItemStack> inputs = getOres();
		boolean useOreStackMeta = (meta == null);
		Item subjectItem = subject.getItem();
		int subjectMeta = subject.getItemDamage();

		for (ItemStack oreStack : inputs) {
			Item oreItem = oreStack.getItem();
			if (oreItem == null) continue; // ignore invalid

			int metaRequired = useOreStackMeta ? oreStack.getItemDamage() : meta;

			if (subjectItem == oreItem &&
					(subjectMeta == metaRequired || metaRequired == OreDictionary.WILDCARD_VALUE)) {
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
		List<ItemStack> ores = getOres();

		// check if we have to filter the list first
		boolean hasInvalidEntries = false;

		for (ItemStack stack : ores) {
			if (stack.getItem() == null) {
				hasInvalidEntries = true;
				break;
			}
		}

		if (!hasInvalidEntries) return ores;

		List<ItemStack> ret = new ArrayList<ItemStack>(ores.size());

		for (ItemStack stack : ores) {
			if (stack.getItem() != null) ret.add(stack); // ignore invalid
		}

		return Collections.unmodifiableList(ret);
	}

	@Override
	public String toString() {
		if (meta == null) {
			return "RInputOreDict<"+amount+"x"+input+">";
		} else {
			return "RInputOreDict<"+amount+"x"+input+"@"+meta+">";
		}
	}

	private List<ItemStack> getOres() {
		if (ores != null) return ores;

		// cache the ore list by making use of the fact that forge always uses the same list,
		// unless it's EMPTY_LIST, which should never happen.
		List<ItemStack> ret = OreDictionary.getOres(input);

		if (ret != OreDictionary.EMPTY_LIST) ores = ret;

		return ret;
	}

	public final String input;
	public final int amount;
	public final Integer meta;
	private List<ItemStack> ores;
}
