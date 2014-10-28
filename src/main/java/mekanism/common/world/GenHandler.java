package mekanism.common.world;

import java.util.Random;

import mekanism.common.Mekanism;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderHell;
import cpw.mods.fml.common.IWorldGenerator;

public class GenHandler implements IWorldGenerator
{
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if(!(chunkGenerator instanceof ChunkProviderHell) && !(chunkGenerator instanceof ChunkProviderEnd))
		{
			for(int i=0; i < Mekanism.saltPerChunk; i++)
			{
				int randPosX=(chunkX*16) + random.nextInt(16);
				int randPosZ=(chunkZ*16) + random.nextInt(16);
				int randPosY=world.getTopSolidOrLiquidBlock(randPosX, randPosZ);
				new WorldGenSalt(6).generate(world, random, randPosX, randPosY, randPosZ);
			}
		}
	}
}
