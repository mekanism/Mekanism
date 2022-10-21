package mekanism.tools.common;

import java.util.function.Predicate;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ToolsTagProvider extends BaseTagProvider {

    public ToolsTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismTools.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
        addToolTags();
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
        createTag(getItemBuilder(ItemTags.CLUSTER_MAX_HARVESTABLES), item -> item instanceof ItemMekanismPickaxe || item instanceof ItemMekanismPaxel);
    }

    private void addToolTags() {
        addPaxels();
        addSwords();
        addAxes();
        addPickaxes();
        addShovels();
        addHoes();
        addShields();
        //Armor
        addHelmets();
        addChestplates();
        addLeggings();
        addBoots();
    }

    private void addPaxels() {
        getItemBuilder(Tags.Items.TOOLS).add(ToolsTags.Items.TOOLS_PAXELS);
        getItemBuilder(ToolsTags.Items.TOOLS_PAXELS).add(
              //Vanilla Paxels
              ToolsTags.Items.TOOLS_PAXELS_WOOD,
              ToolsTags.Items.TOOLS_PAXELS_STONE,
              ToolsTags.Items.TOOLS_PAXELS_GOLD,
              ToolsTags.Items.TOOLS_PAXELS_IRON,
              ToolsTags.Items.TOOLS_PAXELS_DIAMOND,
              ToolsTags.Items.TOOLS_PAXELS_NETHERITE,
              //Our paxels
              ToolsTags.Items.TOOLS_PAXELS_BRONZE,
              ToolsTags.Items.TOOLS_PAXELS_LAPIS_LAZULI,
              ToolsTags.Items.TOOLS_PAXELS_OSMIUM,
              ToolsTags.Items.TOOLS_PAXELS_REFINED_GLOWSTONE,
              ToolsTags.Items.TOOLS_PAXELS_REFINED_OBSIDIAN,
              ToolsTags.Items.TOOLS_PAXELS_STEEL
        );
        addToTag(ToolsTags.Items.TOOLS_PAXELS_WOOD, ToolsItems.WOOD_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_STONE, ToolsItems.STONE_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_GOLD, ToolsItems.GOLD_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_IRON, ToolsItems.IRON_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_DIAMOND, ToolsItems.DIAMOND_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_NETHERITE, ToolsItems.NETHERITE_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_BRONZE, ToolsItems.BRONZE_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_OSMIUM, ToolsItems.OSMIUM_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXELS_STEEL, ToolsItems.STEEL_PAXEL);
    }

    private void addSwords() {
        getItemBuilder(Tags.Items.TOOLS_SWORDS).add(
              ToolsTags.Items.TOOLS_SWORDS_BRONZE,
              ToolsTags.Items.TOOLS_SWORDS_LAPIS_LAZULI,
              ToolsTags.Items.TOOLS_SWORDS_OSMIUM,
              ToolsTags.Items.TOOLS_SWORDS_REFINED_GLOWSTONE,
              ToolsTags.Items.TOOLS_SWORDS_REFINED_OBSIDIAN,
              ToolsTags.Items.TOOLS_SWORDS_STEEL
        );
        addToTag(ToolsTags.Items.TOOLS_SWORDS_BRONZE, ToolsItems.BRONZE_SWORD);
        addToTag(ToolsTags.Items.TOOLS_SWORDS_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_SWORD);
        addToTag(ToolsTags.Items.TOOLS_SWORDS_OSMIUM, ToolsItems.OSMIUM_SWORD);
        addToTag(ToolsTags.Items.TOOLS_SWORDS_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_SWORD);
        addToTag(ToolsTags.Items.TOOLS_SWORDS_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_SWORD);
        addToTag(ToolsTags.Items.TOOLS_SWORDS_STEEL, ToolsItems.STEEL_SWORD);
    }

    private void addAxes() {
        getItemBuilder(Tags.Items.TOOLS_AXES).add(
              ToolsTags.Items.TOOLS_AXES_BRONZE,
              ToolsTags.Items.TOOLS_AXES_LAPIS_LAZULI,
              ToolsTags.Items.TOOLS_AXES_OSMIUM,
              ToolsTags.Items.TOOLS_AXES_REFINED_GLOWSTONE,
              ToolsTags.Items.TOOLS_AXES_REFINED_OBSIDIAN,
              ToolsTags.Items.TOOLS_AXES_STEEL
        );
        addToTag(ToolsTags.Items.TOOLS_AXES_BRONZE, ToolsItems.BRONZE_AXE);
        addToTag(ToolsTags.Items.TOOLS_AXES_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_AXE);
        addToTag(ToolsTags.Items.TOOLS_AXES_OSMIUM, ToolsItems.OSMIUM_AXE);
        addToTag(ToolsTags.Items.TOOLS_AXES_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_AXE);
        addToTag(ToolsTags.Items.TOOLS_AXES_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_AXE);
        addToTag(ToolsTags.Items.TOOLS_AXES_STEEL, ToolsItems.STEEL_AXE);
    }

    private void addPickaxes() {
        getItemBuilder(Tags.Items.TOOLS_PICKAXES).add(
              ToolsTags.Items.TOOLS_PICKAXES_BRONZE,
              ToolsTags.Items.TOOLS_PICKAXES_LAPIS_LAZULI,
              ToolsTags.Items.TOOLS_PICKAXES_OSMIUM,
              ToolsTags.Items.TOOLS_PICKAXES_REFINED_GLOWSTONE,
              ToolsTags.Items.TOOLS_PICKAXES_REFINED_OBSIDIAN,
              ToolsTags.Items.TOOLS_PICKAXES_STEEL
        );
        addToTag(ToolsTags.Items.TOOLS_PICKAXES_BRONZE, ToolsItems.BRONZE_PICKAXE);
        addToTag(ToolsTags.Items.TOOLS_PICKAXES_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_PICKAXE);
        addToTag(ToolsTags.Items.TOOLS_PICKAXES_OSMIUM, ToolsItems.OSMIUM_PICKAXE);
        addToTag(ToolsTags.Items.TOOLS_PICKAXES_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_PICKAXE);
        addToTag(ToolsTags.Items.TOOLS_PICKAXES_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_PICKAXE);
        addToTag(ToolsTags.Items.TOOLS_PICKAXES_STEEL, ToolsItems.STEEL_PICKAXE);
    }

    private void addShovels() {
        getItemBuilder(Tags.Items.TOOLS_SHOVELS).add(
              ToolsTags.Items.TOOLS_SHOVELS_BRONZE,
              ToolsTags.Items.TOOLS_SHOVELS_LAPIS_LAZULI,
              ToolsTags.Items.TOOLS_SHOVELS_OSMIUM,
              ToolsTags.Items.TOOLS_SHOVELS_REFINED_GLOWSTONE,
              ToolsTags.Items.TOOLS_SHOVELS_REFINED_OBSIDIAN,
              ToolsTags.Items.TOOLS_SHOVELS_STEEL
        );
        addToTag(ToolsTags.Items.TOOLS_SHOVELS_BRONZE, ToolsItems.BRONZE_SHOVEL);
        addToTag(ToolsTags.Items.TOOLS_SHOVELS_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_SHOVEL);
        addToTag(ToolsTags.Items.TOOLS_SHOVELS_OSMIUM, ToolsItems.OSMIUM_SHOVEL);
        addToTag(ToolsTags.Items.TOOLS_SHOVELS_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_SHOVEL);
        addToTag(ToolsTags.Items.TOOLS_SHOVELS_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_SHOVEL);
        addToTag(ToolsTags.Items.TOOLS_SHOVELS_STEEL, ToolsItems.STEEL_SHOVEL);
    }

    private void addHoes() {
        getItemBuilder(Tags.Items.TOOLS_HOES).add(
              ToolsTags.Items.TOOLS_HOES_BRONZE,
              ToolsTags.Items.TOOLS_HOES_LAPIS_LAZULI,
              ToolsTags.Items.TOOLS_HOES_OSMIUM,
              ToolsTags.Items.TOOLS_HOES_REFINED_GLOWSTONE,
              ToolsTags.Items.TOOLS_HOES_REFINED_OBSIDIAN,
              ToolsTags.Items.TOOLS_HOES_STEEL
        );
        addToTag(ToolsTags.Items.TOOLS_HOES_BRONZE, ToolsItems.BRONZE_HOE);
        addToTag(ToolsTags.Items.TOOLS_HOES_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_HOE);
        addToTag(ToolsTags.Items.TOOLS_HOES_OSMIUM, ToolsItems.OSMIUM_HOE);
        addToTag(ToolsTags.Items.TOOLS_HOES_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_HOE);
        addToTag(ToolsTags.Items.TOOLS_HOES_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_HOE);
        addToTag(ToolsTags.Items.TOOLS_HOES_STEEL, ToolsItems.STEEL_HOE);
    }

    private void addShields() {
        getItemBuilder(Tags.Items.TOOLS_SHIELDS).add(
              ToolsTags.Items.TOOLS_SHIELDS_BRONZE,
              ToolsTags.Items.TOOLS_SHIELDS_LAPIS_LAZULI,
              ToolsTags.Items.TOOLS_SHIELDS_OSMIUM,
              ToolsTags.Items.TOOLS_SHIELDS_REFINED_GLOWSTONE,
              ToolsTags.Items.TOOLS_SHIELDS_REFINED_OBSIDIAN,
              ToolsTags.Items.TOOLS_SHIELDS_STEEL
        );
        addToTag(ToolsTags.Items.TOOLS_SHIELDS_BRONZE, ToolsItems.BRONZE_SHIELD);
        addToTag(ToolsTags.Items.TOOLS_SHIELDS_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_SHIELD);
        addToTag(ToolsTags.Items.TOOLS_SHIELDS_OSMIUM, ToolsItems.OSMIUM_SHIELD);
        addToTag(ToolsTags.Items.TOOLS_SHIELDS_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_SHIELD);
        addToTag(ToolsTags.Items.TOOLS_SHIELDS_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_SHIELD);
        addToTag(ToolsTags.Items.TOOLS_SHIELDS_STEEL, ToolsItems.STEEL_SHIELD);
    }

    private void addHelmets() {
        getItemBuilder(Tags.Items.ARMORS_HELMETS).add(
              ToolsTags.Items.ARMORS_HELMETS_BRONZE,
              ToolsTags.Items.ARMORS_HELMETS_LAPIS_LAZULI,
              ToolsTags.Items.ARMORS_HELMETS_OSMIUM,
              ToolsTags.Items.ARMORS_HELMETS_REFINED_GLOWSTONE,
              ToolsTags.Items.ARMORS_HELMETS_REFINED_OBSIDIAN,
              ToolsTags.Items.ARMORS_HELMETS_STEEL
        );
        addToTag(ToolsTags.Items.ARMORS_HELMETS_BRONZE, ToolsItems.BRONZE_HELMET);
        addToTag(ToolsTags.Items.ARMORS_HELMETS_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_HELMET);
        addToTag(ToolsTags.Items.ARMORS_HELMETS_OSMIUM, ToolsItems.OSMIUM_HELMET);
        addToTag(ToolsTags.Items.ARMORS_HELMETS_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_HELMET);
        addToTag(ToolsTags.Items.ARMORS_HELMETS_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_HELMET);
        addToTag(ToolsTags.Items.ARMORS_HELMETS_STEEL, ToolsItems.STEEL_HELMET);
    }

    private void addChestplates() {
        getItemBuilder(Tags.Items.ARMORS_CHESTPLATES).add(
              ToolsTags.Items.ARMORS_CHESTPLATES_BRONZE,
              ToolsTags.Items.ARMORS_CHESTPLATES_LAPIS_LAZULI,
              ToolsTags.Items.ARMORS_CHESTPLATES_OSMIUM,
              ToolsTags.Items.ARMORS_CHESTPLATES_REFINED_GLOWSTONE,
              ToolsTags.Items.ARMORS_CHESTPLATES_REFINED_OBSIDIAN,
              ToolsTags.Items.ARMORS_CHESTPLATES_STEEL
        );
        addToTag(ToolsTags.Items.ARMORS_CHESTPLATES_BRONZE, ToolsItems.BRONZE_CHESTPLATE);
        addToTag(ToolsTags.Items.ARMORS_CHESTPLATES_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_CHESTPLATE);
        addToTag(ToolsTags.Items.ARMORS_CHESTPLATES_OSMIUM, ToolsItems.OSMIUM_CHESTPLATE);
        addToTag(ToolsTags.Items.ARMORS_CHESTPLATES_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE);
        addToTag(ToolsTags.Items.ARMORS_CHESTPLATES_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_CHESTPLATE);
        addToTag(ToolsTags.Items.ARMORS_CHESTPLATES_STEEL, ToolsItems.STEEL_CHESTPLATE);
    }

    private void addLeggings() {
        getItemBuilder(Tags.Items.ARMORS_LEGGINGS).add(
              ToolsTags.Items.ARMORS_LEGGINGS_BRONZE,
              ToolsTags.Items.ARMORS_LEGGINGS_LAPIS_LAZULI,
              ToolsTags.Items.ARMORS_LEGGINGS_OSMIUM,
              ToolsTags.Items.ARMORS_LEGGINGS_REFINED_GLOWSTONE,
              ToolsTags.Items.ARMORS_LEGGINGS_REFINED_OBSIDIAN,
              ToolsTags.Items.ARMORS_LEGGINGS_STEEL
        );
        addToTag(ToolsTags.Items.ARMORS_LEGGINGS_BRONZE, ToolsItems.BRONZE_LEGGINGS);
        addToTag(ToolsTags.Items.ARMORS_LEGGINGS_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_LEGGINGS);
        addToTag(ToolsTags.Items.ARMORS_LEGGINGS_OSMIUM, ToolsItems.OSMIUM_LEGGINGS);
        addToTag(ToolsTags.Items.ARMORS_LEGGINGS_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_LEGGINGS);
        addToTag(ToolsTags.Items.ARMORS_LEGGINGS_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_LEGGINGS);
        addToTag(ToolsTags.Items.ARMORS_LEGGINGS_STEEL, ToolsItems.STEEL_LEGGINGS);
    }

    private void addBoots() {
        getItemBuilder(Tags.Items.ARMORS_BOOTS).add(
              ToolsTags.Items.ARMORS_BOOTS_BRONZE,
              ToolsTags.Items.ARMORS_BOOTS_LAPIS_LAZULI,
              ToolsTags.Items.ARMORS_BOOTS_OSMIUM,
              ToolsTags.Items.ARMORS_BOOTS_REFINED_GLOWSTONE,
              ToolsTags.Items.ARMORS_BOOTS_REFINED_OBSIDIAN,
              ToolsTags.Items.ARMORS_BOOTS_STEEL
        );
        addToTag(ToolsTags.Items.ARMORS_BOOTS_BRONZE, ToolsItems.BRONZE_BOOTS);
        addToTag(ToolsTags.Items.ARMORS_BOOTS_LAPIS_LAZULI, ToolsItems.LAPIS_LAZULI_BOOTS);
        addToTag(ToolsTags.Items.ARMORS_BOOTS_OSMIUM, ToolsItems.OSMIUM_BOOTS);
        addToTag(ToolsTags.Items.ARMORS_BOOTS_REFINED_GLOWSTONE, ToolsItems.REFINED_GLOWSTONE_BOOTS);
        addToTag(ToolsTags.Items.ARMORS_BOOTS_REFINED_OBSIDIAN, ToolsItems.REFINED_OBSIDIAN_BOOTS);
        addToTag(ToolsTags.Items.ARMORS_BOOTS_STEEL, ToolsItems.STEEL_BOOTS);
    }

    private void createTag(ForgeRegistryTagBuilder<Item> tag, Predicate<Item> matcher) {
        for (IItemProvider itemProvider : ToolsItems.ITEMS.getAllItems()) {
            Item item = itemProvider.asItem();
            if (matcher.test(item)) {
                tag.add(item);
            }
        }
    }
}