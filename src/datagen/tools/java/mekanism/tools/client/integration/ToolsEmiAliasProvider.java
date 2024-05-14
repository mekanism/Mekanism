package mekanism.tools.client.integration;

import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.integration.MekanismAliases;
import mekanism.client.integration.emi.BaseEmiAliasProvider;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public class ToolsEmiAliasProvider extends BaseEmiAliasProvider {

    public ToolsEmiAliasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, MekanismTools.MODID);
    }

    @Override
    protected void addAliases(HolderLookup.Provider lookupProvider) {
        addVanillaPaxelAliases(ToolsItems.WOOD_PAXEL, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL);
        addVanillaPaxelAliases(ToolsItems.STONE_PAXEL, Items.STONE_AXE, Items.STONE_PICKAXE, Items.STONE_SHOVEL);
        addVanillaPaxelAliases(ToolsItems.GOLD_PAXEL, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL);
        addVanillaPaxelAliases(ToolsItems.IRON_PAXEL, Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_SHOVEL);
        addVanillaPaxelAliases(ToolsItems.DIAMOND_PAXEL, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL);
        addVanillaPaxelAliases(ToolsItems.NETHERITE_PAXEL, Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL);

        addPaxelAliases(ToolsItems.BRONZE_PAXEL, ToolsItems.BRONZE_AXE, ToolsItems.BRONZE_PICKAXE, ToolsItems.BRONZE_SHOVEL);
        addPaxelAliases(ToolsItems.LAPIS_LAZULI_PAXEL, ToolsItems.LAPIS_LAZULI_AXE, ToolsItems.LAPIS_LAZULI_PICKAXE, ToolsItems.LAPIS_LAZULI_SHOVEL);
        addPaxelAliases(ToolsItems.OSMIUM_PAXEL, ToolsItems.OSMIUM_AXE, ToolsItems.OSMIUM_PICKAXE, ToolsItems.OSMIUM_SHOVEL);
        addPaxelAliases(ToolsItems.REFINED_GLOWSTONE_PAXEL, ToolsItems.REFINED_GLOWSTONE_AXE, ToolsItems.REFINED_GLOWSTONE_PICKAXE, ToolsItems.REFINED_GLOWSTONE_SHOVEL);
        addPaxelAliases(ToolsItems.REFINED_OBSIDIAN_PAXEL, ToolsItems.REFINED_OBSIDIAN_AXE, ToolsItems.REFINED_OBSIDIAN_PICKAXE, ToolsItems.REFINED_OBSIDIAN_SHOVEL);
        addPaxelAliases(ToolsItems.STEEL_PAXEL, ToolsItems.STEEL_AXE, ToolsItems.STEEL_PICKAXE, ToolsItems.STEEL_SHOVEL);
    }

    private void addPaxelAliases(ItemLike paxel, IHasTranslationKey axe, IHasTranslationKey pickaxe, IHasTranslationKey shovel) {
        addAliases(paxel, axe, pickaxe, shovel, MekanismAliases.TOOL_MULTI);
    }

    private void addVanillaPaxelAliases(ItemLike paxel, Item axe, Item pickaxe, Item shovel) {
        addPaxelAliases(paxel, axe::getDescriptionId, pickaxe::getDescriptionId, shovel::getDescriptionId);
    }
}