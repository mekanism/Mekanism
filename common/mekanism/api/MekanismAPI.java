package mekanism.api;

import java.util.HashSet;
import java.util.Set;

public class MekanismAPI
{
	//Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
	public static Set<BlockInfo> cardboardBoxIgnore = new HashSet<BlockInfo>();
	
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
}
