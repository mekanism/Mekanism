package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.tier.BaseTier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public enum ChemicalTankTier implements IStorageTier, StringRepresentable {
    BASIC(BaseTier.BASIC, 64L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ADVANCED(BaseTier.ADVANCED, 256L * FluidType.BUCKET_VOLUME, 16 * FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 1_024L * FluidType.BUCKET_VOLUME, 128 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 8_192L * FluidType.BUCKET_VOLUME, 512 * FluidType.BUCKET_VOLUME),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, Integer.MAX_VALUE);

    private final long baseCapacity;
    private final int baseTransferRate;
    private final BaseTier baseTier;
    private CachedLongValue capacityReference;
    private CachedIntValue transferRateReference;

    ChemicalTankTier(BaseTier tier, long capacity, int transferRate) {
        baseCapacity = capacity;
        baseTransferRate = transferRate;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
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

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the GasTankTier a reference to the actual config value object
     */
    public void setConfigReference(CachedLongValue capacityReference, CachedIntValue transferRateReference) {
        this.capacityReference = capacityReference;
        this.transferRateReference = transferRateReference;
    }
}