package mekanism.additions.common;

import mekanism.common.tags.TagUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

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

        public static final TagKey<Item> BALLOONS = tag("balloons");

        public static final TagKey<Item> FENCES_PLASTIC = forgeTag("fences/plastic");
        public static final TagKey<Item> FENCE_GATES_PLASTIC = forgeTag("fence_gates/plastic");
        public static final TagKey<Item> STAIRS_PLASTIC = forgeTag("stairs/plastic");
        public static final TagKey<Item> SLABS_PLASTIC = forgeTag("slabs/plastic");
        public static final TagKey<Item> STAIRS_PLASTIC_GLOW = forgeTag("stairs/plastic/glow");
        public static final TagKey<Item> SLABS_PLASTIC_GLOW = forgeTag("slabs/plastic/glow");
        public static final TagKey<Item> STAIRS_PLASTIC_TRANSPARENT = forgeTag("stairs/plastic/transparent");
        public static final TagKey<Item> SLABS_PLASTIC_TRANSPARENT = forgeTag("slabs/plastic/transparent");

        public static final TagKey<Item> GLOW_PANELS = tag("glow_panels");

        public static final TagKey<Item> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final TagKey<Item> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final TagKey<Item> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final TagKey<Item> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final TagKey<Item> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final TagKey<Item> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final TagKey<Item> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(MekanismAdditions.rl(name));
        }
    }

    public static class Blocks {

        private static void init() {
        }

        private Blocks() {
        }

        public static final TagKey<Block> FENCES_PLASTIC = forgeTag("fences/plastic");
        public static final TagKey<Block> FENCE_GATES_PLASTIC = forgeTag("fence_gates/plastic");
        public static final TagKey<Block> STAIRS_PLASTIC = forgeTag("stairs/plastic");
        public static final TagKey<Block> SLABS_PLASTIC = forgeTag("slabs/plastic");
        public static final TagKey<Block> STAIRS_PLASTIC_GLOW = forgeTag("stairs/plastic/glow");
        public static final TagKey<Block> SLABS_PLASTIC_GLOW = forgeTag("slabs/plastic/glow");
        public static final TagKey<Block> STAIRS_PLASTIC_TRANSPARENT = forgeTag("stairs/plastic/transparent");
        public static final TagKey<Block> SLABS_PLASTIC_TRANSPARENT = forgeTag("slabs/plastic/transparent");

        public static final TagKey<Block> GLOW_PANELS = tag("glow_panels");

        public static final TagKey<Block> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final TagKey<Block> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final TagKey<Block> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final TagKey<Block> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final TagKey<Block> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final TagKey<Block> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final TagKey<Block> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(MekanismAdditions.rl(name));
        }
    }

    public static class Entities {

        private static void init() {
        }

        private Entities() {
        }

        public static final TagKey<EntityType<?>> CREEPERS = forgeTag("creepers");
        public static final TagKey<EntityType<?>> ENDERMEN = forgeTag("endermen");

        private static TagKey<EntityType<?>> forgeTag(String name) {
            return TagUtils.createKey(ForgeRegistries.ENTITY_TYPES, new ResourceLocation("forge", name));
        }
    }
}