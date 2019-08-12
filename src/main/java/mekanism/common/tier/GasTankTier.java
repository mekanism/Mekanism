package mekanism.common.tier;

import java.util.Locale;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public enum GasTankTier implements ITier, IStringSerializable {
    BASIC(64000, 256),
    ADVANCED(128000, 512),
    ELITE(256000, 1028),
    ULTIMATE(512000, 2056),
    CREATIVE(Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

    private final int baseStorage;
    private final int baseOutput;
    private final BaseTier baseTier;
    private IntValue storageReference;
    private IntValue outputReference;

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
        return storageReference == null ? getBaseStorage() : storageReference.get();
    }

    public int getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.get();
    }

    public int getBaseStorage() {
        return baseStorage;
    }

    public int getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the GasTankTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue storageReference, IntValue outputReference) {
        this.storageReference = storageReference;
        this.outputReference = outputReference;
    }
}