package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import net.minecraft.util.IStringSerializable;

public enum GasTankTier implements ITier, IStringSerializable {
    BASIC(BaseTier.BASIC, 64_000, 256),
    ADVANCED(BaseTier.ADVANCED, 128_000, 512),
    ELITE(BaseTier.ELITE, 256_000, 1_028),
    ULTIMATE(BaseTier.ULTIMATE, 512_000, 2_056),
    CREATIVE(BaseTier.CREATIVE, Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

    private final int baseStorage;
    private final int baseOutput;
    private final BaseTier baseTier;
    private CachedIntValue storageReference;
    private CachedIntValue outputReference;

    GasTankTier(BaseTier tier, int s, int o) {
        baseStorage = s;
        baseOutput = o;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
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
     * ONLY CALL THIS FROM TierConfig. It is used to give the GasTankTier a reference to the actual config value object
     */
    public void setConfigReference(CachedIntValue storageReference, CachedIntValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}