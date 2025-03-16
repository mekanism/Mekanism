package mekanism.tools.common;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.tools.common.advancements.ToolsAdvancements;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TieredItem;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class ToolsAdvancementProvider extends BaseAdvancementProvider {

    public ToolsAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(output, provider, existingFileHelper, MekanismTools.MODID);
    }

    @Override
    protected void registerAdvancements(@NotNull Consumer<AdvancementHolder> consumer) {
        advancement(ToolsAdvancements.PAXEL)
              .display(ToolsItems.DIAMOND_PAXEL, AdvancementType.TASK, true)
              .orCriteria("any_paxel", getItems(item -> item instanceof ItemMekanismPaxel))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_ARMOR)
              .display(ToolsItems.OSMIUM_CHESTPLATE, AdvancementType.TASK, false)
              .orCriteria("armor", getItems(item -> item instanceof ArmorItem))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_TOOLS)
              .display(ToolsItems.OSMIUM_PICKAXE, AdvancementType.TASK, false)
              .orCriteria("tools", getItems(item -> item instanceof TieredItem && !(item instanceof ItemMekanismPaxel)))
              .save(consumer);
        advancement(ToolsAdvancements.NOT_ENOUGH_SHIELDING)
              .display(ToolsItems.OSMIUM_SHIELD, AdvancementType.TASK, false)
              .orCriteria("shields", getItems(item -> item instanceof ItemMekanismShield))
              .save(consumer);

        advancement(ToolsAdvancements.BETTER_THAN_NETHERITE)
              .display(ToolsItems.REFINED_OBSIDIAN_CHESTPLATE, AdvancementType.GOAL, false)
              .orCriteria("armor", ToolsItems.REFINED_OBSIDIAN_HELMET,
                    ToolsItems.REFINED_OBSIDIAN_CHESTPLATE,
                    ToolsItems.REFINED_OBSIDIAN_LEGGINGS,
                    ToolsItems.REFINED_OBSIDIAN_BOOTS
              ).save(consumer);
        advancement(ToolsAdvancements.LOVED_BY_PIGLINS)
              .display(ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, AdvancementType.GOAL, false)
              .orCriteria("armor", ToolsItems.REFINED_GLOWSTONE_HELMET,
                    ToolsItems.REFINED_GLOWSTONE_CHESTPLATE,
                    ToolsItems.REFINED_GLOWSTONE_LEGGINGS,
                    ToolsItems.REFINED_GLOWSTONE_BOOTS
              ).save(consumer);
    }

    private Item[] getItems(Predicate<Item> matcher) {
        return getItems(ToolsItems.ITEMS.getEntries(), matcher);
    }
}