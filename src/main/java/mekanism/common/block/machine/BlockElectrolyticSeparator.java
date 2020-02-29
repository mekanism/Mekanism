package mekanism.common.block.machine;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockElectrolyticSeparator extends BlockMachine<TileEntityElectrolyticSeparator> implements IHasModel, IStateFluidLoggable {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape separator = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16),//base
              makeCuboidShape(15, 3, 3, 16, 13, 13),//portToggle1
              makeCuboidShape(0, 4, 4, 1, 12, 12),//portToggle2a
              makeCuboidShape(4, 4, 0, 12, 12, 1),//portToggle3a
              makeCuboidShape(4, 4, 15, 12, 12, 16),//portToggle4a
              makeCuboidShape(1, 4, 7, 3, 11, 9),//portToggle2b
              makeCuboidShape(7, 4, 1, 8, 11, 3),//portToggle3b
              makeCuboidShape(7, 4, 13, 8, 11, 15),//portToggle4b
              makeCuboidShape(8, 4, 0, 16, 16, 16),//tank1
              makeCuboidShape(0, 4, 9, 7, 14, 16),//tank2
              makeCuboidShape(0, 4, 0, 7, 14, 7),//tank3
              makeCuboidShape(6.5, 10, 7.5, 9.5, 11, 8.5),//tube1
              makeCuboidShape(3, 12, 7.5, 7, 13, 8.5),//tube2
              makeCuboidShape(3, 12, 7.5, 4, 15, 8.5),//tube3
              makeCuboidShape(3, 15, 3, 4, 16, 13),//tube4
              makeCuboidShape(3, 14, 3, 4, 15, 4),//tube5
              makeCuboidShape(3, 14, 12, 4, 15, 13)//tube6
        );
        separator = VoxelShapeUtils.rotate(separator, Rotation.CLOCKWISE_90);
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(separator, side);
        }
    }

    public BlockElectrolyticSeparator() {
        super(MekanismMachines.ELECTROLYTIC_SEPARATOR);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
    }
}