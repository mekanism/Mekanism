package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public enum PipeTier implements IStorageTier {//TODO - 26.2: Do we want to change capacities to match chemicals?
    BASIC(BaseTier.BASIC, 2L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME / 4),
    ADVANCED(BaseTier.ADVANCED, 8L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 32L * FluidType.BUCKET_VOLUME, 8 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 128L * FluidType.BUCKET_VOLUME, 32 * FluidType.BUCKET_VOLUME);

    private final long baseCapacity;
    private final int baseTransferRate;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;
    @Nullable
    private CachedIntValue transferRateReference;

    PipeTier(BaseTier tier, long capacity, int transferRate) {
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
    public int getTransferRate() {
        return transferRateReference == null ? getBaseTransferRate() : transferRateReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    public int getBaseTransferRate() {
        return baseTransferRate;
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the PipeTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference, CachedIntValue transferRateReference) {
        this.capacityReference = capacityReference;
        this.transferRateReference = transferRateReference;
    }
}