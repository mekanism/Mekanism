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
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.VariableCapacityInfusionTank;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitInfusionHandler extends ItemStackMekanismInfusionHandler {

    public static RateLimitInfusionHandler create(long rate, LongSupplier capacity) {
        return create(rate, capacity, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrue);
    }

    public static RateLimitInfusionHandler create(long rate, LongSupplier capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert, Predicate<@NonNull InfuseType> isValid) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Infuse type validity check cannot be null");
        return new RateLimitInfusionHandler(handler -> new RateLimitInfusionTank(rate, capacity, canExtract, canInsert, isValid, handler));
    }

    private IChemicalTank<InfuseType, InfusionStack> tank;

    private RateLimitInfusionHandler(Function<IMekanismInfusionHandler, IChemicalTank<InfuseType, InfusionStack>> tankProvider) {
        this.tank = tankProvider.apply(this);
    }

    @Override
    protected List<? extends IChemicalTank<InfuseType, InfusionStack>> getInitialTanks() {
        return Collections.singletonList(tank);
    }

    public static class RateLimitInfusionTank extends VariableCapacityInfusionTank {

        private final long rate;

        public RateLimitInfusionTank(long rate, LongSupplier capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert, Predicate<@NonNull InfuseType> isValid, IMekanismInfusionHandler infusionHandler) {
            super(capacity, canExtract, canInsert, isValid, infusionHandler);
            this.rate = rate;
        }

        @Override
        protected long getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate;
        }
    }
}