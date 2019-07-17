package mekanism.common.tier;

public enum AlloyTier implements ITier {
    ENRICHED("Enriched", BaseTier.ADVANCED),
    REINFORCED("Reinforced", BaseTier.ELITE),
    ATOMIC("Atomic", BaseTier.ULTIMATE);

    private final BaseTier baseTier;
    private final String name;

    AlloyTier(String name, BaseTier base) {
        baseTier = base;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }
}