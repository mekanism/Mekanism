package mekanism.common.block.machine;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockPressurizedReactionChamber extends BlockMachine<TileEntityPressurizedReactionChamber> implements IHasModel, IStateFluidLoggable {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape prc = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16),//base
              makeCuboidShape(1, 4, 1, 10, 15, 6),//front
              makeCuboidShape(0, 4, 6, 16, 16, 16),//body
              makeCuboidShape(13, 3.5, 0.5, 15, 15.5, 6.5),//frontDivider1
              makeCuboidShape(10, 3.5, 0.5, 12, 15.5, 6.5),//frontDivider2
              makeCuboidShape(12, 5, 1, 13, 6, 6),//bar1
              makeCuboidShape(12, 7, 1, 13, 8, 6),//bar2
              makeCuboidShape(12, 9, 1, 13, 10, 6),//bar3
              makeCuboidShape(12, 11, 1, 13, 12, 6),//bar4
              makeCuboidShape(12, 13, 1, 13, 14, 6)//bar5
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(prc, side);
        }
    }

    public BlockPressurizedReactionChamber() {
        super(MekanismMachines.PRESSURIZED_REACTION_CHAMBER);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
    }
}