package mekanism.common;

import java.util.Random;

import net.minecraft.src.*;
import cpw.mods.fml.common.IWorldGenerator;

public class OreHandler implements IWorldGenerator
{	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
	{
		switch(world.provider.dimensionId)
		{
			case 0:
				generateSurface(world, random, chunkX*16, chunkZ*16);
		}
	}
	
	/**
	 * Generate an ore in the overworld.
	 * @param world
	 * @param random
	 * @param chunkX
	 * @param chunkZ
	 */
	public void generateSurface(World world, Random random, int chunkX, int chunkZ)
	{
		if(Mekanism.oreGenerationEnabled == true)
		{
			for(int i=0;i<6;i++)
			{
				int randPosX = chunkX + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = chunkZ + random.nextInt(16);
				(new WorldGenMinable(new ItemStack(Mekanism.OreBlock, 1, 0).itemID, 8)).generate(world, random, randPosX, randPosY, randPosZ);
			}
			for(int i=0;i<2;i++)
			{
				int randPosX = chunkX + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = chunkZ + random.nextInt(16);
				(new WorldGenMinable(new ItemStack(Mekanism.OreBlock, 1, 1).itemID, 6)).generate(world, random, randPosX, randPosY, randPosZ);
			}
		}
	}
}
