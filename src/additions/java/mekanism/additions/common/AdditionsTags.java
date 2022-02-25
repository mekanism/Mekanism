package mekanism.additions.common;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AdditionsTags {

    /**
     * Call to force make sure this is all initialized
     */
    public static void init() {
        Items.init();
        Blocks.init();
        Entities.init();
    }

    private AdditionsTags() {
    }

    public static class Items {

        private static void init() {
        }

        private Items() {
        }

        public static final Named<Item> BALLOONS = tag("balloons");

        public static final Named<Item> FENCES_PLASTIC = forgeTag("fences/plastic");
        public static final Named<Item> FENCE_GATES_PLASTIC = forgeTag("fence_gates/plastic");
        public static final Named<Item> STAIRS_PLASTIC = forgeTag("stairs/plastic");
        public static final Named<Item> SLABS_PLASTIC = forgeTag("slabs/plastic");
        public static final Named<Item> STAIRS_PLASTIC_GLOW = forgeTag("stairs/plastic/glow");
        public static final Named<Item> SLABS_PLASTIC_GLOW = forgeTag("slabs/plastic/glow");
        public static final Named<Item> STAIRS_PLASTIC_TRANSPARENT = forgeTag("stairs/plastic/transparent");
        public static final Named<Item> SLABS_PLASTIC_TRANSPARENT = forgeTag("slabs/plastic/transparent");

        public static final Named<Item> GLOW_PANELS = tag("glow_panels");

        public static final Named<Item> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final Named<Item> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final Named<Item> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final Named<Item> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final Named<Item> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final Named<Item> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final Named<Item> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static Named<Item> forgeTag(String name) {
            return ItemTags.bind("forge:" + name);
        }

        private static Named<Item> tag(String name) {
            return ItemTags.bind(MekanismAdditions.rl(name).toString());
        }
    }

    public static class Blocks {

        private static void init() {
        }

        private Blocks() {
        }

        public static final Named<Block> FENCES_PLASTIC = forgeTag("fences/plastic");
        public static final Named<Block> FENCE_GATES_PLASTIC = forgeTag("fence_gates/plastic");
        public static final Named<Block> STAIRS_PLASTIC = forgeTag("stairs/plastic");
        public static final Named<Block> SLABS_PLASTIC = forgeTag("slabs/plastic");
        public static final Named<Block> STAIRS_PLASTIC_GLOW = forgeTag("stairs/plastic/glow");
        public static final Named<Block> SLABS_PLASTIC_GLOW = forgeTag("slabs/plastic/glow");
        public static final Named<Block> STAIRS_PLASTIC_TRANSPARENT = forgeTag("stairs/plastic/transparent");
        public static final Named<Block> SLABS_PLASTIC_TRANSPARENT = forgeTag("slabs/plastic/transparent");

        public static final Named<Block> GLOW_PANELS = tag("glow_panels");

        public static final Named<Block> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final Named<Block> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final Named<Block> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final Named<Block> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final Named<Block> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final Named<Block> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final Named<Block> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static Named<Block> forgeTag(String name) {
            return BlockTags.bind("forge:" + name);
        }

        private static Named<Block> tag(String name) {
            return BlockTags.bind(MekanismAdditions.rl(name).toString());
        }
    }

    public static class Entities {

        private static void init() {
        }

        private Entities() {
        }

        public static final Named<EntityType<?>> CREEPERS = forgeTag("creepers");
        public static final Named<EntityType<?>> ENDERMEN = forgeTag("endermen");

        private static Named<EntityType<?>> forgeTag(String name) {
            return EntityTypeTags.bind("forge:" + name);
        }
    }
}