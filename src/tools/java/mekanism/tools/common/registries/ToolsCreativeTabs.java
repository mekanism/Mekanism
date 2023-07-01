package mekanism.tools.common.registries;

import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsLang;

public class ToolsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismTools.MODID);

    public static final CreativeTabRegistryObject TOOLS = CREATIVE_TABS.registerMain(ToolsLang.MEKANISM_TOOLS, ToolsItems.NETHERITE_PAXEL, builder ->
          builder.withBackgroundLocation(MekanismTools.rl("textures/gui/creative_tab.png"))
                .withSearchBar(80)//Allow our tabs to be searchable for convenience purposes
                .withTabsBefore(MekanismCreativeTabs.MEKANISM.key())
                .displayItems((displayParameters, output) -> CreativeTabDeferredRegister.addToDisplay(ToolsItems.ITEMS, output))
    );
}