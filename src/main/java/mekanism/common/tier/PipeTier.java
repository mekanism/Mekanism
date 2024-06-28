package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.EnumUtils;
import net.neoforged.neoforge.fluids.FluidType;

public enum PipeTier implements ITier {
    BASIC(BaseTier.BASIC, 2 * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME / 4),
    ADVANCED(BaseTier.ADVANCED, 8 * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 32 * FluidType.BUCKET_VOLUME, 8 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 128 * FluidType.BUCKET_VOLUME, 32 * FluidType.BUCKET_VOLUME);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;
    private CachedIntValue capacityReference;
    private CachedIntValue pullReference;

    PipeTier(BaseTier tier, int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = tier;
    }

    public static PipeTier get(BaseTier tier) {
        for (PipeTier transmitter : EnumUtils.PIPE_TIERS) {
            if (transmitter.getBaseTier() == tier) {
                return transmitter;
            }
        }
        return BASIC;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getPipeCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    public int getPipePullAmount() {
        return pullReference == null ? getBasePull() : pullReference.getOrDefault();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getBasePull() {
        return basePull;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the PipeTier a reference to the actual config value object
     */
    public void setConfigReference(CachedIntValue capacityReference, CachedIntValue pullReference) {
        this.capacityReference = capacityReference;
        this.pullReference = pullReference;
    }
}