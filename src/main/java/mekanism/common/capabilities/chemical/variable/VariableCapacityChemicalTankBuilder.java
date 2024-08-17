package mekanism.common.capabilities.chemical.variable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class VariableCapacityChemicalTankBuilder {

    private VariableCapacityChemicalTankBuilder() {
    }

    public static IChemicalTank createAllValid(LongSupplier capacity, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        return new VariableCapacityChemicalTank(capacity, ChemicalTankBuilder.alwaysTrueBi, ChemicalTankBuilder.alwaysTrueBi, ChemicalTankBuilder.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    public static IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    public static IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new VariableCapacityChemicalTank(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    public static IChemicalTank output(LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new VariableCapacityChemicalTank(capacity, ChemicalTankBuilder.alwaysTrueBi, ChemicalTankBuilder.internalOnly, validator, null, listener);
    }
}