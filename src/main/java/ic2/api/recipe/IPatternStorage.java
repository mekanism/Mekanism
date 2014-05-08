package ic2.api.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IPatternStorage {
	boolean addPattern(ItemStack itemstack);

	List<ItemStack> getPatterns();
}
