package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.util.EnumUtils;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

//TODO: Should cable capacity be upped to a double
public enum CableTier implements ITier {
    BASIC(BaseTier.BASIC, 3_200),
    ADVANCED(BaseTier.ADVANCED, 12_800),
    ELITE(BaseTier.ELITE, 64_000),
    ULTIMATE(BaseTier.ULTIMATE, 320_000);

    private final int baseCapacity;
    private final BaseTier baseTier;
    private IntValue capacityReference;

    CableTier(BaseTier tier, int capacity) {
        baseCapacity = capacity;
        baseTier = tier;
    }

    public static CableTier get(BaseTier tier) {
        for (CableTier transmitter : EnumUtils.CABLE_TIERS) {
            if (transmitter.getBaseTier() == tier) {
                return transmitter;
            }
        }
        return BASIC;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getCableCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the CableTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}