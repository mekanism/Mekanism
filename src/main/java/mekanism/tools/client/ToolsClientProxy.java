package mekanism.tools.client;

import mekanism.client.render.MekanismRenderer;
import mekanism.tools.common.ToolsCommonProxy;
import mekanism.tools.common.ToolsItems;
import net.minecraft.item.Item;

public class ToolsClientProxy extends ToolsCommonProxy {

    @Override
    public void registerItemRenders() {
        //Vanilla Material Paxels
        registerItemRender(ToolsItems.WoodPaxel);
        registerItemRender(ToolsItems.StonePaxel);
        registerItemRender(ToolsItems.IronPaxel);
        registerItemRender(ToolsItems.DiamondPaxel);
        registerItemRender(ToolsItems.GoldPaxel);

        //Glowstone Items
        registerItemRender(ToolsItems.GlowstonePaxel);
        registerItemRender(ToolsItems.GlowstonePickaxe);
        registerItemRender(ToolsItems.GlowstoneAxe);
        registerItemRender(ToolsItems.GlowstoneShovel);
        registerItemRender(ToolsItems.GlowstoneHoe);
        registerItemRender(ToolsItems.GlowstoneSword);
        registerItemRender(ToolsItems.GlowstoneHelmet);
        registerItemRender(ToolsItems.GlowstoneChestplate);
        registerItemRender(ToolsItems.GlowstoneLeggings);
        registerItemRender(ToolsItems.GlowstoneBoots);

        //Bronze Items
        registerItemRender(ToolsItems.BronzePaxel);
        registerItemRender(ToolsItems.BronzePickaxe);
        registerItemRender(ToolsItems.BronzeAxe);
        registerItemRender(ToolsItems.BronzeShovel);
        registerItemRender(ToolsItems.BronzeHoe);
        registerItemRender(ToolsItems.BronzeSword);
        registerItemRender(ToolsItems.BronzeHelmet);
        registerItemRender(ToolsItems.BronzeChestplate);
        registerItemRender(ToolsItems.BronzeLeggings);
        registerItemRender(ToolsItems.BronzeBoots);

        //Osmium Items
        registerItemRender(ToolsItems.OsmiumPaxel);
        registerItemRender(ToolsItems.OsmiumPickaxe);
        registerItemRender(ToolsItems.OsmiumAxe);
        registerItemRender(ToolsItems.OsmiumShovel);
        registerItemRender(ToolsItems.OsmiumHoe);
        registerItemRender(ToolsItems.OsmiumSword);
        registerItemRender(ToolsItems.OsmiumHelmet);
        registerItemRender(ToolsItems.OsmiumChestplate);
        registerItemRender(ToolsItems.OsmiumLeggings);
        registerItemRender(ToolsItems.OsmiumBoots);

        //Obsidian Items
        registerItemRender(ToolsItems.ObsidianPaxel);
        registerItemRender(ToolsItems.ObsidianPickaxe);
        registerItemRender(ToolsItems.ObsidianAxe);
        registerItemRender(ToolsItems.ObsidianShovel);
        registerItemRender(ToolsItems.ObsidianHoe);
        registerItemRender(ToolsItems.ObsidianSword);
        registerItemRender(ToolsItems.ObsidianHelmet);
        registerItemRender(ToolsItems.ObsidianChestplate);
        registerItemRender(ToolsItems.ObsidianLeggings);
        registerItemRender(ToolsItems.ObsidianBoots);

        //Lazuli Items
        registerItemRender(ToolsItems.LazuliPaxel);
        registerItemRender(ToolsItems.LazuliPickaxe);
        registerItemRender(ToolsItems.LazuliAxe);
        registerItemRender(ToolsItems.LazuliShovel);
        registerItemRender(ToolsItems.LazuliHoe);
        registerItemRender(ToolsItems.LazuliSword);
        registerItemRender(ToolsItems.LazuliHelmet);
        registerItemRender(ToolsItems.LazuliChestplate);
        registerItemRender(ToolsItems.LazuliLeggings);
        registerItemRender(ToolsItems.LazuliBoots);

        //Steel Items
        registerItemRender(ToolsItems.SteelPaxel);
        registerItemRender(ToolsItems.SteelPickaxe);
        registerItemRender(ToolsItems.SteelAxe);
        registerItemRender(ToolsItems.SteelShovel);
        registerItemRender(ToolsItems.SteelHoe);
        registerItemRender(ToolsItems.SteelSword);
        registerItemRender(ToolsItems.SteelHelmet);
        registerItemRender(ToolsItems.SteelChestplate);
        registerItemRender(ToolsItems.SteelLeggings);
        registerItemRender(ToolsItems.SteelBoots);
    }

    public void registerItemRender(Item item) {
        MekanismRenderer.registerItemRender("mekanismtools", item);
    }
}
