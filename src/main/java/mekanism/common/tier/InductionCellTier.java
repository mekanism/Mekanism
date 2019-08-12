package mekanism.common.tier;

import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public enum InductionCellTier implements ITier {
    BASIC(1E9D),
    ADVANCED(8E9D),
    ELITE(64E9D),
    ULTIMATE(512E9D);

    private final double baseMaxEnergy;
    private final BaseTier baseTier;
    private DoubleValue storageReference;

    InductionCellTier(double max) {
        baseMaxEnergy = max;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.get();
    }

    public double getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the InductionCellTier a reference to the actual config value object
     */
    public void setConfigReference(DoubleValue storageReference) {
        this.storageReference = storageReference;
    }
}