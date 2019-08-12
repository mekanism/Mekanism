package mekanism.common.tier;

import java.util.Locale;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public enum EnergyCubeTier implements ITier, IStringSerializable {
    BASIC(2_000_000, 800),
    ADVANCED(8_000_000, 3200),
    ELITE(32_000_000, 12800),
    ULTIMATE(128_000_000, 51200),
    CREATIVE(Double.MAX_VALUE, Double.MAX_VALUE);

    private final double baseMaxEnergy;
    private final double baseOutput;
    private final BaseTier baseTier;
    private DoubleValue storageReference;
    private DoubleValue outputReference;

    EnergyCubeTier(double max, double out) {
        baseMaxEnergy = max;
        baseOutput = out;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public double getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.get();
    }

    public double getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.get();
    }

    public double getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    public double getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the EnergyCubeTier a reference to the actual config value object
     */
    public void setConfigReference(DoubleValue storageReference, DoubleValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}