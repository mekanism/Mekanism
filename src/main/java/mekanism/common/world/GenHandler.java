package mekanism.common.world;

import java.util.Random;

import mekanism.api.MekanismConfig.general;
import mekanism.common.MekanismBlocks;

import net.minecraft.block.state.pattern.BlockHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GenHandler implements IWorldGenerator
{
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if(!(chunkGenerator instanceof ChunkProviderHell) && !(chunkGenerator instanceof ChunkProviderEnd))
		{
			for(int i = 0; i < general.osmiumPerChunk; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				new WorldGenMinable(MekanismBlocks.OreBlock.getStateFromMeta(0), 8, BlockHelper.forBlock(Blocks.stone)).generate(world, random, new BlockPos(randPosX, randPosY, randPosZ));
			}

			for(int i = 0; i < general.copperPerChunk; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				new WorldGenMinable(MekanismBlocks.OreBlock.getStateFromMeta(1), 8, BlockHelper.forBlock(Blocks.stone)).generate(world, random, new BlockPos(randPosX, randPosY, randPosZ));
			}

			for(int i = 0; i < general.tinPerChunk; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				new WorldGenMinable(MekanismBlocks.OreBlock.getStateFromMeta(2), 8, BlockHelper.forBlock(Blocks.stone)).generate(world, random, new BlockPos(randPosX, randPosY, randPosZ));
			}
			
			for(int i = 0; i < general.saltPerChunk; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				BlockPos randPos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
				new WorldGenSalt(6).generate(world, random, randPos);
			}
		}
	}
}
