package mekanism.common.tier;

import com.google.common.primitives.UnsignedLongs;
import java.util.Locale;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedUnsignedLongValue;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum EnergyCubeTier implements ITier, StringRepresentable {
    BASIC(BaseTier.BASIC, 4_000_000L, 4_000L),
    ADVANCED(BaseTier.ADVANCED, 16_000_000L, 16_000L),
    ELITE(BaseTier.ELITE, 64_000_000L, 64_000L),
    ULTIMATE(BaseTier.ULTIMATE, 256_000_000L, 256_000L),
    CREATIVE(BaseTier.CREATIVE, UnsignedLongs.MAX_VALUE, UnsignedLongs.MAX_VALUE);

    private final @Unsigned long baseMaxEnergy;
    private final @Unsigned long baseOutput;
    private final BaseTier baseTier;
    @Nullable
    private CachedUnsignedLongValue storageReference;
    @Nullable
    private CachedUnsignedLongValue outputReference;

    EnergyCubeTier(BaseTier tier, @Unsigned long max, @Unsigned long out) {
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

    public @Unsigned long getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.getOrDefault();
    }

    public @Unsigned long getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.getOrDefault();
    }

    public @Unsigned long getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    public @Unsigned long getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the EnergyCubeTier a reference to the actual config value object
     */
    public void setConfigReference(CachedUnsignedLongValue storageReference, CachedUnsignedLongValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}