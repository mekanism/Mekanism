package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.Coord4D;

import net.minecraft.util.EnumFacing;

public class PathfinderCache 
{
	public static Map<PathData, List<Coord4D>> cachedPaths = new HashMap<PathData, List<Coord4D>>();
	
	public static void onChanged(Coord4D location)
	{
		reset();
	}
	
	public static ArrayList<Coord4D> getCache(Coord4D start, Coord4D end, EnumSet<EnumFacing> sides)
	{
		ArrayList<Coord4D> ret = null;
		
		for(Map.Entry<PathData, List<Coord4D>> entry : cachedPaths.entrySet())
		{
			PathData data = entry.getKey();
			
			if(data.startTransporter.equals(start) && data.end.equals(end) && sides.contains(data.endSide))
			{
				if(ret == null || entry.getValue().size() < ret.size())
				{
					ret = (ArrayList)entry.getValue();
				}
			}
		}
		
		return ret;
	}
	
	public static void reset()
	{
		cachedPaths.clear();
	}
	
	public static class PathData
	{
		public Coord4D startTransporter;
		
		public Coord4D end;
		public EnumFacing endSide;
		
		public PathData(Coord4D s, Coord4D e, EnumFacing es)
		{
			startTransporter = s;
			
			end = e;
			endSide = es;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof PathData &&
					((PathData)obj).startTransporter.equals(startTransporter) &&
					((PathData)obj).end.equals(end) &&
					((PathData)obj).endSide.equals(endSide);
		}

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + startTransporter.hashCode();
			code = 31 * code + end.hashCode();
			code = 31 * code + endSide.hashCode();
			return code;
		}
	}
}