package mekanism.common.registries;

import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDatapackRegistryProvider extends DatapackBuiltinEntriesProvider {

    private final String modid;

    protected BaseDatapackRegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, RegistrySetBuilder registrySetBuilder, String modid) {
        super(output, registries, registrySetBuilder, Set.of(modid));
        this.modid = modid;
    }

    @NotNull
    @Override
    public String getName() {
        return "Datapack registries: " + modid;
    }

    protected static PlacedFeaturesHolder registerPlacedFeature(BootstapContext<PlacedFeature> context, ResourceLocation name,
          Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        return registerPlacedFeature(context, name, name, placementModifiers);
    }

    protected static PlacedFeaturesHolder registerPlacedFeature(BootstapContext<PlacedFeature> context, ResourceLocation name, ResourceLocation retrogenName,
          Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Reference<ConfiguredFeature<?, ?>> configuredFeature = configuredFeatures.getOrThrow(configuredFeature(name));
        Reference<ConfiguredFeature<?, ?>> retrogenConfiguredFeature = configuredFeatures.getOrThrow(configuredFeature(retrogenName));
        return new PlacedFeaturesHolder(
              context.register(placedFeature(name), new PlacedFeature(configuredFeature, placementModifiers.get(false))),
              context.register(placedFeature(name.withSuffix("_retrogen")), new PlacedFeature(retrogenConfiguredFeature, placementModifiers.get(true)))
        );
    }

    protected static ResourceKey<ConfiguredFeature<?, ?>> configuredFeature(ResourceLocation name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, name);
    }

    protected static ResourceKey<PlacedFeature> placedFeature(ResourceLocation name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, name);
    }

    protected static ResourceKey<BiomeModifier> biomeModifier(ResourceLocation name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, name);
    }

    protected static ResourceKey<StructureModifier> structureModifier(ResourceLocation name) {
        return ResourceKey.create(ForgeRegistries.Keys.STRUCTURE_MODIFIERS, name);
    }

    protected record PlacedFeaturesHolder(Holder.Reference<PlacedFeature> feature, Holder.Reference<PlacedFeature> retrogen) {

    }
}