package mekanism.common.tier;

public enum FactoryTier implements ITier {
    BASIC(3),
    ADVANCED(5),
    ELITE(7),
    ULTIMATE(9);

    public final int processes;
    private final BaseTier baseTier;

    FactoryTier(int process) {
        processes = process;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }
}