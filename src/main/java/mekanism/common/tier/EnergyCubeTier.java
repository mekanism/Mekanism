package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedDoubleValue;
import net.minecraft.util.IStringSerializable;

public enum EnergyCubeTier implements ITier, IStringSerializable {
    BASIC(BaseTier.BASIC, 2_000_000, 800),
    ADVANCED(BaseTier.ADVANCED, 8_000_000, 3_200),
    ELITE(BaseTier.ELITE, 32_000_000, 12_800),
    ULTIMATE(BaseTier.ULTIMATE, 128_000_000, 51_200),
    CREATIVE(BaseTier.CREATIVE, Double.MAX_VALUE, Double.MAX_VALUE);

    private final double baseMaxEnergy;
    private final double baseOutput;
    private final BaseTier baseTier;
    private CachedDoubleValue storageReference;
    private CachedDoubleValue outputReference;

    EnergyCubeTier(BaseTier tier, double max, double out) {
        baseMaxEnergy = max;
        baseOutput = out;
        baseTier = tier;
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
    public void setConfigReference(CachedDoubleValue storageReference, CachedDoubleValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}