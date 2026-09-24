package mekanism.tools.common;

import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.tools.common.advancements.ToolsAdvancements;
import mekanism.tools.common.material.MaterialType;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;

public class ToolsAdvancementProvider extends BaseAdvancementProvider {

    protected ToolsAdvancementProvider(BootstrapContext<Advancement> output) {
        super(output);
    }

    @Override
    public void generate() {
        HolderGetter<Item> itemLookup = output.lookup(Registries.ITEM);

        advancement(ToolsAdvancements.PAXEL)
              .display(ToolsItems.DIAMOND_PAXEL, AdvancementType.TASK, true)
              .orCriteria("any_paxel", itemLookup, MaterialType.VALUES.stream().map(material -> material.tools.paxel()))
              .save(output);
        advancement(ToolsAdvancements.ALTERNATE_ARMOR)
              .display(ToolsItems.OSMIUM_ARMOR.chestplate(), AdvancementType.TASK, false)
              .orCriteria("armor", itemLookup, MaterialType.VALUES.stream().flatMap(material -> material.armor.asList().stream()))
              .save(output);
        advancement(ToolsAdvancements.ALTERNATE_TOOLS)
              .display(ToolsItems.OSMIUM_TOOLS.pickaxe(), AdvancementType.TASK, false)
              .orCriteria("tools", itemLookup, MaterialType.VALUES.stream().mapMulti((material, streamBuilder) -> {
                  streamBuilder.accept(material.tools.axe());
                  streamBuilder.accept(material.tools.hoe());
                  streamBuilder.accept(material.tools.pickaxe());
                  streamBuilder.accept(material.tools.shovel());
              })).save(output);
        advancement(ToolsAdvancements.NOT_ENOUGH_SHIELDING)
              .display(ToolsItems.OSMIUM_TOOLS.shield(), AdvancementType.TASK, false)
              .orCriteria("shields", itemLookup, MaterialType.VALUES.stream().map(material -> material.tools.shield()))
              .save(output);

        advancement(ToolsAdvancements.BETTER_THAN_NETHERITE)
              .display(ToolsItems.REFINED_OBSIDIAN_ARMOR.chestplate(), AdvancementType.GOAL, false)
              .orCriteria("armor", itemLookup, ToolsItems.REFINED_OBSIDIAN_ARMOR.asList())
              .save(output);
        advancement(ToolsAdvancements.LOVED_BY_PIGLINS)
              .display(ToolsItems.REFINED_GLOWSTONE_ARMOR.chestplate(), AdvancementType.GOAL, false)
              .orCriteria("armor", itemLookup, ToolsItems.REFINED_GLOWSTONE_ARMOR.asList())
              .save(output);
    }
}