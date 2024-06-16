package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.math.Unsigned;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class VariableCapacityEnergyContainer extends BasicEnergyContainer {

    public static VariableCapacityEnergyContainer input(@Unsigned LongSupplier maxEnergy, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, notExternal, alwaysTrue, listener);
    }

    public static VariableCapacityEnergyContainer output(@Unsigned LongSupplier maxEnergy, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, alwaysTrue, internalOnly, listener);
    }

    public static VariableCapacityEnergyContainer create(@Unsigned LongSupplier maxEnergy, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, canExtract, canInsert, listener);
    }

    private final @Unsigned LongSupplier maxEnergy;

    protected VariableCapacityEnergyContainer(@Unsigned LongSupplier maxEnergy, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        super(maxEnergy.getAsLong(), canExtract, canInsert, listener);
        this.maxEnergy = maxEnergy;
    }

    @Override
    public @Unsigned long getMaxEnergy() {
        return maxEnergy.getAsLong();
    }
}