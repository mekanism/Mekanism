package mekanism.common.tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTagProvider implements DataProvider {

    protected static final TagKey<EntityType<?>> PVI_COMPAT = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("per-viam-invenire", "replace_vanilla_navigator"));
    private static final TagKey<Fluid> CREATE_NO_INFINITE_FLUID = FluidTags.create(ResourceLocation.fromNamespaceAndPath("create", "no_infinite_draining"));
    protected static final TagKey<Block> FRAMEABLE = BlockTags.create(Mekanism.hooks.framedBlocks.rl("frameable"));
    protected static final TagKey<Block> FB_BE_WHITELIST = BlockTags.create(Mekanism.hooks.framedBlocks.rl("blockentity_whitelisted"));
    protected static final TagKey<Block> PE_VEIN_SHOVEL = BlockTags.create(Mekanism.hooks.projecte.rl("vein/shovel"));

    private final Map<ResourceKey<? extends Registry<?>>, Map<TagKey<?>, TagBuilder>> supportedTagTypes = new Object2ObjectLinkedOpenHashMap<>();
    private final Set<Block> knownHarvestRequirements = new ReferenceOpenHashSet<>();
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final ExistingFileHelper existingFileHelper;
    private final PackOutput output;
    private final String modid;

    protected BaseTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid, @Nullable ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.modid = modid;
        this.lookupProvider = lookupProvider;
        this.existingFileHelper = existingFileHelper;
    }

    @NotNull
    @Override
    public String getName() {
        return "Tags: " + modid;
    }

    protected abstract void registerTags(HolderLookup.Provider registries);

    protected Collection<? extends DeferredHolder<Block, ?>> getAllBlocks() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return this.lookupProvider.thenApply(registries -> {
            supportedTagTypes.values().forEach(Map::clear);
            registerTags(registries);
            return registries;
        }).thenCompose(registries -> {
            for (DeferredHolder<Block, ?> blockProvider : getAllBlocks()) {
                Block block = blockProvider.value();
                if (block.defaultBlockState().requiresCorrectToolForDrops() && !knownHarvestRequirements.contains(block)) {
                    throw new IllegalStateException("Missing harvest tool type for block '" + blockProvider.getId() + "' that requires the correct tool for drops.");
                }
            }
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<ResourceKey<? extends Registry<?>>, Map<TagKey<?>, TagBuilder>> entry : supportedTagTypes.entrySet()) {
                Map<TagKey<?>, TagBuilder> tagTypeMap = entry.getValue();
                if (!tagTypeMap.isEmpty()) {
                    //Create a dummy provider and pass all our collected data through to it
                    futures.add(createDummyTagsProvider(entry.getKey(), tagTypeMap).run(cache));
                }
            }
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @SuppressWarnings("unchecked")
    private <TYPE> TagsProvider<TYPE> createDummyTagsProvider(ResourceKey<?> registry, Map<TagKey<?>, TagBuilder> tagTypeMap) {
        return new TagsProvider<>(output, (ResourceKey<? extends Registry<TYPE>>) registry, lookupProvider, modid, existingFileHelper) {
            @Override
            protected void addTags(@NotNull HolderLookup.Provider lookupProvider) {
                //Add each tag builder to the wrapped provider's builder
                for (Map.Entry<TagKey<?>, TagBuilder> e : tagTypeMap.entrySet()) {
                    builders.put(e.getKey().location(), e.getValue());
                }
            }
        };
    }

    protected <TYPE> MekanismTagBuilder<TYPE> getBuilder(TagKey<TYPE> tag) {
        Map<TagKey<?>, TagBuilder> tagTypeMap = supportedTagTypes.computeIfAbsent(tag.registry(), type -> new Object2ObjectLinkedOpenHashMap<>());
        return new MekanismTagBuilder<>(tagTypeMap.computeIfAbsent(tag, ignored -> TagBuilder.create()));
    }

    @SafeVarargs
    protected final void addToHarvestTag(TagKey<Block> tag, Holder<Block>... blockProviders) {
        getBuilder(tag).add(blockProviders);
        for (Holder<Block> block : blockProviders) {
            knownHarvestRequirements.add(block.value());
        }
    }

    @SafeVarargs
    protected final void addToHarvestTag(TagKey<Block> blockTag, Map<?, ? extends Holder<Block>>... blockProviders) {
        MekanismTagBuilder<Block> tagBuilder = getBuilder(blockTag);
        for (Map<?, ? extends Holder<Block>> blockProvider : blockProviders) {
            tagBuilder.add(blockProvider.values());
            for (Holder<Block> block : blockProvider.values()) {
                knownHarvestRequirements.add(block.value());
            }
        }
    }

    protected void addToTags(TagKey<Item> itemTag, TagKey<Block> blockTag, BlockRegistryObject<?, ?>... blockProviders) {
        getBuilder(itemTag).add(Arrays.stream(blockProviders).map(BlockRegistryObject::getItemHolder));
        getBuilder(blockTag).add(blockProviders);
    }

    protected void addToTags(TagKey<Item> itemTag, TagKey<Block> blockTag, Collection<? extends BlockRegistryObject<?, ?>> blockProviders) {
        getBuilder(itemTag).add(blockProviders.stream().map(BlockRegistryObject::getItemHolder));
        getBuilder(blockTag).add(blockProviders);
    }

    protected void addToGenericFluidTags(FluidDeferredRegister register) {
        getBuilder(BlockTags.REPLACEABLE).add(register.getBlockEntries());
        //Prevent all our fluids from being duped by create
        getBuilder(CREATE_NO_INFINITE_FLUID).add(register.getFluidEntries());
        //Add bucket tags
        for (DeferredHolder<Item, ?> bucket : register.getBucketEntries()) {
            ResourceLocation bucketTag = Tags.Items.BUCKETS.location().withSuffix("/" + bucket.getId().getPath().replaceAll("_bucket", ""));
            getBuilder(ItemTags.create(bucketTag)).add(bucket);
        }
    }

    protected void addToTag(TagKey<Fluid> tag, FluidRegistryObject<?, ?, ?, ?, ?> fluidRO) {
        getBuilder(tag).add(fluidRO, fluidRO.getFlowingFluid());
    }
}
