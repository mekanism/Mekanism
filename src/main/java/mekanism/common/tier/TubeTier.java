package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public enum TubeTier implements IStorageTier {
    BASIC(BaseTier.BASIC, 4L * FluidType.BUCKET_VOLUME, 750),
    ADVANCED(BaseTier.ADVANCED, 16L * FluidType.BUCKET_VOLUME, 2 * FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 256L * FluidType.BUCKET_VOLUME, 64 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 1_024L * FluidType.BUCKET_VOLUME, 256 * FluidType.BUCKET_VOLUME);

    private final long baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;
    @Nullable
    private CachedIntValue transferRateReference;

    TubeTier(BaseTier tier, long capacity, int transferRate) {
        baseCapacity = capacity;
        basePull = transferRate;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public long getCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    @Override
    public int getTransferRate() {
        return transferRateReference == null ? getBaseTransferRate() : transferRateReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    public int getBaseTransferRate() {
        return basePull;
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the TubeTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference, CachedIntValue transferRateReference) {
        this.capacityReference = capacityReference;
        this.transferRateReference = transferRateReference;
    }
}