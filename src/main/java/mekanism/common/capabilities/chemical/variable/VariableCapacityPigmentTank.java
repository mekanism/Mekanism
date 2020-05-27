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
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.inventory.AutomationType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableCapacityPigmentTank extends VariableCapacityChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {

    public static VariableCapacityPigmentTank create(LongSupplier capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new VariableCapacityPigmentTank(capacity, canExtract, canInsert, validator, listener);
    }

    public static VariableCapacityPigmentTank output(LongSupplier capacity, Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new VariableCapacityPigmentTank(capacity, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.internalOnly, validator, listener);
    }

    protected VariableCapacityPigmentTank(LongSupplier capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> validator, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, validator, listener);
    }
}