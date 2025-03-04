package mekanism.additions.common.registries;

import java.util.Map;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.MekanismAdditions;
import mekanism.api.text.EnumColor;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registries.MekanismCreativeTabs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class AdditionsCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekanismAdditions.MODID, AdditionsCreativeTabs::addToExistingTabs);

    public static final MekanismDeferredHolder<CreativeModeTab, CreativeModeTab> ADDITIONS = CREATIVE_TABS.registerMain(AdditionsLang.MEKANISM_ADDITIONS,
          AdditionsItems.BALLOONS.get(EnumColor.BRIGHT_GREEN), builder ->
                builder.backgroundTexture(MekanismAdditions.rl("textures/gui/creative_tab.png"))
                      .withSearchBar(65)//Allow our tabs to be searchable for convenience purposes
                      .withTabsBefore(MekanismCreativeTabs.MEKANISM.getKey())
                      .displayItems((displayParameters, output) -> {
                          CreativeTabDeferredRegister.addToDisplay(AdditionsItems.ITEMS, output);
                          CreativeTabDeferredRegister.addToDisplay(AdditionsBlocks.BLOCKS, output);
                      })
    );

    private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
        if (tabKey == CreativeModeTabs.COLORED_BLOCKS) {
            addToDisplay(event, AdditionsBlocks.GLOW_PANELS, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsBlocks.SLICK_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS,
                  AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_ROADS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_STAIRS,
                  AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_FENCES, AdditionsBlocks.PLASTIC_FENCE_GATES, AdditionsBlocks.PLASTIC_GLOW_STAIRS,
                  AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS);
        } else if (tabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            addToDisplay(event, AdditionsBlocks.GLOW_PANELS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsBlocks.PLASTIC_GLOW_STAIRS, AdditionsBlocks.PLASTIC_GLOW_SLABS);
        } else if (tabKey == CreativeModeTabs.REDSTONE_BLOCKS) {
            CreativeTabDeferredRegister.addToDisplay(event, AdditionsBlocks.OBSIDIAN_TNT);
        } else if (tabKey == CreativeModeTabs.COMBAT) {
            CreativeTabDeferredRegister.addToDisplay(event, AdditionsBlocks.OBSIDIAN_TNT);
        } else if (tabKey == CreativeModeTabs.SPAWN_EGGS) {
            CreativeTabDeferredRegister.addToDisplay(event, AdditionsItems.BABY_BOGGED_SPAWN_EGG, AdditionsItems.BABY_CREEPER_SPAWN_EGG,
                  AdditionsItems.BABY_ENDERMAN_SPAWN_EGG, AdditionsItems.BABY_SKELETON_SPAWN_EGG, AdditionsItems.BABY_STRAY_SPAWN_EGG,
                  AdditionsItems.BABY_WITHER_SKELETON_SPAWN_EGG);
        }
    }

    @SafeVarargs
    private static void addToDisplay(CreativeModeTab.Output output, Map<EnumColor, ? extends BlockRegistryObject<?, ?>>... blocks) {
        for (Map<EnumColor, ? extends BlockRegistryObject<?, ?>> blockMap : blocks) {
            CreativeTabDeferredRegister.addToDisplay(output, blockMap.values().toArray(new BlockRegistryObject<?, ?>[0]));
        }
    }
}