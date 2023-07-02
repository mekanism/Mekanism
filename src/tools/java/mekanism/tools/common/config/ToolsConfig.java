package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import mekanism.tools.common.material.impl.BronzeMaterialDefaults;
import mekanism.tools.common.material.impl.LapisLazuliMaterialDefaults;
import mekanism.tools.common.material.impl.OsmiumMaterialDefaults;
import mekanism.tools.common.material.impl.RefinedGlowstoneMaterialDefaults;
import mekanism.tools.common.material.impl.RefinedObsidianMaterialDefaults;
import mekanism.tools.common.material.impl.SteelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.DiamondPaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.GoldPaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.IronPaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.NetheritePaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.StonePaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.WoodPaxelMaterialDefaults;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ToolsConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedFloatValue armorSpawnChance;
    public final CachedFloatValue weaponSpawnChance;
    public final CachedFloatValue weaponSpawnChanceHard;
    public final ArmorSpawnChanceConfig bronzeSpawnRate;
    public final ArmorSpawnChanceConfig lapisLazuliSpawnRate;
    public final ArmorSpawnChanceConfig osmiumSpawnRate;
    public final ArmorSpawnChanceConfig refinedGlowstoneSpawnRate;
    public final ArmorSpawnChanceConfig refinedObsidianSpawnRate;
    public final ArmorSpawnChanceConfig steelSpawnRate;
    public final VanillaPaxelMaterialCreator wood;
    public final VanillaPaxelMaterialCreator stone;
    public final VanillaPaxelMaterialCreator iron;
    public final VanillaPaxelMaterialCreator diamond;
    public final VanillaPaxelMaterialCreator gold;
    public final VanillaPaxelMaterialCreator netherite;
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

        wood = new VanillaPaxelMaterialCreator(this, builder, new WoodPaxelMaterialDefaults());
        stone = new VanillaPaxelMaterialCreator(this, builder, new StonePaxelMaterialDefaults());
        iron = new VanillaPaxelMaterialCreator(this, builder, new IronPaxelMaterialDefaults());
        diamond = new VanillaPaxelMaterialCreator(this, builder, new DiamondPaxelMaterialDefaults());
        gold = new VanillaPaxelMaterialCreator(this, builder, new GoldPaxelMaterialDefaults());
        netherite = new VanillaPaxelMaterialCreator(this, builder, new NetheritePaxelMaterialDefaults());

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

        private ArmorSpawnChanceConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, String armorKey, String armor) {
            this(config, builder, armorKey, armor, 0.33, 1, 1, 1, 1, 0.25, 0.5);
        }

        private ArmorSpawnChanceConfig(IMekanismConfig config, ForgeConfigSpec.Builder builder, String armorKey, String armor, double swordChance, double helmetChance,
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