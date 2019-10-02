package mekanism.common.tier;

import mekanism.common.util.EnumUtils;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public enum PipeTier implements ITier {
    BASIC(BaseTier.BASIC, 1_000, 100),
    ADVANCED(BaseTier.ADVANCED, 4_000, 400),
    ELITE(BaseTier.ELITE, 16_000, 1_600),
    ULTIMATE(BaseTier.ULTIMATE, 64_000, 6_400);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;
    private IntValue capacityReference;
    private IntValue pullReference;

    PipeTier(BaseTier tier, int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = tier;
    }

    public static PipeTier get(BaseTier tier) {
        for (PipeTier transmitter : EnumUtils.PIPE_TIERS) {
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

    public int getPipeCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public int getPipePullAmount() {
        return pullReference == null ? getBaseCapacity() : pullReference.get();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getBasePull() {
        return basePull;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the PipeTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue capacityReference, IntValue pullReference) {
        this.capacityReference = capacityReference;
        this.pullReference = pullReference;
    }
}