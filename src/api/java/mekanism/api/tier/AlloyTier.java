package mekanism.api.tier;

/**
 * Enum representing the different tiers of alloys.
 */
public enum AlloyTier implements ITier, IAlloyTier {
    INFUSED("infused", BaseTier.ADVANCED),
    REINFORCED("reinforced", BaseTier.ELITE),
    ATOMIC("atomic", BaseTier.ULTIMATE);

    private final BaseTier baseTier;
    private final String name;

    AlloyTier(String name, BaseTier base) {
        baseTier = base;
        this.name = name;
    }

    /**
     * Gets the name of this alloy tier.
     */
    public String getName() {
        return name;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public int getBaseTierLevel() {
        return ITier.super.getBaseTierLevel();
    }
}