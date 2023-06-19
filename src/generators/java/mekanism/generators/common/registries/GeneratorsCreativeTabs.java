package mekanism.generators.common.registries;

import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismGenerators.MODID);

    public static final CreativeTabRegistryObject GENERATORS = CREATIVE_TABS.registerMain(GeneratorsLang.MEKANISM_GENERATORS, GeneratorsBlocks.HEAT_GENERATOR, builder ->
          //TODO - 1.20: Re-evaluate this search bar declaration, odds are we need to make our own background texture modification that then has a smaller searchbar
          builder.withSearchBar(50)//Allow our tabs to be searchable for convenience purposes
                .withTabsBefore(MekanismCreativeTabs.MEKANISM.key())
                .displayItems((displayParameters, output) -> {
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsItems.ITEMS, output);
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsBlocks.BLOCKS, output);
                    CreativeTabDeferredRegister.addToDisplay(GeneratorsFluids.FLUIDS, output);
                })
    );
}