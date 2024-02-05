package mekanism.common.capabilities.chemical.variable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.config.MekanismConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RateLimitGasTank extends RateLimitChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {

    public static RateLimitGasTank createBasicItem(long capacity, BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Gas, @NotNull AutomationType> canInsert, Predicate<@NotNull Gas> isValid) {
        return createBasicItem(() -> capacity, canExtract, canInsert, isValid);
    }

    public static RateLimitGasTank createBasicItem(LongSupplier capacity, BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Gas, @NotNull AutomationType> canInsert, Predicate<@NotNull Gas> isValid) {
        return create(MekanismConfig.general.chemicalItemFillRate, capacity, canExtract, canInsert, isValid);
    }

    public static RateLimitGasTank createInternalStorage(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull Gas> isValid) {
        return create(rate, capacity, ChemicalTankBuilder.GAS.notExternal, ChemicalTankBuilder.GAS.alwaysTrueBi, isValid);
    }

    public static RateLimitGasTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Gas, @NotNull AutomationType> canInsert, Predicate<@NotNull Gas> isValid) {
        return create(rate, capacity, canExtract, canInsert, isValid, null, null);
    }

    public static RateLimitGasTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Gas, @NotNull AutomationType> canInsert, Predicate<@NotNull Gas> isValid, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Gas validity check cannot be null");
        return new RateLimitGasTank(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }

    private RateLimitGasTank(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Gas, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Gas, @NotNull AutomationType> canInsert, Predicate<@NotNull Gas> isValid, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        super(rate, capacity, canExtract, canInsert, isValid, attributeValidator, listener);
    }
}