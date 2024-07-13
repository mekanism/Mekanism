package mekanism.tools.common;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.IntrinsicMekanismTagBuilder;
import mekanism.tools.common.item.ItemMekanismAxe;
import mekanism.tools.common.item.ItemMekanismPaxel;
import mekanism.tools.common.item.ItemMekanismPickaxe;
import mekanism.tools.common.item.ItemMekanismSword;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
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
        //Make refined glowstone armor make you immune to freezing because of the light it gives off
        addToTag(ItemTags.FREEZE_IMMUNE_WEARABLES,
              ToolsItems.REFINED_GLOWSTONE_HELMET,
              ToolsItems.REFINED_GLOWSTONE_CHESTPLATE,
              ToolsItems.REFINED_GLOWSTONE_LEGGINGS,
              ToolsItems.REFINED_GLOWSTONE_BOOTS
        );
        getBlockBuilder(ToolsTags.Blocks.MINEABLE_WITH_PAXEL).add(
              BlockTags.MINEABLE_WITH_AXE,
              BlockTags.MINEABLE_WITH_PICKAXE,
              BlockTags.MINEABLE_WITH_SHOVEL
        );
        getBlockBuilder(ToolsTags.Blocks.INCORRECT_FOR_BRONZE_TOOL).add(BlockTags.INCORRECT_FOR_IRON_TOOL);
        getBlockBuilder(ToolsTags.Blocks.INCORRECT_FOR_LAPIS_LAZULI_TOOL).add(BlockTags.INCORRECT_FOR_STONE_TOOL);
        getBlockBuilder(ToolsTags.Blocks.INCORRECT_FOR_OSMIUM_TOOL).add(BlockTags.INCORRECT_FOR_IRON_TOOL);
        getBlockBuilder(ToolsTags.Blocks.INCORRECT_FOR_REFINED_GLOWSTONE_TOOL).add(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        getBlockBuilder(ToolsTags.Blocks.INCORRECT_FOR_REFINED_OBSIDIAN_TOOL).add(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
        getBlockBuilder(ToolsTags.Blocks.INCORRECT_FOR_STEEL_TOOL).add(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        createTag(getItemBuilder(ItemTags.CLUSTER_MAX_HARVESTABLES), item -> item instanceof ItemMekanismPickaxe || item instanceof ItemMekanismPaxel);
        createTag(getItemBuilder(Tags.Items.MINING_TOOL_TOOLS), item -> item instanceof ItemMekanismPickaxe || item instanceof ItemMekanismPaxel);
        createTag(getItemBuilder(Tags.Items.MELEE_WEAPON_TOOLS), item -> item instanceof ItemMekanismSword || item instanceof ItemMekanismAxe || item instanceof ItemMekanismPaxel);
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
        getItemBuilder(ItemTags.BREAKS_DECORATED_POTS).add(ToolsTags.Items.TOOLS_PAXEL);
        getItemBuilder(ItemTags.WEAPON_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getItemBuilder(ItemTags.SHARP_WEAPON_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getItemBuilder(ItemTags.MINING_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getItemBuilder(ItemTags.MINING_LOOT_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getItemBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getItemBuilder(Tags.Items.TOOLS).add(ToolsTags.Items.TOOLS_PAXEL);
        addToTag(ToolsTags.Items.TOOLS_PAXEL,
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
        ItemLike[] shields = {
              ToolsItems.BRONZE_SHIELD,
              ToolsItems.LAPIS_LAZULI_SHIELD,
              ToolsItems.OSMIUM_SHIELD,
              ToolsItems.REFINED_GLOWSTONE_SHIELD,
              ToolsItems.REFINED_OBSIDIAN_SHIELD,
              ToolsItems.STEEL_SHIELD
        };
        addToTag(Tags.Items.TOOLS_SHIELD, shields);
        addToTag(ItemTags.DURABILITY_ENCHANTABLE, shields);
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