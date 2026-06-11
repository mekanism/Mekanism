package mekanism.additions.common.block;

import mekanism.common.block.states.IFluidLogType;
import mekanism.common.block.states.IStateFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

/// Helper interface for implementation of smashing vanilla's water logging system with our own fluid logging system to allow easier implementation on blocks that extend
/// something that is already water loggable (fences, stairs, slabs)
public interface IStateExtendedFluidLoggable extends IStateFluidLoggable {

    EnumProperty<ExtendedFluidLogType> FLUID_LOGGED = EnumProperty.create("fluid_logged_extension", ExtendedFluidLogType.class);

    @Override
    default boolean isValidFluid(Fluid fluid) {
        return fluid == Fluids.WATER || IStateFluidLoggable.super.isValidFluid(fluid);
    }

    @Override
    default EnumProperty<? extends IFluidLogType> getFluidLoggedProperty() {
        return FLUID_LOGGED;
    }

    @Override
    default FluidState getFluid(BlockState state) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            return Fluids.WATER.defaultFluidState();
        }
        return IStateFluidLoggable.super.getFluid(state);
    }

    @Override
    default boolean canPlaceLiquid(@Nullable LivingEntity owner, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return !state.getValue(BlockStateProperties.WATERLOGGED) && IStateFluidLoggable.super.canPlaceLiquid(owner, world, pos, state, fluid);
    }

    @Override
    default boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        Fluid fluid = fluidState.getType();
        if (canPlaceLiquid(null, world, pos, state, fluid)) {
            if (!world.isClientSide()) {
                if (fluid == Fluids.WATER) {
                    world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_ALL);
                } else {
                    world.setBlock(pos, setState(state, fluid), Block.UPDATE_ALL);
                }
                world.scheduleTick(pos, fluid, fluid.getTickDelay(world));
            }
            return true;
        }
        return false;
    }

    @Override
    default ItemStack pickupBlock(@Nullable LivingEntity owner, LevelAccessor world, BlockPos pos, BlockState state) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_ALL);
            return new ItemStack(Items.WATER_BUCKET);
        }
        return IStateFluidLoggable.super.pickupBlock(owner, world, pos, state);
    }
}