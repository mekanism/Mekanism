package mekanism.common.tier;

import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public enum BinTier implements ITier {
    BASIC(4_096),
    ADVANCED(8_192),
    ELITE(32_768),
    ULTIMATE(262_144),
    CREATIVE(Integer.MAX_VALUE);

    private final int baseStorage;
    private final BaseTier baseTier;
    private IntValue storageReference;

    BinTier(int s) {
        baseStorage = s;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getStorage() {
        return storageReference == null ? getBaseStorage() : storageReference.get();
    }

    public int getBaseStorage() {
        return baseStorage;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the BinTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue storageReference) {
        this.storageReference = storageReference;
    }
}