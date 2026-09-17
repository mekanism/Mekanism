package mekanism.common.registries;

import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.api.chemical.BasicChemical;
import mekanism.api.chemical.Chemical;
import mekanism.common.base.IChemicalConstant;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public abstract class BaseRegistryProvider {

    public static DatapackBuiltinEntriesProvider forWorldLayer(PackOutput output, CompletableFuture<Provider> worldRegistries, String modid, RegistrySetBuilder entriesBuilder) {
        return DatapackBuiltinEntriesProvider.forWorldLayer(output, modid + "-world", worldRegistries, entriesBuilder, Set.of(modid));
    }

    public static DatapackBuiltinEntriesProvider forReloadableLayer(PackOutput output, CompletableFuture<Provider> worldRegistries,
          CompletableFuture<Provider> reloadableRegistries, String modid, RegistrySetBuilder entriesBuilder) {
        return DatapackBuiltinEntriesProvider.forReloadableLayer(output, modid + "-reloadable", worldRegistries, reloadableRegistries, entriesBuilder, Set.of(modid));
    }

    protected static void registerPlacedFeature(BootstrapContext<PlacedFeature> context, Identifier name, Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        registerPlacedFeature(context, name, name, placementModifiers);
    }

    protected static void registerPlacedFeature(BootstrapContext<PlacedFeature> context, Identifier name, Identifier retrogenName,
          Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        HolderGetter<Feature> configuredFeatures = context.lookup(Registries.FEATURE);

        Reference<Feature> configuredFeature = configuredFeatures.getOrThrow(feature(name));
        context.register(placedFeature(name), new PlacedFeature(configuredFeature, placementModifiers.get(false)));

        Reference<Feature> retrogenConfiguredFeature = configuredFeatures.getOrThrow(feature(retrogenName));
        context.register(placedFeature(name.withSuffix("_retrogen")), new PlacedFeature(retrogenConfiguredFeature, placementModifiers.get(true)));
    }

    protected static ResourceKey<Feature> feature(Identifier name) {
        return ResourceKey.create(Registries.FEATURE, name);
    }

    protected static ResourceKey<PlacedFeature> placedFeature(Identifier name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, name);
    }

    protected static ResourceKey<BiomeModifier> biomeModifier(Identifier name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, name);
    }

    protected static ResourceKey<StructureModifier> structureModifier(Identifier name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS, name);
    }

    protected static void registerConstant(BootstrapContext<Chemical> context, IChemicalConstant constant) {
        context.register(constant.key(), BasicChemical.builder().tint(constant.getColor()).lightLevel(constant.getLightLevel()).build());
    }

    protected static ContextIntProvider cooking(HolderGetter<LootItemCondition> predicates, Holder.Reference<ContextIntProvider> normalBurnTimeDivisor,
          Holder.Reference<ContextIntProvider> fastBurnTimeDivisor, int timeSeconds) {
        Holder<LootItemCondition> fasterCookingBlocks = predicates.getOrThrow(LootPredicates.FAST_FURNACE);
        ContextIntProvider fastConditional = new ConditionalValue(fasterCookingBlocks, fastBurnTimeDivisor, normalBurnTimeDivisor);
        return ContextIntProviders.div(ContextIntProviders.exactly(timeSeconds), Holder.direct(fastConditional)).value();
    }
}