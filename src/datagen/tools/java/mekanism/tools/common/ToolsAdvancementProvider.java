package mekanism.tools.common;

import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.tools.common.advancements.ToolsAdvancements;
import mekanism.tools.common.item.IsMekanismTool;
import mekanism.tools.common.item.ItemMekanismArmor;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ToolsAdvancementProvider extends BaseAdvancementProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
        HolderGetter<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

        advancement(ToolsAdvancements.PAXEL)
              .display(ToolsItems.DIAMOND_PAXEL, AdvancementType.TASK, true)
              .orCriteria("any_paxel", itemLookup, getItems(item -> item instanceof ItemMekanismPaxel))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_ARMOR)
              .display(ToolsItems.OSMIUM_CHESTPLATE, AdvancementType.TASK, false)
              .orCriteria("armor", itemLookup, getItems(item -> item instanceof ItemMekanismArmor))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_TOOLS)
              .display(ToolsItems.OSMIUM_PICKAXE, AdvancementType.TASK, false)
              .orCriteria("tools", itemLookup, getItems(item -> item instanceof IsMekanismTool && !(item instanceof ItemMekanismPaxel)))
              .save(consumer);
        advancement(ToolsAdvancements.NOT_ENOUGH_SHIELDING)
              .display(ToolsItems.OSMIUM_SHIELD, AdvancementType.TASK, false)
              .orCriteria("shields", itemLookup, getItems(item -> item instanceof ItemMekanismShield))
              .save(consumer);

        advancement(ToolsAdvancements.BETTER_THAN_NETHERITE)
              .display(ToolsItems.REFINED_OBSIDIAN_CHESTPLATE, AdvancementType.GOAL, false)
              .orCriteria("armor", itemLookup, ToolsItems.REFINED_OBSIDIAN_HELMET,
                    ToolsItems.REFINED_OBSIDIAN_CHESTPLATE,
                    ToolsItems.REFINED_OBSIDIAN_LEGGINGS,
                    ToolsItems.REFINED_OBSIDIAN_BOOTS
              ).save(consumer);
        advancement(ToolsAdvancements.LOVED_BY_PIGLINS)
              .display(ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, AdvancementType.GOAL, false)
              .orCriteria("armor", itemLookup, ToolsItems.REFINED_GLOWSTONE_HELMET,
                    ToolsItems.REFINED_GLOWSTONE_CHESTPLATE,
                    ToolsItems.REFINED_GLOWSTONE_LEGGINGS,
                    ToolsItems.REFINED_GLOWSTONE_BOOTS
              ).save(consumer);
    }

    private Item[] getItems(Predicate<Item> matcher) {
        return getItems(ToolsItems.ITEMS.getEntries(), matcher);
    }
}