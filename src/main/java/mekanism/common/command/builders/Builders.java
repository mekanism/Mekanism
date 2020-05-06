package mekanism.common.command.builders;

import mekanism.common.registries.MekanismBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Builders {

    public static class BoilerBuilder extends StructureBuilder {

        public BoilerBuilder() {
            super(18, 18, 18);
        }

        @Override
        public void build(World world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayer(world, start, 1, MekanismBlocks.SUPERHEATING_ELEMENT.getBlock());
            buildInteriorLayer(world, start, 2, MekanismBlocks.SUPERHEATING_ELEMENT.getBlock());
            buildInteriorLayer(world, start, 9, MekanismBlocks.PRESSURE_DISPERSER.getBlock());
        }

        @Override
        protected Block getCasing() {
            return MekanismBlocks.BOILER_CASING.getBlock();
        }
    }
}
