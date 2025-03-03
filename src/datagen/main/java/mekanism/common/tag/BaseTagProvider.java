package mekanism.common.tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registries.MekanismDamageTypes.MekanismDamageType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTagProvider implements DataProvider {

    protected static final TagKey<EntityType<?>> PVI_COMPAT = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("per-viam-invenire", "replace_vanilla_navigator"));
    private static final TagKey<Fluid> CREATE_NO_INFINITE_FLUID = FluidTags.create(ResourceLocation.fromNamespaceAndPath("create", "no_infinite_draining"));
    protected static final TagKey<Block> FRAMEABLE = BlockTags.create(ResourceLocation.fromNamespaceAndPath("framedblocks", "frameable"));
    protected static final TagKey<Block> FB_BE_WHITELIST = BlockTags.create(ResourceLocation.fromNamespaceAndPath("framedblocks", "blockentity_whitelisted"));

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

    @SafeVarargs
    protected final void hasHarvestData(Holder<Block>... blocks) {
        for (Holder<Block> block : blocks) {
            knownHarvestRequirements.add(block.value());
        }
    }

    protected void hasHarvestData(Collection<? extends Holder<Block>> blocks) {
        for (Holder<Block> block : blocks) {
            knownHarvestRequirements.add(block.value());
        }
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
                    futures.add(new TagsProvider(output, entry.getKey(), lookupProvider, modid, existingFileHelper) {
                        @Override
                        protected void addTags(@NotNull HolderLookup.Provider lookupProvider) {
                            //Add each tag builder to the wrapped provider's builder
                            for (Map.Entry<TagKey<?>, TagBuilder> e : tagTypeMap.entrySet()) {
                                builders.put(e.getKey().location(), e.getValue());
                            }
                        }
                    }.run(cache));
                }
            }
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private <TYPE> Map<TagKey<?>, TagBuilder> getTagTypeMap(ResourceKey<? extends Registry<TYPE>> registry) {
        return supportedTagTypes.computeIfAbsent(registry, type -> new Object2ObjectLinkedOpenHashMap<>());
    }

    private <TYPE> TagBuilder getTagBuilder(ResourceKey<? extends Registry<TYPE>> registry, TagKey<TYPE> tag) {
        return getTagTypeMap(registry).computeIfAbsent(tag, ignored -> TagBuilder.create());
    }

    protected <TYPE> MekanismTagBuilder<TYPE, ?> getBuilder(ResourceKey<? extends Registry<TYPE>> registry, TagKey<TYPE> tag) {
        return new MekanismTagBuilder<>(getTagBuilder(registry, tag));
    }

    protected <TYPE> IntrinsicMekanismTagBuilder<TYPE> getBuilder(ResourceKey<? extends Registry<TYPE>> registry, Function<TYPE, ResourceKey<TYPE>> keyExtractor, TagKey<TYPE> tag) {
        return new IntrinsicMekanismTagBuilder<>(keyExtractor, getTagBuilder(registry, tag));
    }

    protected <TYPE> IntrinsicMekanismTagBuilder<TYPE> getBuilder(Registry<TYPE> registry, TagKey<TYPE> tag) {
        return new IntrinsicMekanismTagBuilder<>(element -> registry.getResourceKey(element).orElseThrow(), getTagBuilder(registry.key(), tag));
    }

    protected IntrinsicMekanismTagBuilder<Item> getItemBuilder(TagKey<Item> tag) {
        return getBuilder(BuiltInRegistries.ITEM, tag);
    }

    protected IntrinsicMekanismTagBuilder<Block> getBlockBuilder(TagKey<Block> tag) {
        return getBuilder(BuiltInRegistries.BLOCK, tag);
    }

    protected IntrinsicMekanismTagBuilder<EntityType<?>> getEntityTypeBuilder(TagKey<EntityType<?>> tag) {
        return getBuilder(BuiltInRegistries.ENTITY_TYPE, tag);
    }

    protected IntrinsicMekanismTagBuilder<Fluid> getFluidBuilder(TagKey<Fluid> tag) {
        return getBuilder(BuiltInRegistries.FLUID, tag);
    }

    protected MekanismTagBuilder<GameEvent, ?> getGameEventBuilder(TagKey<GameEvent> tag) {
        return getBuilder(Registries.GAME_EVENT, tag);
    }

    protected MekanismTagBuilder<DamageType, ?> getDamageTypeBuilder(TagKey<DamageType> tag) {
        return getBuilder(Registries.DAMAGE_TYPE, tag);
    }

    protected MekanismTagBuilder<Biome, ?> getBiomeBuilder(TagKey<Biome> tag) {
        return getBuilder(Registries.BIOME, tag);
    }

    protected IntrinsicMekanismTagBuilder<Chemical> getChemicalBuilder(TagKey<Chemical> tag) {
        return getBuilder(MekanismAPI.CHEMICAL_REGISTRY, tag);
    }

    protected IntrinsicMekanismTagBuilder<MobEffect> getMobEffectBuilder(TagKey<MobEffect> tag) {
        return getBuilder(BuiltInRegistries.MOB_EFFECT, tag);
    }

    @SafeVarargs
    protected final void addItemsToTag(TagKey<Item> tag, Holder<Item>... itemProviders) {
        getItemBuilder(tag).addHolders(itemProviders);
    }

    @SafeVarargs
    protected final void addBlocksToTag(TagKey<Block> tag, Holder<Block>... blockProviders) {
        getBlockBuilder(tag).addHolders(blockProviders);
    }

    @SafeVarargs
    protected final void addToTag(TagKey<Block> blockTag, Map<?, ? extends Holder<Block>>... blockProviders) {
        IntrinsicMekanismTagBuilder<Block> tagBuilder = getBlockBuilder(blockTag);
        for (Map<?, ? extends Holder<Block>> blockProvider : blockProviders) {
            tagBuilder.addHolders(blockProvider.values());
        }
    }

    @SafeVarargs
    protected final void addToHarvestTag(TagKey<Block> tag, Holder<Block>... blockProviders) {
        IntrinsicMekanismTagBuilder<Block> tagBuilder = getBlockBuilder(tag);
        tagBuilder.addHolders(blockProviders);
        hasHarvestData(blockProviders);
    }

    @SafeVarargs
    protected final void addToHarvestTag(TagKey<Block> blockTag, Map<?, ? extends Holder<Block>>... blockProviders) {
        IntrinsicMekanismTagBuilder<Block> tagBuilder = getBlockBuilder(blockTag);
        for (Map<?, ? extends Holder<Block>> blockProvider : blockProviders) {
            tagBuilder.addHolders(blockProvider.values());
            hasHarvestData(blockProvider.values());
        }
    }

    protected void addToTags(TagKey<Item> itemTag, TagKey<Block> blockTag, BlockRegistryObject<?, ?>... blockProviders) {
        IntrinsicMekanismTagBuilder<Item> itemTagBuilder = getItemBuilder(itemTag);
        for (BlockRegistryObject<?, ?> blockProvider : blockProviders) {
            itemTagBuilder.addHolders(blockProvider.getItemHolder());
        }
        getBlockBuilder(blockTag).addHolders(blockProviders);
    }

    protected void addToGenericFluidTags(FluidDeferredRegister register) {
        getBlockBuilder(BlockTags.REPLACEABLE).add(register.getBlockEntries());
        //Prevent all our fluids from being duped by create
        getFluidBuilder(CREATE_NO_INFINITE_FLUID).add(register.getFluidEntries());
    }

    @SafeVarargs
    protected final void addToTag(TagKey<GameEvent> tag, DeferredHolder<GameEvent, GameEvent>... gameEventROs) {
        getGameEventBuilder(tag).add(DeferredHolder::getId, gameEventROs);
    }

    protected void addToTag(TagKey<DamageType> tag, MekanismDamageType... damageTypes) {
        getDamageTypeBuilder(tag).add(MekanismDamageType::registryName, damageTypes);
    }

    @SafeVarargs
    protected final void addEntitiesToTag(TagKey<EntityType<?>> tag, Holder<EntityType<?>>... entityTypeProviders) {
        getEntityTypeBuilder(tag).addHolders(entityTypeProviders);
    }

    protected void addToTag(TagKey<Fluid> tag, FluidRegistryObject<?, ?, ?, ?, ?>... fluidRegistryObjects) {
        IntrinsicMekanismTagBuilder<Fluid> tagBuilder = getFluidBuilder(tag);
        for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : fluidRegistryObjects) {
            tagBuilder.addHolders(fluidRO, fluidRO.getFlowingFluid());
            addItemsToTag(ItemTags.create(Tags.Items.BUCKETS.location().withSuffix("/" + fluidRO.getName())), fluidRO.getBucket());
        }
    }

    @SafeVarargs
    protected final void addChemicalsToTag(TagKey<Chemical> tag, Holder<Chemical>... chemicalProviders) {
        getChemicalBuilder(tag).addHolders(chemicalProviders);
    }

    @SafeVarargs
    protected final <TYPE> void addToTag(TagKey<TYPE> tag, ResourceKey<TYPE>... values) {
        final TagBuilder builder = getTagBuilder(tag.registry(), tag);
        for (ResourceKey<TYPE> value : values) {
            builder.addElement(value.location());
        }
    }
}
