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
public class VariableCapacityChemicalTankBuilder {

    public static final VariableCapacityChemicalTankBuilder INSTANCE = new VariableCapacityChemicalTankBuilder(ChemicalTankBuilder.CHEMICAL, VariableCapacityChemicalTank::new);

    private final VariableCapacityTankCreator tankCreator;
    private final ChemicalTankBuilder tankBuilder;

    private VariableCapacityChemicalTankBuilder(ChemicalTankBuilder tankBuilder, VariableCapacityTankCreator tankCreator) {
        this.tankBuilder = tankBuilder;
        this.tankCreator = tankCreator;
    }

    public IChemicalTank createAllValid(LongSupplier capacity, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        return tankCreator.create(capacity, ChemicalTankBuilder.alwaysTrueBi, ChemicalTankBuilder.alwaysTrueBi, ChemicalTankBuilder.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    public IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    public IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return tankCreator.create(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    public IChemicalTank output(LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return tankCreator.create(capacity, ChemicalTankBuilder.alwaysTrueBi, ChemicalTankBuilder.internalOnly, validator, null, listener);
    }

    @FunctionalInterface
    private interface VariableCapacityTankCreator {

        IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract, BiPredicate<Chemical, @NotNull AutomationType> canInsert,
              Predicate<Chemical> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener);
    }
}