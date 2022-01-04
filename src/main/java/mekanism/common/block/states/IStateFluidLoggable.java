package mekanism.common.block.states;

import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public interface IStateFluidLoggable extends BucketPickup, LiquidBlockContainer {

    default boolean isValidFluid(@Nonnull Fluid fluid) {
        return getFluidLoggedProperty().getPossibleValues().stream().anyMatch(possibleValue -> possibleValue.getFluid() == fluid);
    }

    /**
     * Gets the fluids this fluid loggable block supports. Overriding this is an easy way to change the block from supporting water and lava logging to supporting
     * specific different types of fluid, but dynamic fluid stuff cannot be done without a sizeable patch to forge/a change in vanilla so that {@link
     * BlockState#getFluidState()} has position information.
     *
     * @return BlockState property for representing fluid loggable blocks
     */
    @Nonnull
    default EnumProperty<? extends IFluidLogType> getFluidLoggedProperty() {
        //TODO - 1.18: When removing CorrectingIntegerProperty, evaluate changing this entire thing to being an EnumProperty
        // so that F3 can show slightly better debug of what it is fluid logged with
        return BlockStateHelper.FLUID_LOGGED;
    }

    @Nonnull
    default FluidState getFluid(@Nonnull BlockState state) {
        IFluidLogType fluidLogged = state.getValue(getFluidLoggedProperty());
        if (!fluidLogged.isEmpty()) {
            Fluid fluid = fluidLogged.getFluid();
            if (fluid instanceof FlowingFluid) {
                return ((FlowingFluid) fluid).getSource(false);
            }
            return fluid.defaultFluidState();
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    default void updateFluids(@Nonnull BlockState state, @Nonnull LevelAccessor world, @Nonnull BlockPos currentPos) {
        IFluidLogType fluidLogged = state.getValue(getFluidLoggedProperty());
        if (!fluidLogged.isEmpty()) {
            Fluid fluid = fluidLogged.getFluid();
            world.scheduleTick(currentPos, fluid, fluid.getTickDelay(world));
        }
    }

    @Override
    default boolean canPlaceLiquid(@Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return state.getValue(getFluidLoggedProperty()).isEmpty() && isValidFluid(fluid);
    }

    default BlockState setState(BlockState state, Fluid fluid) {
        return setState(state, fluid, getFluidLoggedProperty());
    }

    private static <T extends Enum<T> & StringRepresentable & IFluidLogType> BlockState setState(BlockState state, Fluid fluid, EnumProperty<T> property) {
        for (T possibleValue : property.getPossibleValues()) {
            if (possibleValue.getFluid() == fluid) {
                return state.setValue(property, possibleValue);
            }
        }
        return state;
    }

    /**
     * Overwritten to check against canContainFluid instead of inlining the check to water directly.
     */
    @Override
    default boolean placeLiquid(@Nonnull LevelAccessor world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidState) {
        Fluid fluid = fluidState.getType();
        if (canPlaceLiquid(world, pos, state, fluid)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, setState(state, fluid), Block.UPDATE_ALL);
                world.scheduleTick(pos, fluid, fluid.getTickDelay(world));
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    default ItemStack pickupBlock(@Nonnull LevelAccessor world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        //TODO - 1.18: Re-evaluate this
        IFluidLogType fluidLogged = state.getValue(getFluidLoggedProperty());
        if (!fluidLogged.isEmpty()) {
            world.setBlock(pos, setState(state, Fluids.EMPTY), Block.UPDATE_ALL);
            Fluid fluid = fluidLogged.getFluid();
            return fluid.getAttributes().getBucket(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME));
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    default Optional<SoundEvent> getPickupSound() {
        //TODO - 1.18: Implement?
        return Optional.empty();
    }
}