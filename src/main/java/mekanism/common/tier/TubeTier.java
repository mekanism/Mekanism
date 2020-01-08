package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.util.EnumUtils;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public enum TubeTier implements ITier {
    BASIC(BaseTier.BASIC, 256, 64),
    ADVANCED(BaseTier.ADVANCED, 1_024, 256),
    ELITE(BaseTier.ELITE, 4_096, 1_024),
    ULTIMATE(BaseTier.ULTIMATE, 16_384, 4_096);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;
    private IntValue capacityReference;
    private IntValue pullReference;

    TubeTier(BaseTier tier, int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = tier;
    }

    public static TubeTier get(BaseTier tier) {
        for (TubeTier transmitter : EnumUtils.TUBE_TIERS) {
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

    public int getTubeCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public int getTubePullAmount() {
        return pullReference == null ? getBasePull() : pullReference.get();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getBasePull() {
        return basePull;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the TubeTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue capacityReference, IntValue pullReference) {
        this.capacityReference = capacityReference;
        this.pullReference = pullReference;
    }
}