package mekanism.additions.common;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsDataComponents;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.MekanismTagBuilder;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypeIds;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AdditionsTagProvider extends BaseTagProvider {

    public AdditionsTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MekanismAdditions.MODID);
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
        getBuilder(BlockTags.IMPERMEABLE).add(AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS.asList());
    }

    private void addEntities() {
        getBuilder(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(AdditionsEntityTypes.BALLOON);
        getBuilder(EntityTypeTags.SKELETONS).add(
              AdditionsEntityTypes.BABY_BOGGED,
              AdditionsEntityTypes.BABY_PARCHED,
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
              .add(EntityTypeIds.BOGGED)
              .add(AdditionsEntityTypes.BABY_BOGGED);
        getBuilder(MekanismTags.Entities.CREEPERS)
              .add(AdditionsEntityTypes.BABY_CREEPER);
        getBuilder(AdditionsTags.Entities.ENDERMEN)
              .add(EntityTypeIds.ENDERMAN)
              .add(AdditionsEntityTypes.BABY_ENDERMAN);
        getBuilder(AdditionsTags.Entities.PARCHED)
              .add(EntityTypeIds.PARCHED)
              .add(AdditionsEntityTypes.BABY_PARCHED);
        getBuilder(AdditionsTags.Entities.STRAY)
              .add(EntityTypeIds.STRAY)
              .add(AdditionsEntityTypes.BABY_STRAY);
        getBuilder(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(AdditionsEntityTypes.BABY_STRAY);
        getBuilder(EntityTypeTags.BURN_IN_DAYLIGHT).add(
              AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_STRAY,
              AdditionsEntityTypes.BABY_WITHER_SKELETON,
              AdditionsEntityTypes.BABY_BOGGED
        );
        MekanismTagBuilder<EntityType<?>> pviCompatBuilder = getBuilder(PVI_COMPAT);
        for (BabyType babyType : BabyType.VALUES) {
            pviCompatBuilder.add(babyType.id());
        }
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
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_NORMAL, AdditionsTags.BlockItems.COMMON_SLABS_PLASTIC.block(), AdditionsBlocks.PLASTIC_SLABS);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.BlockItems.COMMON_SLABS_PLASTIC_GLOW.block(), AdditionsBlocks.PLASTIC_GLOW_SLABS);
        addToTags(AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, AdditionsTags.BlockItems.COMMON_SLABS_PLASTIC_TRANSPARENT.block(), AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS);
        getBuilder(AdditionsTags.Items.SLABS_PLASTIC).add(AdditionsTags.Items.SLABS_PLASTIC_NORMAL, AdditionsTags.Items.SLABS_PLASTIC_GLOW, AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT);
        getBuilder(AdditionsTags.BlockItems.COMMON_SLABS_PLASTIC.item()).add(AdditionsTags.Items.SLABS_PLASTIC);
        addToTags(BlockItemTags.SLABS, AdditionsTags.BlockItems.COMMON_SLABS_PLASTIC);
        getBuilder(BlockTags.SLABS).add(AdditionsTags.BlockItems.COMMON_SLABS_PLASTIC_GLOW.block(), AdditionsTags.BlockItems.COMMON_SLABS_PLASTIC_TRANSPARENT.block());
    }

    private void addStairs() {
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_NORMAL, AdditionsTags.BlockItems.COMMON_STAIRS_PLASTIC.block(), AdditionsBlocks.PLASTIC_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.BlockItems.COMMON_STAIRS_PLASTIC_GLOW.block(), AdditionsBlocks.PLASTIC_GLOW_STAIRS);
        addToTags(AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT, AdditionsTags.BlockItems.COMMON_STAIRS_PLASTIC_TRANSPARENT.block(), AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS);
        getBuilder(AdditionsTags.Items.STAIRS_PLASTIC).add(AdditionsTags.Items.STAIRS_PLASTIC_NORMAL, AdditionsTags.Items.STAIRS_PLASTIC_GLOW, AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT);
        getBuilder(AdditionsTags.BlockItems.COMMON_STAIRS_PLASTIC.item()).add(AdditionsTags.Items.STAIRS_PLASTIC);
        addToTags(BlockItemTags.STAIRS, AdditionsTags.BlockItems.COMMON_STAIRS_PLASTIC);
        getBuilder(BlockTags.STAIRS).add(AdditionsTags.BlockItems.COMMON_STAIRS_PLASTIC_GLOW.block(), AdditionsTags.BlockItems.COMMON_STAIRS_PLASTIC_TRANSPARENT.block());
    }

    private void addFences() {
        addToTags(AdditionsTags.Items.FENCES_PLASTIC_NORMAL, AdditionsTags.BlockItems.COMMON_FENCES_PLASTIC.block(), AdditionsBlocks.PLASTIC_FENCES);
        getBuilder(AdditionsTags.Items.FENCES_PLASTIC).add(AdditionsTags.Items.FENCES_PLASTIC_NORMAL);
        getBuilder(AdditionsTags.BlockItems.COMMON_FENCES_PLASTIC.item()).add(AdditionsTags.Items.FENCES_PLASTIC);
        addToTags(Tags.Items.FENCES, Tags.Blocks.FENCES, AdditionsTags.BlockItems.COMMON_FENCES_PLASTIC);
        addToTags(BlockItemTags.FENCES, AdditionsTags.BlockItems.COMMON_FENCES_PLASTIC);
    }

    private void addFenceGates() {
        addToTags(AdditionsTags.Items.FENCE_GATES_PLASTIC_NORMAL, AdditionsTags.BlockItems.COMMON_FENCE_GATES_PLASTIC.block(), AdditionsBlocks.PLASTIC_FENCE_GATES);
        getBuilder(AdditionsTags.Items.FENCE_GATES_PLASTIC).add(AdditionsTags.Items.FENCE_GATES_PLASTIC_NORMAL);
        getBuilder(AdditionsTags.BlockItems.COMMON_FENCE_GATES_PLASTIC.item()).add(AdditionsTags.Items.FENCE_GATES_PLASTIC);
        addToTags(Tags.Items.FENCE_GATES, Tags.Blocks.FENCE_GATES, AdditionsTags.BlockItems.COMMON_FENCE_GATES_PLASTIC);
        addToTags(BlockItemTags.FENCE_GATES, AdditionsTags.BlockItems.COMMON_FENCE_GATES_PLASTIC);
    }

    private void addGlowPanels() {
        addToTags(AdditionsTags.BlockItems.GLOW_PANELS, AdditionsBlocks.GLOW_PANELS);
    }

    private void addPlasticBlocks() {
        addToTags(AdditionsTags.BlockItems.PLASTIC_BLOCKS_PLASTIC, AdditionsBlocks.PLASTIC_BLOCKS);
        addToTags(AdditionsTags.BlockItems.PLASTIC_BLOCKS_SLICK, AdditionsBlocks.SLICK_PLASTIC_BLOCKS);
        addToTags(AdditionsTags.BlockItems.PLASTIC_BLOCKS_GLOW, AdditionsBlocks.PLASTIC_GLOW_BLOCKS);
        addToTags(AdditionsTags.BlockItems.PLASTIC_BLOCKS_ROAD, AdditionsBlocks.PLASTIC_ROADS);
        addToTags(AdditionsTags.BlockItems.PLASTIC_BLOCKS_REINFORCED, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS);
        addToTags(AdditionsTags.BlockItems.PLASTIC_BLOCKS_TRANSPARENT, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS);
        addToTags(AdditionsTags.BlockItems.PLASTIC_BLOCKS, AdditionsTags.BlockItems.PLASTIC_BLOCKS_GLOW, AdditionsTags.BlockItems.PLASTIC_BLOCKS_PLASTIC,
              AdditionsTags.BlockItems.PLASTIC_BLOCKS_REINFORCED, AdditionsTags.BlockItems.PLASTIC_BLOCKS_ROAD, AdditionsTags.BlockItems.PLASTIC_BLOCKS_SLICK,
              AdditionsTags.BlockItems.PLASTIC_BLOCKS_TRANSPARENT);

        getBuilder(FRAMEABLE).add(AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS.asList());
    }

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE, AdditionsBlocks.PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_ROADS, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS,
              AdditionsBlocks.SLICK_PLASTIC_BLOCKS, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsBlocks.PLASTIC_FENCES,
              AdditionsBlocks.PLASTIC_FENCE_GATES, AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS,
              AdditionsBlocks.PLASTIC_STAIRS, AdditionsBlocks.PLASTIC_GLOW_STAIRS, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS);
    }

    private void addToTags(BlockItemTagId tag, EnumColorCollection<? extends BlockRegistryObject<?, ?>> blockProviders) {
        addToTags(tag.item(), tag.block(), blockProviders);
    }

    private void addToTags(TagKey<Item> itemTag, TagKey<Block> blockTag, EnumColorCollection<? extends BlockRegistryObject<?, ?>> blockProviders) {
        addToTags(itemTag, blockTag, blockProviders.asList());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, blockProviders, (color, provider) -> {
            DyeColor dyeColor = color.getDyeColor();
            if (dyeColor != null) {
                addToTags(Tags.Items.DYED, Tags.Blocks.DYED, provider);
                addToTags(dyeColor.getDyedTag(), BlockTags.create(dyeColor.getDyedTag().location()), provider);
            }
        });
    }

    private void addToTag(TagKey<Item> itemTag, EnumColorCollection<? extends Holder<Item>> itemProviders) {
        getBuilder(itemTag).add(itemProviders.asList());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, itemProviders, (color, provider) -> {
            DyeColor dyeColor = color.getDyeColor();
            if (dyeColor != null) {
                getBuilder(Tags.Items.DYED).add(provider);
                getBuilder(dyeColor.getDyedTag()).add(provider);
            }
        });
    }
}