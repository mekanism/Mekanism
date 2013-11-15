package cofh.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on Item classes that are themselves inventories.
 * 
 * There's no real interaction here - the point of this is to correctly identify these items and prevent nesting.
 * 
 * @author King Lemming
 * 
 */
public interface IInventoryContainerItem {

	/**
	 * Get the size of this inventory.
	 */
	int getSizeInventory(ItemStack container);

}
