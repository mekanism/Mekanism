package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public enum ChemicalTankTier implements ITier, StringRepresentable {
    BASIC(BaseTier.BASIC, 64 * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ADVANCED(BaseTier.ADVANCED, 256 * FluidType.BUCKET_VOLUME, 16 * FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 1_024 * FluidType.BUCKET_VOLUME, 128 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 8_192 * FluidType.BUCKET_VOLUME, 512 * FluidType.BUCKET_VOLUME),
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

    @NotNull
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public long getStorage() {
        return storageReference == null ? getBaseStorage() : storageReference.getOrDefault();
    }

    public long getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.getOrDefault();
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