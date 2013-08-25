package ic2.api.recipe;

import net.minecraft.item.ItemStack;

public interface IPatternStorage {

	boolean transferPattern(ItemStack itemstack, short amountUU , int amountEU);

}
