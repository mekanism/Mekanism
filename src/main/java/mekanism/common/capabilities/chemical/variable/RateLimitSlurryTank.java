package mekanism.common.capabilities.chemical.variable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RateLimitSlurryTank extends RateLimitChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {

    public static RateLimitSlurryTank createBasicItem(long capacity, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> isValid) {
        //TODO - 1.20.4: Config for transfer rate?? Otherwise use a VariableCapacityChemicalTank instead
        return create(() -> 1_024, () -> capacity, canExtract, canInsert, isValid);
    }

    public static RateLimitSlurryTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> isValid) {
        return create(rate, capacity, canExtract, canInsert, isValid, null);
    }

    public static RateLimitSlurryTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> isValid, @Nullable IContentsListener listener) {
        return create(rate, capacity, canExtract, canInsert, isValid, null, listener);
    }

    public static RateLimitSlurryTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> isValid, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Slurry validity check cannot be null");
        return new RateLimitSlurryTank(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }

    private RateLimitSlurryTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> isValid,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }
}