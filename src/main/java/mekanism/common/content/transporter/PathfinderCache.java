package mekanism.common.content.transporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import net.minecraftforge.common.util.ForgeDirection;

public class PathfinderCache 
{
	public static Map<PathData, List<Coord4D>> cachedPaths = new HashMap<PathData, List<Coord4D>>();
	
	public static void onChanged(Coord4D location)
	{
		Set<PathData> toKill = new HashSet<PathData>();
		
		for(Map.Entry<PathData, List<Coord4D>> entry : cachedPaths.entrySet())
		{
			if(entry.getValue().contains(entry))
			{
				toKill.add(entry.getKey());
			}
		}
		
		for(PathData path : toKill)
		{
			cachedPaths.remove(path);
		}
	}
	
	public static void reset()
	{
		cachedPaths.clear();
	}
	
	public static class PathData
	{
		public Coord4D startTransporter;
		
		public Coord4D end;
		public ForgeDirection endSide;
		
		public PathData(Coord4D s, Coord4D e, ForgeDirection es)
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