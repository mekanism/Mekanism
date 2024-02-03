package mekanism.common.capabilities.energy.item;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RateLimitEnergyContainer extends VariableCapacityEnergyContainer {

    public static RateLimitEnergyContainer create(FloatingLongSupplier rate, FloatingLongSupplier capacity) {
        return create(rate, capacity, manualOnly, alwaysTrue);
    }

    public static RateLimitEnergyContainer create(FloatingLongSupplier capacity, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert) {
        return create(() -> capacity.get().multiply(0.005), capacity, canExtract, canInsert);
    }

    public static RateLimitEnergyContainer create(FloatingLongSupplier rate, FloatingLongSupplier capacity, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new RateLimitEnergyContainer(rate, capacity, canExtract, canInsert, null);
    }

    private final FloatingLongSupplier rate;

    protected RateLimitEnergyContainer(FloatingLongSupplier rate, FloatingLongSupplier capacity, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, @Nullable IContentsListener listener) {
        super(capacity, canExtract, canInsert, listener);
        this.rate = rate;
    }

    @Override
    protected FloatingLong getRate(@Nullable AutomationType automationType) {
        //TODO - 1.20.4: Generify this comment and put somewhere
        //Note: We interact with this capability using "manual" as the automation type, to ensure we can properly bypass the energy limit for extracting
        // Internal is used by the "null" side, which is what will get used for most items

        //TODO - 1.20.4: Do we want to move this up a package and somehow specify this as a parameter or something so that this container isn't limited to items
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate.get();
    }
}