package mekanism.common.world;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismPlacements;
import mekanism.common.resource.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features.Placements;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.ModLoader;

public class GenHandler {

    private GenHandler() {
    }

    private static final Map<OreType, ConfiguredFeature<?, ?>> ORES = new EnumMap<>(OreType.class);
    private static final Map<OreType, ConfiguredFeature<?, ?>> ORE_RETROGENS = new EnumMap<>(OreType.class);

    private static ConfiguredFeature<?, ?> SALT_FEATURE;
    private static ConfiguredFeature<?, ?> SALT_RETROGEN_FEATURE;

    public static void setupWorldGenFeatures() {
        if (ModLoader.isLoadingStateValid()) {
            //Validate our loading state is valid, and if it is register our configured features to the configured features registry
            for (OreType type : EnumUtils.ORE_TYPES) {
                ORES.put(type, getOreFeature(type, MekanismFeatures.ORE.getFeature(), false));
                ORE_RETROGENS.put(type, getOreFeature(type, MekanismFeatures.ORE_RETROGEN.getFeature(), true));
            }
            SALT_FEATURE = getSaltFeature(MekanismConfig.world.salt, Placements.KELP_PLACEMENT, false);
            SALT_RETROGEN_FEATURE = getSaltFeature(MekanismConfig.world.salt, MekanismPlacements.TOP_SOLID_RETROGEN.getConfigured(IPlacementConfig.NO_PLACEMENT_CONFIG), true);
        }
    }

    public static void onBiomeLoad(BiomeLoadingEvent event) {
        if (isValidBiome(event.getCategory())) {
            BiomeGenerationSettingsBuilder generation = event.getGeneration();
            //Add ores
            for (ConfiguredFeature<?, ?> feature : ORES.values()) {
                generation.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
            }
            //Add salt
            generation.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, SALT_FEATURE);
        }
    }

    private static boolean isValidBiome(Biome.Category biomeCategory) {
        //If this does weird things to unclassified biomes (Category.NONE), then we should also mark that biome as invalid
        return biomeCategory != Category.THEEND && biomeCategory != Category.NETHER;
    }

    @Nonnull
    private static ConfiguredFeature<?, ?> getOreFeature(OreType type, Feature<ResizableOreFeatureConfig> feature, boolean retroGen) {
        OreConfig oreConfig = MekanismConfig.world.ores.get(type);
        ConfiguredFeature<?, ?> configuredFeature = new DisableableConfiguredFeature<>(feature, new ResizableOreFeatureConfig(FillerBlockType.BASE_STONE_OVERWORLD,
              type, oreConfig.maxVeinSize), oreConfig.shouldGenerate, retroGen)
              .withPlacement(MekanismPlacements.RESIZABLE_RANGE.getConfigured(new ResizableTopSolidRangeConfig(type, oreConfig)))
              .square()
              .withPlacement(MekanismPlacements.ADJUSTABLE_COUNT.getConfigured(new AdjustableSpreadConfig(type, oreConfig.perChunk)));
        //Register the configured feature
        String name = "ore_" + type.getResource().getRegistrySuffix();
        if (retroGen) {
            name += "_retrogen";
        }
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, Mekanism.rl(name), configuredFeature);
        return configuredFeature;
    }

    @Nonnull
    private static ConfiguredFeature<?, ?> getSaltFeature(SaltConfig saltConfig, ConfiguredPlacement<NoPlacementConfig> placement, boolean retroGen) {
        ConfiguredFeature<?, ?> configuredFeature = new DisableableConfiguredFeature<>(MekanismFeatures.DISK.getFeature(),
              new ResizableSphereReplaceConfig(MekanismBlocks.SALT_BLOCK.getBlock().getDefaultState(), saltConfig), saltConfig.shouldGenerate, retroGen)
              .withPlacement(placement.square())
              .withPlacement(MekanismPlacements.ADJUSTABLE_COUNT.getConfigured(new AdjustableSpreadConfig(null, saltConfig.perChunk)));
        //Register the configured feature
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, Mekanism.rl(retroGen ? "salt_retrogen" : "salt"), configuredFeature);
        return configuredFeature;
    }

    /**
     * @return True if some retro-generation happened, false otherwise
     */
    public static boolean generate(ServerWorld world, Random random, int chunkX, int chunkZ) {
        BlockPos blockPos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        Biome biome = world.getBiome(blockPos);
        boolean generated = false;
        if (isValidBiome(biome.getCategory()) && world.chunkExists(chunkX, chunkZ)) {
            ChunkGenerator chunkGenerator = world.getChunkProvider().getChunkGenerator();
            for (ConfiguredFeature<?, ?> feature : ORE_RETROGENS.values()) {
                generated |= feature.generate(world, chunkGenerator, random, blockPos);
            }
            generated |= SALT_RETROGEN_FEATURE.generate(world, chunkGenerator, random, blockPos);
        }
        return generated;
    }
}