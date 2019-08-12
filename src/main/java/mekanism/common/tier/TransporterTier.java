package mekanism.common.tier;

import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public enum TransporterTier implements ITier {
    BASIC(1, 5),
    ADVANCED(16, 10),
    ELITE(32, 20),
    ULTIMATE(64, 50);

    private final int basePull;
    private final int baseSpeed;
    private final BaseTier baseTier;
    private IntValue pullReference;
    private IntValue speedReference;

    TransporterTier(int pull, int s) {
        basePull = pull;
        baseSpeed = s;
        baseTier = BaseTier.values()[ordinal()];
    }

    public static TransporterTier get(BaseTier tier) {
        for (TransporterTier transmitter : values()) {
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

    public int getPullAmount() {
        return pullReference == null ? getBasePull() : pullReference.get();
    }

    public int getSpeed() {
        return speedReference == null ? getBaseSpeed() : speedReference.get();
    }

    public int getBasePull() {
        return basePull;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the TransporterTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue pullReference, IntValue speedReference) {
        this.pullReference = pullReference;
        this.speedReference = speedReference;
    }
}