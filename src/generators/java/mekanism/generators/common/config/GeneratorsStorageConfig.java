package mekanism.generators.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneratorsStorageConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedLongValue heatGenerator;
    public final CachedLongValue bioGenerator;
    public final CachedLongValue solarGenerator;
    public final CachedLongValue advancedSolarGenerator;
    public final CachedLongValue windGenerator;

    GeneratorsStorageConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Generator Energy Storage Config. This config is synced from server to client.").push("storage");

        heatGenerator = CachedLongValue.defineUnsigned(this, builder, "Base energy storage (Joules).", "heatGenerator",
              160_000L, 1);
        bioGenerator = CachedLongValue.defineUnsigned(this, builder, "Base energy storage (Joules).", "bioGenerator",
              160_000L, 1);
        solarGenerator = CachedLongValue.defineUnsigned(this, builder, "Base energy storage (Joules).", "solarGenerator",
              96_000L, 1);
        advancedSolarGenerator = CachedLongValue.defineUnsigned(this, builder, "Base energy storage (Joules).", "advancedSolarGenerator",
              200_000L, 1);
        windGenerator = CachedLongValue.defineUnsigned(this, builder, "Base energy storage (Joules).", "windGenerator",
              200_000L, 1);

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "generator-storage";
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