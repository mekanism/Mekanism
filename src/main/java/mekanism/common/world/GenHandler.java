package mekanism.common.world;

import java.util.Random;

import mekanism.common.MekanismBlocks;
import mekanism.common.config.MekanismConfig;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GenHandler implements IWorldGenerator
{
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
	{
		if(!(chunkGenerator instanceof ChunkGeneratorHell) && !(chunkGenerator instanceof ChunkGeneratorEnd))
		{
			for(int i = 0; i < MekanismConfig.current().general.osmiumPerChunk.val(); i++)
			{
				BlockPos pos = new BlockPos(chunkX*16 + random.nextInt(16), random.nextInt(60), (chunkZ*16) + random.nextInt(16));
				new WorldGenMinable(MekanismBlocks.OreBlock.getStateFromMeta(0), MekanismConfig.current().general.osmiumMaxVeinSize.val(), BlockMatcher.forBlock(Blocks.STONE)).generate(world, random, pos);
			}

			for(int i = 0; i < MekanismConfig.current().general.copperPerChunk.val(); i++)
			{
				BlockPos pos = new BlockPos(chunkX*16 + random.nextInt(16), random.nextInt(60), (chunkZ*16) + random.nextInt(16));
				new WorldGenMinable(MekanismBlocks.OreBlock.getStateFromMeta(1), MekanismConfig.current().general.copperMaxVeinSize.val(), BlockMatcher.forBlock(Blocks.STONE)).generate(world, random, pos);
			}

			for(int i = 0; i < MekanismConfig.current().general.tinPerChunk.val(); i++)
			{
				BlockPos pos = new BlockPos(chunkX*16 + random.nextInt(16), random.nextInt(60), (chunkZ*16) + random.nextInt(16));
				new WorldGenMinable(MekanismBlocks.OreBlock.getStateFromMeta(2), MekanismConfig.current().general.tinMaxVeinSize.val(), BlockMatcher.forBlock(Blocks.STONE)).generate(world, random, pos);
			}
			
			for(int i = 0; i < MekanismConfig.current().general.saltPerChunk.val(); i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16) + 8;
				int randPosZ = (chunkZ*16) + random.nextInt(16) + 8;
				BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 60, randPosZ));
				new WorldGenSalt(MekanismConfig.current().general.saltMaxVeinSize.val()).generate(world, random, pos);
			}
		}
	}
}
