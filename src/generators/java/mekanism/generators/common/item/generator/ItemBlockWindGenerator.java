package mekanism.generators.common.item.generator;

import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.client.render.item.GeneratorsISTERProvider;
import mekanism.generators.common.block.BlockGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockWindGenerator extends ItemBlockGenerator {

    public ItemBlockWindGenerator(BlockGenerator<?> block) {
        super(block, GeneratorsISTERProvider::wind);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (!MekanismUtils.isValidReplaceableBlock(world, pos)) {
            return false;
        }
        for (int yPos = 1; yPos <= 4; yPos++) {
            BlockPos toCheck = pos.up(yPos);
            if (!MekanismUtils.isValidReplaceableBlock(world, toCheck)) {
                //If there is not enough room, fail
                return false;
            }
        }
        return super.placeBlock(context, state);
    }
}