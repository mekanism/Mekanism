package mekanism.common.config;

import mekanism.common.config.options.DoubleOption;
import mekanism.common.config.options.FloatOption;
import mekanism.common.config.options.IntOption;
import mekanism.common.util.FieldsAreNonnullByDefault;

/**
 * Created by Thiakil on 15/03/2019.
 */
@FieldsAreNonnullByDefault
public class ToolsConfig extends BaseConfig {

    public final DoubleOption armorSpawnRate = new DoubleOption(this, "tools.general", "MobArmorSpawnRate", 0.03,
          "The chance that Mekanism Armor can spawn on mobs.", 0.00, 1.00);

    public final ArmorBalance armorOBSIDIAN = new ArmorBalance(this, "obsidian", 50, 5, 8, 12, 5, 40, 4);
    public final ArmorBalance armorLAZULI = new ArmorBalance(this, "lapis", 13, 2, 6, 5, 2, 8, 0);
    public final ArmorBalance armorOSMIUM = new ArmorBalance(this, "osmium", 30, 3, 6, 5, 3, 12, 1);
    public final ArmorBalance armorBRONZE = new ArmorBalance(this, "bronze", 35, 2, 5, 6, 3, 10, 0);
    public final ArmorBalance armorGLOWSTONE = new ArmorBalance(this, "glowstone", 18, 3, 6, 7, 3, 18, 0);
    public final ArmorBalance armorSTEEL = new ArmorBalance(this, "steel", 40, 3, 6, 7, 3, 10, 1);

    public final ToolBalance toolOBSIDIAN = new ToolBalance(this, "obsidian", "regular", 3, 2500, 20, 10, 40, 12, -2);
    public final ToolBalance toolOBSIDIAN2 = new ToolBalance(this, "obsidian", "paxel", 3, 3000, 25, 10, 50);
    public final ToolBalance toolLAZULI = new ToolBalance(this, "lapis", "regular", 2, 200, 5, 2, 8, 8, -3.1F);
    public final ToolBalance toolLAZULI2 = new ToolBalance(this, "lapis", "paxel", 2, 250, 6, 4, 10);
    public final ToolBalance toolOSMIUM = new ToolBalance(this, "osmium", "regular", 2, 500, 10, 4, 12, 8, -3);
    public final ToolBalance toolOSMIUM2 = new ToolBalance(this, "osmium", "paxel", 3, 700, 12, 5, 16);
    public final ToolBalance toolBRONZE = new ToolBalance(this, "bronze", "regular", 2, 800, 14, 6, 10, 8, -3.1F);
    public final ToolBalance toolBRONZE2 = new ToolBalance(this, "bronze", "paxel", 3, 1100, 16, 10, 14);
    public final ToolBalance toolGLOWSTONE = new ToolBalance(this, "glowstone", "regular", 2, 300, 14, 5, 18, 8, -3.1F);
    public final ToolBalance toolGLOWSTONE2 = new ToolBalance(this, "glowstone", "paxel", 2, 450, 18, 5, 22);
    public final ToolBalance toolSTEEL = new ToolBalance(this, "steel", "regular", 3, 850, 14, 4, 10, 8, -3);
    public final ToolBalance toolSTEEL2 = new ToolBalance(this, "steel", "paxel", 3, 1250, 18, 8, 14);

    @FieldsAreNonnullByDefault
    public static class ArmorBalance {

        public final IntOption durability;
        public final IntOption feetProtection;
        public final IntOption legsProtection;
        public final IntOption chestProtection;
        public final IntOption headProtection;
        public final IntOption enchantability;
        public final FloatOption toughness;

        private ArmorBalance(BaseConfig owner, String toolKey, int durabilityDefault, int feetProtectionDefault, int legsProtectionDefault, int chestProtectionDefault,
              int headProtectionDefault, int enchantabilityDefault, float toughnessDefault) {
            final String category = "tools.armor-balance." + toolKey;
            final String protectionCategory = category + ".protection";
            this.durability = new IntOption(owner, category, "durability", durabilityDefault, "Base durability of " + toolKey + " armor.");
            String protectionComment = "Protection value of " + toolKey;
            this.feetProtection = new IntOption(owner, protectionCategory, "feet", feetProtectionDefault, protectionComment + " boots.");
            this.legsProtection = new IntOption(owner, protectionCategory, "legs", legsProtectionDefault, protectionComment + " leggings.");
            this.chestProtection = new IntOption(owner, protectionCategory, "chest", chestProtectionDefault, protectionComment + " chestplates.");
            this.headProtection = new IntOption(owner, protectionCategory, "head", headProtectionDefault, protectionComment + " helmets.");
            this.enchantability = new IntOption(owner, category, "enchantability", enchantabilityDefault, "Natural enchantability factor of " + toolKey + " armor.");
            this.toughness = new FloatOption(owner, category, "toughness", toughnessDefault, "Base armor toughness value of " + toolKey + " armor.");
            this.durability.setRequiresGameRestart(true);
            this.feetProtection.setRequiresGameRestart(true);
            this.legsProtection.setRequiresGameRestart(true);
            this.chestProtection.setRequiresGameRestart(true);
            this.headProtection.setRequiresGameRestart(true);
            this.enchantability.setRequiresGameRestart(true);
            this.toughness.setRequiresGameRestart(true);
        }
    }

    @FieldsAreNonnullByDefault
    public static class ToolBalance {

        public final IntOption harvestLevel;
        public final IntOption maxUses;
        public final FloatOption efficiency;
        public final IntOption damage;
        public final IntOption enchantability;
        public final FloatOption axeAttackDamage;
        public final FloatOption axeAttackSpeed;

        private ToolBalance(BaseConfig owner, String toolKey, String variant, int harvestLevelDefault, int maxUsesDefault, float efficiencyDefault, int damageDefault,
              int enchantabilityDefault, float axeAttackDamageDefault, float axeAttackSpeedDefault) {
            final String category = "tools.tool-balance." + toolKey + "." + variant;
            this.harvestLevel = new IntOption(owner, category, "harvestLevel", harvestLevelDefault, "Harvest level of " + toolKey + " tools.");
            this.maxUses = new IntOption(owner, category, "maxUses", maxUsesDefault, "Maximum durability of " + toolKey + " tools.");
            this.efficiency = new FloatOption(owner, category, "efficiency", efficiencyDefault, "Base speed of " + toolKey + ".");
            this.damage = new IntOption(owner, category, "damage", damageDefault, "Base attack damage of " + toolKey + ".");
            this.enchantability = new IntOption(owner, category, "enchantability", enchantabilityDefault, "Natural enchantability factor of " + toolKey + ".");

            if (variant.equals("regular")) {
                this.axeAttackDamage = new FloatOption(owner, category, "axeAttackDamage", axeAttackDamageDefault, "Base attack damage of a " + toolKey + " axe.");
                this.axeAttackSpeed = new FloatOption(owner, category, "axeAttackSpeed", axeAttackSpeedDefault, "Base attack speed of a " + toolKey + " axe.");
            } else {
                this.axeAttackDamage = new FloatOption(NULL_OWNER, "", "");
                this.axeAttackSpeed = new FloatOption(NULL_OWNER, "", "");
            }

            this.harvestLevel.setRequiresGameRestart(true);
            this.maxUses.setRequiresGameRestart(true);
            this.efficiency.setRequiresGameRestart(true);
            this.damage.setRequiresGameRestart(true);
            this.enchantability.setRequiresGameRestart(true);
            this.axeAttackDamage.setRequiresGameRestart(true);
            this.axeAttackSpeed.setRequiresGameRestart(true);
        }

        public ToolBalance(BaseConfig owner, String toolKey, String variant, int harvestLevelDefault, int maxUsesDefault, float efficiencyDefault, int damageDefault,
              int enchantabilityDefault) {
            this(owner, toolKey, variant, harvestLevelDefault, maxUsesDefault, efficiencyDefault, damageDefault, enchantabilityDefault, 0, 0);
            if (!variant.equals("paxel")) {
                throw new IllegalStateException("Wrong constructor, only paxel can use this");
            }
        }
    }
}