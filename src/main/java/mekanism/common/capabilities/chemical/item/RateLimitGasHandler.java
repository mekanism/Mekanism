package mekanism.common.capabilities.chemical.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.VariableCapacityGasTank;
import mekanism.common.tier.GasTankTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitGasHandler extends ItemStackMekanismGasHandler {

    public static RateLimitGasHandler create(int rate, IntSupplier capacity) {
        return create(rate, capacity, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue);
    }

    public static RateLimitGasHandler create(GasTankTier tier) {
        Objects.requireNonNull(tier, "Gas tank tier cannot be null");
        return new RateLimitGasHandler(handler -> new GasTankRateLimitGasTank(tier, handler));
    }

    public static RateLimitGasHandler create(int rate, IntSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> isValid) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Gas validity check cannot be null");
        return new RateLimitGasHandler(handler -> new RateLimitGasTank(rate, capacity, canExtract, canInsert, isValid, handler));
    }

    private IChemicalTank<Gas, GasStack> tank;

    private RateLimitGasHandler(Function<IMekanismGasHandler, IChemicalTank<Gas, GasStack>> tankProvider) {
        this.tank = tankProvider.apply(this);
    }

    @Override
    protected List<? extends IChemicalTank<Gas, GasStack>> getInitialTanks() {
        return Collections.singletonList(tank);
    }

    public static class RateLimitGasTank extends VariableCapacityGasTank {

        private final int rate;

        public RateLimitGasTank(int rate, IntSupplier capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> isValid, IMekanismGasHandler gasHandler) {
            super(capacity, canExtract, canInsert, isValid, gasHandler);
            this.rate = rate;
        }

        @Override
        protected int getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate;
        }
    }

    private static class GasTankRateLimitGasTank extends VariableCapacityGasTank {

        private final IntSupplier rate;
        private final boolean isCreative;

        private GasTankRateLimitGasTank(GasTankTier tier, IMekanismGasHandler gasHandler) {
            super(tier::getStorage, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, gasHandler);
            isCreative = tier == GasTankTier.CREATIVE;
            rate = tier::getOutput;
        }

        @Override
        public GasStack insert(GasStack stack, Action action, AutomationType automationType) {
            return super.insert(stack, action.combine(!isCreative), automationType);
        }

        @Override
        public GasStack extract(int amount, Action action, AutomationType automationType) {
            return super.extract(amount, action.combine(!isCreative), automationType);
        }

        /**
         * {@inheritDoc}
         *
         * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped
         * through this method.
         */
        @Override
        public int setStackSize(int amount, Action action) {
            return super.setStackSize(amount, action.combine(!isCreative));
        }

        @Override
        protected int getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate.getAsInt();
        }
    }
}