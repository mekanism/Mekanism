package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum EnergyCubeTier implements ITier, StringRepresentable {
    BASIC(BaseTier.BASIC, 4_000_000L, 4_000L),
    ADVANCED(BaseTier.ADVANCED, 16_000_000L, 16_000L),
    ELITE(BaseTier.ELITE, 64_000_000L, 64_000L),
    ULTIMATE(BaseTier.ULTIMATE, 256_000_000L, 256_000L),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, Long.MAX_VALUE);

    private final long baseMaxEnergy;
    private final long baseOutput;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue storageReference;
    @Nullable
    private CachedLongValue outputReference;

    EnergyCubeTier(BaseTier tier, long max, long out) {
        baseMaxEnergy = max;
        baseOutput = out;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public long getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.getOrDefault();
    }

    public long getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.getOrDefault();
    }

    public long getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    public long getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the EnergyCubeTier a reference to the actual config value object
     */
    public void setConfigReference(CachedLongValue storageReference, CachedLongValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}