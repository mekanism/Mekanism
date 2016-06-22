package ic2.api.tile;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Allows a block to make use of the wrench's removal and rotation functions.
 */
public interface IWrenchable {
	/**
	 * Get direction the block is facing.
	 *
	 * The direction typically refers to the front/main/functionally dominant side of a block.
	 *
	 * @param world World containing the block.
	 * @param pos The block's current position in the world.
	 * @return Current block facing.
	 */
	EnumFacing getFacing(World world, BlockPos pos);

	/**
	 * Set the block's facing to face towards the specified direction.
	 *
	 * Contrary to Block.rotateBlock the block should always face the requested direction after
	 * successfully processing this method.
	 *
	 * @param world World containing the block.
	 * @param pos The block's current position in the world.
	 * @param newDirection Requested facing, see {@link #getFacing}.
	 * @param player Player causing the action, may be null.
	 * @return true if successful, false otherwise.
	 */
	boolean setFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player);

	/**
	 * Determine if the wrench can be used to remove the block.
	 *
	 * @param world World containing the block.
	 * @param pos The block's current position in the world.
	 * @param player Player causing the action, may be null.
	 * @return true if allowed, false otherwise.
	 */
	boolean wrenchCanRemove(World world, BlockPos pos, EntityPlayer player);

	/**
	 * Determine the items the block will drop when the wrenching is successful.
	 *
	 * The ItemStack will be copied before creating the EntityItem.
	 *
	 * @param world World containing the block.
	 * @param pos The block's current position in the world.
	 * @param state The block's block state before removal.
	 * @param te The block's tile entity before removal, if any, may be null.
	 * @param player Player removing the block, may be null.
	 * @param fortune Fortune level for drop calculation.
	 * @return ItemStacks to drop, may be empty.
	 */
	List<ItemStack> getWrenchDrops(World world, BlockPos pos, IBlockState state, TileEntity te, EntityPlayer player, int fortune);
}

