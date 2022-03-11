package mekanism.additions.common;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.MekanismTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AdditionsTagProvider extends BaseTagProvider {

    public AdditionsTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected List<IBlockProvider> getAllBlocks() {
        return AdditionsBlocks.BLOCKS.getAllBlocks();
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
        addHarvestRequirements();
        addToTag(BlockTags.IMPERMEABLE, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS);
    }

    private void addEntities() {
        addToTag(EntityTypeTags.SKELETONS, AdditionsEntityTypes.BABY_SKELETON, AdditionsEntityTypes.BABY_STRAY, AdditionsEntityTypes.BABY_WITHER_SKELETON);
        getEntityTypeBuilder(AdditionsTags.Entities.CREEPERS).add(EntityType.CREEPER, AdditionsEntityTypes.BABY_CREEPER.getEntityType());
        getEntityTypeBuilder(AdditionsTags.Entities.ENDERMEN).add(EntityType.ENDERMAN, AdditionsEntityTypes.BABY_ENDERMAN.getEntityType());
        addToTag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES, AdditionsEntityTypes.BABY_STRAY);
        addToTag(MekanismTagProvider.PVI_COMPAT, AdditionsEntityTypes.BABY_CREEPER, AdditionsEntityTypes.BABY_ENDERMAN, AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_STRAY, AdditionsEntityTypes.BABY_WITHER_SKELETON);
    }

    private void addBalloons() {
        addToTag(AdditionsTags.Items.BALLOONS, AdditionsItems.BALLOONS.values().toArray(new IItemProvider[0]));
    }

    private void addSlabs() {
        addToTags(AdditionsTags.Items.SLABS_PLASTIC, AdditionsTags.Blocks.SLABS_PLASTIC, AdditionsBlocks.PLASTIC_SLABS);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.Blocks.SLABS_PLASTIC_GLOW, AdditionsBlocks.PLASTIC_GLOW_SLABS);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, AdditionsTags.Blocks.SLABS_PLASTIC_TRANSPARENT, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS);
        getItemBuilder(ItemTags.SLABS).add(AdditionsTags.Items.SLABS_PLASTIC, AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT);
        getBlockBuilder(BlockTags.SLABS).add(AdditionsTags.Blocks.SLABS_PLASTIC, AdditionsTags.Blocks.SLABS_PLASTIC_GLOW, AdditionsTags.Blocks.SLABS_PLASTIC_TRANSPARENT);
    }

    private void addStairs() {
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC, AdditionsTags.Blocks.STAIRS_PLASTIC, AdditionsBlocks.PLASTIC_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.Blocks.STAIRS_PLASTIC_GLOW, AdditionsBlocks.PLASTIC_GLOW_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT, AdditionsTags.Blocks.STAIRS_PLASTIC_TRANSPARENT, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS);
        getItemBuilder(ItemTags.STAIRS).add(AdditionsTags.Items.STAIRS_PLASTIC, AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT);
        getBlockBuilder(BlockTags.STAIRS).add(AdditionsTags.Blocks.STAIRS_PLASTIC, AdditionsTags.Blocks.STAIRS_PLASTIC_GLOW, AdditionsTags.Blocks.STAIRS_PLASTIC_TRANSPARENT);
    }

    private void addFences() {
        addToTags(AdditionsTags.Items.FENCES_PLASTIC, AdditionsTags.Blocks.FENCES_PLASTIC, AdditionsBlocks.PLASTIC_FENCES);
        getItemBuilder(Tags.Items.FENCES).add(AdditionsTags.Items.FENCES_PLASTIC);
        getBlockBuilder(Tags.Blocks.FENCES).add(AdditionsTags.Blocks.FENCES_PLASTIC);
        getItemBuilder(ItemTags.FENCES).add(AdditionsTags.Items.FENCES_PLASTIC);
        getBlockBuilder(BlockTags.FENCES).add(AdditionsTags.Blocks.FENCES_PLASTIC);
    }

    private void addFenceGates() {
        addToTags(AdditionsTags.Items.FENCE_GATES_PLASTIC, AdditionsTags.Blocks.FENCE_GATES_PLASTIC, AdditionsBlocks.PLASTIC_FENCE_GATES);
        getItemBuilder(Tags.Items.FENCE_GATES).add(AdditionsTags.Items.FENCE_GATES_PLASTIC);
        getBlockBuilder(Tags.Blocks.FENCE_GATES).add(AdditionsTags.Blocks.FENCE_GATES_PLASTIC);
        getBlockBuilder(BlockTags.FENCE_GATES).add(AdditionsTags.Blocks.FENCE_GATES_PLASTIC);
    }

    private void addGlowPanels() {
        addToTags(AdditionsTags.Items.GLOW_PANELS, AdditionsTags.Blocks.GLOW_PANELS, AdditionsBlocks.GLOW_PANELS);
    }

    private void addPlasticBlocks() {
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC, AdditionsTags.Blocks.PLASTIC_BLOCKS_PLASTIC, AdditionsBlocks.PLASTIC_BLOCKS);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_SLICK, AdditionsTags.Blocks.PLASTIC_BLOCKS_SLICK, AdditionsBlocks.SLICK_PLASTIC_BLOCKS);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, AdditionsTags.Blocks.PLASTIC_BLOCKS_GLOW, AdditionsBlocks.PLASTIC_GLOW_BLOCKS);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, AdditionsTags.Blocks.PLASTIC_BLOCKS_ROAD, AdditionsBlocks.PLASTIC_ROADS);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Blocks.PLASTIC_BLOCKS_REINFORCED, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS);
        addToTags(AdditionsTags.Items.PLASTIC_BLOCKS_TRANSPARENT, AdditionsTags.Blocks.PLASTIC_BLOCKS_TRANSPARENT, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS);
        getItemBuilder(AdditionsTags.Items.PLASTIC_BLOCKS).add(AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC,
              AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, AdditionsTags.Items.PLASTIC_BLOCKS_SLICK,
              AdditionsTags.Items.PLASTIC_BLOCKS_TRANSPARENT);
        getBlockBuilder(AdditionsTags.Blocks.PLASTIC_BLOCKS).add(AdditionsTags.Blocks.PLASTIC_BLOCKS_GLOW, AdditionsTags.Blocks.PLASTIC_BLOCKS_PLASTIC,
              AdditionsTags.Blocks.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Blocks.PLASTIC_BLOCKS_ROAD, AdditionsTags.Blocks.PLASTIC_BLOCKS_SLICK,
              AdditionsTags.Blocks.PLASTIC_BLOCKS_TRANSPARENT);
    }

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_ROADS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS,
              AdditionsBlocks.SLICK_PLASTIC_BLOCKS, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsBlocks.PLASTIC_FENCES,
              AdditionsBlocks.PLASTIC_FENCE_GATES, AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS,
              AdditionsBlocks.PLASTIC_STAIRS, AdditionsBlocks.PLASTIC_GLOW_STAIRS, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS);
    }

    private void addToTags(TagKey<Item> itemTag, TagKey<Block> blockTag, Map<?, ? extends IBlockProvider> blockProviders) {
        addToTags(itemTag, blockTag, blockProviders.values().toArray(new IBlockProvider[0]));
    }
}