package mekanism.common.lib;

import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper of an FluidStack which tests equality and hashes based on fluid type and NBT data, ignoring stack size.
 */
public class HashedFluid {

    public static HashedFluid create(@NotNull FluidStack stack) {
        return new HashedFluid(new FluidStack(stack, 1));
    }

    /**
     * Uses the passed in stack as the raw stack, instead of making a copy of it with a size of one.
     *
     * @apiNote When using this, you should be very careful to not accidentally modify the backing stack, this is mainly for use where we want to use a {@link FluidStack}
     * as a key in a map that is local to a single method, and don't want the overhead of copying the stack when it is not needed.
     */
    public static HashedFluid raw(@NotNull FluidStack stack) {
        return new HashedFluid(stack);
    }

    @NotNull
    private final FluidStack fluidStack;
    private final int hashCode;

    private HashedFluid(@NotNull FluidStack stack) {
        this.fluidStack = stack;
        this.hashCode = initHashCode();
    }

    @NotNull
    public FluidStack getStack() {
        return fluidStack;
    }

    @NotNull
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
        return obj instanceof HashedFluid other && !fluidStack.isEmpty() && fluidStack.isFluidEqual(other.fluidStack);
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