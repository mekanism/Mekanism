package mekanism.tools.common;

import javax.annotation.Nullable;
import mekanism.api.providers.IItemProvider;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.ForgeRegistryTagBuilder;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismPickaxe;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ToolsTagProvider extends BaseTagProvider {

    public ToolsTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismTools.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
        addToTag(ItemTags.PIGLIN_LOVED,
              ToolsItems.GOLD_PAXEL,
              ToolsItems.REFINED_GLOWSTONE_PICKAXE,
              ToolsItems.REFINED_GLOWSTONE_AXE,
              ToolsItems.REFINED_GLOWSTONE_SHOVEL,
              ToolsItems.REFINED_GLOWSTONE_HOE,
              ToolsItems.REFINED_GLOWSTONE_SWORD,
              ToolsItems.REFINED_GLOWSTONE_PAXEL,
              ToolsItems.REFINED_GLOWSTONE_HELMET,
              ToolsItems.REFINED_GLOWSTONE_CHESTPLATE,
              ToolsItems.REFINED_GLOWSTONE_LEGGINGS,
              ToolsItems.REFINED_GLOWSTONE_BOOTS,
              ToolsItems.REFINED_GLOWSTONE_SHIELD
        );
        getBlockBuilder(ToolsTags.Blocks.MINEABLE_WITH_PAXEL).add(
              BlockTags.MINEABLE_WITH_AXE,
              BlockTags.MINEABLE_WITH_PICKAXE,
              BlockTags.MINEABLE_WITH_SHOVEL
        );
        getBlockBuilder(ToolsTags.Blocks.NEEDS_BRONZE_TOOL);
        getBlockBuilder(ToolsTags.Blocks.NEEDS_LAPIS_LAZULI_TOOL);
        getBlockBuilder(ToolsTags.Blocks.NEEDS_OSMIUM_TOOL);
        getBlockBuilder(ToolsTags.Blocks.NEEDS_REFINED_GLOWSTONE_TOOL);
        getBlockBuilder(ToolsTags.Blocks.NEEDS_REFINED_OBSIDIAN_TOOL);
        getBlockBuilder(ToolsTags.Blocks.NEEDS_STEEL_TOOL);
        ForgeRegistryTagBuilder<Item> clusterBuilder = getItemBuilder(ItemTags.CLUSTER_MAX_HARVESTABLES);
        for (IItemProvider itemProvider : ToolsItems.ITEMS.getAllItems()) {
            Item item = itemProvider.asItem();
            if (item instanceof ItemMekanismPickaxe || item instanceof ItemMekanismPaxel) {
                clusterBuilder.add(item);
            }
        }
    }
}