package mekanism.generators.common.registries;

import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismGenerators.MODID);

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
}