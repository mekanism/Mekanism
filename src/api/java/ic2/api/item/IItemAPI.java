package ic2.api.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * A simple interface for the ItemAPI.
 * @author Aroma1997
 *
 */
public interface IItemAPI {

	/**
	 * Get the <b>default</b> blockstate for a specific block name and variant.<br/>
	 * For machines etc, you might want to make your detection a bit more fuzzy,
	 * because the BlockState also contains properties like the facing and the active
	 * state of a machine.
	 * @param name The name of the block.
	 * @param variant The variant of the block.
	 * @return The default state of the block with that variant.
	 */
	IBlockState getBlockState(String name, String variant);

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
