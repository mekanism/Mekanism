package mekanism.api;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.event.Event;
import net.minecraftforge.oredict.OreDictionary;

public class MekanismAPI
{
	//Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
	private static Set<BlockInfo> cardboardBoxIgnore = new HashSet<BlockInfo>();
	
	public static boolean isBlockCompatible(int id, int meta)
	{
		for(BlockInfo i : cardboardBoxIgnore)
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
		cardboardBoxIgnore.add(new BlockInfo(id, meta));
	}
	
	public static void removeBoxBlacklist(int id, int meta)
	{
		cardboardBoxIgnore.remove(new BlockInfo(id, meta));
	}
	
	public static Set<BlockInfo> getBoxIgnore()
	{
		return cardboardBoxIgnore;
	}
	
	public static class BlockInfo
	{	
		public int id;
		public int meta;
		
		public BlockInfo(int i, int j)
		{
			id = i;
			meta = j;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof BlockInfo && 
					((BlockInfo)obj).id == id && 
					((BlockInfo)obj).meta == meta;
		}
		
		@Override
		public int hashCode() 
		{
			int code = 1;
			code = 31 * code + id;
			code = 31 * code + meta;
			return code;
		}
	}
	
	public static class BoxBlacklistEvent extends Event {}
}
