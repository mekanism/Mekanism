package mekanism.common.tier;

import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public enum FluidTankTier implements ITier {
    BASIC(BaseTier.BASIC, 14_000, 400),
    ADVANCED(BaseTier.ADVANCED, 28_000, 800),
    ELITE(BaseTier.ELITE, 56_000, 1_600),
    ULTIMATE(BaseTier.ULTIMATE, 112_000, 3_200),
    CREATIVE(BaseTier.CREATIVE, Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

    private final int baseStorage;
    private final int baseOutput;
    private final BaseTier baseTier;
    private IntValue storageReference;
    private IntValue outputReference;

    FluidTankTier(BaseTier tier, int s, int o) {
        baseStorage = s;
        baseOutput = o;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getStorage() {
        return storageReference == null ? getBaseStorage() : storageReference.get();
    }

    public int getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.get();
    }

    public int getBaseStorage() {
        return baseStorage;
    }

    public int getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the FluidTankTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue storageReference, IntValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}