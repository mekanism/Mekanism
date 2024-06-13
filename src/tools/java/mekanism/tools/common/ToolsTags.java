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
        public static final TagKey<Block> INCORRECT_FOR_BRONZE_TOOL = tag("incorrect_for_bronze_tool");
        public static final TagKey<Block> INCORRECT_FOR_LAPIS_LAZULI_TOOL = tag("incorrect_for_lapis_lazuli_tool");
        public static final TagKey<Block> INCORRECT_FOR_OSMIUM_TOOL = tag("incorrect_for_osmium_tool");
        public static final TagKey<Block> INCORRECT_FOR_REFINED_GLOWSTONE_TOOL = tag("incorrect_for_refined_glowstone_tool");
        public static final TagKey<Block> INCORRECT_FOR_REFINED_OBSIDIAN_TOOL = tag("incorrect_for_refined_obsidian_tool");
        public static final TagKey<Block> INCORRECT_FOR_STEEL_TOOL = tag("incorrect_for_steel_tool");

        private static TagKey<Block> commonTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(MekanismTools.rl(name));
        }
    }

    public static class Items {

        private Items() {
        }

        public static final TagKey<Item> TOOLS_PAXEL = commonTag("tools/paxel");

        private static TagKey<Item> commonTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }
}