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

        private static Named<Block> forgeTag(String name) {
            return BlockTags.bind("forge:" + name);
        }

        private static Named<Block> tag(String name) {
            return BlockTags.bind(MekanismTools.rl(name).toString());
        }
    }
}