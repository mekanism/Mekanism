package universalelectricity.prefab.ore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.registry.GameRegistry;

public class OreGenerator implements IWorldGenerator
{
	public static boolean isInitiated = false;

	/**
	 * Add your ore data to this list of ores for it to automatically generate! No hassle indeed!
	 */
	private static final List<OreGenBase> ORES_TO_GENERATE = new ArrayList<OreGenBase>();

	/**
	 * Adds an ore to the ore generate list. Do this in pre-init.
	 */
	public static void addOre(OreGenBase data)
	{
		if (!isInitiated)
		{
			GameRegistry.registerWorldGenerator(new OreGenerator());
		}

		ORES_TO_GENERATE.add(data);
	}

	/**
	 * Checks to see if this ore
	 * 
	 * @param oreName
	 * @return
	 */
	public static boolean oreExists(String oreName)
	{
		for (OreGenBase ore : ORES_TO_GENERATE)
		{
			if (ore.oreDictionaryName == oreName)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes an ore to the ore generate list. Do this in init.
	 */
	public static void removeOre(OreGenBase data)
	{
		ORES_TO_GENERATE.remove(data);
	}

	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		chunkX = chunkX << 4;
		chunkZ = chunkZ << 4;

		// Checks to make sure this is the normal
		// world
		for (OreGenBase oreData : ORES_TO_GENERATE)
		{
			if (oreData.shouldGenerate && oreData.isOreGeneratedInWorld(world, chunkGenerator))
			{
				oreData.generate(world, rand, chunkX, chunkZ);
			}

		}
	}
}
