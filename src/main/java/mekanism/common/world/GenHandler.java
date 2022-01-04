package mekanism.common.world;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.resource.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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
    public static HeightProviderType<ConfigurableUniformHeight> CONFIGURABLE_UNIFORM_HEIGHT;

    //TODO - 1.18: Look at OreFeatures do we want multiple per type? Quite likely
    private static final Map<OreType, PlacedFeature> ORES = new EnumMap<>(OreType.class);
    private static final Map<OreType, PlacedFeature> ORE_RETROGENS = new EnumMap<>(OreType.class);

    private static PlacedFeature SALT_FEATURE;
    private static PlacedFeature SALT_RETROGEN_FEATURE;

    public static void setupWorldGenFeatures() {
        if (ModLoader.isLoadingStateValid()) {
            //Validate our loading state is valid, and if it is register our configured features to the configured features registry
            // and our placed features to the placed features registry
            registerIntProviderTypes();
            registerHeightProviderTypes();
            for (OreType type : EnumUtils.ORE_TYPES) {
                List<TargetBlockState> targetStates = ORE_STONE_TARGETS.computeIfAbsent(type, oreType -> List.of(
                      OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, MekanismBlocks.ORES.get(type).getBlock().defaultBlockState()),
                      //TODO - 1.18: Deepslate ore variants
                      OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, MekanismBlocks.ORES.get(type).getBlock().defaultBlockState())
                ));
                ORES.put(type, getOreFeature(type, targetStates, MekanismFeatures.ORE.get(), false));
                ORE_RETROGENS.put(type, getOreFeature(type, targetStates, MekanismFeatures.ORE_RETROGEN.get(), true));
            }
            SALT_FEATURE = getSaltFeature(MekanismConfig.world.salt, PlacementUtils.HEIGHTMAP_TOP_SOLID, false);
            //TODO - 1.18: Test that this vanilla palcement works fine
            SALT_RETROGEN_FEATURE = getSaltFeature(MekanismConfig.world.salt, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, true);
        }
    }

    private static void registerHeightProviderTypes() {
        CONFIGURABLE_UNIFORM_HEIGHT = registerHeightProviderType(Mekanism.rl("configurable_uniform"), ConfigurableUniformHeight.CODEC);
    }

    private static <P extends HeightProvider> HeightProviderType<P> registerHeightProviderType(ResourceLocation registryName, Codec<P> codec) {
        return Registry.register(Registry.HEIGHT_PROVIDER_TYPES, registryName, () -> codec);
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
            for (PlacedFeature feature : ORES.values()) {
                generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
            //Add salt
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, SALT_FEATURE);
        }
    }

    private static boolean isValidBiome(Biome.BiomeCategory biomeCategory) {
        //If this does weird things to unclassified biomes (Category.NONE), then we should also mark that biome as invalid
        return biomeCategory != BiomeCategory.THEEND && biomeCategory != BiomeCategory.NETHER;
    }

    @Nonnull
    private static PlacedFeature getOreFeature(OreType type, List<TargetBlockState> targetStates, Feature<ResizableOreFeatureConfig> feature, boolean retroGen) {
        OreConfig oreConfig = MekanismConfig.world.ores.get(type);
        ConfiguredFeature<?, ?> configuredFeature = new DisableableConfiguredFeature<>(feature, new ResizableOreFeatureConfig(targetStates,
              type, oreConfig.maxVeinSize, oreConfig.discardChanceOnAirExposure), oreConfig.shouldGenerate, retroGen);
        PlacedFeature placedFeature = configuredFeature.placed(List.of(CountPlacement.of(new ConfigurableConstantInt(type, oreConfig.perChunk)),
              InSquarePlacement.spread(), HeightRangePlacement.of(ConfigurableUniformHeight.of(type, oreConfig)),
              BiomeFilter.biome()));
        //Register the features
        String name = "ore_" + type.getResource().getRegistrySuffix();
        if (retroGen) {
            name += "_retrogen";
        }
        ResourceLocation registryName = Mekanism.rl(name);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, registryName, configuredFeature);
        Registry.register(BuiltinRegistries.PLACED_FEATURE, registryName, placedFeature);
        return placedFeature;
    }

    @Nonnull
    private static PlacedFeature getSaltFeature(SaltConfig saltConfig, PlacementModifier placement, boolean retroGen) {
        ConfiguredFeature<?, ?> configuredFeature = new DisableableConfiguredFeature<>(MekanismFeatures.DISK.get(),
              new ResizableDiskConfig(MekanismBlocks.SALT_BLOCK.getBlock().defaultBlockState(), saltConfig), saltConfig.shouldGenerate, retroGen);
        PlacedFeature placedFeature = configuredFeature.placed(List.of(CountPlacement.of(new ConfigurableConstantInt(null, saltConfig.perChunk)),
              InSquarePlacement.spread(), placement, BiomeFilter.biome()));
        //Register the features
        ResourceLocation registryName = Mekanism.rl(retroGen ? "salt_retrogen" : "salt");
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, registryName, configuredFeature);
        Registry.register(BuiltinRegistries.PLACED_FEATURE, registryName, placedFeature);
        return placedFeature;
    }

    /**
     * @return True if some retro-generation happened, false otherwise
     */
    public static boolean generate(ServerLevel world, Random random, int chunkX, int chunkZ) {
        BlockPos blockPos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        Biome biome = world.getBiome(blockPos);
        boolean generated = false;
        if (isValidBiome(biome.getBiomeCategory()) && world.hasChunk(chunkX, chunkZ)) {
            ChunkGenerator chunkGenerator = world.getChunkSource().getGenerator();
            for (PlacedFeature feature : ORE_RETROGENS.values()) {
                generated |= feature.place(world, chunkGenerator, random, blockPos);
            }
            generated |= SALT_RETROGEN_FEATURE.place(world, chunkGenerator, random, blockPos);
        }
        return generated;
    }
}