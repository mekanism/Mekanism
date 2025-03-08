package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.lib.multiblock.MultiblockData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class VariableCapacityFluidTank extends BasicFluidTank {

    public static VariableCapacityFluidTank create(MultiblockData multiblock, IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new VariableCapacityFluidTank(capacity, multiblock.formedBiPred(), multiblock.formedBiPred(), validator, listener);
    }

    public static VariableCapacityFluidTank input(MultiblockData multiblock, IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        return create(capacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(), validator, listener);
    }

    public static VariableCapacityFluidTank output(MultiblockData multiblock, IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        return create(capacity, multiblock.formedBiPred(), multiblock.notExternalFormedBiPred(), validator, listener);
    }

    public static VariableCapacityFluidTank input(IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new VariableCapacityFluidTank(capacity, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), validator, listener);
    }

    public static VariableCapacityFluidTank output(IntSupplier capacity, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new VariableCapacityFluidTank(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), validator, listener);
    }

    public static VariableCapacityFluidTank create(IntSupplier capacity, BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canInsert, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new VariableCapacityFluidTank(capacity, canExtract, canInsert, validator, listener);
    }

    private final IntSupplier capacity;

    protected VariableCapacityFluidTank(IntSupplier capacity, BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canInsert, Predicate<@NotNull FluidStack> validator, @Nullable IContentsListener listener) {
        super(capacity.getAsInt(), canExtract, canInsert, validator, listener);
        this.capacity = capacity;
    }

    @Override
    public int getCapacity() {
        return capacity.getAsInt();
    }

    @Override
    public int setStackSize(int amount, @NotNull Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        int maxStackSize = getCapacity();
        //Our capacity should never actually be zero, and given we fake it being zero
        // until we finish building the network, we need to override this method to bypass the upper limit check
        // when our upper limit is zero
        if (maxStackSize > 0 && amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (getFluidAmount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        stored.setAmount(amount);
        onContentsChanged();
        return amount;
    }
}