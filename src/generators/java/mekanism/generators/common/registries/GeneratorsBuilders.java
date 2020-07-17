package mekanism.generators.common.registries;

import mekanism.common.command.builders.StructureBuilder;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GeneratorsBuilders {

    public static class TurbineBuilder extends StructureBuilder {

        public TurbineBuilder() {
            super(17, 18, 17);
        }

        @Override
        protected void build(World world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            buildColumn(world, start, new BlockPos(sizeX / 2, 1, sizeZ / 2), 14, GeneratorsBlocks.TURBINE_ROTOR.getBlock());
            buildInteriorLayer(world, start, 15, MekanismBlocks.PRESSURE_DISPERSER.getBlock());
            world.setBlockState(start.add(sizeX / 2, 15, sizeZ / 2), GeneratorsBlocks.ROTATIONAL_COMPLEX.getBlock().getDefaultState());
            buildInteriorLayer(world, start, 16, GeneratorsBlocks.SATURATING_CONDENSER.getBlock());
            buildPlane(world, start, 5, 5, 13, 13, 16, GeneratorsBlocks.ELECTROMAGNETIC_COIL.getBlock());
        }

        @Override
        protected Block getWallBlock(BlockPos pos) {
            return pos.getY() >= 15 ? GeneratorsBlocks.TURBINE_VENT.getBlock() : super.getWallBlock(pos);
        }

        @Override
        protected Block getRoofBlock(BlockPos pos) {
            return GeneratorsBlocks.TURBINE_VENT.getBlock();
        }

        @Override
        protected Block getCasing() {
            return GeneratorsBlocks.TURBINE_CASING.getBlock();
        }
    }

    public static class FissionReactorBuilder extends StructureBuilder {

        public FissionReactorBuilder() {
            super(18, 18, 18);
        }

        @Override
        protected void build(World world, BlockPos start) {
            buildFrame(world, start);
            buildWalls(world, start);
            for (int x = 1; x < sizeX - 1; x++) {
                for (int z = 1; z < sizeZ - 1; z++) {
                    if (x % 2 != z % 2) {
                        continue;
                    }
                    buildColumn(world, start, new BlockPos(x, 1, z), 15, GeneratorsBlocks.FISSION_FUEL_ASSEMBLY.getBlock());
                    world.setBlockState(start.add(x, sizeY - 2, z), GeneratorsBlocks.CONTROL_ROD_ASSEMBLY.getBlock().getDefaultState());
                }
            }
        }

        @Override
        protected Block getWallBlock(BlockPos pos) {
            return GeneratorsBlocks.REACTOR_GLASS.getBlock();
        }

        @Override
        protected Block getCasing() {
            return GeneratorsBlocks.FISSION_REACTOR_CASING.getBlock();
        }
    }

    public static class FusionReactorBuilder extends StructureBuilder {

        public FusionReactorBuilder() {
            super(5, 5, 5);
        }

        @Override
        protected void build(World world, BlockPos start) {
            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 3; ++y) {
                    for (int z = 0; z < 3; ++z) {
                        if ((x == 0 || x == 2) && (y == 0 || y == 2) && (z == 0 || z == 2)) {
                            continue;
                        } else if ((x == 1 && y == 1) || (y == 1 && z == 1) || (x == 1 && z == 1)) {
                            continue;
                        }
                        world.setBlockState(start.add(sizeX * x / 2.5, sizeY * y / 2.5, sizeZ * z / 2.5), GeneratorsBlocks.FUSION_REACTOR_FRAME.getBlock().getDefaultState());
                    }
                }
            }
            buildWalls(world, start);
            world.setBlockState(start.add(2, 4, 2), GeneratorsBlocks.FUSION_REACTOR_CONTROLLER.getBlock().getDefaultState());
        }

        @Override
        protected Block getWallBlock(BlockPos pos) {
            return GeneratorsBlocks.FUSION_REACTOR_FRAME.getBlock();
        }

        @Override
        protected Block getCasing() {
            return GeneratorsBlocks.FUSION_REACTOR_FRAME.getBlock();
        }
    }
}
