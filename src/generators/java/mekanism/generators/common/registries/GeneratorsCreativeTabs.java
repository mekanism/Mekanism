package mekanism.generators.common.registries;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class GeneratorsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismGenerators.MODID, GeneratorsCreativeTabs::addToExistingTabs);

    public static final MekanismDeferredHolder<CreativeModeTab, CreativeModeTab> GENERATORS = CREATIVE_TABS.registerMain(GeneratorsLang.MEKANISM_GENERATORS, GeneratorsBlocks.HEAT_GENERATOR, builder ->
          builder.backgroundTexture(MekanismGenerators.rl("textures/gui/creative_tab.png"))
                .withSearchBar(50)//Allow our tabs to be searchable for convenience purposes
                .withTabsBefore(MekanismCreativeTabs.MEKANISM.getKey())
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
            for (Holder<Block> blockProvider : GeneratorsBlocks.BLOCKS.getPrimaryEntries()) {
                Block block = blockProvider.value();
                if (Attribute.has(block, AttributeComparator.class)) {
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