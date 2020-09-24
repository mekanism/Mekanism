package mekanism.tools.common;

import javax.annotation.Nullable;
import mekanism.common.tag.BaseTagProvider;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
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
    }
}