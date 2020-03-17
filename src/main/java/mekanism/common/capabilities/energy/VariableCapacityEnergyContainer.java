package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableCapacityEnergyContainer extends BasicEnergyContainer {//TODO: Evaluate if this should even exist

    public static VariableCapacityEnergyContainer input(DoubleSupplier maxEnergy, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, notExternal, alwaysTrue, energyHandler);
    }

    public static VariableCapacityEnergyContainer output(DoubleSupplier maxEnergy, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, alwaysTrue, internalOnly, energyHandler);
    }

    public static VariableCapacityEnergyContainer create(DoubleSupplier maxEnergy, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(maxEnergy, "Max energy supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new VariableCapacityEnergyContainer(maxEnergy, canExtract, canInsert, energyHandler);
    }

    private DoubleSupplier maxEnergy;

    protected VariableCapacityEnergyContainer(DoubleSupplier maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IMekanismStrictEnergyHandler energyHandler) {
        super(maxEnergy.getAsDouble(), canExtract, canInsert, energyHandler);
        this.maxEnergy = maxEnergy;
    }

    @Override
    public double getMaxEnergy() {
        return maxEnergy.getAsDouble();
    }
}