package net.uberkat.obsidian.common;

import java.util.Random;

import net.minecraft.src.IChunkProvider;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class OreHandler implements IWorldGenerator
{
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
	{
		if(ObsidianIngots.oreGenerationEnabled == true)
		{
			for (int i=0;i<1;i++)
			{
				int randPosX = chunkX + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = chunkZ + random.nextInt(16);
				(new WorldGenMinable(ObsidianIngots.PlatinumOre.blockID, 8)).generate(world, random, randPosX, randPosY, randPosZ);
			}
		}
	}

}
