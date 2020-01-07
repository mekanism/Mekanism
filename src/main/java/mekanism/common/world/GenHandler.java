package mekanism.common.world;

import com.google.common.collect.Lists;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig.OreConfig;
import mekanism.common.config.WorldConfig.SaltConfig;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

public class GenHandler {

    public static void setupWorldGeneration() {
        ForgeRegistries.BIOMES.forEach(biome -> {
            if (biome.getCategory() != Category.THEEND && biome.getCategory() != Category.NETHER) {
                //TODO: Decide if we want to stop the generation when the category is Category.NONE as well
                // we probably do not so that in case mods do not categorize their biome, we can still add our ore
                addGeneration(biome);
            }
        });
    }

    private static void addGeneration(Biome biome) {
        //Add ores
        addOreGeneration(biome, MekanismBlocks.COPPER_ORE, MekanismConfig.world.copper);
        addOreGeneration(biome, MekanismBlocks.TIN_ORE, MekanismConfig.world.tin);
        addOreGeneration(biome, MekanismBlocks.OSMIUM_ORE, MekanismConfig.world.osmium);
        //Add salt
        addSaltGeneration(biome, MekanismBlocks.SALT_BLOCK, MekanismConfig.world.salt);
    }

    private static void addOreGeneration(Biome biome, IBlockProvider blockProvider, OreConfig oreConfig) {
        if (oreConfig.shouldGenerate.get()) {
            biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE.func_225566_b_(new OreFeatureConfig(FillerBlockType.NATURAL_STONE,
                  blockProvider.getBlock().getDefaultState(), oreConfig.maxVeinSize.get())).func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(
                  new CountRangeConfig(oreConfig.perChunk.get(), oreConfig.bottomOffset.get(), oreConfig.topOffset.get(), oreConfig.maxHeight.get()))));
        }
    }

    private static void addSaltGeneration(Biome biome, IBlockProvider blockProvider, SaltConfig saltConfig) {
        if (saltConfig.shouldGenerate.get()) {
            BlockState state = blockProvider.getBlock().getDefaultState();
            //TODO: Includes clay and salt as alternate targets like it used to, though maybe this should be changed
            biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.DISK.func_225566_b_(new SphereReplaceConfig(state, saltConfig.maxVeinSize.get(),
                  saltConfig.ySize.get(), Lists.newArrayList(Blocks.DIRT.getDefaultState(), Blocks.CLAY.getDefaultState(), state)))
                  .func_227228_a_(Placement.COUNT_TOP_SOLID.func_227446_a_(new FrequencyConfig(saltConfig.perChunk.get()))));
        }
    }
}