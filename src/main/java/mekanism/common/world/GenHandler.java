package mekanism.common.world;

import com.google.common.collect.ImmutableList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismPlacements;
import mekanism.common.resource.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.feature.Features.Placements;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class GenHandler {

    private GenHandler() {
    }

    private static final Map<OreType, ConfiguredFeature<?, ?>> ORES = new EnumMap<>(OreType.class);
    private static final Map<OreType, ConfiguredFeature<?, ?>> ORE_RETROGENS = new EnumMap<>(OreType.class);

    private static ConfiguredFeature<?, ?> SALT_FEATURE;
    private static ConfiguredFeature<?, ?> SALT_RETROGEN_FEATURE;

    public static void setupWorldGenFeatures() {
        for (OreType type : EnumUtils.ORE_TYPES) {
            ORES.put(type, getOreFeature(MekanismBlocks.ORES.get(type), MekanismConfig.world.ores.get(type), Feature.ORE));
        }
        SALT_FEATURE = getSaltFeature(MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt, Placements.KELP_PLACEMENT);
        //Retrogen features
        if (MekanismConfig.world.enableRegeneration.get()) {
            for (OreType type : EnumUtils.ORE_TYPES) {
                ORE_RETROGENS.put(type, getOreFeature(MekanismBlocks.ORES.get(type), MekanismConfig.world.ores.get(type), MekanismFeatures.ORE_RETROGEN.getFeature()));
            }
            SALT_RETROGEN_FEATURE = getSaltFeature(MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt,
                  MekanismPlacements.TOP_SOLID_RETROGEN.getConfigured(IPlacementConfig.NO_PLACEMENT_CONFIG));
        }
    }

    public static void onBiomeLoad(BiomeLoadingEvent event) {
        if (isValidBiome(event.getCategory())) {
            BiomeGenerationSettingsBuilder generation = event.getGeneration();
            //Add ores
            for (ConfiguredFeature<?, ?> feature : ORES.values()) {
                addFeature(generation, feature);
            }
            //Add salt
            addFeature(generation, SALT_FEATURE);
        }
    }

    private static boolean isValidBiome(Biome.Category biomeCategory) {
        //If this does weird things to unclassified biomes (Category.NONE), then we should also mark that biome as invalid
        return biomeCategory != Category.THEEND && biomeCategory != Category.NETHER;
    }

    private static void addFeature(BiomeGenerationSettingsBuilder generation, @Nullable ConfiguredFeature<?, ?> feature) {
        if (feature != null) {
            generation.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
        }
    }

    @Nullable
    private static ConfiguredFeature<?, ?> getOreFeature(IBlockProvider blockProvider, OreConfig oreConfig, Feature<OreFeatureConfig> feature) {
        if (oreConfig.shouldGenerate.get()) {
            return feature.withConfiguration(new OreFeatureConfig(FillerBlockType.field_241882_a, blockProvider.getBlock().getDefaultState(), oreConfig.maxVeinSize.get()))
                  .func_242733_d(oreConfig.maxHeight.get())
                  .func_242728_a()
                  .func_242731_b(oreConfig.perChunk.get());
        }
        return null;
    }

    @Nullable
    private static ConfiguredFeature<?, ?> getSaltFeature(IBlockProvider blockProvider, SaltConfig saltConfig, ConfiguredPlacement<NoPlacementConfig> placement) {
        if (saltConfig.shouldGenerate.get()) {
            BlockState state = blockProvider.getBlock().getDefaultState();
            return Feature.DISK.withConfiguration(new SphereReplaceConfig(state, FeatureSpread.func_242253_a(saltConfig.baseRadius.get(), saltConfig.spread.get()),
                  saltConfig.ySize.get(), ImmutableList.of(Blocks.DIRT.getDefaultState(), Blocks.CLAY.getDefaultState(), state)))
                  .withPlacement(placement.func_242728_a()).func_242731_b(saltConfig.perChunk.get());
        }
        return null;
    }

    /**
     * @return True if some retro-generation happened, false otherwise
     */
    public static boolean generate(ServerWorld world, Random random, int chunkX, int chunkZ) {
        BlockPos blockPos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        Biome biome = world.getBiome(blockPos);
        boolean generated = false;
        if (isValidBiome(biome.getCategory()) && world.chunkExists(chunkX, chunkZ)) {
            for (ConfiguredFeature<?, ?> feature : ORE_RETROGENS.values()) {
                generated |= placeFeature(feature, world, random, blockPos);
            }
            generated |= placeFeature(SALT_RETROGEN_FEATURE, world, random, blockPos);
        }
        return generated;
    }

    private static boolean placeFeature(@Nullable ConfiguredFeature<?, ?> feature, ServerWorld world, Random random, BlockPos blockPos) {
        if (feature != null) {
            feature.func_242765_a(world, world.getChunkProvider().getChunkGenerator(), random, blockPos);
            return true;
        }
        return false;
    }
}