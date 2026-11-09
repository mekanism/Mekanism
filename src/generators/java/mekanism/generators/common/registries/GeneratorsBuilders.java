package mekanism.generators.common.registries;

import mekanism.common.command.builders.StructureBuilder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorsBuilders {

    private GeneratorsBuilders() {
    }

    public static class TurbineBuilder extends StructureBuilder {

        public TurbineBuilder() {
            super(17, 18, 17);
        }

        public TurbineBuilder(int sizeX, int sizeY) {
            super(sizeX, sizeY, sizeX);
        }

        @Override
        protected void build(Level world, BlockPos start, boolean empty) {
            buildFrame(world, start);
            buildWalls(world, start);
            //Clear out the inside
            buildInteriorLayers(world, start, 1, sizeY - 4, Blocks.AIR.defaultBlockState());
            if (empty) {
                //Clear out the rest
                buildInteriorLayers(world, start, sizeY - 3, sizeY - 2, Blocks.AIR.defaultBlockState());
            } else {
                //Add two blades to each rotor, they will be properly scanned when the multiblock forms at the end
                buildColumn(world, start, new BlockPos(sizeX / 2, 1, sizeZ / 2), sizeY - 4, GeneratorsBlocks.TURBINE_ROTOR.defaultState(),
                      TileEntityTurbineRotor.class, rotor -> rotor.blades = 2);
                buildInteriorLayer(world, start, sizeY - 3, MekanismBlocks.PRESSURE_DISPERSER.defaultState());
                world.setBlockAndUpdate(start.offset(sizeX / 2, sizeY - 3, sizeZ / 2), GeneratorsBlocks.ROTATIONAL_COMPLEX.defaultState());
                buildInteriorLayer(world, start, sizeY - 2, GeneratorsBlocks.SATURATING_CONDENSER.defaultState());
                int coilRadiusX = sizeX / /*size*/2 / /*to radius*/2;
                int coilRadiusZ = sizeZ / /*size*/2 / /*to radius*/2;
                //buildPlane(world, start, 5, 5, 13, 13, sizeY - 2, GeneratorsBlocks.ELECTROMAGNETIC_COIL.defaultState());
                buildPlane(world, start, sizeX / 2 - coilRadiusX + 1, sizeZ / 2 - coilRadiusZ + 1, sizeX / 2 + coilRadiusX + 1, sizeZ / 2 + coilRadiusZ + 1, sizeY - 2, GeneratorsBlocks.ELECTROMAGNETIC_COIL.defaultState());
            }
        }

        @Override
        protected BlockState getWallBlock(BlockPos pos) {
            return pos.getY() >= sizeY - 3 ? GeneratorsBlocks.TURBINE_VENT.defaultState() : super.getWallBlock(pos);
        }

        @Override
        protected BlockState getRoofBlock(BlockPos pos) {
            return GeneratorsBlocks.TURBINE_VENT.defaultState();
        }

        @Override
        protected BlockState getCasing() {
            return GeneratorsBlocks.TURBINE_CASING.defaultState();
        }
    }

    public static class FissionReactorBuilder extends StructureBuilder {

        public FissionReactorBuilder() {
            super(18, 18, 18);
        }

        @Override
        protected void build(Level world, BlockPos start, boolean empty) {
            buildFrame(world, start);
            buildWalls(world, start);
            if (empty) {
                buildInteriorLayers(world, start, 1, 16, Blocks.AIR.defaultBlockState());
            } else {
                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                for (int x = 1; x < sizeX - 1; x++) {
                    for (int z = 1; z < sizeZ - 1; z++) {
                        mutablePos.set(x, 1, z);
                        if (x % 2 == z % 2) {
                            buildColumn(world, start, mutablePos, 15, GeneratorsBlocks.FISSION_FUEL_ASSEMBLY.defaultState());
                            mutablePos.setWithOffset(start, x, sizeY - 2, z);
                            world.setBlockAndUpdate(mutablePos, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY.defaultState());
                        } else {
                            buildColumn(world, start, mutablePos, 16, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }

        @Override
        protected BlockState getWallBlock(BlockPos pos) {
            return GeneratorsBlocks.REACTOR_GLASS.defaultState();
        }

        @Override
        protected BlockState getCasing() {
            return GeneratorsBlocks.FISSION_REACTOR_CASING.defaultState();
        }
    }

    public static class FusionReactorBuilder extends StructureBuilder {

        public FusionReactorBuilder() {
            super(5, 5, 5);
        }

        @Override
        protected void build(Level world, BlockPos start, boolean empty) {
            buildPartialFrame(world, start, 1);
            buildWalls(world, start);
            buildInteriorLayers(world, start, 1, 3, Blocks.AIR.defaultBlockState());
            world.setBlockAndUpdate(start.offset(2, 4, 2), GeneratorsBlocks.FUSION_REACTOR_CONTROLLER.defaultState());
        }

        @Override
        protected BlockState getWallBlock(BlockPos pos) {
            return GeneratorsBlocks.FUSION_REACTOR_FRAME.defaultState();
        }

        @Override
        protected BlockState getCasing() {
            return GeneratorsBlocks.FUSION_REACTOR_FRAME.defaultState();
        }
    }
}
