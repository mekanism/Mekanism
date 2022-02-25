package mekanism.generators.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneratorsStorageConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedFloatingLongValue heatGenerator;
    public final CachedFloatingLongValue bioGenerator;
    public final CachedFloatingLongValue solarGenerator;
    public final CachedFloatingLongValue advancedSolarGenerator;
    public final CachedFloatingLongValue windGenerator;

    GeneratorsStorageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Generator Energy Storage Config. This config is synced from server to client.").push("storage");

        heatGenerator = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "heatGenerator",
              FloatingLong.createConst(160_000));
        bioGenerator = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "bioGenerator",
              FloatingLong.createConst(160_000));
        solarGenerator = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "solarGenerator",
              FloatingLong.createConst(96_000));
        advancedSolarGenerator = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "advancedSolarGenerator",
              FloatingLong.createConst(200_000));
        windGenerator = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "windGenerator",
              FloatingLong.createConst(200_000));

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "generator-storage";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    @Override
    public boolean addToContainer() {
        return false;
    }
}