package mekanism.common.tier;

import java.util.Locale;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.IStringSerializable;

public enum GasTankTier implements ITier, IStringSerializable {
    BASIC(64000, 256),
    ADVANCED(128000, 512),
    ELITE(256000, 1028),
    ULTIMATE(512000, 2056),
    CREATIVE(Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

    private final int baseStorage;
    private final int baseOutput;
    private final BaseTier baseTier;

    GasTankTier(int s, int o) {
        baseStorage = s;
        baseOutput = o;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public int getStorage() {
        return MekanismConfig.current().general.tiers.get(baseTier).GasTankStorage.val();
    }

    public int getOutput() {
        return MekanismConfig.current().general.tiers.get(baseTier).GasTankOutput.val();
    }

    public int getBaseStorage() {
        return baseStorage;
    }

    public int getBaseOutput() {
        return baseOutput;
    }
}