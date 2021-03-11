package mekanism.additions.common.block.plastic;

import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.additions.common.block.IStateExtendedFluidLoggable;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ToolType;

public class BlockPlasticStairs extends StairsBlock implements IColoredBlock, IStateExtendedFluidLoggable {

    private final EnumColor color;

    public BlockPlasticStairs(IBlockProvider blockProvider, EnumColor color, UnaryOperator<Properties> propertyModifier) {
        super(() -> blockProvider.getBlock().defaultBlockState(), BlockStateHelper.applyLightLevelAdjustments(propertyModifier.apply(AbstractBlock.Properties
              .of(BlockPlastic.PLASTIC, color.getMapColor()).strength(5, 6).harvestTool(ToolType.PICKAXE))));
        this.color = color;
        this.registerDefaultState(defaultBlockState().setValue(getFluidLoggedProperty(), 0));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public FluidState getFluidState(@Nonnull BlockState state) {
        return getFluid(state);
    }

    @Override
    public boolean placeLiquid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidState) {
        return IStateExtendedFluidLoggable.super.placeLiquid(world, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return IStateExtendedFluidLoggable.super.canPlaceLiquid(world, pos, state, fluid);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState updateShape(@Nonnull BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld world,
          @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Nonnull
    @Override
    public Fluid takeLiquid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        //Manually declare which pickupFluidMethod we want to be using
        return IStateExtendedFluidLoggable.super.takeLiquid(world, pos, state);
    }
}