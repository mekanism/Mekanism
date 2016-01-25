package buildcraft.api.crops;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ICropHandler {

    /** Check if an item is a seed.
     *
     * @param stack
     * @return true if the item can be planted. */
    boolean isSeed(ItemStack stack);

    /** Check if the item can be planted. You can assume canSustainPlant() will only be called if isSeed() returned
     * true.
     *
     * @param world
     * @param seed
     * @param pos
     * @return true if the item can be planted at pos. */
    boolean canSustainPlant(World world, ItemStack seed, BlockPos pos);

    /** Plant the item in the block. You can assume plantCrop() will only be called if canSustainPlant() returned true.
     *
     * @param world
     * @param player
     * @param seed
     * @param pos
     * @return true if the item was planted at pos. */
    boolean plantCrop(World world, EntityPlayer player, ItemStack seed, BlockPos pos);

    /** Check if a crop is mature and can be harvested.
     *
     * @param blockAccess
     * @param state
     * @param pos
     * @return true if the block at pos is mature and can be harvested. */
    boolean isMature(IBlockAccess blockAccess, IBlockState state, BlockPos pos);

    /** Harvest the crop. You can assume harvestCrop() will only be called if isMature() returned true.
     *
     * @param world
     * @param pos
     * @param drops a list to return the harvest's drops.
     * @return true if the block was successfully harvested. */
    boolean harvestCrop(World world, BlockPos pos, List<ItemStack> drops);

}
