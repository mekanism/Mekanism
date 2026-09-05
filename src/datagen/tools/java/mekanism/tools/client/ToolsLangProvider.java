package mekanism.tools.client;

import java.util.Objects;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.lang.BaseLanguageProvider;
import mekanism.common.Mekanism;
import mekanism.common.util.EnumUtils;
import mekanism.tools.client.recipe_viewer.aliases.ToolsAliases;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.advancements.ToolsAdvancements;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.config.ToolsConfigTranslations;
import mekanism.tools.common.config.ToolsConfigTranslations.ArmorSpawnChanceTranslations;
import mekanism.tools.common.config.ToolsConfigTranslations.MaterialTranslations;
import mekanism.tools.common.config.ToolsConfigTranslations.VanillaPaxelMaterialTranslations;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import mekanism.tools.common.registration.ArmorCollection;
import mekanism.tools.common.registration.ToolCollection;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.data.PackOutput;

public class ToolsLangProvider extends BaseLanguageProvider {

    public ToolsLangProvider(PackOutput output) {
        super(output, MekanismTools.MODID, Objects.requireNonNull(MekanismTools.instance));
    }

    @Override
    protected void addTranslations() {
        addConfigs();
        addTags();
        addItems();
        addAdvancements();
        addMisc();
        addAliases(ToolsAliases.values());
    }

    private void addConfigs() {
        addConfigs(MekanismToolsConfig.getConfigs());
        addConfigs(ToolsConfigTranslations.values());
        //Vanilla paxels
        addConfigs(MekanismToolsConfig.materials.wood);
        addConfigs(MekanismToolsConfig.materials.stone);
        addConfigs(MekanismToolsConfig.materials.iron);
        addConfigs(MekanismToolsConfig.materials.diamond);
        addConfigs(MekanismToolsConfig.materials.gold);
        addConfigs(MekanismToolsConfig.materials.netherite);
        //Mekanism materials
        addConfigs(MekanismToolsConfig.materials.bronze);
        addConfigs(MekanismToolsConfig.materials.lapisLazuli);
        addConfigs(MekanismToolsConfig.materials.osmium);
        addConfigs(MekanismToolsConfig.materials.refinedGlowstone);
        addConfigs(MekanismToolsConfig.materials.refinedObsidian);
        addConfigs(MekanismToolsConfig.materials.steel);
    }

    private void addConfigs(VanillaPaxelMaterialCreator config) {
        addConfigs(VanillaPaxelMaterialTranslations.create(config.getRegistryPrefix()).toArray());
    }

    private void addConfigs(MaterialCreator config) {
        addConfigs(MaterialTranslations.create(config.getRegistryPrefix()).toArray());
        addConfigs(ArmorSpawnChanceTranslations.create(config.getRegistryPrefix()).toArray());
    }

    private void addTags() {
        add(ToolsTags.Items.TOOLS_PAXEL, "Paxels");
    }

    private void addItems() {
        //Vanilla Paxels
        add(ToolsItems.WOOD_PAXEL, "Wood Paxel");
        add(ToolsItems.STONE_PAXEL, "Stone Paxel");
        add(ToolsItems.IRON_PAXEL, "Iron Paxel");
        add(ToolsItems.GOLD_PAXEL, "Gold Paxel");
        add(ToolsItems.DIAMOND_PAXEL, "Diamond Paxel");
        add(ToolsItems.NETHERITE_PAXEL, "Netherite Paxel");
        //Tool sets
        addSet("Bronze", ToolsItems.BRONZE_ARMOR, ToolsItems.BRONZE_TOOLS);
        addSet("Lapis Lazuli", ToolsItems.LAPIS_LAZULI_ARMOR, ToolsItems.LAPIS_LAZULI_TOOLS);
        addSet("Osmium", ToolsItems.OSMIUM_ARMOR, ToolsItems.OSMIUM_TOOLS);
        addSet("Refined Glowstone", ToolsItems.REFINED_GLOWSTONE_ARMOR, ToolsItems.REFINED_GLOWSTONE_TOOLS);
        addSet("Refined Obsidian", ToolsItems.REFINED_OBSIDIAN_ARMOR, ToolsItems.REFINED_OBSIDIAN_TOOLS);
        addSet("Steel", ToolsItems.STEEL_ARMOR, ToolsItems.STEEL_TOOLS);
    }

    private void addAdvancements() {
        add(ToolsAdvancements.PAXEL, "Multi-Tool", "Craft any Paxel (Pickaxe, Axe, Shovel)");
        add(ToolsAdvancements.ALTERNATE_ARMOR, "More Armor Types!", "Craft any piece of Armor from " + basicModName);
        add(ToolsAdvancements.ALTERNATE_TOOLS, "More Tool Types!", "Craft any tool or weapon (except Paxels) from " + basicModName);
        add(ToolsAdvancements.NOT_ENOUGH_SHIELDING, "Not Enough Shielding", "Craft any Shield added by " + basicModName);
        add(ToolsAdvancements.BETTER_THAN_NETHERITE, "Better Than Netherite", "Protect yourself with a piece of Refined Obsidian Armor");
        add(ToolsAdvancements.LOVED_BY_PIGLINS, "Loved By Piglins", "Refined Glowstone Armor glows even brighter than gold!");
    }

    private void addMisc() {
        addModInfo("Tools module for " + Mekanism.MOD_NAME + ".");
        addPackData(ToolsLang.MEKANISM_TOOLS, ToolsLang.PACK_DESCRIPTION);
        add(ToolsLang.HP, "HP: %1$s");
    }

    private void addSet(String type, ArmorCollection armor, ToolCollection tools) {
        add(armor.helmet(), type + " Helmet");
        add(armor.chestplate(), type + " Chestplate");
        add(armor.leggings(), type + " Leggings");
        add(armor.boots(), type + " Boots");
        add(tools.sword(), type + " Sword");
        add(tools.pickaxe(), type + " Pickaxe");
        add(tools.axe(), type + " Axe");
        add(tools.shovel(), type + " Shovel");
        add(tools.hoe(), type + " Hoe");
        add(tools.paxel(), type + " Paxel");
        addShield(tools.shield(), type + " Shield");
    }

    private void addShield(IHasTranslationKey shield, String name) {
        add(shield, name);
        //Add names for all the bannered overlay types
        for (EnumColor color : EnumUtils.COLORS) {
            if (color.getDyeColor() != null) {
                add(shield.getTranslationKey() + "." + color.getRegistryPrefix(), color.getEnglishName() + " " + name);
            }
        }
    }
}