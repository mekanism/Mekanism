package mekanism.common.tier;

import java.util.Locale;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import net.minecraft.util.IStringSerializable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum EnergyCubeTier implements ITier, IStringSerializable {
    BASIC(BaseTier.BASIC, FloatingLong.createConst(4_000_000), FloatingLong.createConst(4_000)),
    ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(16_000_000), FloatingLong.createConst(16_000)),
    ELITE(BaseTier.ELITE, FloatingLong.createConst(64_000_000), FloatingLong.createConst(64_000)),
    ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(256_000_000), FloatingLong.createConst(256_000)),
    CREATIVE(BaseTier.CREATIVE, FloatingLong.MAX_VALUE, FloatingLong.MAX_VALUE);

    private final FloatingLong baseMaxEnergy;
    private final FloatingLong baseOutput;
    private final BaseTier baseTier;
    private CachedFloatingLongValue storageReference;
    private CachedFloatingLongValue outputReference;

    EnergyCubeTier(BaseTier tier, FloatingLong max, FloatingLong out) {
        baseMaxEnergy = max;
        baseOutput = out;
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

    public FloatingLong getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.get();
    }

    public FloatingLong getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.get();
    }

    public FloatingLong getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    public FloatingLong getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the EnergyCubeTier a reference to the actual config value object
     */
    public void setConfigReference(CachedFloatingLongValue storageReference, CachedFloatingLongValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}