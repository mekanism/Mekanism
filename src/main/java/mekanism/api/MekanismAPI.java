package mekanism.api;

import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.oredict.OreDictionary;

public class MekanismAPI
{
	//Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
	private static Set<ItemInfo> cardboardBoxIgnore = new HashSet<ItemInfo>();

	public static boolean isBlockCompatible(int id, int meta)
	{
		for(ItemInfo i : cardboardBoxIgnore)
		{
			if(i.id == id && (i.meta == OreDictionary.WILDCARD_VALUE || i.meta == meta))
			{
				return false;
			}
		}

		return true;
	}

	public static void addBoxBlacklist(int id, int meta)
	{
		cardboardBoxIgnore.add(new ItemInfo(id, meta));
	}

	public static void removeBoxBlacklist(int id, int meta)
	{
		cardboardBoxIgnore.remove(new ItemInfo(id, meta));
	}

	public static Set<ItemInfo> getBoxIgnore()
	{
		return cardboardBoxIgnore;
	}

	public static class BoxBlacklistEvent extends Event {}
}
