package cofh.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

/**
 * Implement this interface on subclasses of Item to have that item work as a tool for CoFH mods.
 */
public interface IToolHammer {

	/**
	 * Called to ensure that the wrench can be used.
	 * 
	 * @param item
	 *            The itemstack for the tool. Not required to match equipped item (e.g., multi-tools that contain other tools)
	 * @param user
	 *            The entity using the tool
	 * @param x
	 *            X location of the block/tile
	 * @param y
	 *            Y location of the block/tile
	 * @param z
	 *            Z location of the block/tile
	 * @return True if this tool can be used
	 */
	boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos);

	/**
	 * Callback for when the tool has been used reactively.
	 * 
	 * @param item
	 *            The ItemStack for the tool. Not required to match equipped item (e.g., multi-tools that contain other tools)
	 * @param user
	 *            The entity using the tool
	 * @param x
	 *            X location of the block/tile
	 * @param y
	 *            Y location of the block/tile
	 * @param z
	 *            Z location of the block/tile
	 */
	void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos);

}
