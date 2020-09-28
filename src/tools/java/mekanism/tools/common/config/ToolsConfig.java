package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedDoubleValue;
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
    public final ArmorSpawnChanceConfig bronzeSpawnRate;
    public final ArmorSpawnChanceConfig lapisLazuliSpawnRate;
    public final ArmorSpawnChanceConfig osmiumSpawnRate;
    public final ArmorSpawnChanceConfig refinedGlowstoneSpawnRate;
    public final ArmorSpawnChanceConfig refinedObsidianSpawnRate;
    public final ArmorSpawnChanceConfig steelSpawnRate;
    public final MaterialCreator bronze;
    public final MaterialCreator lapisLazuli;
    public final MaterialCreator osmium;
    public final MaterialCreator refinedGlowstone;
    public final MaterialCreator refinedObsidian;
    public final MaterialCreator steel;

    ToolsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Tools Config. This config is synced from server to client.").push("tools");

        builder.push("mobArmorSpawnRate");
        armorSpawnRate = CachedDoubleValue.wrap(this, builder.comment("The chance that Mekanism Armor can spawn on mobs.")
              .defineInRange("general", 0.03D, 0, 1));
        bronzeSpawnRate = new ArmorSpawnChanceConfig(this, builder, "bronze", "Bronze", 0.5, 0.5, 0.5, 0.5, 0.5);
        lapisLazuliSpawnRate = new ArmorSpawnChanceConfig(this, builder, "lapis_lazuli", "Lapis Lazuli", 0.5, 0.5, 0.5, 0.5, 0.5);
        osmiumSpawnRate = new ArmorSpawnChanceConfig(this, builder, "osmium", "Osmium", 0.5, 0.5, 0.5, 0.5, 0.5);
        refinedGlowstoneSpawnRate = new ArmorSpawnChanceConfig(this, builder, "refined_glowstone", "Refined Glowstone", 0.5, 0.5, 0.5, 0.5, 0.5);
        refinedObsidianSpawnRate = new ArmorSpawnChanceConfig(this, builder, "refined_obsidian", "Refined Obsidian", 0.5, 0.5, 0.5, 0.5, 0.5);
        steelSpawnRate = new ArmorSpawnChanceConfig(this, builder, "steel", "Steel", 0.5, 0.5, 0.5, 0.5, 0.5);
        builder.pop();

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

    public static class ArmorSpawnChanceConfig {

        public final CachedDoubleValue swordChance;
        public final CachedDoubleValue helmetChance;
        public final CachedDoubleValue chestplateChance;
        public final CachedDoubleValue leggingsChance;
        public final CachedDoubleValue bootsChance;

        private ArmorSpawnChanceConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, String armorKey, String armor, double swordChance, double helmetChance,
              double chestplateChance, double leggingsChance, double bootsChance) {
            builder.comment("Spawn chances for pieces of " + armor + " gear. Note: These values are after the general mobArmorSpawnRate has been checked, and " +
                  "after an even split between armor types has been done.").push(armorKey);
            this.swordChance = CachedDoubleValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Swords.")
                  .defineInRange("swordChance", swordChance, 0, 1));
            this.helmetChance = CachedDoubleValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Helmets.")
                  .defineInRange("helmetChance", helmetChance, 0, 1));
            this.chestplateChance = CachedDoubleValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Chestplates.")
                  .defineInRange("chestplateChance", chestplateChance, 0, 1));
            this.leggingsChance = CachedDoubleValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Leggings.")
                  .defineInRange("leggingsChance", leggingsChance, 0, 1));
            this.bootsChance = CachedDoubleValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Boots.")
                  .defineInRange("bootsChance", bootsChance, 0, 1));
            builder.pop();
        }
    }
}