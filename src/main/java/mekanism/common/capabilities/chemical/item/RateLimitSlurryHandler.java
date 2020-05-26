package mekanism.common.capabilities.chemical.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.VariableCapacitySlurryTank;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitSlurryHandler extends ItemStackMekanismSlurryHandler {

    public static RateLimitSlurryHandler create(long rate, LongSupplier capacity) {
        return create(rate, capacity, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrue);
    }

    public static RateLimitSlurryHandler create(long rate, LongSupplier capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> isValid) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Slurry validity check cannot be null");
        return new RateLimitSlurryHandler(listener -> new RateLimitSlurryTank(rate, capacity, canExtract, canInsert, isValid, listener));
    }

    private final ISlurryTank tank;

    private RateLimitSlurryHandler(Function<IContentsListener, ISlurryTank> tankProvider) {
        this.tank = tankProvider.apply(this);
    }

    @Override
    protected List<ISlurryTank> getInitialTanks() {
        return Collections.singletonList(tank);
    }

    public static class RateLimitSlurryTank extends VariableCapacitySlurryTank {

        private final long rate;

        public RateLimitSlurryTank(long rate, LongSupplier capacity, IContentsListener listener) {
            this(rate, capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, listener);
        }

        public RateLimitSlurryTank(long rate, LongSupplier capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> isValid, IContentsListener listener) {
            super(capacity, canExtract, canInsert, isValid, listener);
            this.rate = rate;
        }

        @Override
        protected long getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate;
        }
    }
}