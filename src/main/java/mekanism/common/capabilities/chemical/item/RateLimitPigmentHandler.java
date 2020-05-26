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
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.VariableCapacityPigmentTank;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitPigmentHandler extends ItemStackMekanismPigmentHandler {

    public static RateLimitPigmentHandler create(long rate, LongSupplier capacity) {
        return create(rate, capacity, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrue);
    }

    public static RateLimitPigmentHandler create(long rate, LongSupplier capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> isValid) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Pigment validity check cannot be null");
        return new RateLimitPigmentHandler(listener -> new RateLimitPigmentTank(rate, capacity, canExtract, canInsert, isValid, listener));
    }

    private final IPigmentTank tank;

    private RateLimitPigmentHandler(Function<IContentsListener, IPigmentTank> tankProvider) {
        this.tank = tankProvider.apply(this);
    }

    @Override
    protected List<IPigmentTank> getInitialTanks() {
        return Collections.singletonList(tank);
    }

    public static class RateLimitPigmentTank extends VariableCapacityPigmentTank {

        private final long rate;

        public RateLimitPigmentTank(long rate, LongSupplier capacity, IContentsListener listener) {
            this(rate, capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, listener);
        }

        public RateLimitPigmentTank(long rate, LongSupplier capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> isValid, IContentsListener listener) {
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