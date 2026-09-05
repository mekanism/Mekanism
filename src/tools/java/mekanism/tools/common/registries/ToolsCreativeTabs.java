package mekanism.tools.common.registries;

import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.material.MaterialType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class ToolsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismTools.MODID, ToolsCreativeTabs::addToExistingTabs);

    public static final MekanismDeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS = CREATIVE_TABS.registerMain(ToolsLang.MEKANISM_TOOLS, ToolsItems.DIAMOND_PAXEL, builder ->
          builder.backgroundTexture(MekanismTools.rl("textures/gui/creative_tab.png"))
                .withSearchBar(80)//Allow our tabs to be searchable for convenience purposes
                .withTabsBefore(MekanismCreativeTabs.MEKANISM.getId())
                .displayItems((displayParameters, output) -> CreativeTabDeferredRegister.addToDisplay(ToolsItems.ITEMS, displayParameters, output))
    );

    private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
        if (tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (MaterialType material : MaterialType.VALUES) {
                CreativeTabDeferredRegister.addToDisplay(event, material.tools.axe());
                CreativeTabDeferredRegister.addToDisplay(event, material.tools.hoe());
                CreativeTabDeferredRegister.addToDisplay(event, material.tools.paxel());
                CreativeTabDeferredRegister.addToDisplay(event, material.tools.pickaxe());
                CreativeTabDeferredRegister.addToDisplay(event, material.tools.shovel());
            }
            ToolsItems.vanillaPaxels().forEach(paxel -> CreativeTabDeferredRegister.addToDisplay(event, paxel));

        } else if (tabKey == CreativeModeTabs.COMBAT) {
            for (MaterialType material : MaterialType.VALUES) {
                material.armor.forEach(armor -> CreativeTabDeferredRegister.addToDisplay(event, armor));
                CreativeTabDeferredRegister.addToDisplay(event, material.tools.sword());
                CreativeTabDeferredRegister.addToDisplay(event, material.tools.shield());
            }
        }
    }
}