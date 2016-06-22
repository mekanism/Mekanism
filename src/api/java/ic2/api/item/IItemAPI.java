package ic2.api.item;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * A simple interface for the ItemAPI.
 * @author Aroma1997
 *
 */
public interface IItemAPI {

	/**
	 * Get an ItemStack for a specific item name.
	 *
	 * @param name
	 *            item name
	 * @param variant
	 *            the variant/subtype for the Item.
	 * @return The item or null if the item does not exist or an error occurred
	 */
	ItemStack getItemStack(String name, String variant);

	/**
	 * Get a Block for a specific block name.
	 *
	 * @param name
	 *            block name
	 * @return The Block or null if the block does not exist or an error occurred
	 */
	Block getBlock(String name);

	/**
	 * Get an Item for a specific block name.
	 *
	 * @param name
	 *            item name
	 * @return The Item or null if the block does not exist or an error occurred
	 */
	Item getItem(String name);

}
