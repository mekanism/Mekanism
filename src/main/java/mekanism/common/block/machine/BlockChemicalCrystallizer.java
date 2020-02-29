package mekanism.common.block.machine;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockChemicalCrystallizer extends BlockMachine<TileEntityChemicalCrystallizer> implements IHasModel, IStateFluidLoggable {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape crystallizer = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 5, 16),//base
              makeCuboidShape(0, 11, 0, 16, 16, 16),//tank
              makeCuboidShape(3, 4.5, 3, 13, 5.5, 13),//tray
              makeCuboidShape(1, 7, 1, 15, 11, 15),//Shape1
              makeCuboidShape(0, 3, 3, 1, 13, 13),//portRight
              makeCuboidShape(15, 4, 4, 16, 12, 12),//portLeft
              makeCuboidShape(0, 5, 0, 16, 7, 2),//rimBack
              makeCuboidShape(0, 5, 2, 2, 7, 14),//rimRight
              makeCuboidShape(14, 5, 2, 16, 7, 14),//rimLeft
              makeCuboidShape(0, 5, 14, 16, 7, 16),//rimFront
              makeCuboidShape(14.5, 6, 14.5, 15.5, 11, 15.5),//support1
              makeCuboidShape(0.5, 6, 14.5, 1.5, 11, 15.5),//support2
              makeCuboidShape(14.5, 6, 0.5, 15.5, 11, 1.5),//support3
              makeCuboidShape(0.5, 6, 0.5, 1.5, 11, 1.5)//support4
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(crystallizer, side);
        }
    }

    public BlockChemicalCrystallizer() {
        super(MekanismMachines.CHEMICAL_CRYSTALLIZER);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
    }
}