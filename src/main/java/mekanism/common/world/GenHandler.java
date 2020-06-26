package mekanism.common.world;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import com.google.common.collect.Lists;
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
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

public class GenHandler {

    private static final Map<OreType, ConfiguredFeature<?, ?>> ORES = new EnumMap<>(OreType.class);
    private static final Map<OreType, ConfiguredFeature<?, ?>> ORE_RETROGENS = new EnumMap<>(OreType.class);

    private static ConfiguredFeature<?, ?> SALT_FEATURE;
    private static ConfiguredFeature<?, ?> SALT_RETROGEN_FEATURE;

    public static void setupWorldGeneration() {
        for (OreType type : EnumUtils.ORE_TYPES) {
            ORES.put(type, getOreFeature(MekanismBlocks.ORES.get(type), MekanismConfig.world.ores.get(type), Feature.ORE));
        }
        SALT_FEATURE = getSaltFeature(MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt, Placement.COUNT_TOP_SOLID);
        //Retrogen features
        if (MekanismConfig.world.enableRegeneration.get()) {
            for (OreType type : EnumUtils.ORE_TYPES) {
                ORE_RETROGENS.put(type, getOreFeature(MekanismBlocks.ORES.get(type), MekanismConfig.world.ores.get(type), MekanismFeatures.ORE_RETROGEN.getFeature()));
            }
            SALT_RETROGEN_FEATURE = getSaltFeature(MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt, MekanismPlacements.TOP_SOLID_RETROGEN.getPlacement());
        }
        ForgeRegistries.BIOMES.forEach(biome -> {
            if (isValidBiome(biome)) {
                //Add ores
                for (ConfiguredFeature<?, ?> feature : ORES.values()) {
                    addFeature(biome, feature);
                }
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
            for (ConfiguredFeature<?, ?> feature : ORE_RETROGENS.values()) {
                generated |= placeFeature(feature, world, random, blockPos);
            }
            generated |= placeFeature(SALT_RETROGEN_FEATURE, world, random, blockPos);
        }
        return generated;
    }

    private static boolean placeFeature(@Nullable ConfiguredFeature<?, ?> feature, ServerWorld world, Random random, BlockPos blockPos) {
        if (feature != null) {
            feature.func_236265_a_(world, world.func_241112_a_(), world.getChunkProvider().getChunkGenerator(), random, blockPos);
            return true;
        }
        return false;
    }
}