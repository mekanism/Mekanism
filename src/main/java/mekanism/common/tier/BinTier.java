package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.tier.BaseTier;
import mekanism.common.config.value.CachedLongValue;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: Do we want to up the default config limits for any of these tiers?
public enum BinTier implements IStorageTier, StringRepresentable {
    BASIC(BaseTier.BASIC, 4_096),
    ADVANCED(BaseTier.ADVANCED, 8_192),
    ELITE(BaseTier.ELITE, 32_768),
    ULTIMATE(BaseTier.ULTIMATE, 262_144),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE);

    private final long baseCapacity;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;

    BinTier(BaseTier tier, long capacity) {
        baseTier = tier;
        baseCapacity = capacity;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isCreative() {
        return this == CREATIVE;
    }

    @Override
    public int getTransferRate() {
        //TODO - 26.2: Do we want to set a transfer rate here?
        return Integer.MAX_VALUE;
    }

    @Override
    public long getCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the BinTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}