package mekanism.additions.common;

import javax.annotation.Nullable;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.common.tag.BaseTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AdditionsTagProvider extends BaseTagProvider {

    public AdditionsTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
        addEntities();
        addBalloons();
        addSlabs();
        addStairs();
        addFences();
        addFenceGates();
        addGlowPanels();
        addPlasticBlocks();
    }

    private void addEntities() {
        addToTag(EntityTypeTags.SKELETONS, AdditionsEntityTypes.BABY_SKELETON, AdditionsEntityTypes.BABY_STRAY, AdditionsEntityTypes.BABY_WITHER_SKELETON);
        getEntityTypeBuilder(AdditionsTags.Entities.CREEPERS).add(EntityType.CREEPER, AdditionsEntityTypes.BABY_CREEPER.getEntityType());
        getEntityTypeBuilder(AdditionsTags.Entities.ENDERMEN).add(EntityType.ENDERMAN, AdditionsEntityTypes.BABY_ENDERMAN.getEntityType());
    }

    private void addBalloons() {
        addToTag(AdditionsTags.Items.BALLOONS,
              AdditionsItems.BLACK_BALLOON, AdditionsItems.RED_BALLOON, AdditionsItems.GREEN_BALLOON, AdditionsItems.BROWN_BALLOON, AdditionsItems.BLUE_BALLOON,
              AdditionsItems.PURPLE_BALLOON, AdditionsItems.CYAN_BALLOON, AdditionsItems.LIGHT_GRAY_BALLOON, AdditionsItems.GRAY_BALLOON, AdditionsItems.PINK_BALLOON,
              AdditionsItems.LIME_BALLOON, AdditionsItems.YELLOW_BALLOON, AdditionsItems.LIGHT_BLUE_BALLOON, AdditionsItems.MAGENTA_BALLOON, AdditionsItems.ORANGE_BALLOON,
              AdditionsItems.WHITE_BALLOON);
    }

    private void addSlabs() {
        addToTags(AdditionsTags.Items.SLABS_PLASTIC, AdditionsTags.Blocks.SLABS_PLASTIC,
              AdditionsBlocks.BLACK_PLASTIC_SLAB, AdditionsBlocks.RED_PLASTIC_SLAB, AdditionsBlocks.GREEN_PLASTIC_SLAB, AdditionsBlocks.BROWN_PLASTIC_SLAB,
              AdditionsBlocks.BLUE_PLASTIC_SLAB, AdditionsBlocks.PURPLE_PLASTIC_SLAB, AdditionsBlocks.CYAN_PLASTIC_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_SLAB,
              AdditionsBlocks.GRAY_PLASTIC_SLAB, AdditionsBlocks.PINK_PLASTIC_SLAB, AdditionsBlocks.LIME_PLASTIC_SLAB, AdditionsBlocks.YELLOW_PLASTIC_SLAB,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_SLAB, AdditionsBlocks.ORANGE_PLASTIC_SLAB, AdditionsBlocks.WHITE_PLASTIC_SLAB);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.Blocks.SLABS_PLASTIC_GLOW,
              AdditionsBlocks.BLACK_PLASTIC_GLOW_SLAB, AdditionsBlocks.RED_PLASTIC_GLOW_SLAB, AdditionsBlocks.GREEN_PLASTIC_GLOW_SLAB, AdditionsBlocks.BROWN_PLASTIC_GLOW_SLAB,
              AdditionsBlocks.BLUE_PLASTIC_GLOW_SLAB, AdditionsBlocks.PURPLE_PLASTIC_GLOW_SLAB, AdditionsBlocks.CYAN_PLASTIC_GLOW_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_SLAB,
              AdditionsBlocks.GRAY_PLASTIC_GLOW_SLAB, AdditionsBlocks.PINK_PLASTIC_GLOW_SLAB, AdditionsBlocks.LIME_PLASTIC_GLOW_SLAB, AdditionsBlocks.YELLOW_PLASTIC_GLOW_SLAB,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_SLAB, AdditionsBlocks.ORANGE_PLASTIC_GLOW_SLAB, AdditionsBlocks.WHITE_PLASTIC_GLOW_SLAB);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, AdditionsTags.Blocks.SLABS_PLASTIC_TRANSPARENT,
              AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_SLAB,
              AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_SLAB,
              AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_SLAB,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_SLAB);
        getItemBuilder(ItemTags.SLABS).add(AdditionsTags.Items.SLABS_PLASTIC, AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT);
        getBlockBuilder(BlockTags.SLABS).add(AdditionsTags.Blocks.SLABS_PLASTIC, AdditionsTags.Blocks.SLABS_PLASTIC_GLOW, AdditionsTags.Blocks.SLABS_PLASTIC_TRANSPARENT);
    }

    private void addStairs() {
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC, AdditionsTags.Blocks.STAIRS_PLASTIC,
              AdditionsBlocks.BLACK_PLASTIC_STAIRS, AdditionsBlocks.RED_PLASTIC_STAIRS, AdditionsBlocks.GREEN_PLASTIC_STAIRS, AdditionsBlocks.BROWN_PLASTIC_STAIRS,
              AdditionsBlocks.BLUE_PLASTIC_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_STAIRS, AdditionsBlocks.CYAN_PLASTIC_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_STAIRS,
              AdditionsBlocks.GRAY_PLASTIC_STAIRS, AdditionsBlocks.PINK_PLASTIC_STAIRS, AdditionsBlocks.LIME_PLASTIC_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_STAIRS,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_STAIRS, AdditionsBlocks.WHITE_PLASTIC_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.Blocks.STAIRS_PLASTIC_GLOW,
              AdditionsBlocks.BLACK_PLASTIC_GLOW_STAIRS, AdditionsBlocks.RED_PLASTIC_GLOW_STAIRS, AdditionsBlocks.GREEN_PLASTIC_GLOW_STAIRS, AdditionsBlocks.BROWN_PLASTIC_GLOW_STAIRS,
              AdditionsBlocks.BLUE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.CYAN_PLASTIC_GLOW_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_STAIRS,
              AdditionsBlocks.GRAY_PLASTIC_GLOW_STAIRS, AdditionsBlocks.PINK_PLASTIC_GLOW_STAIRS, AdditionsBlocks.LIME_PLASTIC_GLOW_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_GLOW_STAIRS,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.WHITE_PLASTIC_GLOW_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT, AdditionsTags.Blocks.STAIRS_PLASTIC_TRANSPARENT,
              AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_STAIRS,
              AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_STAIRS,
              AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_STAIRS,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_STAIRS);
        getItemBuilder(ItemTags.STAIRS).add(AdditionsTags.Items.STAIRS_PLASTIC, AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT);
        getBlockBuilder(BlockTags.STAIRS).add(AdditionsTags.Blocks.STAIRS_PLASTIC, AdditionsTags.Blocks.STAIRS_PLASTIC_GLOW, AdditionsTags.Blocks.STAIRS_PLASTIC_TRANSPARENT);
    }

    private void addFences() {
        addToTags(AdditionsTags.Items.FENCES_PLASTIC, AdditionsTags.Blocks.FENCES_PLASTIC,
              AdditionsBlocks.BLACK_PLASTIC_FENCE, AdditionsBlocks.RED_PLASTIC_FENCE, AdditionsBlocks.GREEN_PLASTIC_FENCE, AdditionsBlocks.BROWN_PLASTIC_FENCE,
              AdditionsBlocks.BLUE_PLASTIC_FENCE, AdditionsBlocks.PURPLE_PLASTIC_FENCE, AdditionsBlocks.CYAN_PLASTIC_FENCE, AdditionsBlocks.LIGHT_GRAY_PLASTIC_FENCE,
              AdditionsBlocks.GRAY_PLASTIC_FENCE, AdditionsBlocks.PINK_PLASTIC_FENCE, AdditionsBlocks.LIME_PLASTIC_FENCE, AdditionsBlocks.YELLOW_PLASTIC_FENCE,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_FENCE, AdditionsBlocks.MAGENTA_PLASTIC_FENCE, AdditionsBlocks.ORANGE_PLASTIC_FENCE, AdditionsBlocks.WHITE_PLASTIC_FENCE);
        getItemBuilder(Tags.Items.FENCES).add(AdditionsTags.Items.FENCES_PLASTIC);
        getBlockBuilder(Tags.Blocks.FENCES).add(AdditionsTags.Blocks.FENCES_PLASTIC);
        getItemBuilder(ItemTags.FENCES).add(AdditionsTags.Items.FENCES_PLASTIC);
        getBlockBuilder(BlockTags.FENCES).add(AdditionsTags.Blocks.FENCES_PLASTIC);
    }

    private void addFenceGates() {
        addToTags(AdditionsTags.Items.FENCE_GATES_PLASTIC, AdditionsTags.Blocks.FENCE_GATES_PLASTIC,
              AdditionsBlocks.BLACK_PLASTIC_FENCE_GATE, AdditionsBlocks.RED_PLASTIC_FENCE_GATE, AdditionsBlocks.GREEN_PLASTIC_FENCE_GATE,
              AdditionsBlocks.BROWN_PLASTIC_FENCE_GATE, AdditionsBlocks.BLUE_PLASTIC_FENCE_GATE, AdditionsBlocks.PURPLE_PLASTIC_FENCE_GATE,
              AdditionsBlocks.CYAN_PLASTIC_FENCE_GATE, AdditionsBlocks.LIGHT_GRAY_PLASTIC_FENCE_GATE, AdditionsBlocks.GRAY_PLASTIC_FENCE_GATE,
              AdditionsBlocks.PINK_PLASTIC_FENCE_GATE, AdditionsBlocks.LIME_PLASTIC_FENCE_GATE, AdditionsBlocks.YELLOW_PLASTIC_FENCE_GATE,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_FENCE_GATE, AdditionsBlocks.MAGENTA_PLASTIC_FENCE_GATE, AdditionsBlocks.ORANGE_PLASTIC_FENCE_GATE,
              AdditionsBlocks.WHITE_PLASTIC_FENCE_GATE);
        getItemBuilder(Tags.Items.FENCE_GATES).add(AdditionsTags.Items.FENCE_GATES_PLASTIC);
        getBlockBuilder(Tags.Blocks.FENCE_GATES).add(AdditionsTags.Blocks.FENCE_GATES_PLASTIC);
        getBlockBuilder(BlockTags.FENCE_GATES).add(AdditionsTags.Blocks.FENCE_GATES_PLASTIC);
    }

    private void addGlowPanels() {
        addToTags(AdditionsTags.Items.GLOW_PANELS, AdditionsTags.Blocks.GLOW_PANELS,
              AdditionsBlocks.BLACK_GLOW_PANEL, AdditionsBlocks.RED_GLOW_PANEL, AdditionsBlocks.GREEN_GLOW_PANEL, AdditionsBlocks.BROWN_GLOW_PANEL,
              AdditionsBlocks.BLUE_GLOW_PANEL, AdditionsBlocks.PURPLE_GLOW_PANEL, AdditionsBlocks.CYAN_GLOW_PANEL, AdditionsBlocks.LIGHT_GRAY_GLOW_PANEL,
              AdditionsBlocks.GRAY_GLOW_PANEL, AdditionsBlocks.PINK_GLOW_PANEL, AdditionsBlocks.LIME_GLOW_PANEL, AdditionsBlocks.YELLOW_GLOW_PANEL,
              AdditionsBlocks.LIGHT_BLUE_GLOW_PANEL, AdditionsBlocks.MAGENTA_GLOW_PANEL, AdditionsBlocks.ORANGE_GLOW_PANEL, AdditionsBlocks.WHITE_GLOW_PANEL);
    }

    private void addPlasticBlocks() {
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, AdditionsTags.Blocks.PLASTIC_BLOCKS_GLOW,
              AdditionsBlocks.BLACK_PLASTIC_GLOW_BLOCK, AdditionsBlocks.RED_PLASTIC_GLOW_BLOCK, AdditionsBlocks.GREEN_PLASTIC_GLOW_BLOCK,
              AdditionsBlocks.BROWN_PLASTIC_GLOW_BLOCK, AdditionsBlocks.BLUE_PLASTIC_GLOW_BLOCK, AdditionsBlocks.PURPLE_PLASTIC_GLOW_BLOCK,
              AdditionsBlocks.CYAN_PLASTIC_GLOW_BLOCK, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_BLOCK, AdditionsBlocks.GRAY_PLASTIC_GLOW_BLOCK,
              AdditionsBlocks.PINK_PLASTIC_GLOW_BLOCK, AdditionsBlocks.LIME_PLASTIC_GLOW_BLOCK, AdditionsBlocks.YELLOW_PLASTIC_GLOW_BLOCK,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_BLOCK, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_BLOCK, AdditionsBlocks.ORANGE_PLASTIC_GLOW_BLOCK,
              AdditionsBlocks.WHITE_PLASTIC_GLOW_BLOCK);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC, AdditionsTags.Blocks.PLASTIC_BLOCKS_PLASTIC,
              AdditionsBlocks.BLACK_PLASTIC_BLOCK, AdditionsBlocks.RED_PLASTIC_BLOCK, AdditionsBlocks.GREEN_PLASTIC_BLOCK, AdditionsBlocks.BROWN_PLASTIC_BLOCK,
              AdditionsBlocks.BLUE_PLASTIC_BLOCK, AdditionsBlocks.PURPLE_PLASTIC_BLOCK, AdditionsBlocks.CYAN_PLASTIC_BLOCK, AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK,
              AdditionsBlocks.GRAY_PLASTIC_BLOCK, AdditionsBlocks.PINK_PLASTIC_BLOCK, AdditionsBlocks.LIME_PLASTIC_BLOCK, AdditionsBlocks.YELLOW_PLASTIC_BLOCK,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK, AdditionsBlocks.MAGENTA_PLASTIC_BLOCK, AdditionsBlocks.ORANGE_PLASTIC_BLOCK, AdditionsBlocks.WHITE_PLASTIC_BLOCK);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Blocks.PLASTIC_BLOCKS_REINFORCED,
              AdditionsBlocks.BLACK_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.RED_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.GREEN_REINFORCED_PLASTIC_BLOCK,
              AdditionsBlocks.BROWN_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.BLUE_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.PURPLE_REINFORCED_PLASTIC_BLOCK,
              AdditionsBlocks.CYAN_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.GRAY_REINFORCED_PLASTIC_BLOCK,
              AdditionsBlocks.PINK_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.LIME_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.YELLOW_REINFORCED_PLASTIC_BLOCK,
              AdditionsBlocks.LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.MAGENTA_REINFORCED_PLASTIC_BLOCK, AdditionsBlocks.ORANGE_REINFORCED_PLASTIC_BLOCK,
              AdditionsBlocks.WHITE_REINFORCED_PLASTIC_BLOCK);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, AdditionsTags.Blocks.PLASTIC_BLOCKS_ROAD,
              AdditionsBlocks.BLACK_PLASTIC_ROAD, AdditionsBlocks.RED_PLASTIC_ROAD, AdditionsBlocks.GREEN_PLASTIC_ROAD, AdditionsBlocks.BROWN_PLASTIC_ROAD,
              AdditionsBlocks.BLUE_PLASTIC_ROAD, AdditionsBlocks.PURPLE_PLASTIC_ROAD, AdditionsBlocks.CYAN_PLASTIC_ROAD, AdditionsBlocks.LIGHT_GRAY_PLASTIC_ROAD,
              AdditionsBlocks.GRAY_PLASTIC_ROAD, AdditionsBlocks.PINK_PLASTIC_ROAD, AdditionsBlocks.LIME_PLASTIC_ROAD, AdditionsBlocks.YELLOW_PLASTIC_ROAD,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_ROAD, AdditionsBlocks.MAGENTA_PLASTIC_ROAD, AdditionsBlocks.ORANGE_PLASTIC_ROAD, AdditionsBlocks.WHITE_PLASTIC_ROAD);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_SLICK, AdditionsTags.Blocks.PLASTIC_BLOCKS_SLICK,
              AdditionsBlocks.BLACK_SLICK_PLASTIC_BLOCK, AdditionsBlocks.RED_SLICK_PLASTIC_BLOCK, AdditionsBlocks.GREEN_SLICK_PLASTIC_BLOCK,
              AdditionsBlocks.BROWN_SLICK_PLASTIC_BLOCK, AdditionsBlocks.BLUE_SLICK_PLASTIC_BLOCK, AdditionsBlocks.PURPLE_SLICK_PLASTIC_BLOCK,
              AdditionsBlocks.CYAN_SLICK_PLASTIC_BLOCK, AdditionsBlocks.LIGHT_GRAY_SLICK_PLASTIC_BLOCK, AdditionsBlocks.GRAY_SLICK_PLASTIC_BLOCK,
              AdditionsBlocks.PINK_SLICK_PLASTIC_BLOCK, AdditionsBlocks.LIME_SLICK_PLASTIC_BLOCK, AdditionsBlocks.YELLOW_SLICK_PLASTIC_BLOCK,
              AdditionsBlocks.LIGHT_BLUE_SLICK_PLASTIC_BLOCK, AdditionsBlocks.MAGENTA_SLICK_PLASTIC_BLOCK, AdditionsBlocks.ORANGE_SLICK_PLASTIC_BLOCK,
              AdditionsBlocks.WHITE_SLICK_PLASTIC_BLOCK);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_TRANSPARENT, AdditionsTags.Blocks.PLASTIC_BLOCKS_TRANSPARENT,
              AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_BLOCK,
              AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_BLOCK,
              AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_BLOCK,
              AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_BLOCK,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_BLOCK,
              AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_BLOCK);
        getItemBuilder(AdditionsTags.Items.PLASTIC_BLOCKS).add(AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC,
              AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, AdditionsTags.Items.PLASTIC_BLOCKS_SLICK,
              AdditionsTags.Items.PLASTIC_BLOCKS_TRANSPARENT);
        getBlockBuilder(AdditionsTags.Blocks.PLASTIC_BLOCKS).add(AdditionsTags.Blocks.PLASTIC_BLOCKS_GLOW, AdditionsTags.Blocks.PLASTIC_BLOCKS_PLASTIC,
              AdditionsTags.Blocks.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Blocks.PLASTIC_BLOCKS_ROAD, AdditionsTags.Blocks.PLASTIC_BLOCKS_SLICK,
              AdditionsTags.Blocks.PLASTIC_BLOCKS_TRANSPARENT);
    }
}