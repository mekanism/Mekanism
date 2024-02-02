package mekanism.common.capabilities.chemical.variable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RateLimitPigmentTank extends RateLimitChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {

    public static RateLimitPigmentTank createBasicItem(long capacity, BiPredicate<@NotNull Pigment, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Pigment, @NotNull AutomationType> canInsert, Predicate<@NotNull Pigment> isValid) {
        //TODO - 1.20.4: Config for transfer rate?? Otherwise use a VariableCapacityChemicalTank instead
        return create(() -> 1_024, () -> capacity, canExtract, canInsert, isValid);
    }

    public static RateLimitPigmentTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Pigment, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Pigment, @NotNull AutomationType> canInsert, Predicate<@NotNull Pigment> isValid) {
        return create(rate, capacity, canExtract, canInsert, isValid, null);
    }

    public static RateLimitPigmentTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Pigment, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Pigment, @NotNull AutomationType> canInsert, Predicate<@NotNull Pigment> isValid, @Nullable IContentsListener listener) {
        return create(rate, capacity, canExtract, canInsert, isValid, null, listener);
    }

    public static RateLimitPigmentTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Pigment, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Pigment, @NotNull AutomationType> canInsert, Predicate<@NotNull Pigment> isValid, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Pigment validity check cannot be null");
        return new RateLimitPigmentTank(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }

    private RateLimitPigmentTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Pigment, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Pigment, @NotNull AutomationType> canInsert, Predicate<@NotNull Pigment> isValid,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }
}