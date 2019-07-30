package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockSecurityDesk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockSecurityDesk extends ItemBlockTooltip {

    public ItemBlockSecurityDesk(BlockSecurityDesk block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        if (world.isOutsideBuildHeight(pos.up()) || !world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up())) {
            //If there is not enough room, fail
            return false;
        }
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);
    }
}