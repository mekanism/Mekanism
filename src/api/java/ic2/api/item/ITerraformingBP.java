package ic2.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Allows an item to act as a terraformer blueprint.
 */
public interface ITerraformingBP {
	/**
	 * Get the energy consumption per operation of the blueprint.
	 * @param stack TODO
	 *
	 * @return Energy consumption in EU
	 */
	double getConsume(ItemStack stack);

	/**
	 * Get the maximum range of the blueprint.
	 * Should be a divisor of 5.
	 * @param stack TODO
	 *
	 * @return Maximum range in blocks
	 */
	int getRange(ItemStack stack);

	boolean canInsert(ItemStack stack, EntityPlayer player, World world, BlockPos pos);

	/**
	 * Perform the terraforming operation.
	 * @param stack TODO
	 * @param world world to terraform
	 * @param x X position to terraform
	 * @param z Z position to terraform
	 * @param yCoord Y position of the terraformer
	 *
	 * @return Whether the operation was successful and the terraformer should consume energy.
	 */
	boolean terraform(ItemStack stack, World world, BlockPos pos);
}
