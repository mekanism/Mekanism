package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import net.neoforged.neoforge.fluids.FluidType;

public enum FluidTankTier implements ITier {
    BASIC(BaseTier.BASIC, 32 * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ADVANCED(BaseTier.ADVANCED, 64 * FluidType.BUCKET_VOLUME, 4 * FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 128 * FluidType.BUCKET_VOLUME, 16 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 256 * FluidType.BUCKET_VOLUME, 64 * FluidType.BUCKET_VOLUME),
    CREATIVE(BaseTier.CREATIVE, Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

    private final int baseStorage;
    private final int baseOutput;
    private final BaseTier baseTier;
    private CachedIntValue storageReference;
    private CachedIntValue outputReference;

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
        return storageReference == null ? getBaseStorage() : storageReference.getOrDefault();
    }

    public int getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.getOrDefault();
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
    public void setConfigReference(CachedIntValue storageReference, CachedIntValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}