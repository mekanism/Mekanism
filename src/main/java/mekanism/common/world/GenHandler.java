package mekanism.common.world;

import com.google.common.collect.Lists;
import java.util.Random;
import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

public class GenHandler {

    private static ConfiguredFeature<?, ?> COPPER_FEATURE;
    private static ConfiguredFeature<?, ?> TIN_FEATURE;
    private static ConfiguredFeature<?, ?> OSMIUM_FEATURE;
    private static ConfiguredFeature<?, ?> SALT_FEATURE;

    public static void setupWorldGeneration() {
        COPPER_FEATURE = getOreFeature(MekanismBlocks.COPPER_ORE, MekanismConfig.world.copper);
        TIN_FEATURE = getOreFeature(MekanismBlocks.TIN_ORE, MekanismConfig.world.tin);
        OSMIUM_FEATURE = getOreFeature(MekanismBlocks.OSMIUM_ORE, MekanismConfig.world.osmium);
        SALT_FEATURE = getSaltFeature(MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt);
        ForgeRegistries.BIOMES.forEach(biome -> {
            if (isValidBiome(biome)) {
                //Add ores
                addFeature(biome, COPPER_FEATURE);
                addFeature(biome, TIN_FEATURE);
                addFeature(biome, OSMIUM_FEATURE);
                //Add salt
                addFeature(biome, SALT_FEATURE);
            }
        });
    }

    private static boolean isValidBiome(Biome biome) {
        //TODO: Decide if we want to stop the generation when the category is Category.NONE as well
        // we probably do not so that in case mods do not categorize their biome, we can still add our ore
        return biome.getCategory() != Category.THEEND && biome.getCategory() != Category.NETHER;
    }

    private static void addFeature(Biome biome, @Nullable ConfiguredFeature<?, ?> feature) {
        if (feature != null) {
            biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
        }
    }

    @Nullable
    private static ConfiguredFeature<?, ?> getOreFeature(IBlockProvider blockProvider, OreConfig oreConfig) {
        if (oreConfig.shouldGenerate.get()) {
            return Feature.ORE.withConfiguration(new OreFeatureConfig(FillerBlockType.NATURAL_STONE,
                  blockProvider.getBlock().getDefaultState(), oreConfig.maxVeinSize.get())).func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(
                  new CountRangeConfig(oreConfig.perChunk.get(), oreConfig.bottomOffset.get(), oreConfig.topOffset.get(), oreConfig.maxHeight.get())));
        }
        return null;
    }

    @Nullable
    private static ConfiguredFeature<?, ?> getSaltFeature(IBlockProvider blockProvider, SaltConfig saltConfig) {
        if (saltConfig.shouldGenerate.get()) {
            BlockState state = blockProvider.getBlock().getDefaultState();
            return Feature.DISK.withConfiguration(new SphereReplaceConfig(state, saltConfig.maxVeinSize.get(),
                  saltConfig.ySize.get(), Lists.newArrayList(Blocks.DIRT.getDefaultState(), Blocks.CLAY.getDefaultState(), state)))
                  .func_227228_a_(Placement.COUNT_TOP_SOLID.func_227446_a_(new FrequencyConfig(saltConfig.perChunk.get())));
        }
        return null;
    }

    public static void generate(IWorld world, ChunkGenerator<? extends GenerationSettings> chunkGenerator, Random random, int chunkX, int chunkZ) {
        BlockPos blockPos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        Biome biome = world.getBiome(blockPos);
        if (isValidBiome(biome)) {
            placeFeature(COPPER_FEATURE, world, chunkGenerator, random, blockPos);
            placeFeature(TIN_FEATURE, world, chunkGenerator, random, blockPos);
            placeFeature(OSMIUM_FEATURE, world, chunkGenerator, random, blockPos);
            placeFeature(SALT_FEATURE, world, chunkGenerator, random, blockPos);
        }
    }

    private static void placeFeature(@Nullable ConfiguredFeature<?, ?> feature, IWorld world, ChunkGenerator<? extends GenerationSettings> chunkGenerator, Random random,
          BlockPos blockPos) {
        if (feature != null) {
            feature.place(world, chunkGenerator, random, blockPos);
        }
    }
}