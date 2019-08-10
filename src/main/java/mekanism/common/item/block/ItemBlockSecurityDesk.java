package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockSecurityDesk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockSecurityDesk extends ItemBlockTooltip<BlockSecurityDesk> {

    public ItemBlockSecurityDesk(BlockSecurityDesk block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (world.isOutsideBuildHeight(pos.up()) || !world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up())) {
            //If there is not enough room, fail
            return false;
        }
        return super.placeBlock(context, state);
    }
}