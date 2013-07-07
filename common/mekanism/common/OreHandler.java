package mekanism.common;

import java.util.Random;

import net.minecraft.item.ItemStack;
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
			for(int i = 0; i < 8; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				new WorldGenMinable(new ItemStack(Mekanism.OreBlock, 1, 0).itemID, 8).generate(world, random, randPosX, randPosY, randPosZ);
			}
		}
	}
}
