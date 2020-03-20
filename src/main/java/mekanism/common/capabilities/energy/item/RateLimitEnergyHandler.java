package mekanism.common.capabilities.energy.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.tier.EnergyCubeTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitEnergyHandler extends ItemStackEnergyHandler {

    public static RateLimitEnergyHandler create(double rate, DoubleSupplier capacity) {
        return create(rate, capacity, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue);
    }

    public static RateLimitEnergyHandler create(EnergyCubeTier tier) {
        Objects.requireNonNull(tier, "Energy cube tier cannot be null");
        return new RateLimitEnergyHandler(handler -> new EnergyCubeRateLimitEnergyContainer(tier, handler));
    }

    public static RateLimitEnergyHandler create(double rate, DoubleSupplier capacity, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new RateLimitEnergyHandler(handler -> new RateLimitEnergyContainer(rate, capacity, canExtract, canInsert, handler));
    }

    private IEnergyContainer energyContainer;

    private RateLimitEnergyHandler(Function<IMekanismStrictEnergyHandler, IEnergyContainer> energyContainerProvider) {
        this.energyContainer = energyContainerProvider.apply(this);
    }

    @Override
    protected List<IEnergyContainer> getInitialContainers() {
        return Collections.singletonList(energyContainer);
    }

    private static class RateLimitEnergyContainer extends VariableCapacityEnergyContainer {

        private final double rate;

        private RateLimitEnergyContainer(double rate, DoubleSupplier capacity, Predicate<@NonNull AutomationType> canExtract,
              Predicate<@NonNull AutomationType> canInsert, IMekanismStrictEnergyHandler energyHandler) {
            super(capacity, canExtract, canInsert, energyHandler);
            this.rate = rate;
        }

        @Override
        protected double getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate;
        }
    }

    private static class EnergyCubeRateLimitEnergyContainer extends VariableCapacityEnergyContainer {

        private final DoubleSupplier rate;
        private final boolean isCreative;

        private EnergyCubeRateLimitEnergyContainer(EnergyCubeTier tier, IMekanismStrictEnergyHandler energyHandler) {
            super(tier::getMaxEnergy, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, energyHandler);
            isCreative = tier == EnergyCubeTier.CREATIVE;
            rate = tier::getOutput;
        }

        @Override
        public double insert(double amount, Action action, AutomationType automationType) {
            return super.insert(amount, action.combine(!isCreative), automationType);
        }

        @Override
        public double extract(double amount, Action action, AutomationType automationType) {
            return super.extract(amount, action.combine(!isCreative), automationType);
        }

        @Override
        protected double getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate.getAsDouble();
        }
    }
}