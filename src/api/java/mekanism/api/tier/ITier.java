package mekanism.api.tier;

public interface ITier {

    /**
     * Gets the base tier version of this tiered object.
     */
    BaseTier getBaseTier();

    /**
     * Helper method to get the level of the base tier that this tier corresponds to.
     *
     * @since 10.7.11
     */
    default int getBaseTierLevel() {
        return getBaseTier().ordinal();
    }
}