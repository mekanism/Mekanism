package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.RateLimitTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class VariableCapacityEnergyContainer extends BasicEnergyContainer {

    public static VariableCapacityEnergyContainer input(LongSupplier maxEnergy, @Nullable IContentsListener listener) {
        return create(maxEnergy, notExternal, ConstantPredicates.alwaysTrue(), listener);
    }

    public static VariableCapacityEnergyContainer output(LongSupplier maxEnergy, @Nullable IContentsListener listener) {
        return create(maxEnergy, ConstantPredicates.alwaysTrue(), internalOnly, listener);
    }

    public static VariableCapacityEnergyContainer create(LongSupplier maxEnergy, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, canExtract, canInsert, null, null, listener);
    }

    private final LongSupplier maxEnergy;

    protected VariableCapacityEnergyContainer(LongSupplier maxEnergy, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener) {
        super(maxEnergy.getAsLong(), canExtract, canInsert, insertionRateLimiter, extractionRateLimiter, listener);
        this.maxEnergy = maxEnergy;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
        return maxEnergy.getAsLong();
    }
}