package mekanism.tools.client.recipe_viewer.aliases;

import mekanism.api.text.IHasTranslationKey;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import mekanism.client.recipe_viewer.alias.MekanismAliases;
import mekanism.client.recipe_viewer.alias.RVAliasHelper;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ToolsAliasMapping implements IAliasMapping {

    @Override
    public <ITEM, FLUID, CHEMICAL> void addAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        addVanillaPaxelAliases(rv, ToolsItems.WOOD_PAXEL, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL);
        addVanillaPaxelAliases(rv, ToolsItems.STONE_PAXEL, Items.STONE_AXE, Items.STONE_PICKAXE, Items.STONE_SHOVEL);
        addVanillaPaxelAliases(rv, ToolsItems.GOLD_PAXEL, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL);
        addVanillaPaxelAliases(rv, ToolsItems.IRON_PAXEL, Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_SHOVEL);
        addVanillaPaxelAliases(rv, ToolsItems.DIAMOND_PAXEL, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL);
        addVanillaPaxelAliases(rv, ToolsItems.NETHERITE_PAXEL, Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL);

        addPaxelAliases(rv, ToolsItems.BRONZE_PAXEL, ToolsItems.BRONZE_AXE, ToolsItems.BRONZE_PICKAXE, ToolsItems.BRONZE_SHOVEL);
        addPaxelAliases(rv, ToolsItems.LAPIS_LAZULI_PAXEL, ToolsItems.LAPIS_LAZULI_AXE, ToolsItems.LAPIS_LAZULI_PICKAXE, ToolsItems.LAPIS_LAZULI_SHOVEL);
        addPaxelAliases(rv, ToolsItems.OSMIUM_PAXEL, ToolsItems.OSMIUM_AXE, ToolsItems.OSMIUM_PICKAXE, ToolsItems.OSMIUM_SHOVEL);
        addPaxelAliases(rv, ToolsItems.REFINED_GLOWSTONE_PAXEL, ToolsItems.REFINED_GLOWSTONE_AXE, ToolsItems.REFINED_GLOWSTONE_PICKAXE, ToolsItems.REFINED_GLOWSTONE_SHOVEL);
        addPaxelAliases(rv, ToolsItems.REFINED_OBSIDIAN_PAXEL, ToolsItems.REFINED_OBSIDIAN_AXE, ToolsItems.REFINED_OBSIDIAN_PICKAXE, ToolsItems.REFINED_OBSIDIAN_SHOVEL);
        addPaxelAliases(rv, ToolsItems.STEEL_PAXEL, ToolsItems.STEEL_AXE, ToolsItems.STEEL_PICKAXE, ToolsItems.STEEL_SHOVEL);
    }

    private <ITEM, FLUID, CHEMICAL> void addPaxelAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv, Holder<Item> paxel, IHasTranslationKey axe, IHasTranslationKey pickaxe,
          IHasTranslationKey shovel) {
        rv.addItemAliases(paxel, axe, pickaxe, shovel, MekanismAliases.TOOL_MULTI);
    }

    private <ITEM, FLUID, CHEMICAL> void addVanillaPaxelAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv, Holder<Item> paxel, Item axe, Item pickaxe, Item shovel) {
        addPaxelAliases(rv, paxel, axe::getDescriptionId, pickaxe::getDescriptionId, shovel::getDescriptionId);
    }
}