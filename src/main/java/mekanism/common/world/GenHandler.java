package mekanism.common.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.MekanismBlock;
import mekanism.common.config.MekanismConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class GenHandler {

    public static void setupWorldGeneration() {
        for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) {
            ImmutableList<BiomeEntry> biomes = BiomeManager.getBiomes(type);
            if (biomes != null) {
                for (BiomeManager.BiomeEntry entry : biomes) {
                    addGeneration(entry.biome);
                }
            }
        }
    }

    private static void addGeneration(Biome biome) {
        addOreGeneration(biome, MekanismBlock.COPPER_ORE, MekanismConfig.general.copperPerChunk, MekanismConfig.general.copperMaxVeinSize, 0, 0, 60);
        addOreGeneration(biome, MekanismBlock.TIN_ORE, MekanismConfig.general.tinPerChunk, MekanismConfig.general.tinMaxVeinSize, 0, 0, 60);
        addOreGeneration(biome, MekanismBlock.OSMIUM_ORE, MekanismConfig.general.osmiumPerChunk, MekanismConfig.general.osmiumMaxVeinSize, 0, 0, 60);
        //TODO: Add proper values for this
        addSaltGeneration(biome, MekanismBlock.SALT_BLOCK, MekanismConfig.general.saltPerChunk, MekanismConfig.general.saltMaxVeinSize, 4, 1, 6);
    }

    private static void addOreGeneration(Biome biome, IBlockProvider blockProvider, IntValue maxVeinSize, IntValue veinsPerChunk, int minHeight, int topOffset, int maxHeight) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
              Feature.ORE.func_225566_b_(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, blockProvider.getBlock().getDefaultState(),
                    maxVeinSize.get())).func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(new CountRangeConfig(veinsPerChunk.get(), minHeight, topOffset, maxHeight))));
    }

    private static void addSaltGeneration(Biome biome, IBlockProvider blockProvider, IntValue maxVeinSize, IntValue veinsPerChunk, int radius, int ySize, int count) {
        BlockState state = blockProvider.getBlock().getDefaultState();
        //TODO: Does this need to include the above state in the targets or is dirt fine
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.DISK.func_225566_b_(new SphereReplaceConfig(state, radius, ySize,
              Lists.newArrayList(Blocks.DIRT.getDefaultState()))).func_227228_a_(Placement.COUNT_TOP_SOLID.func_227446_a_(new FrequencyConfig(count))));
    }
}