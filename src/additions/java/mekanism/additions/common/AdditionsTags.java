package mekanism.additions.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AdditionsTags {

    private AdditionsTags() {
    }

    public static class Items {

        private Items() {
        }

        public static final TagKey<Item> BALLOONS = tag("balloons");

        public static final TagKey<Item> COMMON_FENCES_PLASTIC = commonTag("fences/plastic");
        public static final TagKey<Item> COMMON_FENCE_GATES_PLASTIC = commonTag("fence_gates/plastic");
        public static final TagKey<Item> COMMON_STAIRS_PLASTIC = commonTag("stairs/plastic");
        public static final TagKey<Item> COMMON_SLABS_PLASTIC = commonTag("slabs/plastic");
        public static final TagKey<Item> COMMON_STAIRS_PLASTIC_GLOW = commonTag("stairs/plastic/glow");
        public static final TagKey<Item> COMMON_SLABS_PLASTIC_GLOW = commonTag("slabs/plastic/glow");
        public static final TagKey<Item> COMMON_STAIRS_PLASTIC_TRANSPARENT = commonTag("stairs/plastic/transparent");
        public static final TagKey<Item> COMMON_SLABS_PLASTIC_TRANSPARENT = commonTag("slabs/plastic/transparent");

        public static final TagKey<Item> FENCES_PLASTIC = tag("plastic_fences");
        public static final TagKey<Item> FENCES_PLASTIC_NORMAL = tag("plastic_fences/normal");
        public static final TagKey<Item> FENCE_GATES_PLASTIC = tag("plastic_fence_gates");
        public static final TagKey<Item> FENCE_GATES_PLASTIC_NORMAL = tag("plastic_fence_gates/normal");
        public static final TagKey<Item> STAIRS_PLASTIC = tag("plastic_stairs");
        public static final TagKey<Item> STAIRS_PLASTIC_NORMAL = tag("plastic_stairs/normal");
        public static final TagKey<Item> SLABS_PLASTIC = tag("plastic_slabs");
        public static final TagKey<Item> SLABS_PLASTIC_NORMAL = tag("plastic_slabs/normal");
        public static final TagKey<Item> STAIRS_PLASTIC_GLOW = tag("plastic_stairs/glow");
        public static final TagKey<Item> SLABS_PLASTIC_GLOW = tag("plastic_slabs/glow");
        public static final TagKey<Item> STAIRS_PLASTIC_TRANSPARENT = tag("plastic_stairs/transparent");
        public static final TagKey<Item> SLABS_PLASTIC_TRANSPARENT = tag("plastic_slabs/transparent");

        public static final TagKey<Item> GLOW_PANELS = tag("glow_panels");

        public static final TagKey<Item> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final TagKey<Item> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final TagKey<Item> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final TagKey<Item> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final TagKey<Item> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final TagKey<Item> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final TagKey<Item> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static TagKey<Item> commonTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(MekanismAdditions.rl(name));
        }
    }

    public static class Blocks {

        private Blocks() {
        }

        public static final TagKey<Block> FENCES_PLASTIC = commonTag("fences/plastic");
        public static final TagKey<Block> FENCE_GATES_PLASTIC = commonTag("fence_gates/plastic");
        public static final TagKey<Block> STAIRS_PLASTIC = commonTag("stairs/plastic");
        public static final TagKey<Block> SLABS_PLASTIC = commonTag("slabs/plastic");
        public static final TagKey<Block> STAIRS_PLASTIC_GLOW = commonTag("stairs/plastic/glow");
        public static final TagKey<Block> SLABS_PLASTIC_GLOW = commonTag("slabs/plastic/glow");
        public static final TagKey<Block> STAIRS_PLASTIC_TRANSPARENT = commonTag("stairs/plastic/transparent");
        public static final TagKey<Block> SLABS_PLASTIC_TRANSPARENT = commonTag("slabs/plastic/transparent");

        public static final TagKey<Block> GLOW_PANELS = tag("glow_panels");

        public static final TagKey<Block> PLASTIC_BLOCKS = tag("plastic_blocks");
        public static final TagKey<Block> PLASTIC_BLOCKS_GLOW = tag("plastic_blocks/glow");
        public static final TagKey<Block> PLASTIC_BLOCKS_PLASTIC = tag("plastic_blocks/plastic");
        public static final TagKey<Block> PLASTIC_BLOCKS_REINFORCED = tag("plastic_blocks/reinforced");
        public static final TagKey<Block> PLASTIC_BLOCKS_ROAD = tag("plastic_blocks/road");
        public static final TagKey<Block> PLASTIC_BLOCKS_SLICK = tag("plastic_blocks/slick");
        public static final TagKey<Block> PLASTIC_BLOCKS_TRANSPARENT = tag("plastic_blocks/transparent");

        private static TagKey<Block> commonTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(MekanismAdditions.rl(name));
        }
    }

    public static class DamageTypes {

        private DamageTypes() {
        }

        public static final TagKey<DamageType> BALLOON_INVULNERABLE = tag("balloon_invulnerable");

        private static TagKey<DamageType> tag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, MekanismAdditions.rl(name));
        }
    }

    public static class Entities {

        private Entities() {
        }

        public static final TagKey<EntityType<?>> BOGGED = commonTag("bogged");
        public static final TagKey<EntityType<?>> ENDERMEN = commonTag("endermen");

        private static TagKey<EntityType<?>> commonTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }
}