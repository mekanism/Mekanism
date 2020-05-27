package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableCapacityEnergyContainer extends BasicEnergyContainer {

    public static VariableCapacityEnergyContainer input(FloatingLongSupplier maxEnergy, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, notExternal, alwaysTrue, listener);
    }

    public static VariableCapacityEnergyContainer output(FloatingLongSupplier maxEnergy, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, alwaysTrue, internalOnly, listener);
    }

    public static VariableCapacityEnergyContainer create(FloatingLongSupplier maxEnergy, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert, @Nullable IContentsListener listener) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, canExtract, canInsert, listener);
    }

    private final FloatingLongSupplier maxEnergy;

    protected VariableCapacityEnergyContainer(FloatingLongSupplier maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        super(maxEnergy.get(), canExtract, canInsert, listener);
        this.maxEnergy = maxEnergy;
    }

    @Override
    public FloatingLong getMaxEnergy() {
        return maxEnergy.get();
    }
}