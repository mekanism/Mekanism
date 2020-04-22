package mekanism.common.tier;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum InductionCellTier implements ITier {
    BASIC(BaseTier.BASIC, FloatingLong.createConst(8_000_000_000L)),
    ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(64_000_000_000L)),
    ELITE(BaseTier.ELITE, FloatingLong.createConst(512_000_000_000L)),
    ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(4_000_000_000_000L));

    private final FloatingLong baseMaxEnergy;
    private final BaseTier baseTier;
    private CachedFloatingLongValue storageReference;

    InductionCellTier(BaseTier tier, FloatingLong max) {
        baseMaxEnergy = max;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public FloatingLong getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.get();
    }

    public FloatingLong getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the InductionCellTier a reference to the actual config value object
     */
    public void setConfigReference(CachedFloatingLongValue storageReference) {
        this.storageReference = storageReference;
    }
}