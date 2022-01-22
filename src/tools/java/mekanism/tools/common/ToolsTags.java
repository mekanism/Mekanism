package mekanism.tools.common;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.block.Block;

public class ToolsTags {

    /**
     * Call to force make sure this is all initialized
     */
    public static void init() {
        Blocks.init();
    }

    private ToolsTags() {
    }

    public static class Blocks {

        private static void init() {
        }

        private Blocks() {
        }

        public static final Named<Block> MINEABLE_WITH_PAXEL = forgeTag("mineable/paxel");
        public static final Named<Block> NEEDS_BRONZE_TOOL = tag("needs_bronze_tool");
        public static final Named<Block> NEEDS_LAPIS_LAZULI_TOOL = tag("needs_lapis_lazuli_tool");
        public static final Named<Block> NEEDS_OSMIUM_TOOL = tag("needs_osmium_tool");
        public static final Named<Block> NEEDS_REFINED_GLOWSTONE_TOOL = tag("needs_refined_glowstone_tool");
        public static final Named<Block> NEEDS_REFINED_OBSIDIAN_TOOL = tag("needs_refined_obsidian_tool");
        public static final Named<Block> NEEDS_STEEL_TOOL = tag("needs_steel_tool");

        private static Named<Block> forgeTag(String name) {
            return BlockTags.bind("forge:" + name);
        }

        private static Named<Block> tag(String name) {
            return BlockTags.bind(MekanismTools.rl(name).toString());
        }
    }
}