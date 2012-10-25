package universalelectricity.prefab.ore;

import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.UEConfig;
import universalelectricity.core.UniversalElectricity;

/**
 * This class is used for storing ore generation data. If you are too lazy to
 * generate your own ores, you can do
 * {@link #OreGenerator.ORES_TO_GENERATE.add()} to add your ore to the list of
 * ores to generate.
 * 
 * @author Calclavia
 * 
 */
public abstract class OreGenBase
{
	public String name;

	public String oreDictionaryName;

	public boolean shouldGenerate;

	public int blockIndexTexture;

	public ItemStack oreStack;

	public int oreID;

	public int oreMeta;

	/**
	 * What harvest level does this machine need to be acquired?
	 */
	public int harvestLevel;

	/**
	 * The predefined tool classes are "pickaxe", "shovel", "axe". You can add
	 * others for custom tools.
	 */
	public String harvestTool;

	/**
	 * @param name
	 *            - The name of the ore for display
	 * @param textureFile
	 *            - The 16x16 png texture of your ore to override
	 * @param minGenerateLevel
	 *            - The highest generation level of your ore
	 * @param maxGenerateLevel
	 *            - The lowest generation level of your ore
	 * @param amountPerChunk
	 *            - The amount of ores to generate per chunk
	 * @param amountPerBranch
	 *            - The amount of ores to generate in a clutter. E.g coal
	 *            generates with a lot of other coal next to it. How much do you
	 *            want?
	 */
	public OreGenBase(String name, String oreDiectionaryName, ItemStack stack, String harvestTool, int harvestLevel)
	{
		this.name = name;
		this.shouldGenerate = false;
		this.harvestTool = harvestTool;
		this.harvestLevel = harvestLevel;
		this.oreDictionaryName = oreDiectionaryName;
		this.oreStack = stack;
		this.oreID = stack.itemID;
		this.oreMeta = stack.getItemDamage();

		OreDictionary.registerOre(oreDictionaryName, stack);
		MinecraftForge.setBlockHarvestLevel(Block.blocksList[stack.itemID], stack.getItemDamage(), harvestTool, harvestLevel);
	}

	public OreGenBase enable()
	{
		this.shouldGenerate = shouldGenerateOre(name);
		return this;
	}

	// You may inherit from this class and change this function if you want a
	// custom texture render for your ore.
	public int getBlockTextureFromSide(int side)
	{
		return this.blockIndexTexture;
	}

	// Checks the config file and see if Universal Electricity should generate
	// this ore
	private static boolean shouldGenerateOre(String oreName)
	{
		return UEConfig.getConfigData(UniversalElectricity.CONFIGURATION, "Generate " + oreName, true);
	}

	public abstract void generate(World world, Random random, int varX, int varZ);

	public abstract boolean isOreGeneratedInWorld(World world, IChunkProvider chunkGenerator);
}
