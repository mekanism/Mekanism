package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class VariableCapacityEnergyContainer extends BasicEnergyContainer {

    public static VariableCapacityEnergyContainer input(LongSupplier maxEnergy, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, notExternal, ConstantPredicates.alwaysTrue(), listener);
    }

    public static VariableCapacityEnergyContainer output(LongSupplier maxEnergy, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, ConstantPredicates.alwaysTrue(), internalOnly, listener);
    }

    public static VariableCapacityEnergyContainer create(LongSupplier maxEnergy, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, canExtract, canInsert, listener);
    }

    private final LongSupplier maxEnergy;

    protected VariableCapacityEnergyContainer(LongSupplier maxEnergy, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        super(maxEnergy.getAsLong(), canExtract, canInsert, listener);
        this.maxEnergy = maxEnergy;
    }

    @Override
    public long getMaxEnergy() {
        return maxEnergy.getAsLong();
    }
}