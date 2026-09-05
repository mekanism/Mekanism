package mekanism.tools.common;

import java.util.Arrays;
import java.util.function.Consumer;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.tools.common.advancements.ToolsAdvancements;
import mekanism.tools.common.material.MaterialType;
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
              .orCriteria("any_paxel", itemLookup, Arrays.stream(MaterialType.VALUES).map(material -> material.tools.paxel()))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_ARMOR)
              .display(ToolsItems.OSMIUM_ARMOR.chestplate(), AdvancementType.TASK, false)
              .orCriteria("armor", itemLookup, Arrays.stream(MaterialType.VALUES).flatMap(material -> material.armor.asList().stream()))
              .save(consumer);
        advancement(ToolsAdvancements.ALTERNATE_TOOLS)
              .display(ToolsItems.OSMIUM_TOOLS.pickaxe(), AdvancementType.TASK, false)
              .orCriteria("tools", itemLookup, Arrays.stream(MaterialType.VALUES).mapMulti((material, streamBuilder) -> {
                  streamBuilder.accept(material.tools.axe());
                  streamBuilder.accept(material.tools.hoe());
                  streamBuilder.accept(material.tools.pickaxe());
                  streamBuilder.accept(material.tools.shovel());
              })).save(consumer);
        advancement(ToolsAdvancements.NOT_ENOUGH_SHIELDING)
              .display(ToolsItems.OSMIUM_TOOLS.shield(), AdvancementType.TASK, false)
              .orCriteria("shields", itemLookup, Arrays.stream(MaterialType.VALUES).map(material -> material.tools.shield()))
              .save(consumer);

        advancement(ToolsAdvancements.BETTER_THAN_NETHERITE)
              .display(ToolsItems.REFINED_OBSIDIAN_ARMOR.chestplate(), AdvancementType.GOAL, false)
              .orCriteria("armor", itemLookup, ToolsItems.REFINED_OBSIDIAN_ARMOR.asList()).save(consumer);
        advancement(ToolsAdvancements.LOVED_BY_PIGLINS)
              .display(ToolsItems.REFINED_GLOWSTONE_ARMOR.chestplate(), AdvancementType.GOAL, false)
              .orCriteria("armor", itemLookup, ToolsItems.REFINED_GLOWSTONE_ARMOR.asList()).save(consumer);
    }
}