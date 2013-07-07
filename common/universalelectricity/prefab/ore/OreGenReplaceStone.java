package universalelectricity.prefab.ore;

import net.minecraft.item.ItemStack;

public class OreGenReplaceStone extends OreGenReplace
{
	public OreGenReplaceStone(String name, String oreDiectionaryName, ItemStack stack, int minGenerateLevel, int maxGenerateLevel, int amountPerChunk, int amountPerBranch, String harvestTool, int harvestLevel)
	{
		super(name, oreDiectionaryName, stack, 1, minGenerateLevel, maxGenerateLevel, amountPerChunk, amountPerBranch, harvestTool, harvestLevel);
	}

	// A simplified version of the constructor
	public OreGenReplaceStone(String name, String oreDiectionaryName, ItemStack stack, int maxGenerateLevel, int amountPerChunk, int amountPerBranch)
	{
		this(name, oreDiectionaryName, stack, 0, maxGenerateLevel, amountPerChunk, amountPerBranch, "pickaxe", 1);
	}
}
