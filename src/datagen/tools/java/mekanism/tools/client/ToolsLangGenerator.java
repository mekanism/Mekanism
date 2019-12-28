package mekanism.tools.client;

import mekanism.api.providers.IItemProvider;
import mekanism.client.lang.BaseLanguageProvider;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsItem;
import mekanism.tools.common.ToolsLang;
import net.minecraft.data.DataGenerator;

public class ToolsLangGenerator extends BaseLanguageProvider {

    public ToolsLangGenerator(DataGenerator gen) {
        super(gen, MekanismTools.MODID);
    }

    @Override
    protected void addTranslations() {
        addItems();
        addMisc();
    }

    private void addItems() {
        //Vanilla Paxels
        add(ToolsItem.WOOD_PAXEL, "Wood Paxel");
        add(ToolsItem.STONE_PAXEL, "Stone Paxel");
        add(ToolsItem.IRON_PAXEL, "Iron Paxel");
        add(ToolsItem.GOLD_PAXEL, "Gold Paxel");
        add(ToolsItem.DIAMOND_PAXEL, "Diamond Paxel");
        //Tool sets
        addSet("Bronze", ToolsItem.BRONZE_HELMET, ToolsItem.BRONZE_CHESTPLATE, ToolsItem.BRONZE_LEGGINGS, ToolsItem.BRONZE_BOOTS, ToolsItem.BRONZE_SWORD,
              ToolsItem.BRONZE_PICKAXE, ToolsItem.BRONZE_AXE, ToolsItem.BRONZE_SHOVEL, ToolsItem.BRONZE_HOE, ToolsItem.BRONZE_PAXEL);
        addSet("Lapis Lazuli", ToolsItem.LAPIS_LAZULI_HELMET, ToolsItem.LAPIS_LAZULI_CHESTPLATE, ToolsItem.LAPIS_LAZULI_LEGGINGS, ToolsItem.LAPIS_LAZULI_BOOTS,
              ToolsItem.LAPIS_LAZULI_SWORD, ToolsItem.LAPIS_LAZULI_PICKAXE, ToolsItem.LAPIS_LAZULI_AXE, ToolsItem.LAPIS_LAZULI_SHOVEL, ToolsItem.LAPIS_LAZULI_HOE,
              ToolsItem.LAPIS_LAZULI_PAXEL);
        addSet("Osmium", ToolsItem.OSMIUM_HELMET, ToolsItem.OSMIUM_CHESTPLATE, ToolsItem.OSMIUM_LEGGINGS, ToolsItem.OSMIUM_BOOTS, ToolsItem.OSMIUM_SWORD,
              ToolsItem.OSMIUM_PICKAXE, ToolsItem.OSMIUM_AXE, ToolsItem.OSMIUM_SHOVEL, ToolsItem.OSMIUM_HOE, ToolsItem.OSMIUM_PAXEL);
        addSet("Refined Glowstone", ToolsItem.REFINED_GLOWSTONE_HELMET, ToolsItem.REFINED_GLOWSTONE_CHESTPLATE, ToolsItem.REFINED_GLOWSTONE_LEGGINGS,
              ToolsItem.REFINED_GLOWSTONE_BOOTS, ToolsItem.REFINED_GLOWSTONE_SWORD, ToolsItem.REFINED_GLOWSTONE_PICKAXE, ToolsItem.REFINED_GLOWSTONE_AXE,
              ToolsItem.REFINED_GLOWSTONE_SHOVEL, ToolsItem.REFINED_GLOWSTONE_HOE, ToolsItem.REFINED_GLOWSTONE_PAXEL);
        addSet("Refined Obsidian", ToolsItem.REFINED_OBSIDIAN_HELMET, ToolsItem.REFINED_OBSIDIAN_CHESTPLATE, ToolsItem.REFINED_OBSIDIAN_LEGGINGS,
              ToolsItem.REFINED_OBSIDIAN_BOOTS, ToolsItem.REFINED_OBSIDIAN_SWORD, ToolsItem.REFINED_OBSIDIAN_PICKAXE, ToolsItem.REFINED_OBSIDIAN_AXE,
              ToolsItem.REFINED_OBSIDIAN_SHOVEL, ToolsItem.REFINED_OBSIDIAN_HOE, ToolsItem.REFINED_OBSIDIAN_PAXEL);
        addSet("Steel", ToolsItem.STEEL_HELMET, ToolsItem.STEEL_CHESTPLATE, ToolsItem.STEEL_LEGGINGS, ToolsItem.STEEL_BOOTS, ToolsItem.STEEL_SWORD,
              ToolsItem.STEEL_PICKAXE, ToolsItem.STEEL_AXE, ToolsItem.STEEL_SHOVEL, ToolsItem.STEEL_HOE, ToolsItem.STEEL_PAXEL);
    }

    private void addMisc() {
        add(ToolsLang.HP, "HP: %s");
    }

    private void addSet(String type, IItemProvider helmet, IItemProvider chestplate, IItemProvider leggings, IItemProvider boots, IItemProvider sword,
          IItemProvider pickaxe, IItemProvider axe, IItemProvider shovel, IItemProvider hoe, IItemProvider paxel) {
        add(helmet, type + " Helmet");
        add(chestplate, " Chestplate");
        add(leggings, type + " Leggings");
        add(boots, type + " Boots");
        add(sword, type + " Sword");
        add(pickaxe, type + " Pickaxe");
        add(axe, type + " Axe");
        add(shovel, type + " Shovel");
        add(hoe, type + " Hoe");
        add(paxel, type + " Paxel");
    }
}