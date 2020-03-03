package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableCapacityFluidTank extends BasicFluidTank {

    public static VariableCapacityFluidTank input(IntSupplier capacity, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new VariableCapacityFluidTank(capacity, manualOnly, alwaysTrueBi, validator, fluidHandler);
    }

    public static VariableCapacityFluidTank output(IntSupplier capacity, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new VariableCapacityFluidTank(capacity, alwaysTrueBi, internalOnly, validator, fluidHandler);
    }

    public static VariableCapacityFluidTank create(IntSupplier capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new VariableCapacityFluidTank(capacity, canExtract, canInsert, validator, fluidHandler);
    }

    private final IntSupplier capacity;

    protected VariableCapacityFluidTank(IntSupplier capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        super(capacity.getAsInt(), canExtract, canInsert, validator, fluidHandler);
        this.capacity = capacity;
    }

    @Override
    public int getCapacity() {
        return capacity.getAsInt();
    }

    @Override
    public int setStackSize(int amount, @Nonnull Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setStack(FluidStack.EMPTY);
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
            //If our size is not changing or we are only simulating the change, don't do anything
            return amount;
        }
        stored.setAmount(amount);
        onContentsChanged();
        return amount;
    }
}