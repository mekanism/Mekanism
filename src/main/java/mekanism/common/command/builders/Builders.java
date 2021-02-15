package mekanism.common.command.builders;

import mekanism.common.registries.MekanismBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Builders {

    private Builders() {
    }

    public static class BoilerBuilder extends StructureBuilder {

        public BoilerBuilder() {
            super(18, 18, 18);
        }

        @Override
        public void build(World world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayer(world, start, 1, MekanismBlocks.SUPERHEATING_ELEMENT.getBlock());
            buildInteriorLayers(world, start, 2, 14, Blocks.AIR);
            buildInteriorLayer(world, start, 15, MekanismBlocks.PRESSURE_DISPERSER.getBlock());
            buildInteriorLayer(world, start, 16, Blocks.AIR);
        }

        @Override
        protected Block getCasing() {
            return MekanismBlocks.BOILER_CASING.getBlock();
        }
    }

    public static class TankBuilder extends StructureBuilder {

        public TankBuilder() {
            super(18, 18, 18);
        }

        @Override
        public void build(World world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 16, Blocks.AIR);
        }

        @Override
        protected Block getCasing() {
            return MekanismBlocks.DYNAMIC_TANK.getBlock();
        }
    }

    public static class MatrixBuilder extends StructureBuilder {

        public MatrixBuilder() {
            super(18, 18, 18);
        }

        @Override
        public void build(World world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 15, MekanismBlocks.ULTIMATE_INDUCTION_CELL.getBlock());
            buildInteriorLayer(world, start, 16, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER.getBlock());
        }

        @Override
        protected Block getCasing() {
            return MekanismBlocks.INDUCTION_CASING.getBlock();
        }
    }

    public static class EvaporationBuilder extends StructureBuilder {

        public EvaporationBuilder() {
            super(4, 18, 4);
        }

        @Override
        public void build(World world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 17, Blocks.AIR);
            world.setBlockState(start.add(1, 1, 0), MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getBlock().getDefaultState());
        }

        @Override
        protected Block getCasing() {
            return MekanismBlocks.THERMAL_EVAPORATION_BLOCK.getBlock();
        }
    }

    public static class SPSBuilder extends StructureBuilder {

        public SPSBuilder() {
            super(7, 7, 7);
        }

        @Override
        protected void build(World world, BlockPos start) {
            buildPartialFrame(world, start, 1);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 5, Blocks.AIR);
            for (int x = -2; x < 2; ++x) {
                for (int y = -2; y < 2; ++y) {
                    for (int z = -2; z < 2; ++z) {
                        // Check whether one or all three vars ar 0 or -1.
                        // Something that checks whether its exactly one would be better, but that seems very hard.
                        if ((x == -1) == (y == -1) == (z == -1) == (x == 0) == (y == 0) != (z == 0)) {
                            // Check that not all three vars are 0 or -1.
                            if (!(x == -1 || x == 0) || !(y == -1 || y == 0) || !(z == -1 || z == 0)) {
                                world.setBlockState(start.add(x < 0 ? sizeX + x : x, y < 0 ? sizeY + y : y,
                                      z < 0 ? sizeZ + z : z), getCasing().getDefaultState());
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected Block getCasing() {
            return MekanismBlocks.SPS_CASING.getBlock();
        }
    }
}
