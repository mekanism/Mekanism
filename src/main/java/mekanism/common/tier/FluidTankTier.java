package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public enum FluidTankTier implements IStorageTier {//TODO - 26.2: Do we want to change capacities to match chemicals?
    BASIC(BaseTier.BASIC, 32L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ADVANCED(BaseTier.ADVANCED, 64L * FluidType.BUCKET_VOLUME, 4 * FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 128L * FluidType.BUCKET_VOLUME, 16 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 256L * FluidType.BUCKET_VOLUME, 64 * FluidType.BUCKET_VOLUME),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, Integer.MAX_VALUE);

    private final long baseCapacity;
    private final int baseTransferRate;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;
    @Nullable
    private CachedIntValue transferRateReference;

    FluidTankTier(BaseTier tier, long capacity, int transferRate) {
        baseCapacity = capacity;
        baseTransferRate = transferRate;
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
    public boolean isCreative() {
        return this == CREATIVE;
    }

    @Override
    public int getTransferRate() {
        return transferRateReference == null ? getBaseTransferRate() : transferRateReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    public int getBaseTransferRate() {
        return baseTransferRate;
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the FluidTankTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference, CachedIntValue transferRateReference) {
        this.capacityReference = capacityReference;
        this.transferRateReference = transferRateReference;
    }
}