package mekanism.common.capabilities.energy.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.tier.EnergyCubeTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitEnergyHandler extends ItemStackEnergyHandler {

    public static RateLimitEnergyHandler create(EnergyCubeTier tier) {
        Objects.requireNonNull(tier, "Energy cube tier cannot be null");
        return new RateLimitEnergyHandler(handler -> new EnergyCubeRateLimitEnergyContainer(tier, handler));
    }

    public static RateLimitEnergyHandler create(FloatingLongSupplier capacity, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert) {
        return create(() -> capacity.get().multiply(0.005), capacity, canExtract, canInsert);
    }

    public static RateLimitEnergyHandler create(FloatingLongSupplier rate, FloatingLongSupplier capacity, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new RateLimitEnergyHandler(handler -> new RateLimitEnergyContainer(rate, capacity, canExtract, canInsert, handler));
    }

    private final IEnergyContainer energyContainer;

    private RateLimitEnergyHandler(Function<IMekanismStrictEnergyHandler, IEnergyContainer> energyContainerProvider) {
        this.energyContainer = energyContainerProvider.apply(this);
    }

    @Override
    protected List<IEnergyContainer> getInitialContainers() {
        return Collections.singletonList(energyContainer);
    }

    private static class RateLimitEnergyContainer extends VariableCapacityEnergyContainer {

        private final FloatingLongSupplier rate;

        private RateLimitEnergyContainer(FloatingLongSupplier rate, FloatingLongSupplier capacity, Predicate<@NonNull AutomationType> canExtract,
              Predicate<@NonNull AutomationType> canInsert, IContentsListener listener) {
            super(capacity, canExtract, canInsert, listener);
            this.rate = rate;
        }

        @Override
        protected FloatingLong getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate.get();
        }
    }

    private static class EnergyCubeRateLimitEnergyContainer extends RateLimitEnergyContainer {

        private final boolean isCreative;

        private EnergyCubeRateLimitEnergyContainer(EnergyCubeTier tier, IContentsListener listener) {
            super(tier::getOutput, tier::getMaxEnergy, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, listener);
            isCreative = tier == EnergyCubeTier.CREATIVE;
        }

        @Override
        public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
            return super.insert(amount, action.combine(!isCreative), automationType);
        }

        @Override
        public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
            return super.extract(amount, action.combine(!isCreative), automationType);
        }
    }
}