package mekanism.common.capabilities.energy.item;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class NoClampRateLimitEnergyContainer extends RateLimitEnergyContainer {

    public static NoClampRateLimitEnergyContainer create(FloatingLongSupplier rate, FloatingLongSupplier capacity) {
        return create(rate, capacity, manualOnly, alwaysTrue);
    }

    public static NoClampRateLimitEnergyContainer create(FloatingLongSupplier capacity, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert) {
        return create(() -> capacity.get().multiply(0.005), capacity, canExtract, canInsert);
    }

    public static NoClampRateLimitEnergyContainer create(FloatingLongSupplier rate, FloatingLongSupplier capacity, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new NoClampRateLimitEnergyContainer(rate, capacity, canExtract, canInsert, null);
    }

    protected NoClampRateLimitEnergyContainer(FloatingLongSupplier rate, FloatingLongSupplier capacity, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        super(rate, capacity, canExtract, canInsert, listener);
    }

    @Override
    protected FloatingLong clampEnergy(FloatingLong energy) {
        //Don't clamp the energy
        return energy;
    }
}