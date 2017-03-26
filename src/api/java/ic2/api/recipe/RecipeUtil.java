package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

class RecipeUtil {
	public static ItemStack setImmutableSize(ItemStack stack, int size) {
		if (stack.getCount() != size) {
			stack = stack.copy();
			stack.setCount(size);
		}

		return stack;
	}

	/**
	 * Checks whether the first Compound has all tags, the second one has as well. Used for ItemStack matching.
	 * @param subject The NBT to check.
	 * @param target The NBT to match.
	 * @return whether the first NBT has all tags equal to the one of the second.
	 */
	public static boolean matchesNBT(NBTTagCompound subject, NBTTagCompound target) {
		if (subject == null) return target == null || target.hasNoTags();
		if (target == null) return true;
		for (String key : target.getKeySet()) {
			NBTBase targetNBT = target.getTag(key);
			if (!subject.hasKey(key) || targetNBT.getId() != subject.getTagId(key)) return false;
			NBTBase subjectNBT = subject.getTag(key);
			if (!targetNBT.equals(subjectNBT)) return false;
		}
		return true;
	}
}
