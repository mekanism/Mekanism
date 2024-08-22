package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.tools.common.config.ToolsConfigTranslations.ArmorSpawnChanceTranslations;
import mekanism.tools.common.material.MaterialCreator;
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

        ToolsConfigTranslations.SERVER_GEAR_SPAWN_CHANCE.applyToBuilder(builder).push("mobGearSpawnRate");
        armorSpawnChance = CachedFloatValue.wrap(this, ToolsConfigTranslations.SERVER_GEAR_SPAWN_CHANCE_ARMOR.applyToBuilder(builder)
              .defineInRange("general", 0.1D, 0, 1));
        weaponSpawnChance = CachedFloatValue.wrap(this, ToolsConfigTranslations.SERVER_GEAR_SPAWN_CHANCE_WEAPON.applyToBuilder(builder)
              .defineInRange("weapon", 0.01D, 0, 1));
        weaponSpawnChanceHard = CachedFloatValue.wrap(this, ToolsConfigTranslations.SERVER_GEAR_SPAWN_CHANCE_WEAPON_HARD.applyToBuilder(builder)
              .defineInRange("weaponHard", 0.05F, 0, 1));

        bronzeSpawnRate = new ArmorSpawnChanceConfig(this, builder, MekanismToolsConfig.materials.bronze);
        lapisLazuliSpawnRate = new ArmorSpawnChanceConfig(this, builder, MekanismToolsConfig.materials.lapisLazuli);
        osmiumSpawnRate = new ArmorSpawnChanceConfig(this, builder, MekanismToolsConfig.materials.osmium);
        refinedGlowstoneSpawnRate = new ArmorSpawnChanceConfig(this, builder, MekanismToolsConfig.materials.refinedGlowstone);
        refinedObsidianSpawnRate = new ArmorSpawnChanceConfig(this, builder, MekanismToolsConfig.materials.refinedObsidian);
        steelSpawnRate = new ArmorSpawnChanceConfig(this, builder, MekanismToolsConfig.materials.steel);
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

        private ArmorSpawnChanceConfig(IMekanismConfig config, ModConfigSpec.Builder builder, MaterialCreator material) {
            this(config, builder, material.getRegistryPrefix(), 0.33, 1, 1, 1, 1, 0.25, 0.5);
        }

        private ArmorSpawnChanceConfig(IMekanismConfig config, ModConfigSpec.Builder builder, String key, double swordChance, double helmetChance,
              double chestplateChance, double leggingsChance, double bootsChance, double weaponEnchantmentChance, double armorEnchantmentChance) {
            ArmorSpawnChanceTranslations translations = ArmorSpawnChanceTranslations.create(key);
            translations.topLevel().applyToBuilder(builder).push(key);
            this.canSpawnWeapon = CachedBooleanValue.wrap(config, translations.canSpawnWeapon().applyToBuilder(builder)
                  .define("canSpawnWeapon", true));
            this.swordWeight = CachedFloatValue.wrap(config, translations.swordWeight().applyToBuilder(builder)
                  .defineInRange("swordWeight", swordChance, 0, 1));
            this.helmetChance = CachedFloatValue.wrap(config, translations.helmetChance().applyToBuilder(builder)
                  .defineInRange("helmetChance", helmetChance, 0, 1));
            this.chestplateChance = CachedFloatValue.wrap(config, translations.chestplateChance().applyToBuilder(builder)
                  .defineInRange("chestplateChance", chestplateChance, 0, 1));
            this.leggingsChance = CachedFloatValue.wrap(config, translations.leggingsChance().applyToBuilder(builder)
                  .defineInRange("leggingsChance", leggingsChance, 0, 1));
            this.bootsChance = CachedFloatValue.wrap(config, translations.bootsChance().applyToBuilder(builder)
                  .defineInRange("bootsChance", bootsChance, 0, 1));

            this.multiplePieceChance = CachedFloatValue.wrap(config, translations.multiplePieceChance().applyToBuilder(builder)
                  .defineInRange("multiplePieceChance", 0.25, 0, 1));
            this.multiplePieceChanceHard = CachedFloatValue.wrap(config, translations.multiplePieceChanceHard().applyToBuilder(builder)
                  .defineInRange("multiplePieceChanceHard", 0.1, 0, 1));

            this.weaponEnchantmentChance = CachedFloatValue.wrap(config, translations.weaponEnchantmentChance().applyToBuilder(builder)
                  .defineInRange("weaponEnchantmentChance", weaponEnchantmentChance, 0, 1));
            this.armorEnchantmentChance = CachedFloatValue.wrap(config, translations.armorEnchantmentChance().applyToBuilder(builder)
                  .defineInRange("armorEnchantmentChance", armorEnchantmentChance, 0, 1));
            builder.pop();
        }
    }
}