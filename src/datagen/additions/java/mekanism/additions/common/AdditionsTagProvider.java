package mekanism.additions.common;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsDataComponents;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.text.EnumColor;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

public class AdditionsTagProvider extends BaseTagProvider {

    public AdditionsTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected Collection<? extends DeferredHolder<Block, ?>> getAllBlocks() {
        return AdditionsBlocks.BLOCKS.getPrimaryEntries();
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        addEntities();
        addDamageTypes();
        addDataComponents();
        addBalloons();
        addSlabs();
        addStairs();
        addFences();
        addFenceGates();
        addGlowPanels();
        addPlasticBlocks();
        addHarvestRequirements();
        getBuilder(BlockTags.IMPERMEABLE).add(AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS.values());
    }

    private void addEntities() {
        getBuilder(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(AdditionsEntityTypes.BALLOON);
        getBuilder(EntityTypeTags.SKELETONS).add(
              AdditionsEntityTypes.BABY_BOGGED,
              AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_STRAY,
              AdditionsEntityTypes.BABY_WITHER_SKELETON
        );
        getBuilder(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE).add(
              AdditionsEntityTypes.BABY_BOGGED,
              AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_STRAY
        );
        getBuilder(AdditionsTags.Entities.BOGGED)
              .addIntrinsic(BuiltInRegistries.ENTITY_TYPE, EntityType.BOGGED)
              .add(AdditionsEntityTypes.BABY_BOGGED);
        getBuilder(MekanismTags.Entities.CREEPERS)
              .add(AdditionsEntityTypes.BABY_CREEPER);
        getBuilder(AdditionsTags.Entities.ENDERMEN)
              .addIntrinsic(BuiltInRegistries.ENTITY_TYPE, EntityType.ENDERMAN)
              .add(AdditionsEntityTypes.BABY_ENDERMAN);
        getBuilder(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(AdditionsEntityTypes.BABY_STRAY);
        getBuilder(PVI_COMPAT).add(
              AdditionsEntityTypes.BABY_CREEPER,
              AdditionsEntityTypes.BABY_ENDERMAN,
              AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_STRAY,
              AdditionsEntityTypes.BABY_WITHER_SKELETON
        );
    }

    private void addDamageTypes() {
        getBuilder(AdditionsTags.DamageTypes.BALLOON_INVULNERABLE).add(
              DamageTypeTags.IS_FALL,
              Tags.DamageTypes.IS_MAGIC
        ).add(
              DamageTypes.DROWN,
              DamageTypes.FLY_INTO_WALL
        );
    }

    private void addDataComponents() {
        getBuilder(MekanismTags.DataComponents.CLEARABLE_CONFIG).add(AdditionsDataComponents.WALKIE_DATA);
    }

    private void addBalloons() {
        addToTag(AdditionsTags.Items.BALLOONS, AdditionsItems.BALLOONS);
    }

    private void addSlabs() {
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_NORMAL, AdditionsTags.Blocks.SLABS_PLASTIC, AdditionsBlocks.PLASTIC_SLABS);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.Blocks.SLABS_PLASTIC_GLOW, AdditionsBlocks.PLASTIC_GLOW_SLABS);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, AdditionsTags.Blocks.SLABS_PLASTIC_TRANSPARENT, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS);
        getBuilder(AdditionsTags.Items.SLABS_PLASTIC).add(AdditionsTags.Items.SLABS_PLASTIC_NORMAL, AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT);
        getBuilder(AdditionsTags.Items.COMMON_SLABS_PLASTIC).add(AdditionsTags.Items.SLABS_PLASTIC);
        getBuilder(ItemTags.SLABS).add(AdditionsTags.Items.COMMON_SLABS_PLASTIC);
        getBuilder(BlockTags.SLABS).add(AdditionsTags.Blocks.SLABS_PLASTIC, AdditionsTags.Blocks.SLABS_PLASTIC_GLOW, AdditionsTags.Blocks.SLABS_PLASTIC_TRANSPARENT);
    }

    private void addStairs() {
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_NORMAL, AdditionsTags.Blocks.STAIRS_PLASTIC, AdditionsBlocks.PLASTIC_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.Blocks.STAIRS_PLASTIC_GLOW, AdditionsBlocks.PLASTIC_GLOW_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT, AdditionsTags.Blocks.STAIRS_PLASTIC_TRANSPARENT, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS);
        getBuilder(AdditionsTags.Items.STAIRS_PLASTIC).add(AdditionsTags.Items.STAIRS_PLASTIC_NORMAL, AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT);
        getBuilder(AdditionsTags.Items.COMMON_STAIRS_PLASTIC).add(AdditionsTags.Items.STAIRS_PLASTIC);
        getBuilder(ItemTags.STAIRS).add(AdditionsTags.Items.COMMON_STAIRS_PLASTIC);
        getBuilder(BlockTags.STAIRS).add(AdditionsTags.Blocks.STAIRS_PLASTIC, AdditionsTags.Blocks.STAIRS_PLASTIC_GLOW, AdditionsTags.Blocks.STAIRS_PLASTIC_TRANSPARENT);
    }

    private void addFences() {
        addToTags(AdditionsTags.Items.FENCES_PLASTIC_NORMAL, AdditionsTags.Blocks.FENCES_PLASTIC, AdditionsBlocks.PLASTIC_FENCES);
        getBuilder(AdditionsTags.Items.FENCES_PLASTIC).add(AdditionsTags.Items.FENCES_PLASTIC_NORMAL);
        getBuilder(AdditionsTags.Items.COMMON_FENCES_PLASTIC).add(AdditionsTags.Items.FENCES_PLASTIC);
        getBuilder(Tags.Items.FENCES).add(AdditionsTags.Items.COMMON_FENCES_PLASTIC);
        getBuilder(Tags.Blocks.FENCES).add(AdditionsTags.Blocks.FENCES_PLASTIC);
        getBuilder(ItemTags.FENCES).add(AdditionsTags.Items.COMMON_FENCES_PLASTIC);
        getBuilder(BlockTags.FENCES).add(AdditionsTags.Blocks.FENCES_PLASTIC);
    }

    private void addFenceGates() {
        addToTags(AdditionsTags.Items.FENCE_GATES_PLASTIC_NORMAL, AdditionsTags.Blocks.FENCE_GATES_PLASTIC, AdditionsBlocks.PLASTIC_FENCE_GATES);
        getBuilder(AdditionsTags.Items.FENCE_GATES_PLASTIC).add(AdditionsTags.Items.FENCE_GATES_PLASTIC_NORMAL);
        getBuilder(AdditionsTags.Items.COMMON_FENCE_GATES_PLASTIC).add(AdditionsTags.Items.FENCE_GATES_PLASTIC);
        getBuilder(Tags.Items.FENCE_GATES).add(AdditionsTags.Items.COMMON_FENCE_GATES_PLASTIC);
        getBuilder(Tags.Blocks.FENCE_GATES).add(AdditionsTags.Blocks.FENCE_GATES_PLASTIC);
        getBuilder(ItemTags.FENCE_GATES).add(AdditionsTags.Items.COMMON_FENCE_GATES_PLASTIC);
        getBuilder(BlockTags.FENCE_GATES).add(AdditionsTags.Blocks.FENCE_GATES_PLASTIC);
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
        getBuilder(AdditionsTags.Items.PLASTIC_BLOCKS).add(AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC,
              AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, AdditionsTags.Items.PLASTIC_BLOCKS_SLICK,
              AdditionsTags.Items.PLASTIC_BLOCKS_TRANSPARENT);
        getBuilder(AdditionsTags.Blocks.PLASTIC_BLOCKS).add(AdditionsTags.Blocks.PLASTIC_BLOCKS_GLOW, AdditionsTags.Blocks.PLASTIC_BLOCKS_PLASTIC,
              AdditionsTags.Blocks.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.Blocks.PLASTIC_BLOCKS_ROAD, AdditionsTags.Blocks.PLASTIC_BLOCKS_SLICK,
              AdditionsTags.Blocks.PLASTIC_BLOCKS_TRANSPARENT);

        getBuilder(FRAMEABLE).add(AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS.values());
    }

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_ROADS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS,
              AdditionsBlocks.SLICK_PLASTIC_BLOCKS, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsBlocks.PLASTIC_FENCES,
              AdditionsBlocks.PLASTIC_FENCE_GATES, AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS,
              AdditionsBlocks.PLASTIC_STAIRS, AdditionsBlocks.PLASTIC_GLOW_STAIRS, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS);
    }

    private void addToTags(TagKey<Item> itemTag, TagKey<Block> blockTag, Map<EnumColor, ? extends BlockRegistryObject<?, ?>> blockProviders) {
        addToTags(itemTag, blockTag, blockProviders.values());
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<?, ?>> entry : blockProviders.entrySet()) {
            DyeColor dyeColor = entry.getKey().getDyeColor();
            if (dyeColor != null) {
                addToTags(Tags.Items.DYED, Tags.Blocks.DYED, entry.getValue());
                addToTags(dyeColor.getDyedTag(), BlockTags.create(dyeColor.getDyedTag().location()), entry.getValue());
            }
        }
    }

    private void addToTag(TagKey<Item> itemTag, Map<EnumColor, ? extends Holder<Item>> itemProviders) {
        getBuilder(itemTag).add(itemProviders.values());
        for (Map.Entry<EnumColor, ? extends Holder<Item>> entry : itemProviders.entrySet()) {
            DyeColor dyeColor = entry.getKey().getDyeColor();
            if (dyeColor != null) {
                getBuilder(Tags.Items.DYED).add(entry.getValue());
                getBuilder(dyeColor.getDyedTag()).add(entry.getValue());
            }
        }
    }
}