package mekanism.api.fluid;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

@NothingNullByDefault
public interface IFluidTank extends IResourceContainer<FluidResource> {

    /**
     * Overrides the stack in this {@link IFluidTank}.
     *
     * @param stack {@link FluidStack} to set this tanks' contents to (may be empty).
     *
     * @throws RuntimeException if this tank is called in a way that it was not expecting.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default void setStack(FluidStack stack) {//TODO - 26.1: Re-evaluate callers
        setContents(FluidResource.of(stack), stack.amount());
    }

    /**
     * Overrides the stack in this {@link IFluidTank}.
     *
     * @param stack {@link FluidStack} to set this tank's contents to (may be empty).
     *
     * @apiNote Unsafe version of {@link #setStack(FluidStack)}. This method is exposed for implementation and code deduplication reasons only and should
     * <strong>NOT</strong> be directly called outside your own {@link IFluidTank} where you already know the given {@link FluidStack} is valid, or on the
     * client side for purposes of receiving sync data and rendering.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default void setStackUnchecked(FluidStack stack) {//TODO - 26.1: Re-evaluate callers
        setContentsUnchecked(FluidResource.of(stack), stack.amount());
    }

    @Override
    default void setEmpty() {
        setContents(FluidResource.EMPTY, 0);
    }

    /**
     * Convenience method for checking if this tank's contents are of an equal type to a given fluid stack's.
     *
     * @param other The stack to compare to.
     *
     * @return True if the tank's contents are equal, false otherwise.
     *
     * @implNote If your implementation of {@link #getFluid()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    default boolean isFluidEqual(FluidStack other) {
        return getResource().matches(other);
    }

    @Override
    default void serialize(ValueOutput output) {
        if (!isEmpty()) {
            output.store(SerializationConstants.STORED, FluidStack.CODEC, getFluid());
        }
    }

    @Override
    default void deserialize(ValueInput input) {
        setStackUnchecked(input.read(SerializationConstants.STORED, FluidStack.CODEC).orElse(FluidStack.EMPTY));
    }

    @Deprecated(forRemoval = true)//TODO - 26.1: From IFluidTank
    default FluidStack getFluid() {
        return getResource().toStack(amount());
    }
}