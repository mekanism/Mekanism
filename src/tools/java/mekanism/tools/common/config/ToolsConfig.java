package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.material.impl.BronzeMaterialDefaults;
import mekanism.tools.common.material.impl.LapisLazuliMaterialDefaults;
import mekanism.tools.common.material.impl.OsmiumMaterialDefaults;
import mekanism.tools.common.material.impl.RefinedGlowstoneMaterialDefaults;
import mekanism.tools.common.material.impl.RefinedObsidianMaterialDefaults;
import mekanism.tools.common.material.impl.SteelMaterialDefaults;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ToolsConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedDoubleValue armorSpawnRate;
    public final BaseMekanismMaterial bronze;
    public final BaseMekanismMaterial lapisLazuli;
    public final BaseMekanismMaterial osmium;
    public final BaseMekanismMaterial refinedGlowstone;
    public final BaseMekanismMaterial refinedObsidian;
    public final BaseMekanismMaterial steel;

    ToolsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Tools Config. This config is synced from server to client.").push("tools");

        armorSpawnRate = CachedDoubleValue.wrap(this, builder.comment("The chance that Mekanism Armor can spawn on mobs.")
              .defineInRange("mobArmorSpawnRate", 0.03D, 0, 1));

        bronze = new MaterialCreator(this, builder, new BronzeMaterialDefaults());
        lapisLazuli = new MaterialCreator(this, builder, new LapisLazuliMaterialDefaults());
        osmium = new MaterialCreator(this, builder, new OsmiumMaterialDefaults());
        refinedGlowstone = new MaterialCreator(this, builder, new RefinedGlowstoneMaterialDefaults());
        refinedObsidian = new MaterialCreator(this, builder, new RefinedObsidianMaterialDefaults());
        steel = new MaterialCreator(this, builder, new SteelMaterialDefaults());
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "tools";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}