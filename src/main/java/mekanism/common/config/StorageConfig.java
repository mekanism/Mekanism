package mekanism.common.config;

import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class StorageConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedLongValue laserAmplifier;
    public final CachedLongValue laserTractorBeam;
    public final CachedLongValue teleporter;
    public final CachedLongValue spsPort;

    StorageConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        //TODO - 26.2: Update these config values
        laserAmplifier = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_LASER_AMPLIFIER, "laserAmplifier",
              5_000_000L, 1);
        laserTractorBeam = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_TRACTOR_BEAM, "laserTractorBeam",
              5_000_000L, 1);
        teleporter = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_TELEPORTER, "teleporter",
              1_000L, 1);
        spsPort = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_SPS_PORT, "spsPort",
              1_000_000_000L, 1);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "machine-storage";
    }


    @Override
    public String getTranslation() {
        return "Storage Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}