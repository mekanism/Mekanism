package mekanism.common.lib;

import javax.annotation.Nonnull;
import net.minecraftforge.fluids.FluidStack;

/**
 * A wrapper of an FluidStack which tests equality and hashes based on fluid type and NBT data, ignoring stack size.
 */
public class HashedFluid {

    public static HashedFluid create(@Nonnull FluidStack stack) {
        return new HashedFluid(new FluidStack(stack, 1));
    }

    /**
     * Uses the passed in stack as the raw stack, instead of making a copy of it with a size of one.
     *
     * @apiNote When using this, you should be very careful to not accidentally modify the backing stack, this is mainly for use where we want to use a {@link FluidStack}
     * as a key in a map that is local to a single method, and don't want the overhead of copying the stack when it is not needed.
     */
    public static HashedFluid raw(@Nonnull FluidStack stack) {
        return new HashedFluid(stack);
    }

    @Nonnull
    private final FluidStack fluidStack;
    private final int hashCode;

    private HashedFluid(@Nonnull FluidStack stack) {
        this.fluidStack = stack;
        this.hashCode = initHashCode();
    }

    @Nonnull
    public FluidStack getStack() {
        return fluidStack;
    }

    @Nonnull
    public FluidStack createStack(int size) {
        if (size <= 0 || fluidStack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluidStack, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof HashedFluid) {
            HashedFluid other = (HashedFluid) obj;
            return !fluidStack.isEmpty() && fluidStack.isFluidEqual(other.fluidStack);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int initHashCode() {
        int code = 1;
        code = 31 * code + fluidStack.getFluid().hashCode();
        if (fluidStack.hasTag()) {
            code = 31 * code + fluidStack.getTag().hashCode();
        }
        return code;
    }
}