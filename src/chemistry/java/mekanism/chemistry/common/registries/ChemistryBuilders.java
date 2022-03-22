package mekanism.chemistry.common.registries;

import mekanism.common.command.builders.StructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ChemistryBuilders {

    private ChemistryBuilders() {
    }

    public static class DistillerBuilder extends StructureBuilder {
        public DistillerBuilder() {
            super(3, 8, 3);
        }

        @Override
        public void build(Level world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 6, Blocks.AIR);
            world.setBlockAndUpdate(start.offset(1, 1, 0), ChemistryBlocks.FRACTIONATING_DISTILLER_CONTROLLER.getBlock().defaultBlockState());
        }

        @Override
        protected Block getCasing() {
            return ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK.getBlock();
        }
    }
}
