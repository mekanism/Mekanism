package mekanism.generators.common.item.generator;

import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockAdvancedSolarGenerator extends ItemBlockMachine {

    public ItemBlockAdvancedSolarGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (!MekanismUtils.isValidReplaceableBlock(world, pos.up())) {
            return false;
        }
        for (int xPos = -1; xPos <= 1; xPos++) {
            for (int zPos = -1; zPos <= 1; zPos++) {
                BlockPos toCheck = pos.add(xPos, 2, zPos);
                if (!MekanismUtils.isValidReplaceableBlock(world, toCheck)) {
                    //If there is not enough room, fail
                    return false;
                }
            }
        }
        return super.placeBlock(context, state);
    }
}