package mekanism.tools.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.common.util.text.TextUtils;
import mekanism.tools.common.MekanismTools;
import org.jspecify.annotations.Nullable;

public enum ToolsConfigTranslations implements IConfigTranslation {
    //Client config
    CLIENT_DURABILITY_TOOLTIPS("client.durability_tooltips", "Durability Tooltips", "Enable durability tooltips for gear provided by Mekanism: Tools."),

    //Startup config
    STARTUP_MATERIALS("startup.materials", "Material Settings",
          "Settings for configuring Mekanism: Tools' material settings. This config is not synced automatically between client and server. It is highly "
          + "recommended to ensure you are using the same values for this config on the server and client.", true),

    //Server config
    SERVER_GEAR_SPAWN_CHANCE("server.gear_spawn_chance", "Mob Gear Spawn Chance",
          "Settings for configuring the spawn chance of Mekanism: Tools' gear on mobs", "Edit Gear Spawn Chance"),
    SERVER_GEAR_SPAWN_CHANCE_ARMOR("server.gear_spawn_chance.armor", "Armor Chance",
          "The chance that Mekanism: Tools' armor can spawn on mobs. This is multiplied modified by the chunk's difficulty modifier. "
          + "Vanilla uses 0.15 for its armor spawns, we use 0.1 as default to lower chances of mobs getting some vanilla and some mek armor."),
    SERVER_GEAR_SPAWN_CHANCE_WEAPON("server.gear_spawn_chance.weapon", "Weapon Chance", "The chance that Mekanism: Tools' weapons can spawn in a zombie's hand."),
    SERVER_GEAR_SPAWN_CHANCE_WEAPON_HARD("server.gear_spawn_chance.weapon.hard", "Weapon Chance, Hard",
          "The chance that Mekanism: Tools' weapons can spawn in a zombie's hand when on hard difficulty."),
    ;

    private final String key;
    private final String title;
    private final String tooltip;
    @Nullable
    private final String button;

    ToolsConfigTranslations(String path, String title, String tooltip) {
        this(path, title, tooltip, false);
    }

    ToolsConfigTranslations(String path, String title, String tooltip, boolean isSection) {
        this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection));
    }

    ToolsConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
        this.key = MekanismTools.rl(path).toLanguageKey("configuration");
        this.title = title;
        this.tooltip = tooltip;
        this.button = button;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }

    @Nullable
    @Override
    public String button() {
        return button;
    }

    public record ArmorSpawnChanceTranslations(
          IConfigTranslation topLevel,
          IConfigTranslation canSpawnWeapon,
          IConfigTranslation swordWeight,
          IConfigTranslation helmetChance,
          IConfigTranslation chestplateChance,
          IConfigTranslation leggingsChance,
          IConfigTranslation bootsChance,

          IConfigTranslation multiplePieceChance,
          IConfigTranslation multiplePieceChanceHard,

          IConfigTranslation weaponEnchantmentChance,
          IConfigTranslation armorEnchantmentChance
    ) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, canSpawnWeapon, swordWeight,
                                            helmetChance, chestplateChance, leggingsChance, bootsChance,
                                            multiplePieceChance, multiplePieceChanceHard,
                                            weaponEnchantmentChance, armorEnchantmentChance
            };
        }

        private static String getKey(String name, String path) {
            return MekanismTools.rl("server.gear_spawn_chance." + name + "." + path).toLanguageKey("configuration");
        }

        public static ArmorSpawnChanceTranslations create(String registryPrefix) {
            String name = TextUtils.formatAndCapitalize(registryPrefix);
            return new ArmorSpawnChanceTranslations(
                  new ConfigTranslation(getKey(registryPrefix, "top_level"), name + " Gear",
                        "Spawn chances for pieces of " + name + " gear. Note: These values are after the general mobArmorSpawnRate (or corresponding weapon rate) "
                        + "has been checked, and after an even split between material types has been done.",
                        "Edit Spawn Chance"
                  ),
                  new ConfigTranslation(getKey(registryPrefix, "spawn_weapon"), "With Weapon", "If enabled, zombies can spawn with " + name + " weapons."),
                  new ConfigTranslation(getKey(registryPrefix, "sword_weight"), "Sword Weight",
                        "The chance that mobs will spawn with " + name + " swords rather than " + name + " shovels. Requires canSpawnWeapon to be enabled."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.helmet"), "Helmet Chance", "The chance that mobs can spawn with " + name + " helmets."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.chestplate"), "Chestplate Chance", "The chance that mobs can spawn with " + name + " chestplates."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.leggings"), "Leggings Chance", "The chance that mobs can spawn with " + name + " leggings."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.boots"), "Boots Chance", "The chance that mobs can spawn with " + name + " boots."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.multiple_piece"), "Multiple Pieces Chance",
                        "The chance that after each piece of " + name + " armor a mob spawns with no more pieces will be added. Order of pieces tried is boots, "
                        + "leggings, chestplate, helmet."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.multiple_piece.hard"), "Multiple Pieces Chance Hard",
                        "The chance on hard mode that after each piece of " + name + " armor a mob spawns with no more pieces will be added. Order of pieces tried "
                        + "is boots, leggings, chestplate, helmet."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.enchantment.weapon"), "Weapon Enchantment Chance",
                        "The chance that if a mob spawns with " + name + " weapons that it will be enchanted. This is multiplied modified by the chunk's difficulty "
                        + "modifier. Requires canSpawnWeapon to be enabled."),
                  new ConfigTranslation(getKey(registryPrefix, "chance.enchantment.armor"), "Armor Enchantment Chance",
                        "The chance that if a mob spawns with " + name + " armor that they will be enchanted. This is multiplied modified by the chunk's difficulty modifier.")
            );
        }
    }

    public record VanillaPaxelMaterialTranslations(
          IConfigTranslation topLevel,
          IConfigTranslation damage,
          IConfigTranslation attackSpeed,
          IConfigTranslation efficiency,
          IConfigTranslation enchantability,
          IConfigTranslation durability
    ) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, damage, attackSpeed, efficiency, enchantability, durability};
        }

        private static String getKey(String name, String path) {
            return MekanismTools.rl("startup.materials." + name + "." + path).toLanguageKey("configuration");
        }

        public static VanillaPaxelMaterialTranslations create(String registryPrefix) {
            String name = TextUtils.formatAndCapitalize(registryPrefix);
            return new VanillaPaxelMaterialTranslations(
                  new ConfigTranslation(getKey(registryPrefix, "top_level"), name + " Paxels", "Vanilla Material Paxel Settings for " + name + ".",
                        "Edit " + name + " Settings"
                  ),
                  new ConfigTranslation(getKey(registryPrefix, "damage"), "Damage", "Attack damage modifier of " + name + " paxels. Must be greater than or equal to negative Base Damage."),
                  new ConfigTranslation(getKey(registryPrefix, "attack_speed"), "Attack Speed", "Attack speed of " + name + " paxels."),
                  new ConfigTranslation(getKey(registryPrefix, "efficiency"), "Efficiency", "Efficiency of " + name + " paxels."),
                  new ConfigTranslation(getKey(registryPrefix, "enchantability"), "Enchantability", "Natural enchantability factor of " + name + " paxels."),
                  new ConfigTranslation(getKey(registryPrefix, "durability"), "Durability", "Maximum durability of " + name + " paxels.")
            );
        }
    }

    public record MaterialTranslations(
          IConfigTranslation topLevel,
          IConfigTranslation toolDurability,
          IConfigTranslation efficiency,
          IConfigTranslation damage,
          IConfigTranslation enchantability,
          IConfigTranslation swordDamage, IConfigTranslation swordAtkSpeed,
          IConfigTranslation shovelDamage, IConfigTranslation shovelAtkSpeed,
          IConfigTranslation axeDamage, IConfigTranslation axeAtkSpeed,
          IConfigTranslation pickaxeDamage, IConfigTranslation pickaxeAtkSpeed,
          IConfigTranslation hoeDamage, IConfigTranslation hoeAtkSpeed,
          //Paxels
          IConfigTranslation paxelDamage, IConfigTranslation paxelAtkSpeed, IConfigTranslation paxelEfficiency, IConfigTranslation paxelEnchantability,
          IConfigTranslation paxelDurability,
          //Spears
          IConfigTranslation spearAttackDuration, IConfigTranslation spearDamageMultiplier, IConfigTranslation spearDelay, IConfigTranslation spearDismountTime,
          IConfigTranslation spearDismountThreshold, IConfigTranslation spearKnockbackTime, IConfigTranslation spearKnockbackThreshold,
          IConfigTranslation spearDamageTime, IConfigTranslation spearDamageThreshold,
          //Shields
          IConfigTranslation shieldDurability, IConfigTranslation shieldBlockDelay, IConfigTranslation shieldDisableCooldownScale,
          IConfigTranslation shieldHorizontalBlockingAngle, IConfigTranslation shieldDamageReductionBase, IConfigTranslation shieldDamageReductionFactor,
          IConfigTranslation shieldDamageThreshold, IConfigTranslation shieldItemDamageBase, IConfigTranslation shieldItemDamageFactor,
          //Armor
          IConfigTranslation toughness, IConfigTranslation knockbackResistance, IConfigTranslation armorEnchantability,
          IConfigTranslation bootDurability, IConfigTranslation bootArmor,
          IConfigTranslation leggingDurability, IConfigTranslation leggingArmor,
          IConfigTranslation chestplateDurability, IConfigTranslation chestplateArmor,
          IConfigTranslation helmetDurability, IConfigTranslation helmetArmor
    ) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, toolDurability, efficiency, damage, enchantability,
                                            swordDamage, swordAtkSpeed,
                                            shovelDamage, shovelAtkSpeed,
                                            axeDamage, axeAtkSpeed,
                                            pickaxeDamage, pickaxeAtkSpeed,
                                            hoeDamage, hoeAtkSpeed,
                                            paxelDamage, paxelAtkSpeed, paxelEfficiency, paxelEnchantability, paxelDurability,
                                            spearAttackDuration, spearDamageMultiplier, spearDelay, spearDismountTime, spearDismountThreshold, spearKnockbackTime,
                                            spearKnockbackThreshold, spearDamageTime, spearDamageThreshold,
                                            shieldDurability, shieldBlockDelay, shieldDisableCooldownScale, shieldHorizontalBlockingAngle, shieldDamageReductionBase,
                                            shieldDamageReductionFactor, shieldDamageThreshold, shieldItemDamageBase, shieldItemDamageFactor,
                                            toughness, knockbackResistance, armorEnchantability,
                                            bootDurability, bootArmor,
                                            leggingDurability, leggingArmor,
                                            chestplateDurability, chestplateArmor,
                                            helmetDurability, helmetArmor
            };
        }

        private static String getKey(String name, String path) {
            return MekanismTools.rl("startup.materials." + name + "." + path).toLanguageKey("configuration");
        }

        public static MaterialTranslations create(String registryPrefix) {
            String name = TextUtils.formatAndCapitalize(registryPrefix);
            return new MaterialTranslations(
                  new ConfigTranslation(getKey(registryPrefix, "top_level"), name + " Gear", "Material Settings for " + name, "Edit " + name + " Settings"),
                  new ConfigTranslation(getKey(registryPrefix, "durability.tool"), "Tool Durability", "Maximum durability of " + name + " tools."),
                  new ConfigTranslation(getKey(registryPrefix, "efficiency"), "Efficiency", "Efficiency of " + name + " tools."),
                  new ConfigTranslation(getKey(registryPrefix, "damage"), "Base Damage", "Base attack damage of " + name + " items."),
                  new ConfigTranslation(getKey(registryPrefix, "enchantability"), "Enchantability", "Natural enchantability factor of " + name + " items."),
                  //Swords
                  new ConfigTranslation(getKey(registryPrefix, "damage.sword"), "Sword Damage", "Attack damage modifier of " + name + " swords. Must be less than or equal to Base Damage."),
                  new ConfigTranslation(getKey(registryPrefix, "attack_speed.sword"), "Sword Attack Speed", "Attack speed of " + name + " swords."),
                  //Shovels
                  new ConfigTranslation(getKey(registryPrefix, "damage.shovel"), "Shovel Damage", "Attack damage modifier of " + name + " shovels. Must be greater than or equal to negative Base Damage."),
                  new ConfigTranslation(getKey(registryPrefix, "attack_speed.shovel"), "Shovel Attack Speed", "Attack speed of " + name + " shovels."),
                  //Axes
                  new ConfigTranslation(getKey(registryPrefix, "axe_damage"), "Axe Damage", "Attack damage modifier of " + name + " axes."),
                  new ConfigTranslation(getKey(registryPrefix, "attack_speed.axe"), "Axe Attack Speed", "Attack speed of " + name + " axes."),
                  //Pickaxes
                  new ConfigTranslation(getKey(registryPrefix, "damage.pickaxe"), "Pickaxe Damage", "Attack damage modifier of " + name + " pickaxes. Must be greater than or equal to negative Base Damage."),
                  new ConfigTranslation(getKey(registryPrefix, "attack_speed.pickaxe"), "Pickaxe Attack Speed", "Attack speed of " + name + " pickaxes."),
                  //Hoes
                  new ConfigTranslation(getKey(registryPrefix, "damage.hoe"), "Hoe Damage", "Attack damage modifier of " + name + " hoes. Must be greater than or equal to negative Base Damage."),
                  new ConfigTranslation(getKey(registryPrefix, "attack_speed.hoe"), "Hoe Attack Speed", "Attack speed of " + name + " hoes."),
                  //Paxels
                  new ConfigTranslation(getKey(registryPrefix, "damage.paxel"), "Paxel Damage", "Attack damage modifier of " + name + " paxels. Must be greater than or equal to negative Base Damage."),
                  new ConfigTranslation(getKey(registryPrefix, "attack_speed.paxel"), "Paxel Attack Speed", "Attack speed of " + name + " paxels."),
                  new ConfigTranslation(getKey(registryPrefix, "efficiency.paxel"), "Paxel Efficiency", "Efficiency of " + name + " paxels."),
                  new ConfigTranslation(getKey(registryPrefix, "enchantability.paxel"), "Paxel Enchantability", "Natural enchantability factor of " + name + " paxels."),
                  new ConfigTranslation(getKey(registryPrefix, "durability.paxel"), "Paxel Durability", "Maximum durability of " + name + " paxels."),
                  //Spears
                  new ConfigTranslation(getKey(registryPrefix, "attack_duration.spear"), "Spear Attack Duration", "The duration that jab attacks with " + name + " spears take. The jab attack speed of a spear is equal to (1 / attack_duration) - 4"),
                  new ConfigTranslation(getKey(registryPrefix, "damage_multiplier.spear"), "Spear Damage Multiplier", "Amount to multiply the damage that was calculated from the relative speed of charge-type attacks performed by " + name + " spears."),
                  new ConfigTranslation(getKey(registryPrefix, "delay.spear"), "Spear Delay", "The time in seconds (rounded to the nearest number of ticks) " + name + " spears must be used to start being considered as performing a charge-type attack."),
                  new ConfigTranslation(getKey(registryPrefix, "dismount.time.spear"), "Spear Dismount Time", "The time in seconds (rounded to the nearest number of ticks) that " + name + " spears should be able to dismount targets once a charge-type attack has started."),
                  new ConfigTranslation(getKey(registryPrefix, "dismount.threshold.spear"), "Spear Dismount Threshold", "Minimum speed in blocks per second of the entity attacking with " + name + " spears to have the attack dismount the attacked entity."),
                  new ConfigTranslation(getKey(registryPrefix, "knockback.time.spear"), "Spear Knockback Time", "The time in seconds (rounded to the nearest number of ticks) that " + name + " spears should be able to knockback targets once a charge-type attack has started."),
                  new ConfigTranslation(getKey(registryPrefix, "knockback.threshold.spear"), "Spear Knockback Threshold", "Minimum speed in blocks per second of the entity attacking with " + name + " spears to have the attack knock back the attacked entity."),
                  new ConfigTranslation(getKey(registryPrefix, "damage.time.spear"), "Spear Damage Time", "The time in seconds (rounded to the nearest number of ticks) that " + name + " spears can perform charge-type attacks."),
                  new ConfigTranslation(getKey(registryPrefix, "damage.threshold.spear"), "Spear Damage Threshold", "Minimum relative speed in blocks per second to consider the entity attacking with " + name + " spears to be charging the attacked entity."),
                  //Shields
                  new ConfigTranslation(getKey(registryPrefix, "durability.shield"), "Shield Durability", "Maximum durability of " + name + " shields."),
                  new ConfigTranslation(getKey(registryPrefix, "block_delay.shield"), "Shield Block Delay", "The amount of time (in seconds) that " + name + " shields must be actively used before being able to block an attack."),
                  new ConfigTranslation(getKey(registryPrefix, "disable_cooldown_scale.shield"), "Shield Disable Cooldown Scale", "The multiplier applied to the cooldown time for " + name + " shields when attacked by a disabling attack (the multiplier for disable_blocking_for_seconds on the minecraft:weapon component). If set to 0, " + name + " shields can never be disabled by attacks."),
                  new ConfigTranslation(getKey(registryPrefix, "horizontal_blocking_angle.shield"), "Shield Blocking Angle", "The maximum horizontal angle between the " + name + " shield's user's facing direction and the direction of the incoming attack to be blocked."),
                  new ConfigTranslation(getKey(registryPrefix, "damage_reduction.base.shield"), "Shield Damage Reduction Base", "The base amount of damage " + name + " shields will block regardless of the amount of damage being dealt."),
                  new ConfigTranslation(getKey(registryPrefix, "damage_reduction.factor.shield"), "Shield Dmg Reduction Factor", "The fraction of damage dealt that " + name + " shields will attempt to block."),
                  new ConfigTranslation(getKey(registryPrefix, "damage_threshold.shield"), "Shield Damage Threshold", "The minimum amount of damage dealt by the attack before item damage is applied to " + name + " shields. Damage under this amount will be blocked without damaging the shield."),
                  new ConfigTranslation(getKey(registryPrefix, "item_damage.base.shield"), "Shield Item Damage Base", "The flat amount of item damage applied to " + name + " shields if their damage threshold is passed."),
                  new ConfigTranslation(getKey(registryPrefix, "item_damage.factor.shield"), "Shield Item Damage Factor", "The fraction of dealt damage that should be applied as item damage to " + name + " shields if their damage threshold is passed."),
                  //Armor
                  new ConfigTranslation(getKey(registryPrefix, "toughness"), "Armor Toughness", "Base armor toughness value of " + name + " armor."),
                  new ConfigTranslation(getKey(registryPrefix, "knockback_resistance"), "Knockback Resistance", "Base armor knockback resistance value of " + name + " armor."),
                  new ConfigTranslation(getKey(registryPrefix, "enchantability.armor"), "Armor Enchantability", "Natural enchantability factor of " + name + " armor."),
                  new ConfigTranslation(getKey(registryPrefix, "durability.boots"), "Boots Durability", "Maximum durability of " + name + " boots."),
                  new ConfigTranslation(getKey(registryPrefix, "armor.boots"), "Boots Armor", "Protection value of " + name + " boots."),
                  new ConfigTranslation(getKey(registryPrefix, "durability.leggings"), "Leggings Durability", "Maximum durability of " + name + " leggings."),
                  new ConfigTranslation(getKey(registryPrefix, "armor.leggings"), "Leggings Armor", "Protection value of " + name + " leggings."),
                  new ConfigTranslation(getKey(registryPrefix, "durability.chestplate"), "Chestplate Durability", "Maximum durability of " + name + " chestplates."),
                  new ConfigTranslation(getKey(registryPrefix, "armor.chestplate"), "Chestplate Armor", "Protection value of " + name + " chestplates."),
                  new ConfigTranslation(getKey(registryPrefix, "durability.helmet"), "Helmet Durability", "Maximum durability of " + name + " helmets."),
                  new ConfigTranslation(getKey(registryPrefix, "armor.helmet"), "Helmet Armor", "Protection value of " + name + " helmets.")
            );
        }
    }
}