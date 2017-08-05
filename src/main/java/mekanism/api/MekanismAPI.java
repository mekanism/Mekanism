package mekanism.api;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.util.BlockInfo;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.oredict.OreDictionary;

public class MekanismAPI
{
	//Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
	private static Set<BlockInfo> cardboardBoxIgnore = new HashSet<>();
	
	/** Mekanism debug mode */
	public static boolean debug = false;

	public static boolean isBlockCompatible(Block block, int meta)
	{
		for(BlockInfo i : cardboardBoxIgnore)
		{
			if(i.block == block && (i.meta == OreDictionary.WILDCARD_VALUE || i.meta == meta))
			{
				return false;
			}
		}

		return true;
	}

	public static void addBoxBlacklist(Block block, int meta)
	{
		cardboardBoxIgnore.add(new BlockInfo(block, meta));
	}

	public static void removeBoxBlacklist(Block block, int meta)
	{
		cardboardBoxIgnore.remove(new BlockInfo(block, meta));
	}

	public static Set<BlockInfo> getBoxIgnore()
	{
		return cardboardBoxIgnore;
	}

	public static class BoxBlacklistEvent extends Event {}
}
