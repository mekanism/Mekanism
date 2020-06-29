package mekanism.tools.client;

import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.lang.BaseLanguageProvider;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.data.DataGenerator;

public class ToolsLangProvider extends BaseLanguageProvider {

    public ToolsLangProvider(DataGenerator gen) {
        super(gen, MekanismTools.MODID);
    }

    @Override
    protected void addTranslations() {
        addItems();
        addMisc();
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
        addSet("Bronze", ToolsItems.BRONZE_HELMET, ToolsItems.BRONZE_CHESTPLATE, ToolsItems.BRONZE_LEGGINGS, ToolsItems.BRONZE_BOOTS, ToolsItems.BRONZE_SWORD,
              ToolsItems.BRONZE_PICKAXE, ToolsItems.BRONZE_AXE, ToolsItems.BRONZE_SHOVEL, ToolsItems.BRONZE_HOE, ToolsItems.BRONZE_PAXEL, ToolsItems.BRONZE_SHIELD);
        addSet("Lapis Lazuli", ToolsItems.LAPIS_LAZULI_HELMET, ToolsItems.LAPIS_LAZULI_CHESTPLATE, ToolsItems.LAPIS_LAZULI_LEGGINGS, ToolsItems.LAPIS_LAZULI_BOOTS,
              ToolsItems.LAPIS_LAZULI_SWORD, ToolsItems.LAPIS_LAZULI_PICKAXE, ToolsItems.LAPIS_LAZULI_AXE, ToolsItems.LAPIS_LAZULI_SHOVEL, ToolsItems.LAPIS_LAZULI_HOE,
              ToolsItems.LAPIS_LAZULI_PAXEL, ToolsItems.LAPIS_LAZULI_SHIELD);
        addSet("Osmium", ToolsItems.OSMIUM_HELMET, ToolsItems.OSMIUM_CHESTPLATE, ToolsItems.OSMIUM_LEGGINGS, ToolsItems.OSMIUM_BOOTS, ToolsItems.OSMIUM_SWORD,
              ToolsItems.OSMIUM_PICKAXE, ToolsItems.OSMIUM_AXE, ToolsItems.OSMIUM_SHOVEL, ToolsItems.OSMIUM_HOE, ToolsItems.OSMIUM_PAXEL, ToolsItems.OSMIUM_SHIELD);
        addSet("Refined Glowstone", ToolsItems.REFINED_GLOWSTONE_HELMET, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, ToolsItems.REFINED_GLOWSTONE_LEGGINGS,
              ToolsItems.REFINED_GLOWSTONE_BOOTS, ToolsItems.REFINED_GLOWSTONE_SWORD, ToolsItems.REFINED_GLOWSTONE_PICKAXE, ToolsItems.REFINED_GLOWSTONE_AXE,
              ToolsItems.REFINED_GLOWSTONE_SHOVEL, ToolsItems.REFINED_GLOWSTONE_HOE, ToolsItems.REFINED_GLOWSTONE_PAXEL, ToolsItems.REFINED_GLOWSTONE_SHIELD);
        addSet("Refined Obsidian", ToolsItems.REFINED_OBSIDIAN_HELMET, ToolsItems.REFINED_OBSIDIAN_CHESTPLATE, ToolsItems.REFINED_OBSIDIAN_LEGGINGS,
              ToolsItems.REFINED_OBSIDIAN_BOOTS, ToolsItems.REFINED_OBSIDIAN_SWORD, ToolsItems.REFINED_OBSIDIAN_PICKAXE, ToolsItems.REFINED_OBSIDIAN_AXE,
              ToolsItems.REFINED_OBSIDIAN_SHOVEL, ToolsItems.REFINED_OBSIDIAN_HOE, ToolsItems.REFINED_OBSIDIAN_PAXEL, ToolsItems.REFINED_OBSIDIAN_SHIELD);
        addSet("Steel", ToolsItems.STEEL_HELMET, ToolsItems.STEEL_CHESTPLATE, ToolsItems.STEEL_LEGGINGS, ToolsItems.STEEL_BOOTS, ToolsItems.STEEL_SWORD,
              ToolsItems.STEEL_PICKAXE, ToolsItems.STEEL_AXE, ToolsItems.STEEL_SHOVEL, ToolsItems.STEEL_HOE, ToolsItems.STEEL_PAXEL, ToolsItems.STEEL_SHIELD);
    }

    private void addMisc() {
        add(ToolsLang.HP, "HP: %s");
    }

    private void addSet(String type, IItemProvider helmet, IItemProvider chestplate, IItemProvider leggings, IItemProvider boots, IItemProvider sword,
          IItemProvider pickaxe, IItemProvider axe, IItemProvider shovel, IItemProvider hoe, IItemProvider paxel, IItemProvider shield) {
        add(helmet, type + " Helmet");
        add(chestplate, type + " Chestplate");
        add(leggings, type + " Leggings");
        add(boots, type + " Boots");
        add(sword, type + " Sword");
        add(pickaxe, type + " Pickaxe");
        add(axe, type + " Axe");
        add(shovel, type + " Shovel");
        add(hoe, type + " Hoe");
        add(paxel, type + " Paxel");
        addShield(shield, type + " Shield");
    }

    private void addShield(IItemProvider shield, String name) {
        add(shield, name);
        //Add names for all the bannered overlay types
        for (EnumColor color : EnumColor.values()) {
            if (color.hasDyeName()) {
                add(shield.getTranslationKey() + "." + color.getRegistryPrefix(), color.getEnglishName() + " " + name);
            }
        }
    }
}