package mekanism.additions.common;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;

public class AdditionsTags {

    private AdditionsTags() {
    }

    public static class Items {

        private Items() {
        }

        public static final INamedTag<Item> BALLOONS = tag("balloons");

        public static final INamedTag<Item> FENCES_PLASTIC = forgeTag("fences/plastic");
        public static final INamedTag<Item> FENCE_GATES_PLASTIC = forgeTag("fence_gates/plastic");
        public static final INamedTag<Item> STAIRS_PLASTIC = forgeTag("stairs/plastic");
        public static final INamedTag<Item> SLABS_PLASTIC = forgeTag("slabs/plastic");
        public static final INamedTag<Item> STAIRS_PLASTIC_GLOW = forgeTag("stairs/plastic/glow");
        public static final INamedTag<Item> SLABS_PLASTIC_GLOW = forgeTag("slabs/plastic/glow");
        public static final INamedTag<Item> STAIRS_PLASTIC_TRANSPARENT = forgeTag("stairs/plastic/transparent");
        public static final INamedTag<Item> SLABS_PLASTIC_TRANSPARENT = forgeTag("slabs/plastic/transparent");

        public static final INamedTag<Item> GLOW_PANELS = tag("glow_panels");

        public static final INamedTag<Item> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final INamedTag<Item> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final INamedTag<Item> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final INamedTag<Item> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final INamedTag<Item> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final INamedTag<Item> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final INamedTag<Item> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static INamedTag<Item> forgeTag(String name) {
            return ItemTags.makeWrapperTag("forge:" + name);
        }

        private static INamedTag<Item> tag(String name) {
            return ItemTags.makeWrapperTag(MekanismAdditions.rl(name).toString());
        }
    }

    public static class Blocks {

        private Blocks() {
        }

        public static final INamedTag<Block> FENCES_PLASTIC = forgeTag("fences/plastic");
        public static final INamedTag<Block> FENCE_GATES_PLASTIC = forgeTag("fence_gates/plastic");
        public static final INamedTag<Block> STAIRS_PLASTIC = forgeTag("stairs/plastic");
        public static final INamedTag<Block> SLABS_PLASTIC = forgeTag("slabs/plastic");
        public static final INamedTag<Block> STAIRS_PLASTIC_GLOW = forgeTag("stairs/plastic/glow");
        public static final INamedTag<Block> SLABS_PLASTIC_GLOW = forgeTag("slabs/plastic/glow");
        public static final INamedTag<Block> STAIRS_PLASTIC_TRANSPARENT = forgeTag("stairs/plastic/transparent");
        public static final INamedTag<Block> SLABS_PLASTIC_TRANSPARENT = forgeTag("slabs/plastic/transparent");

        public static final INamedTag<Block> GLOW_PANELS = tag("glow_panels");

        public static final INamedTag<Block> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final INamedTag<Block> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final INamedTag<Block> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final INamedTag<Block> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final INamedTag<Block> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final INamedTag<Block> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final INamedTag<Block> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static INamedTag<Block> forgeTag(String name) {
            return BlockTags.makeWrapperTag("forge:" + name);
        }

        private static INamedTag<Block> tag(String name) {
            return BlockTags.makeWrapperTag(MekanismAdditions.rl(name).toString());
        }
    }

    public static class Entities {

        private Entities() {
        }

        public static final INamedTag<EntityType<?>> CREEPERS = forgeTag("creepers");
        public static final INamedTag<EntityType<?>> ENDERMEN = forgeTag("endermen");

        private static INamedTag<EntityType<?>> forgeTag(String name) {
            return EntityTypeTags.getTagById("forge:" + name);
        }
    }
}