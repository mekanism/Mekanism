package mekanism.common;

import java.util.Random;

import net.minecraft.block.Block;
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
			for(int i = 0; i < Mekanism.osmiumGenerationAmount; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				new WorldGenMinable(Mekanism.oreBlockID, 0, 8, Block.stone.blockID).generate(world, random, randPosX, randPosY, randPosZ);
			}
			
			for(int i = 0; i < Mekanism.copperGenerationAmount; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				new WorldGenMinable(Mekanism.oreBlockID, 1, 8, Block.stone.blockID).generate(world, random, randPosX, randPosY, randPosZ);
			}
			
			for(int i = 0; i < Mekanism.tinGenerationAmount; i++)
			{
				int randPosX = (chunkX*16) + random.nextInt(16);
				int randPosY = random.nextInt(60);
				int randPosZ = (chunkZ*16) + random.nextInt(16);
				new WorldGenMinable(Mekanism.oreBlockID, 2, 8, Block.stone.blockID).generate(world, random, randPosX, randPosY, randPosZ);
			}
		}
	}
}
