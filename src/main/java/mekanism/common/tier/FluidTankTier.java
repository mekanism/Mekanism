package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.neoforge.fluids.FluidType;

public enum FluidTankTier implements ITier {//TODO - 26.1: Do we want to change capacities to match chemicals?
    BASIC(BaseTier.BASIC, 32L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ADVANCED(BaseTier.ADVANCED, 64L * FluidType.BUCKET_VOLUME, 4 * FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 128L * FluidType.BUCKET_VOLUME, 16 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 256L * FluidType.BUCKET_VOLUME, 64 * FluidType.BUCKET_VOLUME),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, Integer.MAX_VALUE);

    private final long baseStorage;
    private final int baseOutput;
    private final BaseTier baseTier;
    private CachedLongValue storageReference;
    private CachedIntValue outputReference;

    FluidTankTier(BaseTier tier, long storage, int output) {
        baseStorage = storage;
        baseOutput = output;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public long getStorage() {
        return storageReference == null ? getBaseStorage() : storageReference.getOrDefault();
    }

    public int getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.getOrDefault();
    }

    public long getBaseStorage() {
        return baseStorage;
    }

    public int getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the FluidTankTier a reference to the actual config value object
     */
    public void setConfigReference(CachedLongValue storageReference, CachedIntValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}