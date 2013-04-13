package universalelectricity.prefab.ore;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.FMLLog;

/**
 * This class is used for storing ore generation data. If you are too lazy to generate your own
 * ores, you can do {@link #OreGenerator.ORES_TO_GENERATE.add()} to add your ore to the list of ores
 * to generate.
 * 
 * @author Calclavia
 * 
 */
public abstract class OreGenBase
{
	public String name;

	public String oreDictionaryName;

	public boolean shouldGenerate = false;

	public int blockIndexTexture;

	public ItemStack oreStack;

	public int oreID;

	public int oreMeta;

	/**
	 * What harvest level does this machine need to be acquired?
	 */
	public int harvestLevel;

	/**
	 * The predefined tool classes are "pickaxe", "shovel", "axe". You can add others for custom
	 * tools.
	 */
	public String harvestTool;

	/**
	 * @param name - The name of the ore for display
	 * @param textureFile - The 16x16 png texture of your ore to override
	 * @param minGenerateLevel - The highest generation level of your ore
	 * @param maxGenerateLevel - The lowest generation level of your ore
	 * @param amountPerChunk - The amount of ores to generate per chunk
	 * @param amountPerBranch - The amount of ores to generate in a clutter. E.g coal generates with
	 * a lot of other coal next to it. How much do you want?
	 */
	public OreGenBase(String name, String oreDiectionaryName, ItemStack stack, String harvestTool, int harvestLevel)
	{
		if (stack != null)
		{
			this.name = name;
			this.harvestTool = harvestTool;
			this.harvestLevel = harvestLevel;
			this.oreDictionaryName = oreDiectionaryName;
			this.oreStack = stack;
			this.oreID = stack.itemID;
			this.oreMeta = stack.getItemDamage();

			OreDictionary.registerOre(oreDictionaryName, stack);
			MinecraftForge.setBlockHarvestLevel(Block.blocksList[stack.itemID], stack.getItemDamage(), harvestTool, harvestLevel);
		}
		else
		{
			FMLLog.severe("ItemStack is null while registering ore generation!");
		}
	}

	public OreGenBase enable(Configuration config)
	{
		this.shouldGenerate = shouldGenerateOre(config, this.name);
		return this;
	}

	/**
	 * Checks the config file and see if Universal Electricity should generate this ore
	 */
	private static boolean shouldGenerateOre(Configuration configuration, String oreName)
	{
		configuration.load();
		boolean shouldGenerate = configuration.get("Ore Generation", "Generate " + oreName, true).getBoolean(true);
		configuration.save();
		return shouldGenerate;
	}

	public abstract void generate(World world, Random random, int varX, int varZ);

	public abstract boolean isOreGeneratedInWorld(World world, IChunkProvider chunkGenerator);
}
