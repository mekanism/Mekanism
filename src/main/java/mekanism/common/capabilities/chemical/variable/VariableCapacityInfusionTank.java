package mekanism.common.capabilities.chemical.variable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.AutomationType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableCapacityInfusionTank extends VariableCapacityChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {

    public static VariableCapacityInfusionTank create(LongSupplier capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert, Predicate<@NonNull InfuseType> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new VariableCapacityInfusionTank(capacity, canExtract, canInsert, validator, listener);
    }

    public static VariableCapacityInfusionTank output(LongSupplier capacity, Predicate<@NonNull InfuseType> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new VariableCapacityInfusionTank(capacity, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.internalOnly, validator, listener);
    }

    protected VariableCapacityInfusionTank(LongSupplier capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert, Predicate<@NonNull InfuseType> validator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, listener);
    }
}