package mekanism.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import mekanism.common.Mekanism;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreType.OreVeinType;
import mekanism.common.util.EnumUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.jetbrains.annotations.Nullable;

public class GenHandler {

    private GenHandler() {
    }

    @Nullable
    private static List<MekFeature> cachedFeatures;

    /**
     * Call to clear the cached holder lookup of placed features.
     */
    public static void reset() {
        cachedFeatures = null;
    }

    /**
     * @return {@code true} if some retro-generation happened.
     *
     * @apiNote Only call this method if the chunk at the given position is loaded.
     * @implNote Adapted from {@link ChunkGenerator#applyBiomeDecoration(WorldGenLevel, ChunkAccess, StructureManager)}.
     */
    public static boolean generate(ServerLevel world, ChunkPos chunkPos) {
        boolean generated = false;
        if (!SharedConstants.debugVoidTerrain(chunkPos)) {
            SectionPos sectionPos = SectionPos.of(chunkPos, world.getMinSection());
            BlockPos blockPos = sectionPos.origin();
            ChunkGenerator chunkGenerator = world.getChunkSource().getGenerator();
            WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
            long decorationSeed = random.setDecorationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());
            int decorationStep = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal() - 1;
            ToIntFunction<PlacedFeature> featureIndex;
            List<FeatureSorter.StepFeatureData> list = chunkGenerator.featuresPerStep.get();
            if (decorationStep < list.size()) {
                //Use the feature index lookup mapping. We can skip a lot of vanilla's logic here that is needed
                // for purposes of getting all the features we want to be doing, as we know which features we want
                // to generate and only lookup those. We also don't need to worry about if the biome can actually
                // support our feature as that is validated via the placement context and allows us to drastically
                // cut down on calculating it here
                featureIndex = list.get(decorationStep).indexMapping();
            } else {
                featureIndex = feature -> -1;
            }
            List<MekFeature> features = getMekanismFeatures(world.registryAccess());
            for (MekFeature feature : features) {
                generated |= place(world, chunkGenerator, blockPos, random, decorationSeed, decorationStep, featureIndex, feature);
            }
            world.setCurrentlyGenerating(null);
        }
        return generated;
    }

    private static boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, BlockPos blockPos, WorldgenRandom random,
          long decorationSeed, int decorationStep, ToIntFunction<PlacedFeature> featureIndex, MekFeature feature) {
        PlacedFeature baseFeature = feature.feature().value();
        //Check the index of the source feature instead of the retrogen feature
        random.setFeatureSeed(decorationSeed, featureIndex.applyAsInt(baseFeature), decorationStep);
        world.setCurrentlyGenerating(feature::retrogenKey);
        //Note: We call placeWithContext directly to allow for doing a placeWithBiomeCheck, except by having the context pretend
        // it is the non retrogen feature which actually is added to the various biomes
        return feature.retrogen().value().placeWithContext(new PlacementContext(world, chunkGenerator, Optional.of(baseFeature)), random, blockPos);
    }

    private static List<MekFeature> getMekanismFeatures(RegistryAccess registryAccess) {
        if (cachedFeatures != null) {
            return cachedFeatures;
        }
        cachedFeatures = new ArrayList<>();
        Registry<PlacedFeature> placedFeatures = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
        for (OreType type : EnumUtils.ORE_TYPES) {
            for (int vein = 0, features = type.getBaseConfigs().size(); vein < features; vein++) {
                OreVeinType oreVeinType = new OreVeinType(type, vein);
                MekFeature mekFeature = MekFeature.create(placedFeatures, Mekanism.rl(oreVeinType.name()));
                if (mekFeature != null) {
                    cachedFeatures.add(mekFeature);
                }
            }
        }
        MekFeature saltFeature = MekFeature.create(placedFeatures, Mekanism.rl("salt"));
        if (saltFeature != null) {
            cachedFeatures.add(saltFeature);
        }
        return cachedFeatures;
    }

    private record MekFeature(Holder<PlacedFeature> feature, Holder<PlacedFeature> retrogen, String retrogenKey) {

        @Nullable
        public static MekFeature create(Registry<PlacedFeature> placedFeatures, ResourceLocation name) {
            Optional<Reference<PlacedFeature>> placedFeature = placedFeatures.getHolder(ResourceKey.create(Registries.PLACED_FEATURE, name));
            if (placedFeature.isEmpty()) {
                Mekanism.logger.error("Failed to retrieve placed feature ({}).", name);
                return null;
            }
            ResourceLocation retrogenName = name.withSuffix("_retrogen");
            ResourceKey<PlacedFeature> retrogenKey = ResourceKey.create(Registries.PLACED_FEATURE, retrogenName);
            Optional<Reference<PlacedFeature>> retrogenFeature = placedFeatures.getHolder(retrogenKey);
            if (retrogenFeature.isEmpty()) {
                Mekanism.logger.error("Failed to retrieve retrogen placed feature ({}).", retrogenName);
                return null;
            }
            return new MekFeature(placedFeature.get(), retrogenFeature.get(), retrogenKey.toString());
        }
    }
}