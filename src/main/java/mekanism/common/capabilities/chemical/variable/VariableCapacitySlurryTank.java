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
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.inventory.AutomationType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableCapacitySlurryTank extends VariableCapacityChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {

    public static VariableCapacitySlurryTank create(LongSupplier capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new VariableCapacitySlurryTank(capacity, canExtract, canInsert, validator, listener);
    }

    public static VariableCapacitySlurryTank output(LongSupplier capacity, Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new VariableCapacitySlurryTank(capacity, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.internalOnly, validator, listener);
    }

    protected VariableCapacitySlurryTank(LongSupplier capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> validator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, listener);
    }
}