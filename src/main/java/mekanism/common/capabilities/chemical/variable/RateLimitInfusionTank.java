package mekanism.common.capabilities.chemical.variable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RateLimitInfusionTank extends RateLimitChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {

    public static RateLimitInfusionTank createBasicItem(long capacity, BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canInsert, Predicate<@NotNull InfuseType> isValid) {
        //TODO - 1.20.4: Config for transfer rate?? Otherwise use a VariableCapacityChemicalTank instead
        return create(() -> 1_024, () -> capacity, canExtract, canInsert, isValid);
    }

    public static RateLimitInfusionTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canInsert, Predicate<@NotNull InfuseType> isValid) {
        return create(rate, capacity, canExtract, canInsert, isValid, null);
    }

    public static RateLimitInfusionTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canInsert, Predicate<@NotNull InfuseType> isValid, @Nullable IContentsListener listener) {
        return create(rate, capacity, canExtract, canInsert, isValid, null, listener);
    }

    public static RateLimitInfusionTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canInsert, Predicate<@NotNull InfuseType> isValid,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Infuse Type validity check cannot be null");
        return new RateLimitInfusionTank(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }

    private RateLimitInfusionTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canInsert, Predicate<@NotNull InfuseType> isValid,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        super(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }
}