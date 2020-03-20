package mekanism.common.block.attribute;

import java.util.function.DoubleSupplier;

public class AttributeEnergy implements Attribute {

    private DoubleSupplier energyUsage = () -> 0;
    // 2 operations (20 secs) worth of ticks * usage
    private DoubleSupplier energyStorage = () -> 400 * energyUsage.getAsDouble();

    public AttributeEnergy(DoubleSupplier energyUsage, DoubleSupplier energyStorage) {
        if (energyUsage != null) {
            this.energyUsage = energyUsage;
        }
        if (energyStorage != null) {
            this.energyStorage = energyStorage;
        }
    }

    public double getUsage() {
        return energyUsage.getAsDouble();
    }

    public double getConfigStorage() {
        return energyStorage.getAsDouble();
    }

    public double getStorage() {
        return Math.max(getConfigStorage(), getUsage());
    }
}
