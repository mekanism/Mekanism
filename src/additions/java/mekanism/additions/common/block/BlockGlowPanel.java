package mekanism.additions.common.block;

import javax.annotation.Nonnull;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockGlowPanel extends BlockMekanism implements IStateFacing, IColoredBlock, IStateFluidLoggable {

    private static VoxelShape[] bounds = new VoxelShape[EnumUtils.DIRECTIONS.length];

    static {
        VoxelShape glowPanel = VoxelShapeUtils.combine(
              makeCuboidShape(4, 14, 4, 12, 16, 12),
              makeCuboidShape(5, 13.5, 5, 11, 14, 11)
        );
        for (Direction side : EnumUtils.DIRECTIONS) {
            bounds[side.ordinal()] = VoxelShapeUtils.rotate(glowPanel, side);
        }
    }

    private final EnumColor color;

    public BlockGlowPanel(EnumColor color) {
        super(Block.Properties.create(Material.PISTON, color.getMapColor()).hardnessAndResistance(1F, 10F).lightValue(15));
        this.color = color;
    }

    @Nonnull
    @Override
    public DirectionProperty getFacingProperty() {
        return BlockStateHelper.facingProperty;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            Direction side = getDirection(state);
            BlockPos adj = pos.offset(side);
            if (!Block.hasSolidSide(world.getBlockState(adj), world, adj, side.getOpposite())) {
                Block.spawnDrops(world.getBlockState(pos), world, pos, null);
                world.removeBlock(pos, isMoving);
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal()];
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        Direction side = getDirection(state);
        BlockPos positionOn = pos.offset(side.getOpposite());
        //TODO: Maybe improve this so it matches the shape of the glow panel for what it checks
        // This commented out thing is more or less how the torch checks it
        //return !VoxelShapes.compare(state.getCollisionShape(world, pos).project(side), field_220084_c, IBooleanFunction.ONLY_SECOND);
        return Block.hasSolidSide(world.getBlockState(positionOn), world, positionOn, side);
    }
}