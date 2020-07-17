package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import net.minecraft.util.IStringSerializable;

public enum ChemicalTankTier implements ITier, IStringSerializable {
    BASIC(BaseTier.BASIC, 64_000, 256),
    ADVANCED(BaseTier.ADVANCED, 128_000, 512),
    ELITE(BaseTier.ELITE, 256_000, 1_028),
    ULTIMATE(BaseTier.ULTIMATE, 512_000, 2_056),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, Long.MAX_VALUE / 2);

    private final long baseStorage;
    private final long baseOutput;
    private final BaseTier baseTier;
    private CachedLongValue storageReference;
    private CachedLongValue outputReference;

    ChemicalTankTier(BaseTier tier, long s, long o) {
        baseStorage = s;
        baseOutput = o;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public long getStorage() {
        return storageReference == null ? getBaseStorage() : storageReference.get();
    }

    public long getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.get();
    }

    public long getBaseStorage() {
        return baseStorage;
    }

    public long getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the GasTankTier a reference to the actual config value object
     */
    public void setConfigReference(CachedLongValue storageReference, CachedLongValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}