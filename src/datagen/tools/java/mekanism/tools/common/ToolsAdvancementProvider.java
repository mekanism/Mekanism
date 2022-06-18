package mekanism.tools.common;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.tools.common.advancements.ToolsAdvancements;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ToolsAdvancementProvider extends BaseAdvancementProvider {

    public ToolsAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, MekanismTools.MODID);
    }

    @Override
    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper) {
        advancement(ToolsAdvancements.PAXEL)
              .display(ToolsItems.DIAMOND_PAXEL, FrameType.TASK)
              .addCriterion("any_paxel", hasItems(ToolsTags.Items.ADVANCEMENTS_ANY_PAXEL))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_ARMOR)
              .display(ToolsItems.OSMIUM_CHESTPLATE, FrameType.TASK)
              .addCriterion("armor", hasItems(ToolsTags.Items.ADVANCEMENTS_ALTERNATE_ARMOR))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_TOOLS)
              .display(ToolsItems.OSMIUM_PICKAXE, FrameType.TASK)
              .addCriterion("tools", hasItems(ToolsTags.Items.ADVANCEMENTS_ALTERNATE_TOOLS))
              .save(consumer);
        advancement(ToolsAdvancements.NOT_ENOUGH_SHIELDING)
              .display(ToolsItems.OSMIUM_SHIELD, FrameType.TASK)
              .addCriterion("shields", hasItems(ToolsTags.Items.ADVANCEMENTS_SHIELDS))
              .save(consumer);

        advancement(ToolsAdvancements.BETTER_THAN_NETHERITE)
              .display(ToolsItems.REFINED_OBSIDIAN_CHESTPLATE, FrameType.GOAL)
              .addCriterion("armor", hasItems(ToolsTags.Items.ADVANCEMENTS_REFINED_OBSIDIAN))
              .save(consumer);
        advancement(ToolsAdvancements.LOVED_BY_PIGLINS)
              .display(ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, FrameType.GOAL)
              .addCriterion("armor", hasItems(ToolsTags.Items.ADVANCEMENTS_REFINED_GLOWSTONE))
              .save(consumer);
    }
}