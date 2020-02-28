package mekanism.common.capabilities.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.inventory.AutomationType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitGasHandler create(IntSupplier rate, IntSupplier capacity) {
        //TODO: Validate capacity and rate are positive?
        return new RateLimitGasHandler(rate, capacity, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue);
    }

    public static RateLimitGasHandler create(IntSupplier rate, IntSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> validator) {
        //TODO: Validate capacity and rate are positive?
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new RateLimitGasHandler(rate, capacity, canExtract, canInsert, validator);
    }

    private IChemicalTank<Gas, GasStack> tank;

    private RateLimitGasHandler(IntSupplier rate, IntSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> isValid) {
        tank = new RateLimitGasTank(rate, capacity, canExtract, canInsert, isValid, this);
    }

    @Override
    protected List<? extends IChemicalTank<Gas, GasStack>> getInitialTanks() {
        return Collections.singletonList(tank);
    }

    private static class RateLimitGasTank extends BasicGasTank {

        private final IntSupplier rate;
        private final IntSupplier capacity;

        private RateLimitGasTank(IntSupplier rate, IntSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> isValid, IMekanismGasHandler gasHandler) {
            super(capacity.getAsInt(), canExtract, canInsert, isValid, gasHandler);
            this.rate = rate;
            this.capacity = capacity;
        }

        @Override
        protected int getRate() {
            return rate.getAsInt();
        }

        @Override
        public int getCapacity() {
            return capacity.getAsInt();
        }
    }
}