package mekanism.additions.common.registries;

import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.MekanismAdditions;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.registries.MekanismCreativeTabs;

public class AdditionsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismAdditions.MODID);

    public static final CreativeTabRegistryObject ADDITIONS = CREATIVE_TABS.registerMain(AdditionsLang.MEKANISM_ADDITIONS, AdditionsBlocks.OBSIDIAN_TNT, builder ->
          //TODO - 1.20: Re-evaluate this search bar declaration, odds are we need to make our own background texture modification that then has a smaller searchbar
          builder.withSearchBar(65)//Allow our tabs to be searchable for convenience purposes
                .withTabsBefore(MekanismCreativeTabs.MEKANISM.key())
                .displayItems((displayParameters, output) -> {
                    CreativeTabDeferredRegister.addToDisplay(AdditionsItems.ITEMS, output);
                    CreativeTabDeferredRegister.addToDisplay(AdditionsBlocks.BLOCKS, output);
                })
    );
}