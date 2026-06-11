package mekanism.additions.common.block.plastic;

import java.util.Optional;
import mekanism.additions.common.block.IStateExtendedFluidLoggable;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class BlockPlasticFence extends FenceBlock implements IColoredBlock, IStateExtendedFluidLoggable {

    private final EnumColor color;

    public BlockPlasticFence(BlockBehaviour.Properties properties, EnumColor color) {
        super(BlockStateHelper.applyLightLevelAdjustments(properties.mapColor(color.getMapColor()).strength(5, 6)));
        this.color = color;
        //Uses getDefaultState as starting state to take into account the stuff from super
        registerDefaultState(BlockStateHelper.getDefaultState(defaultBlockState()));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacementNN(super.getStateForPlacement(context), context);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return getFluid(state);
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return IStateExtendedFluidLoggable.super.placeLiquid(world, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity owner, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return IStateExtendedFluidLoggable.super.canPlaceLiquid(owner, world, pos, state, fluid);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos,
          Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        updateFluids(level, currentPos, state, scheduledTickAccess);
        return super.updateShape(state, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity owner, LevelAccessor world, BlockPos pos, BlockState state) {
        //Manually declare which pickupBlock we want to be using
        return IStateExtendedFluidLoggable.super.pickupBlock(owner, world, pos, state);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        //Manually declare which getPickupSound we want to be using
        return IStateExtendedFluidLoggable.super.getPickupSound();
    }

    @Override
    public Optional<SoundEvent> getPickupSound(BlockState state) {
        //Manually declare which getPickupSound we want to be using
        return IStateExtendedFluidLoggable.super.getPickupSound(state);
    }
}