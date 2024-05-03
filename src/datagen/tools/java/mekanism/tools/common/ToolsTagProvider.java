package mekanism.tools.common;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.IntrinsicMekanismTagBuilder;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismPickaxe;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ToolsTagProvider extends BaseTagProvider {

    public ToolsTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MekanismTools.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
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
        addToTag(ItemTags.TRIMMABLE_ARMOR,
              ToolsItems.BRONZE_HELMET, ToolsItems.BRONZE_CHESTPLATE, ToolsItems.BRONZE_LEGGINGS, ToolsItems.BRONZE_BOOTS,
              ToolsItems.LAPIS_LAZULI_HELMET, ToolsItems.LAPIS_LAZULI_CHESTPLATE, ToolsItems.LAPIS_LAZULI_LEGGINGS, ToolsItems.LAPIS_LAZULI_BOOTS,
              ToolsItems.OSMIUM_HELMET, ToolsItems.OSMIUM_CHESTPLATE, ToolsItems.OSMIUM_LEGGINGS, ToolsItems.OSMIUM_BOOTS,
              ToolsItems.REFINED_GLOWSTONE_HELMET, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, ToolsItems.REFINED_GLOWSTONE_LEGGINGS, ToolsItems.REFINED_GLOWSTONE_BOOTS,
              ToolsItems.REFINED_OBSIDIAN_HELMET, ToolsItems.REFINED_OBSIDIAN_CHESTPLATE, ToolsItems.REFINED_OBSIDIAN_LEGGINGS, ToolsItems.REFINED_OBSIDIAN_BOOTS,
              ToolsItems.STEEL_HELMET, ToolsItems.STEEL_CHESTPLATE, ToolsItems.STEEL_LEGGINGS, ToolsItems.STEEL_BOOTS
        );
    }

    private void addPaxels() {
        getItemBuilder(ItemTags.BREAKS_DECORATED_POTS).add(ToolsTags.Items.TOOLS_PAXELS);
        getItemBuilder(Tags.Items.TOOLS).add(ToolsTags.Items.TOOLS_PAXELS);
        addToTag(ToolsTags.Items.TOOLS_PAXELS,
              //Vanilla Paxels
              ToolsItems.WOOD_PAXEL,
              ToolsItems.STONE_PAXEL,
              ToolsItems.GOLD_PAXEL,
              ToolsItems.IRON_PAXEL,
              ToolsItems.DIAMOND_PAXEL,
              ToolsItems.NETHERITE_PAXEL,
              //Our paxels
              ToolsItems.BRONZE_PAXEL,
              ToolsItems.LAPIS_LAZULI_PAXEL,
              ToolsItems.OSMIUM_PAXEL,
              ToolsItems.REFINED_GLOWSTONE_PAXEL,
              ToolsItems.REFINED_OBSIDIAN_PAXEL,
              ToolsItems.STEEL_PAXEL
        );
    }

    private void addSwords() {
        addToTag(ItemTags.SWORDS,
              ToolsItems.BRONZE_SWORD,
              ToolsItems.LAPIS_LAZULI_SWORD,
              ToolsItems.OSMIUM_SWORD,
              ToolsItems.REFINED_GLOWSTONE_SWORD,
              ToolsItems.REFINED_OBSIDIAN_SWORD,
              ToolsItems.STEEL_SWORD
        );
    }

    private void addAxes() {
        addToTag(ItemTags.AXES,
              ToolsItems.BRONZE_AXE,
              ToolsItems.LAPIS_LAZULI_AXE,
              ToolsItems.OSMIUM_AXE,
              ToolsItems.REFINED_GLOWSTONE_AXE,
              ToolsItems.REFINED_OBSIDIAN_AXE,
              ToolsItems.STEEL_AXE
        );
    }

    private void addPickaxes() {
        addToTag(ItemTags.PICKAXES,
              ToolsItems.BRONZE_PICKAXE,
              ToolsItems.LAPIS_LAZULI_PICKAXE,
              ToolsItems.OSMIUM_PICKAXE,
              ToolsItems.REFINED_GLOWSTONE_PICKAXE,
              ToolsItems.REFINED_OBSIDIAN_PICKAXE,
              ToolsItems.STEEL_PICKAXE
        );
    }

    private void addShovels() {
        addToTag(ItemTags.SHOVELS,
              ToolsItems.BRONZE_SHOVEL,
              ToolsItems.LAPIS_LAZULI_SHOVEL,
              ToolsItems.OSMIUM_SHOVEL,
              ToolsItems.REFINED_GLOWSTONE_SHOVEL,
              ToolsItems.REFINED_OBSIDIAN_SHOVEL,
              ToolsItems.STEEL_SHOVEL
        );
    }

    private void addHoes() {
        addToTag(ItemTags.HOES,
              ToolsItems.BRONZE_HOE,
              ToolsItems.LAPIS_LAZULI_HOE,
              ToolsItems.OSMIUM_HOE,
              ToolsItems.REFINED_GLOWSTONE_HOE,
              ToolsItems.REFINED_OBSIDIAN_HOE,
              ToolsItems.STEEL_HOE
        );
    }

    private void addShields() {
        addToTag(Tags.Items.TOOLS_SHIELDS,
              ToolsItems.BRONZE_SHIELD,
              ToolsItems.LAPIS_LAZULI_SHIELD,
              ToolsItems.OSMIUM_SHIELD,
              ToolsItems.REFINED_GLOWSTONE_SHIELD,
              ToolsItems.REFINED_OBSIDIAN_SHIELD,
              ToolsItems.STEEL_SHIELD
        );
    }

    private void addHelmets() {
        addToTag(ItemTags.HEAD_ARMOR,
              ToolsItems.BRONZE_HELMET,
              ToolsItems.LAPIS_LAZULI_HELMET,
              ToolsItems.OSMIUM_HELMET,
              ToolsItems.REFINED_GLOWSTONE_HELMET,
              ToolsItems.REFINED_OBSIDIAN_HELMET,
              ToolsItems.STEEL_HELMET
        );
    }

    private void addChestplates() {
        addToTag(ItemTags.CHEST_ARMOR,
              ToolsItems.BRONZE_CHESTPLATE,
              ToolsItems.LAPIS_LAZULI_CHESTPLATE,
              ToolsItems.OSMIUM_CHESTPLATE,
              ToolsItems.REFINED_GLOWSTONE_CHESTPLATE,
              ToolsItems.REFINED_OBSIDIAN_CHESTPLATE,
              ToolsItems.STEEL_CHESTPLATE
        );
    }

    private void addLeggings() {
        addToTag(ItemTags.LEG_ARMOR,
              ToolsItems.BRONZE_LEGGINGS,
              ToolsItems.LAPIS_LAZULI_LEGGINGS,
              ToolsItems.OSMIUM_LEGGINGS,
              ToolsItems.REFINED_GLOWSTONE_LEGGINGS,
              ToolsItems.REFINED_OBSIDIAN_LEGGINGS,
              ToolsItems.STEEL_LEGGINGS
        );
    }

    private void addBoots() {
        addToTag(ItemTags.FOOT_ARMOR,
              ToolsItems.BRONZE_BOOTS,
              ToolsItems.LAPIS_LAZULI_BOOTS,
              ToolsItems.OSMIUM_BOOTS,
              ToolsItems.REFINED_GLOWSTONE_BOOTS,
              ToolsItems.REFINED_OBSIDIAN_BOOTS,
              ToolsItems.STEEL_BOOTS
        );
    }

    private void createTag(IntrinsicMekanismTagBuilder<Item> tag, Predicate<Item> matcher) {
        for (Holder<Item> itemProvider : ToolsItems.ITEMS.getEntries()) {
            Item item = itemProvider.value();
            if (matcher.test(item)) {
                tag.add(item);
            }
        }
    }
}