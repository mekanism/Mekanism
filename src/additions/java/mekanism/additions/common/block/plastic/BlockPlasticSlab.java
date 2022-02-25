package mekanism.additions.common.block.plastic;

import java.util.Optional;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.additions.common.block.IStateExtendedFluidLoggable;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class BlockPlasticSlab extends SlabBlock implements IColoredBlock, IStateExtendedFluidLoggable {

    private final EnumColor color;

    public BlockPlasticSlab(EnumColor color, UnaryOperator<Properties> propertyModifier) {
        super(BlockStateHelper.applyLightLevelAdjustments(propertyModifier.apply(BlockBehaviour.Properties.of(BlockPlastic.PLASTIC, color.getMapColor())
              .strength(5, 6))));
        this.color = color;
        //Uses getDefaultState as starting state to take into account the stuff from super
        registerDefaultState(BlockStateHelper.getDefaultState(defaultBlockState()));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public FluidState getFluidState(@Nonnull BlockState state) {
        return getFluid(state);
    }

    @Override
    public boolean placeLiquid(@Nonnull LevelAccessor world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidState) {
        return state.getValue(TYPE) != SlabType.DOUBLE && IStateExtendedFluidLoggable.super.placeLiquid(world, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(@Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return state.getValue(TYPE) != SlabType.DOUBLE && IStateExtendedFluidLoggable.super.canPlaceLiquid(world, pos, state, fluid);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState updateShape(@Nonnull BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor world,
          @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Nonnull
    @Override
    public ItemStack pickupBlock(@Nonnull LevelAccessor world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        //Manually declare which pickupBlock we want to be using
        return IStateExtendedFluidLoggable.super.pickupBlock(world, pos, state);
    }

    @Nonnull
    @Override
    public Optional<SoundEvent> getPickupSound() {
        //Manually declare which getPickupSound we want to be using
        return IStateExtendedFluidLoggable.super.getPickupSound();
    }

    @Nonnull
    @Override
    public Optional<SoundEvent> getPickupSound(BlockState state) {
        //Manually declare which getPickupSound we want to be using
        return IStateExtendedFluidLoggable.super.getPickupSound(state);
    }
}