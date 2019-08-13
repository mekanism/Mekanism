package mekanism.common.world;

import java.util.Random;
import mekanism.common.MekanismBlock;
import mekanism.common.config.MekanismConfig;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.EndChunkGenerator;
import net.minecraft.world.gen.NetherChunkGenerator;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GenHandler implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, ChunkGenerator chunkGenerator, AbstractChunkProvider chunkProvider) {
        if (!(chunkGenerator instanceof NetherChunkGenerator) && !(chunkGenerator instanceof EndChunkGenerator)) {
            for (int i = 0; i < MekanismConfig.general.osmiumPerChunk.get(); i++) {
                BlockPos pos = new BlockPos(chunkX * 16 + random.nextInt(16), random.nextInt(60), (chunkZ * 16) + random.nextInt(16));
                new OreFeature(MekanismBlock.OSMIUM_ORE.getBlock().getDefaultState(), MekanismConfig.general.osmiumMaxVeinSize.get(),
                      BlockMatcher.forBlock(Blocks.STONE)).generate(world, random, pos);
            }

            for (int i = 0; i < MekanismConfig.general.copperPerChunk.get(); i++) {
                BlockPos pos = new BlockPos(chunkX * 16 + random.nextInt(16), random.nextInt(60), (chunkZ * 16) + random.nextInt(16));
                new OreFeature(MekanismBlock.COPPER_ORE.getBlock().getDefaultState(), MekanismConfig.general.copperMaxVeinSize.get(),
                      BlockMatcher.forBlock(Blocks.STONE)).generate(world, random, pos);
            }

            for (int i = 0; i < MekanismConfig.general.tinPerChunk.get(); i++) {
                BlockPos pos = new BlockPos(chunkX * 16 + random.nextInt(16), random.nextInt(60), (chunkZ * 16) + random.nextInt(16));
                new OreFeature(MekanismBlock.TIN_ORE.getBlock().getDefaultState(), MekanismConfig.general.tinMaxVeinSize.get(),
                      BlockMatcher.forBlock(Blocks.STONE)).generate(world, random, pos);
            }

            for (int i = 0; i < MekanismConfig.general.saltPerChunk.get(); i++) {
                int randPosX = (chunkX * 16) + random.nextInt(16) + 8;
                int randPosZ = (chunkZ * 16) + random.nextInt(16) + 8;
                BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 60, randPosZ));
                new WorldGenSalt(MekanismConfig.general.saltMaxVeinSize.get()).generate(world, random, pos);
            }
        }
    }
}