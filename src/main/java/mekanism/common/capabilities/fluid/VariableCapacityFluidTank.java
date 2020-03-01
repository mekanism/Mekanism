package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
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
}