package cofh.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on subclasses of Item to change how the item works in Thermal Dynamics Itemducts filter slots.
 *
 * This can be used to create customizable Items which are determined to be "equal" for the purposes of filtering.
 */
public interface ISpecialFilterItem {

	/**
	 * This method is called to find out if the given ItemStack should be matched by the given Filter ItemStack.
	 *
	 * @param filter
	 *            ItemStack representing the filter.
	 * @param item
	 *            ItemStack representing the queried item.
	 * @return True if the filter should match. False if the default matching should be used.
	 */
	public boolean matchesItem(ItemStack filter, ItemStack item);

}
