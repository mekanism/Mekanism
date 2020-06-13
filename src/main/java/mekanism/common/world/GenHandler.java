package mekanism.common.world;

import com.google.common.collect.Lists;
import java.util.Random;
import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismPlacements;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.registries.ForgeRegistries;

public class GenHandler {

    private static ConfiguredFeature<?, ?> COPPER_FEATURE;
    private static ConfiguredFeature<?, ?> COPPER_RETROGEN_FEATURE;
    private static ConfiguredFeature<?, ?> TIN_FEATURE;
    private static ConfiguredFeature<?, ?> TIN_RETROGEN_FEATURE;
    private static ConfiguredFeature<?, ?> OSMIUM_FEATURE;
    private static ConfiguredFeature<?, ?> OSMIUM_RETROGEN_FEATURE;
    private static ConfiguredFeature<?, ?> SALT_FEATURE;
    private static ConfiguredFeature<?, ?> SALT_RETROGEN_FEATURE;

    public static void setupWorldGeneration() {
        COPPER_FEATURE = getOreFeature(MekanismBlocks.COPPER_ORE, MekanismConfig.world.copper, Feature.ORE);
        TIN_FEATURE = getOreFeature(MekanismBlocks.TIN_ORE, MekanismConfig.world.tin, Feature.ORE);
        OSMIUM_FEATURE = getOreFeature(MekanismBlocks.OSMIUM_ORE, MekanismConfig.world.osmium, Feature.ORE);
        SALT_FEATURE = getSaltFeature(MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt, Placement.COUNT_TOP_SOLID);
        //Retrogen features
        if (MekanismConfig.world.enableRegeneration.get()) {
            COPPER_RETROGEN_FEATURE = getOreFeature(MekanismBlocks.COPPER_ORE, MekanismConfig.world.copper, MekanismFeatures.ORE_RETROGEN.getFeature());
            TIN_RETROGEN_FEATURE = getOreFeature(MekanismBlocks.TIN_ORE, MekanismConfig.world.tin, MekanismFeatures.ORE_RETROGEN.getFeature());
            OSMIUM_RETROGEN_FEATURE = getOreFeature(MekanismBlocks.OSMIUM_ORE, MekanismConfig.world.osmium, MekanismFeatures.ORE_RETROGEN.getFeature());
            SALT_RETROGEN_FEATURE = getSaltFeature(MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt, MekanismPlacements.TOP_SOLID_RETROGEN.getPlacement());
        }
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
        //If this does weird things to unclassified biomes (Category.NONE), then we should also mark that biome as invalid
        return biome.getCategory() != Category.THEEND && biome.getCategory() != Category.NETHER;
    }

    private static void addFeature(Biome biome, @Nullable ConfiguredFeature<?, ?> feature) {
        if (feature != null) {
            biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
        }
    }

    @Nullable
    private static ConfiguredFeature<?, ?> getOreFeature(IBlockProvider blockProvider, OreConfig oreConfig, Feature<OreFeatureConfig> feature) {
        if (oreConfig.shouldGenerate.get()) {
            return feature.withConfiguration(new OreFeatureConfig(FillerBlockType.NATURAL_STONE,
                  blockProvider.getBlock().getDefaultState(), oreConfig.maxVeinSize.get())).withPlacement(Placement.COUNT_RANGE.configure(
                  new CountRangeConfig(oreConfig.perChunk.get(), oreConfig.bottomOffset.get(), oreConfig.topOffset.get(), oreConfig.maxHeight.get())));
        }
        return null;
    }

    @Nullable
    private static ConfiguredFeature<?, ?> getSaltFeature(IBlockProvider blockProvider, SaltConfig saltConfig, Placement<FrequencyConfig> placement) {
        if (saltConfig.shouldGenerate.get()) {
            BlockState state = blockProvider.getBlock().getDefaultState();
            return Feature.DISK.withConfiguration(new SphereReplaceConfig(state, saltConfig.maxVeinSize.get(), saltConfig.ySize.get(),
                  Lists.newArrayList(Blocks.DIRT.getDefaultState(), Blocks.CLAY.getDefaultState(), state)))
                  .withPlacement(placement.configure(new FrequencyConfig(saltConfig.perChunk.get())));
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
        if (isValidBiome(biome) && world.chunkExists(chunkX, chunkZ)) {
            generated = placeFeature(COPPER_RETROGEN_FEATURE, world, random, blockPos);
            generated |= placeFeature(TIN_RETROGEN_FEATURE, world, random, blockPos);
            generated |= placeFeature(OSMIUM_RETROGEN_FEATURE, world, random, blockPos);
            generated |= placeFeature(SALT_RETROGEN_FEATURE, world, random, blockPos);
        }
        return generated;
    }

    private static boolean placeFeature(@Nullable ConfiguredFeature<?, ?> feature, ServerWorld world, Random random, BlockPos blockPos) {
        if (feature != null) {
            feature.place(world, world.getChunkProvider().getChunkGenerator(), random, blockPos);
            return true;
        }
        return false;
    }
}