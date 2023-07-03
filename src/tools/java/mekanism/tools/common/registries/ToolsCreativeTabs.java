package mekanism.tools.common.registries;

import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.item.ItemMekanismArmor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public class ToolsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismTools.MODID, ToolsCreativeTabs::addToExistingTabs);

    public static final CreativeTabRegistryObject TOOLS = CREATIVE_TABS.registerMain(ToolsLang.MEKANISM_TOOLS, ToolsItems.DIAMOND_PAXEL, builder ->
          builder.withBackgroundLocation(MekanismTools.rl("textures/gui/creative_tab.png"))
                .withSearchBar(80)//Allow our tabs to be searchable for convenience purposes
                .withTabsBefore(MekanismCreativeTabs.MEKANISM.key())
                .displayItems((displayParameters, output) -> CreativeTabDeferredRegister.addToDisplay(ToolsItems.ITEMS, output))
    );

    private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
        if (tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (IItemProvider item : ToolsItems.ITEMS.getAllItems()) {
                if (item.asItem() instanceof DiggerItem) {
                    CreativeTabDeferredRegister.addToDisplay(event, item);
                }
            }
        } else if (tabKey == CreativeModeTabs.COMBAT) {
            for (IItemProvider itemProvider : ToolsItems.ITEMS.getAllItems()) {
                Item item = itemProvider.asItem();
                if (item instanceof ItemMekanismArmor || item instanceof SwordItem || item instanceof ShieldItem) {
                    CreativeTabDeferredRegister.addToDisplay(event, item);
                }
            }
        }
    }
}