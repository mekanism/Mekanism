package mekanism.generators.common.registries;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class GeneratorsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismGenerators.MODID, GeneratorsCreativeTabs::addToExistingTabs);

    public static final CreativeTabRegistryObject GENERATORS = CREATIVE_TABS.registerMain(GeneratorsLang.MEKANISM_GENERATORS, GeneratorsBlocks.HEAT_GENERATOR, builder ->
          builder.withBackgroundLocation(MekanismGenerators.rl("textures/gui/creative_tab.png"))
                .withSearchBar(50)//Allow our tabs to be searchable for convenience purposes
                .withTabsBefore(MekanismCreativeTabs.MEKANISM.key())
                .displayItems((displayParameters, output) -> {
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsItems.ITEMS, output);
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsBlocks.BLOCKS, output);
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsFluids.FLUIDS, output);
                })
    );

    private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
         if (tabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
             CreativeTabDeferredRegister.addToDisplay(event, GeneratorsBlocks.HEAT_GENERATOR, GeneratorsBlocks.SOLAR_GENERATOR, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
                   GeneratorsBlocks.WIND_GENERATOR, GeneratorsBlocks.BIO_GENERATOR, GeneratorsBlocks.GAS_BURNING_GENERATOR);
        } else if (tabKey == CreativeModeTabs.REDSTONE_BLOCKS) {
            for (IBlockProvider block : GeneratorsBlocks.BLOCKS.getAllBlocks()) {
                if (Attribute.has(block.getBlock(), AttributeComparator.class)) {
                    CreativeTabDeferredRegister.addToDisplay(event, block);
                }
            }
        } else if (tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            CreativeTabDeferredRegister.addToDisplay(GeneratorsFluids.FLUIDS, event);
        } else if (tabKey == CreativeModeTabs.INGREDIENTS) {
             CreativeTabDeferredRegister.addToDisplay(event, GeneratorsItems.HOHLRAUM, GeneratorsItems.SOLAR_PANEL);
         }
    }
}