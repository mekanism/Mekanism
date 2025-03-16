package mekanism.common.command.builders;

import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Builders {

    private Builders() {
    }

    public static class BoilerBuilder extends StructureBuilder {

        public BoilerBuilder() {
            super(18, 18, 18);
        }

        @Override
        public void build(Level world, BlockPos start, boolean empty) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 2, 14, Blocks.AIR.defaultBlockState());
            buildInteriorLayer(world, start, 16, Blocks.AIR.defaultBlockState());
            if (empty) {
                buildInteriorLayer(world, start, 1, Blocks.AIR.defaultBlockState());
                buildInteriorLayer(world, start, 15, Blocks.AIR.defaultBlockState());
            } else {
                buildInteriorLayer(world, start, 1, MekanismBlocks.SUPERHEATING_ELEMENT.defaultState());
                buildInteriorLayer(world, start, 15, MekanismBlocks.PRESSURE_DISPERSER.defaultState());
            }
        }

        @Override
        protected BlockState getCasing() {
            return MekanismBlocks.BOILER_CASING.defaultState();
        }
    }

    public static class TankBuilder extends StructureBuilder {

        public TankBuilder() {
            super(18, 18, 18);
        }

        @Override
        public void build(Level world, BlockPos start, boolean empty) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 16, Blocks.AIR.defaultBlockState());
        }

        @Override
        protected BlockState getCasing() {
            return MekanismBlocks.DYNAMIC_TANK.defaultState();
        }
    }

    public static class MatrixBuilder extends StructureBuilder {

        public MatrixBuilder() {
            super(18, 18, 18);
        }

        @Override
        public void build(Level world, BlockPos start, boolean empty) {
            buildFrame(world, start);
            buildWalls(world, start);
            if (empty) {
                buildInteriorLayers(world, start, 1, 16, Blocks.AIR.defaultBlockState());
            } else {
                buildInteriorLayers(world, start, 1, 15, MekanismBlocks.ULTIMATE_INDUCTION_CELL.defaultState());
                buildInteriorLayer(world, start, 16, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER.defaultState());
            }
        }

        @Override
        protected BlockState getCasing() {
            return MekanismBlocks.INDUCTION_CASING.defaultState();
        }
    }

    public static class EvaporationBuilder extends StructureBuilder {

        public EvaporationBuilder() {
            super(4, 18, 4);
        }

        @Override
        public void build(Level world, BlockPos start, boolean empty) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 17, Blocks.AIR.defaultBlockState());
            world.setBlockAndUpdate(start.offset(1, 1, 0), MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.defaultState());
        }

        @Override
        protected BlockState getCasing() {
            return MekanismBlocks.THERMAL_EVAPORATION_BLOCK.defaultState();
        }
    }

    public static class SPSBuilder extends StructureBuilder {

        public SPSBuilder() {
            super(7, 7, 7);
        }

        @Override
        protected void build(Level world, BlockPos start, boolean empty) {
            buildPartialFrame(world, start, 1);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 5, Blocks.AIR.defaultBlockState());
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int x = -2; x < 2; ++x) {
                for (int y = -2; y < 2; ++y) {
                    for (int z = -2; z < 2; ++z) {
                        // Check whether one or all three vars ar 0 or -1.
                        // Something that checks whether its exactly one would be better, but that seems very hard.
                        if ((x == -1) == (y == -1) == (z == -1) == (x == 0) == (y == 0) != (z == 0)) {
                            // Check that not all three vars are 0 or -1.
                            if (!(x == -1 || x == 0) || !(y == -1 || y == 0) || !(z == -1 || z == 0)) {
                                mutablePos.setWithOffset(start, x < 0 ? sizeX + x : x, y < 0 ? sizeY + y : y, z < 0 ? sizeZ + z : z);
                                world.setBlockAndUpdate(mutablePos, getCasing());
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected BlockState getCasing() {
            return MekanismBlocks.SPS_CASING.defaultState();
        }
    }
}
