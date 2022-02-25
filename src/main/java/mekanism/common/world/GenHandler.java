package mekanism.common.world;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreVeinConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreType.OreVeinType;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.height.ConfigurableHeightProvider;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.ModLoader;

public class GenHandler {

    private GenHandler() {
    }

    private static final Map<OreType, List<TargetBlockState>> ORE_STONE_TARGETS = new EnumMap<>(OreType.class);

    //TODO - 1.18: Move these to a better place?
    //TODO - 1.18: Do we want to name salt stuff like configurable uniform more accurately for them being hardcoded
    public static IntProviderType<ConfigurableConstantInt> CONFIGURABLE_CONSTANT;
    public static IntProviderType<ConfigurableUniformInt> CONFIGURABLE_UNIFORM;
    public static HeightProviderType<ConfigurableHeightProvider> CONFIGURABLE_HEIGHT_PROVIDER;

    private static final Map<OreType, MekFeature[]> ORES = new EnumMap<>(OreType.class);
    private static MekFeature SALT_FEATURE;

    public static void setupWorldGenFeatures() {
        if (ModLoader.isLoadingStateValid()) {
            //Validate our loading state is valid, and if it is register our configured features to the configured features registry
            // and our placed features to the placed features registry
            registerIntProviderTypes();
            CONFIGURABLE_HEIGHT_PROVIDER = Registry.register(Registry.HEIGHT_PROVIDER_TYPES, Mekanism.rl("configurable"), () -> ConfigurableHeightProvider.CODEC);
            for (OreType type : EnumUtils.ORE_TYPES) {
                List<TargetBlockState> targetStates = ORE_STONE_TARGETS.computeIfAbsent(type, oreType -> {
                    OreBlockType oreBlockType = MekanismBlocks.ORES.get(type);
                    return List.of(
                          OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, oreBlockType.stoneBlock().defaultBlockState()),
                          OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, oreBlockType.deepslateBlock().defaultBlockState())
                    );
                });
                int features = type.getBaseConfigs().size();
                MekFeature[] oreFeatures = new MekFeature[features];
                for (int vein = 0; vein < features; vein++) {
                    OreVeinType oreVeinType = new OreVeinType(type, vein);
                    oreFeatures[vein] = new MekFeature(
                          getOreFeature(oreVeinType, targetStates, MekanismFeatures.ORE.get(), false),
                          getOreFeature(oreVeinType, targetStates, MekanismFeatures.ORE_RETROGEN.get(), true)
                    );
                }
                ORES.put(type, oreFeatures);
            }
            SALT_FEATURE = new MekFeature(
                  getSaltFeature(PlacementUtils.HEIGHTMAP_TOP_SOLID, false),
                  getSaltFeature(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, true)
            );
        }
    }

    private static void registerIntProviderTypes() {
        CONFIGURABLE_CONSTANT = registerIntProviderType(Mekanism.rl("configurable_constant"), ConfigurableConstantInt.CODEC);
        CONFIGURABLE_UNIFORM = registerIntProviderType(Mekanism.rl("configurable_uniform"), ConfigurableUniformInt.CODEC);
    }

    private static <P extends IntProvider> IntProviderType<P> registerIntProviderType(ResourceLocation registryName, Codec<P> codec) {
        return Registry.register(Registry.INT_PROVIDER_TYPES, registryName, () -> codec);
    }

    public static void onBiomeLoad(BiomeLoadingEvent event) {
        if (isValidBiome(event.getCategory())) {
            BiomeGenerationSettingsBuilder generation = event.getGeneration();
            //Add ores
            for (MekFeature[] features : ORES.values()) {
                for (MekFeature feature : features) {
                    generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature.feature());
                }
            }
            //Add salt
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, SALT_FEATURE.feature());
        }
    }

    private static boolean isValidBiome(Biome.BiomeCategory biomeCategory) {
        //If this does weird things to unclassified biomes (Category.NONE), then we should also mark that biome as invalid
        return biomeCategory != BiomeCategory.THEEND && biomeCategory != BiomeCategory.NETHER;
    }

    @Nonnull
    private static PlacedFeature getOreFeature(OreVeinType oreVeinType, List<TargetBlockState> targetStates, Feature<ResizableOreFeatureConfig> feature,
          boolean retrogen) {
        OreVeinConfig oreVeinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
        ConfiguredFeature<?, ?> configuredFeature = new DisableableConfiguredFeature<>(feature, new ResizableOreFeatureConfig(targetStates,
              oreVeinType, oreVeinConfig.maxVeinSize(), oreVeinConfig.discardChanceOnAirExposure()), oreVeinConfig.shouldGenerate(), retrogen);
        PlacedFeature placedFeature = configuredFeature.placed(List.of(CountPlacement.of(new ConfigurableConstantInt(oreVeinType, oreVeinConfig.perChunk())),
              InSquarePlacement.spread(), HeightRangePlacement.of(ConfigurableHeightProvider.of(oreVeinType, oreVeinConfig)),
              BiomeFilter.biome()));
        //Register the features
        ResourceLocation registryName = registryName(oreVeinType.name(), retrogen);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, registryName, configuredFeature);
        Registry.register(BuiltinRegistries.PLACED_FEATURE, registryName, placedFeature);
        return placedFeature;
    }

    @Nonnull
    private static PlacedFeature getSaltFeature(PlacementModifier placement, boolean retrogen) {
        SaltConfig saltConfig = MekanismConfig.world.salt;
        ConfiguredFeature<?, ?> configuredFeature = new DisableableConfiguredFeature<>(MekanismFeatures.DISK.get(),
              new ResizableDiskConfig(MekanismBlocks.SALT_BLOCK.getBlock().defaultBlockState(), saltConfig), saltConfig.shouldGenerate, retrogen);
        PlacedFeature placedFeature = configuredFeature.placed(List.of(CountPlacement.of(new ConfigurableConstantInt(null, saltConfig.perChunk)),
              InSquarePlacement.spread(), placement, BiomeFilter.biome()));
        //Register the features
        ResourceLocation registryName = registryName("salt", retrogen);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, registryName, configuredFeature);
        Registry.register(BuiltinRegistries.PLACED_FEATURE, registryName, placedFeature);
        return placedFeature;
    }

    private static ResourceLocation registryName(String name, boolean retrogen) {
        if (retrogen) {
            name += "_retrogen";
        }
        return Mekanism.rl(name);
    }

    /**
     * @return {@code true} if some retro-generation happened.
     *
     * @apiNote Only call this method if the chunk at the given position is loaded.
     * @implNote Adapted from {@link ChunkGenerator#applyBiomeDecoration(WorldGenLevel, ChunkAccess, StructureFeatureManager)}.
     */
    public static boolean generate(ServerLevel world, ChunkPos chunkPos) {
        boolean generated = false;
        if (!SharedConstants.debugVoidTerrain(chunkPos)) {
            SectionPos sectionPos = SectionPos.of(chunkPos, world.getMinSection());
            BlockPos blockPos = sectionPos.origin();
            ChunkGenerator chunkGenerator = world.getChunkSource().getGenerator();
            WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
            long decorationSeed = random.setDecorationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());
            int decorationStep = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal() - 1;
            ToIntFunction<PlacedFeature> featureIndex;
            //Note: We use the runtime biome source instead of the actual biome source as that is all we have access to
            // and the only case in vanilla where it actually seems like it might make a difference is for super-flat
            // worlds which don't really have any generation to begin with. If this ends up causing issues in any modded
            // dimensions, then it might be worth ATing to access the actual biomeSource variable
            BiomeSource biomeSource = chunkGenerator.getBiomeSource();
            List<BiomeSource.StepFeatureData> list = biomeSource.featuresPerStep();
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
            Registry<PlacedFeature> registry = world.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            for (MekFeature[] features : ORES.values()) {
                for (MekFeature feature : features) {
                    generated |= place(registry, world, chunkGenerator, blockPos, random, decorationSeed, decorationStep, featureIndex, feature);
                }
            }
            generated |= place(registry, world, chunkGenerator, blockPos, random, decorationSeed, decorationStep, featureIndex, SALT_FEATURE);
            world.setCurrentlyGenerating(null);
        }
        return generated;
    }

    private static boolean place(Registry<PlacedFeature> registry, WorldGenLevel world, ChunkGenerator chunkGenerator, BlockPos blockPos, WorldgenRandom random,
          long decorationSeed, int decorationStep, ToIntFunction<PlacedFeature> featureIndex, MekFeature feature) {
        PlacedFeature retrogenFeature = feature.retrogen();
        //Check the index of the source feature instead of the retrogen feature
        random.setFeatureSeed(decorationSeed, featureIndex.applyAsInt(feature.feature()), decorationStep);
        world.setCurrentlyGenerating(() -> registry.getResourceKey(retrogenFeature).map(Object::toString).orElseGet(retrogenFeature::toString));
        //Note: We call placeWithContext directly to allow for doing a placeWithBiomeCheck, except by having the context pretend
        // it is the non retrogen feature which actually is added to the various biomes
        return retrogenFeature.placeWithContext(new PlacementContext(world, chunkGenerator, Optional.of(feature.feature())), random, blockPos);
    }

    private record MekFeature(PlacedFeature feature, PlacedFeature retrogen) {
    }
}