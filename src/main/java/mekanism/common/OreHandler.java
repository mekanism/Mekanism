package mekanism.common;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.feature.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class OreHandler implements IWorldGenerator
{
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if(!(chunkGenerator instanceof ChunkProviderHell) && !(chunkGenerator instanceof ChunkProviderEnd))
		{
			if(Mekanism.osmiumGenerationEnabled)
			{
				for(int i = 0; i < Mekanism.osmiumGenerationAmount; i++)
				{
					int randPosX = (chunkX*16) + random.nextInt(16);
					int randPosY = random.nextInt(60);
					int randPosZ = (chunkZ*16) + random.nextInt(16);
					new WorldGenMinable(Mekanism.OreBlock, 0, 8, Blocks.stone).generate(world, random, randPosX, randPosY, randPosZ);
				}
			}

			if(Mekanism.copperGenerationEnabled)
			{
				for(int i = 0; i < Mekanism.copperGenerationAmount; i++)
				{
					int randPosX = (chunkX*16) + random.nextInt(16);
					int randPosY = random.nextInt(60);
					int randPosZ = (chunkZ*16) + random.nextInt(16);
					new WorldGenMinable(Mekanism.OreBlock, 1, 8, Blocks.stone).generate(world, random, randPosX, randPosY, randPosZ);
				}
			}

			if(Mekanism.tinGenerationEnabled)
			{
				for(int i = 0; i < Mekanism.tinGenerationAmount; i++)
				{
					int randPosX = (chunkX*16) + random.nextInt(16);
					int randPosY = random.nextInt(60);
					int randPosZ = (chunkZ*16) + random.nextInt(16);
					new WorldGenMinable(Mekanism.OreBlock, 2, 8, Blocks.stone).generate(world, random, randPosX, randPosY, randPosZ);
				}
			}
		}
	}
}
