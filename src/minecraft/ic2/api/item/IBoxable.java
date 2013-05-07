package ic2.api.item;

import net.minecraft.item.ItemStack;

/**
 * Provides custom toolbox storage behavior for items.
 * 
 * The normal condition for storing an item in a toolbox is having a maximum stack size of 1.
 */
public interface IBoxable {
	/**
	 * Determine whether an item can be stored in a toolbox or not.
	 * 
	 * @param itemstack item to be stored
	 * @return Whether to store the item in the toolbox or not
	 */
	public abstract boolean canBeStoredInToolbox(ItemStack itemstack);
}
