package mekanism.common.tier;

public enum  AlloyTier implements ITier {
    ENRICHED(BaseTier.ADVANCED),
    REINFORCED(BaseTier.ELITE),
    ATOMIC(BaseTier.ULTIMATE);

    private final BaseTier baseTier;

    AlloyTier(BaseTier base) {
        baseTier = base;
    }
    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }
}