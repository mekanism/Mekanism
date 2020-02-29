package mekanism.common.block.machine;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockMetallurgicInfuser extends BlockMachine<TileEntityMetallurgicInfuser> implements IHasModel, IStateFluidLoggable {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape infuser = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16),//base
              makeCuboidShape(0, 4, 15, 16, 16, 16),//back
              makeCuboidShape(0, 15, 8, 16, 16, 15),//top
              makeCuboidShape(1.5, 7, 1.5, 14.5, 8, 15.5),//divider
              makeCuboidShape(0, 4, 8, 1, 15, 15),//sideRight
              makeCuboidShape(15, 4, 8, 16, 15, 15),//sideLeft
              makeCuboidShape(13.5, 11, 1.5, 14.5, 12, 2.5),//bar1
              makeCuboidShape(1.5, 11, 1.5, 2.5, 12, 2.5),//bar2
              makeCuboidShape(11, 10.5, 5, 12, 15.5, 8),//connector1
              makeCuboidShape(4, 10.5, 5, 5, 15.5, 8),//connector2
              makeCuboidShape(10.5, 10.5, 13, 12.5, 11.5, 15),//tapBase1
              makeCuboidShape(3.5, 10.5, 13, 5.5, 11.5, 15),//tapBase2
              makeCuboidShape(10.5, 11.5, 4, 12.5, 12.5, 15),//tap1
              makeCuboidShape(3.5, 11.5, 4, 5.5, 12.5, 15),//tap2
              makeCuboidShape(1, 12, 1, 15, 15, 15),//plate1
              makeCuboidShape(1, 8, 1, 15, 11, 15),//plate2
              makeCuboidShape(1, 4, 1, 15, 7, 15)//plate3
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(infuser, side);
        }
    }

    public BlockMetallurgicInfuser() {
        super(MekanismMachines.METALLURGIC_INFUSER);
    }
    
    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
    }
}