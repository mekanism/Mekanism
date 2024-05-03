package mekanism.tools.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ToolsTags {

    private ToolsTags() {
    }

    public static class Blocks {

        private Blocks() {
        }

        public static final TagKey<Block> MINEABLE_WITH_PAXEL = commonTag("mineable/paxel");
        public static final TagKey<Block> NEEDS_BRONZE_TOOL = tag("needs_bronze_tool");
        public static final TagKey<Block> NEEDS_LAPIS_LAZULI_TOOL = tag("needs_lapis_lazuli_tool");
        public static final TagKey<Block> NEEDS_OSMIUM_TOOL = tag("needs_osmium_tool");
        public static final TagKey<Block> NEEDS_REFINED_GLOWSTONE_TOOL = tag("needs_refined_glowstone_tool");
        public static final TagKey<Block> NEEDS_REFINED_OBSIDIAN_TOOL = tag("needs_refined_obsidian_tool");
        public static final TagKey<Block> NEEDS_STEEL_TOOL = tag("needs_steel_tool");

        private static TagKey<Block> commonTag(String name) {
            return BlockTags.create(new ResourceLocation("c", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(MekanismTools.rl(name));
        }
    }

    public static class Items {

        private Items() {
        }

        public static final TagKey<Item> TOOLS_PAXELS = commonTag("tools/paxels");

        private static TagKey<Item> commonTag(String name) {
            return ItemTags.create(new ResourceLocation("c", name));
        }
    }
}