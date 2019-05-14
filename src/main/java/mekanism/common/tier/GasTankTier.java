package mekanism.common.tier;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.IStringSerializable;

public enum GasTankTier implements ITier<GasTankTier>, IStringSerializable {
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
        baseTier = BaseTier.get(ordinal());
    }

    public static GasTankTier getDefault() {
        return BASIC;
    }

    public static GasTankTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static GasTankTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public boolean hasNext() {
        return EnumUtils.hasNext(values(), ordinal());
    }

    @Nullable
    @Override
    public GasTankTier next() {
        return EnumUtils.nextValue(values(), ordinal());
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