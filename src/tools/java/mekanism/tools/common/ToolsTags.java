package mekanism.tools.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ToolsTags {

    /**
     * Call to force make sure this is all initialized
     */
    public static void init() {
        Blocks.init();
        Items.init();
    }

    private ToolsTags() {
    }

    public static class Blocks {

        private static void init() {
        }

        private Blocks() {
        }

        public static final TagKey<Block> MINEABLE_WITH_PAXEL = forgeTag("mineable/paxel");
        public static final TagKey<Block> NEEDS_BRONZE_TOOL = tag("needs_bronze_tool");
        public static final TagKey<Block> NEEDS_LAPIS_LAZULI_TOOL = tag("needs_lapis_lazuli_tool");
        public static final TagKey<Block> NEEDS_OSMIUM_TOOL = tag("needs_osmium_tool");
        public static final TagKey<Block> NEEDS_REFINED_GLOWSTONE_TOOL = tag("needs_refined_glowstone_tool");
        public static final TagKey<Block> NEEDS_REFINED_OBSIDIAN_TOOL = tag("needs_refined_obsidian_tool");
        public static final TagKey<Block> NEEDS_STEEL_TOOL = tag("needs_steel_tool");

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(MekanismTools.rl(name));
        }
    }

    public static class Items {

        private static void init() {
        }

        private Items() {
        }

        public static final TagKey<Item> ADVANCEMENTS_ANY_PAXEL = tag("advancements/any_paxel");
        public static final TagKey<Item> ADVANCEMENTS_REFINED_GLOWSTONE = tag("advancements/refined_glowstone_armor");
        public static final TagKey<Item> ADVANCEMENTS_REFINED_OBSIDIAN = tag("advancements/refined_obsidian_armor");
        public static final TagKey<Item> ADVANCEMENTS_ALTERNATE_ARMOR = tag("advancements/alternate_armor");
        public static final TagKey<Item> ADVANCEMENTS_ALTERNATE_TOOLS = tag("advancements/alternate_tools");
        public static final TagKey<Item> ADVANCEMENTS_SHIELDS = tag("advancements/shields");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(MekanismTools.rl(name));
        }
    }
}