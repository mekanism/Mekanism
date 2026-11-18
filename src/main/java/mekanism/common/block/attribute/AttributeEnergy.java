package mekanism.common.block.attribute;

import java.util.function.IntSupplier;
import mekanism.common.tier.FactoryTier;

public class AttributeEnergy implements Attribute {

    /// How many ticks of usage should the storage for machines get multiplied by
    ///
    /// @implNote This is a long so that we don't have to worry about overflow if someone sets the usage config to max int
    private static final long STORAGE_MULTIPLIER = 4;

    private final IntSupplier energyUsage;
    private final IntSupplier baseTicksRequired;
    private final long baseMultiplier;

    public AttributeEnergy(IntSupplier energyUsage, IntSupplier baseTicksRequired) {
        this(energyUsage, baseTicksRequired, 1);
    }

    private AttributeEnergy(IntSupplier energyUsage, IntSupplier baseTicksRequired, long multiplier) {
        this.energyUsage = energyUsage;
        this.baseTicksRequired = baseTicksRequired;
        this.baseMultiplier = multiplier;
    }

    public int getUsage() {
        return energyUsage.getAsInt();
    }

    public long getStorage() {
        return getUsage() * getBaseMultiplier();
    }

    public int getBaseTicksRequired() {
        return baseTicksRequired.getAsInt();
    }

    public long getBaseMultiplier() {
        return getMultiplier(getBaseTicksRequired());
    }

    public long getMultiplier(int ticksRequired) {
        return Math.max(ticksRequired, STORAGE_MULTIPLIER) * baseMultiplier;
    }

    public AttributeEnergy scaleForFactory(FactoryTier tier) {
        return new AttributeEnergy(energyUsage, baseTicksRequired, baseMultiplier * tier.processes);
    }
}
