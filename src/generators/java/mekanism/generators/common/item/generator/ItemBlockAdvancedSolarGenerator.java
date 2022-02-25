package mekanism.generators.common.item.generator;

import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemBlockAdvancedSolarGenerator extends ItemBlockMachine {

    public ItemBlockAdvancedSolarGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockPlaceContext context, @Nonnull BlockState state) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!WorldUtils.isValidReplaceableBlock(world, pos.above())) {
            return false;
        }
        for (int xPos = -1; xPos <= 1; xPos++) {
            for (int zPos = -1; zPos <= 1; zPos++) {
                BlockPos toCheck = pos.offset(xPos, 2, zPos);
                if (!WorldUtils.isValidReplaceableBlock(world, toCheck)) {
                    //If there is not enough room, fail
                    return false;
                }
            }
        }
        return super.placeBlock(context, state);
    }
}