package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ToolsConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedFloatValue armorSpawnChance;
    public final CachedFloatValue weaponSpawnChance;
    public final CachedFloatValue weaponSpawnChanceHard;
    public final ArmorSpawnChanceConfig bronzeSpawnRate;
    public final ArmorSpawnChanceConfig lapisLazuliSpawnRate;
    public final ArmorSpawnChanceConfig osmiumSpawnRate;
    public final ArmorSpawnChanceConfig refinedGlowstoneSpawnRate;
    public final ArmorSpawnChanceConfig refinedObsidianSpawnRate;
    public final ArmorSpawnChanceConfig steelSpawnRate;

    ToolsConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Mekanism Tools Config. This config is synced from server to client.").push("tools");

        builder.push("mobArmorSpawnRate");
        armorSpawnChance = CachedFloatValue.wrap(this, builder.comment("The chance that Mekanism Armor can spawn on mobs. This is multiplied modified by the chunk's difficulty modifier. "
                                                                       + "Vanilla uses 0.15 for its armor spawns, we use 0.1 as default to lower chances of mobs getting some vanilla and some mek armor.")
              .defineInRange("general", 0.1D, 0, 1));
        weaponSpawnChance = CachedFloatValue.wrap(this, builder.comment("The chance that Mekanism Weapons can spawn in a zombie's hand.")
              .defineInRange("weapon", 0.01D, 0, 1));
        weaponSpawnChanceHard = CachedFloatValue.wrap(this, builder.comment("The chance that Mekanism Weapons can spawn in a zombie's hand when on hard difficulty.")
              .defineInRange("weaponHard", 0.05F, 0, 1));
        bronzeSpawnRate = new ArmorSpawnChanceConfig(this, builder, "bronze", "Bronze");
        lapisLazuliSpawnRate = new ArmorSpawnChanceConfig(this, builder, "lapis_lazuli", "Lapis Lazuli");
        osmiumSpawnRate = new ArmorSpawnChanceConfig(this, builder, "osmium", "Osmium");
        refinedGlowstoneSpawnRate = new ArmorSpawnChanceConfig(this, builder, "refined_glowstone", "Refined Glowstone");
        refinedObsidianSpawnRate = new ArmorSpawnChanceConfig(this, builder, "refined_obsidian", "Refined Obsidian");
        steelSpawnRate = new ArmorSpawnChanceConfig(this, builder, "steel", "Steel");
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "tools";
    }

    @Override
    public String getTranslation() {
        return "General Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    public static class ArmorSpawnChanceConfig {

        public final CachedBooleanValue canSpawnWeapon;
        public final CachedFloatValue swordWeight;
        public final CachedFloatValue helmetChance;
        public final CachedFloatValue chestplateChance;
        public final CachedFloatValue leggingsChance;
        public final CachedFloatValue bootsChance;

        public final CachedFloatValue multiplePieceChance;
        public final CachedFloatValue multiplePieceChanceHard;

        public final CachedFloatValue weaponEnchantmentChance;
        public final CachedFloatValue armorEnchantmentChance;

        private ArmorSpawnChanceConfig(IMekanismConfig config, ModConfigSpec.Builder builder, String armorKey, String armor) {
            this(config, builder, armorKey, armor, 0.33, 1, 1, 1, 1, 0.25, 0.5);
        }

        private ArmorSpawnChanceConfig(IMekanismConfig config, ModConfigSpec.Builder builder, String armorKey, String armor, double swordChance, double helmetChance,
              double chestplateChance, double leggingsChance, double bootsChance, double weaponEnchantmentChance, double armorEnchantmentChance) {
            builder.comment("Spawn chances for pieces of " + armor + " gear. Note: These values are after the general mobArmorSpawnRate (or corresponding weapon rate) has been checked, "
                            + "and after an even split between material types has been done.").push(armorKey);
            this.canSpawnWeapon = CachedBooleanValue.wrap(config, builder.comment("Whether mobs can spawn with " + armor + " Weapons.")
                  .define("canSpawnWeapon", true));
            this.swordWeight = CachedFloatValue.wrap(config, builder.comment("The chance that mobs will spawn with " + armor + " Swords rather than " + armor + " Shovels.")
                  .defineInRange("swordWeight", swordChance, 0, 1));
            this.helmetChance = CachedFloatValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Helmets.")
                  .defineInRange("helmetChance", helmetChance, 0, 1));
            this.chestplateChance = CachedFloatValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Chestplates.")
                  .defineInRange("chestplateChance", chestplateChance, 0, 1));
            this.leggingsChance = CachedFloatValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Leggings.")
                  .defineInRange("leggingsChance", leggingsChance, 0, 1));
            this.bootsChance = CachedFloatValue.wrap(config, builder.comment("The chance that mobs can spawn with " + armor + " Boots.")
                  .defineInRange("bootsChance", bootsChance, 0, 1));

            this.multiplePieceChance = CachedFloatValue.wrap(config, builder.comment("The chance that after each piece of " + armor + " Armor a mob spawns with that no more pieces will be added. Order of pieces tried is boots, leggings, chestplate, helmet.")
                  .defineInRange("multiplePieceChance", 0.25, 0, 1));
            this.multiplePieceChanceHard = CachedFloatValue.wrap(config, builder.comment("The chance on hard mode that after each piece of " + armor + " Armor a mob spawns with that no more pieces will be added. Order of pieces tried is boots, leggings, chestplate, helmet.")
                  .defineInRange("multiplePieceChanceHard", 0.1, 0, 1));

            this.weaponEnchantmentChance = CachedFloatValue.wrap(config, builder.comment("The chance that if a mob spawns with " + armor + " Weapons that it will be enchanted. This is multiplied modified by the chunk's difficulty modifier.")
                  .defineInRange("weaponEnchantmentChance", weaponEnchantmentChance, 0, 1));
            this.armorEnchantmentChance = CachedFloatValue.wrap(config, builder.comment("The chance that if a mob spawns with " + armor + " Armor that they will be enchanted. This is multiplied modified by the chunk's difficulty modifier.")
                  .defineInRange("armorEnchantmentChance", armorEnchantmentChance, 0, 1));
            builder.pop();
        }
    }
}