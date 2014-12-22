package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.InventoryNetwork;
import mekanism.common.InventoryNetwork.AcceptorData;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.content.transporter.PathfinderCache.PathData;
import mekanism.common.content.transporter.TransporterPathfinder.Pathfinder.DestChecker;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.InventoryUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

public final class TransporterPathfinder
{
	public static class IdlePath
	{
		public World worldObj;

		public Coord4D start;

		public TransporterStack transportStack;

		public IdlePath(World world, Coord4D obj, TransporterStack stack)
		{
			worldObj = world;
			start = obj;
			transportStack = stack;
		}

		public Destination find()
		{
			ArrayList<Coord4D> ret = new ArrayList<Coord4D>();
			ret.add(start);
			
			if(transportStack.idleDir == null)
			{
				EnumFacing newSide = findSide();
				
				if(newSide == null)
				{
					return null;
				}
				
				transportStack.idleDir = newSide;
				loopSide(ret, newSide);
				return new Destination(ret, true, null).setPathType(Path.NONE);
			}
			else {
				TileEntity tile = start.offset(transportStack.idleDir).getTileEntity(worldObj);
				
				if(transportStack.canInsertToTransporter(tile, transportStack.idleDir))
				{
					loopSide(ret, transportStack.idleDir);
					return new Destination(ret, true, null).setPathType(Path.NONE);
				}
				else {
					Destination newPath = TransporterPathfinder.getNewBasePath((ILogisticalTransporter)start.getTileEntity(worldObj), transportStack, 0);
					
					if(newPath != null && TransporterManager.didEmit(transportStack.itemStack, newPath.rejected))
					{
						transportStack.idleDir = null;
						newPath.setPathType(Path.DEST);
						return newPath;
					}
					else {
						EnumFacing newSide = findSide();
						
						if(newSide == null)
						{
							return null;
						}
						
						transportStack.idleDir = newSide;
						loopSide(ret, newSide);
						return new Destination(ret, true, null).setPathType(Path.NONE);
					}
				}
			}
		}
		
		private void loopSide(List<Coord4D> list, EnumFacing side)
		{
			int count = 1;
			
			while(true)
			{
				Coord4D coord = start.offset(side, count);
				
				if(transportStack.canInsertToTransporter(coord.getTileEntity(worldObj), side))
				{
					list.add(coord);
					count++;
				}
				else {
					break;
				}
			}
		}
		
		private EnumFacing findSide()
		{
			if(transportStack.idleDir == null)
			{
				for(EnumFacing side : EnumFacing.values())
				{
					TileEntity tile = start.offset(side).getTileEntity(worldObj);
	
					if(transportStack.canInsertToTransporter(tile, side))
					{
						return side;
					}
				}
			}
			else {
				for(EnumFacing side : EnumSet.complementOf(EnumSet.of(null, transportStack.idleDir.getOpposite())))
				{
					TileEntity tile = start.offset(side).getTileEntity(worldObj);
	
					if(transportStack.canInsertToTransporter(tile, side))
					{
						return side;
					}
				}
				
				TileEntity tile = start.offset(transportStack.idleDir.getOpposite()).getTileEntity(worldObj);
				
				if(transportStack.canInsertToTransporter(tile, transportStack.idleDir.getOpposite()))
				{
					return transportStack.idleDir.getOpposite();
				}
			}
			
			return null;
		}
	}

	public static class Destination implements Comparable<Destination>
	{
		public List<Coord4D> path = new ArrayList<Coord4D>();
		public Path pathType;
		public ItemStack rejected;

		public Destination(ArrayList<Coord4D> list, boolean inv, ItemStack rejects)
		{
			path = (List<Coord4D>)list.clone();

			if(inv)
			{
				Collections.reverse(path);
			}

			rejected = rejects;
		}
		
		public Destination setPathType(Path type)
		{
			pathType = type;
			return this;
		}

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + path.hashCode();
			return code;
		}

		@Override
		public boolean equals(Object dest)
		{
			return dest instanceof Destination && ((Destination)dest).path.equals(path);
		}

		@Override
		public int compareTo(Destination dest)
		{
			if(path.size() < dest.path.size())
			{
				return -1;
			}
			else if(path.size() > dest.path.size())
			{
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	public static List<Destination> getPaths(ILogisticalTransporter start, TransporterStack stack, int min)
	{		
		InventoryNetwork network = start.getTransmitterNetwork();
		List<AcceptorData> acceptors = network.calculateAcceptors(stack.itemStack, stack.color);
		List<Destination> paths = new ArrayList<Destination>();

		for(AcceptorData entry : acceptors)
		{
			DestChecker checker = new DestChecker()
			{
				@Override
				public boolean isValid(TransporterStack stack, EnumFacing dir, TileEntity tile)
				{
					return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, dir, false);
				}
			};
			
			Destination d = getPath(checker, entry.sides, start, entry.location, stack, entry.rejected, min);
			
			if(d != null)
			{
				paths.add(d);
			}
		}

		Collections.sort(paths);

		return paths;
	}
	
	public static Destination getPath(DestChecker checker, EnumSet<EnumFacing> sides, ILogisticalTransporter start, Coord4D dest, TransporterStack stack, ItemStack rejects, int min)
	{
		ArrayList<Coord4D> test = PathfinderCache.getCache(Coord4D.get(start.getTile()), dest, sides);
		
		if(test != null)
		{
			return new Destination(test, false, rejects);
		}
		
		Pathfinder p = new Pathfinder(checker, start.getTile().getWorld(), dest, Coord4D.get(start.getTile()), stack);
		
		if(p.getPath().size() >= 2)
		{
			if(TransporterManager.getToUse(stack.itemStack, rejects).stackSize >= min)
			{
				PathfinderCache.cachedPaths.put(new PathData(Coord4D.get(start.getTile()), dest, p.side), p.getPath());
				
				return new Destination(p.getPath(), false, rejects);
			}
		}
		
		return null;
	}
	
	public static Destination getNewBasePath(ILogisticalTransporter start, TransporterStack stack, int min)
	{
		List<Destination> paths = getPaths(start, stack, min);

		if(paths.isEmpty())
		{
			return null;
		}

		return paths.get(0);
	}

	public static Destination getNewRRPath(ILogisticalTransporter start, TransporterStack stack, TileEntityLogisticalSorter outputter, int min)
	{
		List<Destination> paths = getPaths(start, stack, min);

		Map<Coord4D, Destination> destPaths = new HashMap<Coord4D, Destination>();

		for(Destination d : paths)
		{
			if(destPaths.get(d.path.get(0)) == null || destPaths.get(d.path.get(0)).path.size() < d.path.size())
			{
				destPaths.put(d.path.get(0), d);
			}
		}

		List<Destination> dests = new ArrayList<Destination>();
		dests.addAll(destPaths.values());

		Collections.sort(dests);

		Destination closest = null;

		if(!dests.isEmpty())
		{
			if(outputter.rrIndex <= dests.size()-1)
			{
				closest = dests.get(outputter.rrIndex);

				if(outputter.rrIndex == dests.size()-1)
				{
					outputter.rrIndex = 0;
				}
				else if(outputter.rrIndex < dests.size()-1)
				{
					outputter.rrIndex++;
				}
			}
			else {
				closest = dests.get(dests.size()-1);
				outputter.rrIndex = 0;
			}
		}

		if(closest == null)
		{
			return null;
		}

		return closest;
	}

	public static class Pathfinder
	{
		public final Set<Coord4D> openSet, closedSet;

		public final HashMap<Coord4D, Coord4D> navMap;

		public final HashMap<Coord4D, Double> gScore, fScore;

		public final Coord4D start;

		public final Coord4D finalNode;

		public final TransporterStack transportStack;

		public final DestChecker destChecker;

		public double finalScore;
		
		public EnumFacing side;

		public ArrayList<Coord4D> results;

		private World worldObj;

		public Pathfinder(DestChecker checker, World world, Coord4D finishObj, Coord4D startObj, TransporterStack stack)
		{
			destChecker = checker;
			worldObj = world;

			finalNode = finishObj;
			start = startObj;

			transportStack = stack;

			openSet = new HashSet<Coord4D>();
			closedSet = new HashSet<Coord4D>();

			navMap = new HashMap<Coord4D, Coord4D>();

			gScore = new HashMap<Coord4D, Double>();
			fScore = new HashMap<Coord4D, Double>();

			results = new ArrayList<Coord4D>();

			find(start);
		}

		public boolean find(Coord4D start)
		{
			openSet.add(start);
			gScore.put(start, 0D);
			fScore.put(start, gScore.get(start) + getEstimate(start, finalNode));

			int blockCount = 0;

			for(int i = 0; i < 6; i++)
			{
				EnumFacing direction = EnumFacing.getFront(i);
				Coord4D neighbor = start.offset(direction);

				if(!transportStack.canInsertToTransporter(neighbor.getTileEntity(worldObj), direction) && (!neighbor.equals(finalNode) || !destChecker.isValid(transportStack, direction, neighbor.getTileEntity(worldObj))))
				{
					blockCount++;
				}
			}

			if(blockCount >= 6)
			{
				return false;
			}

			double maxSearchDistance = start.distanceTo(finalNode) * 2;

			while(!openSet.isEmpty())
			{
				Coord4D currentNode = null;
				double lowestFScore = 0;

				for(Coord4D node : openSet)
				{
					if(currentNode == null || fScore.get(node) < lowestFScore)
					{
						currentNode = node;
						lowestFScore = fScore.get(node);
					}
				}

				if(currentNode == null && start.distanceTo(currentNode) > maxSearchDistance)
				{
					break;
				}

				openSet.remove(currentNode);
				closedSet.add(currentNode);

				for(int i = 0; i < 6; i++)
				{
					EnumFacing direction = EnumFacing.getFront(i);
					Coord4D neighbor = currentNode.offset(direction);

					if(transportStack.canInsertToTransporter(neighbor.getTileEntity(worldObj), direction))
					{
						TileEntity tile = neighbor.getTileEntity(worldObj);
						double tentativeG = gScore.get(currentNode) + currentNode.distanceTo(neighbor);

						tentativeG += ((ILogisticalTransporter)tile).getCost();

						if(closedSet.contains(neighbor))
						{
							if(tentativeG >= gScore.get(neighbor))
							{
								continue;
							}
						}

						TileEntity currTile = currentNode.getTileEntity(worldObj);

						if(!openSet.contains(neighbor) || tentativeG < gScore.get(neighbor))
						{
							navMap.put(neighbor, currentNode);
							gScore.put(neighbor, tentativeG);
							fScore.put(neighbor, gScore.get(neighbor) + getEstimate(neighbor, finalNode));
							openSet.add(neighbor);
						}
					}
					else if(neighbor.equals(finalNode) && destChecker.isValid(transportStack, direction, neighbor.getTileEntity(worldObj)))
					{
						side = direction;
						results = reconstructPath(navMap, currentNode);
						return true;
					}
				}
			}

			return false;
		}

		private ArrayList<Coord4D> reconstructPath(HashMap<Coord4D, Coord4D> naviMap, Coord4D currentNode)
		{
			ArrayList<Coord4D> path = new ArrayList<Coord4D>();

			path.add(currentNode);

			if(naviMap.containsKey(currentNode))
			{
				path.addAll(reconstructPath(naviMap, naviMap.get(currentNode)));
			}

			finalScore = gScore.get(currentNode) + currentNode.distanceTo(finalNode);

			return path;
		}

		public ArrayList<Coord4D> getPath()
		{
			ArrayList<Coord4D> path = new ArrayList<Coord4D>();
			path.add(finalNode);
			path.addAll((ArrayList<Coord4D>)results.clone());

			return path;
		}

		private double getEstimate(Coord4D start, Coord4D target2)
		{
			return start.distanceTo(target2);
		}

		public static class DestChecker
		{
			public boolean isValid(TransporterStack stack, EnumFacing side, TileEntity tile)
			{
				return false;
			}
		}
	}

	public static List<Coord4D> getIdlePath(ILogisticalTransporter start, TransporterStack stack)
	{
		if(stack.homeLocation != null)
		{
			DestChecker checker = new DestChecker()
			{
				@Override
				public boolean isValid(TransporterStack stack, EnumFacing side, TileEntity tile)
				{
					return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
				}
			};

			Pathfinder p = new Pathfinder(checker, start.getTile().getWorld(), stack.homeLocation, Coord4D.get(start.getTile()), stack);
			List<Coord4D> path = p.getPath();

			if(path.size() >= 2)
			{
				stack.pathType = Path.HOME;
				return path;
			}
			else {
				stack.homeLocation = null;
			}
		}

		IdlePath d = new IdlePath(start.getTile().getWorld(), Coord4D.get(start.getTile()), stack);
		Destination dest = d.find();

		if(dest == null)
		{
			return null;
		}
		
		stack.pathType = dest.pathType;

		return dest.path;
	}
}
