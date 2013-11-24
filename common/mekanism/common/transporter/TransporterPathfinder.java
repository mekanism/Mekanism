package mekanism.common.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityDiversionTransporter;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.transporter.TransporterPathfinder.Pathfinder.DestChecker;
import mekanism.common.transporter.TransporterStack.Path;
import mekanism.common.util.InventoryUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public final class TransporterPathfinder
{
	public static class IdlePath
	{
		public World worldObj;
		
		public Object3D start;
		
		public Set<Destination> destinations = new HashSet<Destination>();
		
		public TransporterStack transportStack;
		
		public IdlePath(World world, Object3D obj, TransporterStack stack)
		{
			worldObj = world;
			start = obj;
			transportStack = stack;
		}
		
		public void loop(Object3D pointer, ArrayList<Object3D> currentPath, int dist)
		{
			if(pointer == null)
			{
				return;
			}
			
			currentPath.add(pointer);
			
			if(pointer.getMetadata(worldObj) == 4)
			{
				dist += 1000;
			}
			else {
				dist++;
			}
			
			boolean found = false;
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = pointer.getFromSide(side).getTileEntity(worldObj);
				
				if(transportStack.canInsertToTransporter(tile) && !currentPath.contains(Object3D.get(tile)))
				{
					loop(Object3D.get(tile), (ArrayList<Object3D>)currentPath.clone(), dist);
					found = true;
				}
			}
			
			if(!found)
			{
				destinations.add(new Destination(currentPath, dist, true));
			}
		}
		
		public List<Object3D> find()
		{
			loop(start, new ArrayList<Object3D>(), 0);
			
		    Destination farthest = null;
			
			for(Destination obj : destinations)
			{
				if(farthest == null || obj.score > farthest.score)
				{
					if(!obj.path.isEmpty() && !obj.path.get(0).equals(start))
					{
						farthest = obj;
					}
				}
			}
			
			if(farthest == null)
			{
				return null;
			}
			
			return farthest.path;
		}
	}
	
	public static class Destination implements Comparable<Destination>
	{
		public List<Object3D> path = new ArrayList<Object3D>();
		public double score;
		
		public Destination(ArrayList<Object3D> list, double d, boolean inv)
		{
			path = (List<Object3D>)list.clone();
			
			if(inv)
			{
				Collections.reverse(path);
			}
			
			score = d;
		}
		
		@Override
		public int hashCode() 
		{
			int code = 1;
			code = 31 * code + path.hashCode();
			code = 31 * code + new Double(score).hashCode();
			return code;
		}
		
		@Override
		public boolean equals(Object dest)
		{
			return dest instanceof Destination && ((Destination)dest).path.equals(path) && ((Destination)dest).score == score;
		}

		@Override
		public int compareTo(Destination dest)
		{
			if(score < dest.score)
			{
				return -1;
			}
			else if(score > dest.score)
			{
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	
	public static class InventoryFinder
	{
		public World worldObj;
		
		public Set<Object3D> iterated = new HashSet<Object3D>();
		
		public Set<Object3D> destsFound = new HashSet<Object3D>();
		
		public Object3D start;
		
		public TransporterStack transportStack;
		
		public InventoryFinder(World world, Object3D obj, TransporterStack stack)
		{
			worldObj = world;
			start = obj;
			transportStack = stack;
		}
		
		public void loop(Object3D pointer)
		{
			if(pointer == null)
			{
				return;
			}
			
			iterated.add(pointer);
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = pointer.getFromSide(side).getTileEntity(worldObj);
				
				if(tile != null)
				{
					TileEntity currTile = pointer.getTileEntity(worldObj);
					
					if(currTile instanceof TileEntityDiversionTransporter)
					{
						int mode = ((TileEntityDiversionTransporter)currTile).modes[side.ordinal()];
						boolean redstone = currTile.worldObj.isBlockIndirectlyGettingPowered(currTile.xCoord, currTile.yCoord, currTile.zCoord);
						
						if((mode == 2 && redstone == true) || (mode == 1 && redstone == false))
						{
							continue;
						}
					}
					
					if(tile instanceof TileEntityDiversionTransporter)
					{
						int mode = ((TileEntityDiversionTransporter)tile).modes[side.ordinal()];
						boolean redstone = tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);
						
						if((mode == 2 && redstone == true) || (mode == 1 && redstone == false))
						{
							continue;
						}
					}
					
					if(Object3D.get(tile).equals(transportStack.originalLocation))
					{
						continue;
					}
					
					if(InventoryUtils.canInsert(tile, transportStack.color, transportStack.itemStack, side.ordinal(), false))
					{
						destsFound.add(Object3D.get(tile));
					}
					else if(transportStack.canInsertToTransporter(tile) && !iterated.contains(Object3D.get(tile)))
					{
						loop(Object3D.get(tile));
					}
				}
			}
		}
		
	public Set<Object3D> find()
	{
		loop(start);
		
		return destsFound;
	}
}
	
	public static List<Destination> getPaths(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		DestChecker checker = new DestChecker()
		{
			@Override
			public boolean isValid(TransporterStack stack, int side, TileEntity tile)
			{
				return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, side, false);
			}
		};
		
		InventoryFinder d = new InventoryFinder(start.worldObj, Object3D.get(start), stack);
		Set<Object3D> destsFound = d.find();
		List<Destination> paths = new ArrayList<Destination>();
		
		for(Object3D obj : destsFound)
		{
			Pathfinder p = new Pathfinder(checker, start.worldObj, obj, Object3D.get(start), stack);
			
			if(p.getPath().size() >= 2)
			{
				paths.add(new Destination(p.getPath(), p.finalScore, false));
			}
		}
		
		Collections.sort(paths);
		
		return paths;
	}
	
	public static List<Object3D> getNewBasePath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		List<Destination> paths = getPaths(start, stack);
		
		if(paths.isEmpty())
		{
			return null;
		}
		
		return paths.get(0).path;
	}
	
	public static List<Object3D> getNewRRPath(TileEntityLogisticalTransporter start, TransporterStack stack, TileEntityLogisticalSorter outputter)
	{
		List<Destination> paths = getPaths(start, stack);
		
		Map<Object3D, Destination> destPaths = new HashMap<Object3D, Destination>();
		
		for(Destination d : paths)
		{
			if(destPaths.get(d.path.get(0)) == null || destPaths.get(d.path.get(0)).score < d.score)
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
		
		return closest.path;
	}
	
	public static class Pathfinder
	{
		public final Set<Object3D> openSet, closedSet;

		public final HashMap<Object3D, Object3D> navMap;

		public final HashMap<Object3D, Double> gScore, fScore;

		public final Object3D start;

		public final Object3D finalNode;

		public final TransporterStack transportStack;
		
		public final DestChecker destChecker;
		
		public double finalScore;

		public ArrayList<Object3D> results;

		private World worldObj;

		public Pathfinder(DestChecker checker, World world, Object3D finishObj, Object3D startObj, TransporterStack stack) 
		{
			destChecker = checker;
			worldObj = world;
			
			finalNode = finishObj;
			start = startObj;
			
			transportStack = stack;

			openSet = new HashSet<Object3D>();
			closedSet = new HashSet<Object3D>();

			navMap = new HashMap<Object3D, Object3D>();

			gScore = new HashMap<Object3D, Double>();
			fScore = new HashMap<Object3D, Double>();

			results = new ArrayList<Object3D>();
			
			find(start);
		}

		public boolean find(Object3D start) 
		{
			openSet.add(start);
			gScore.put(start, 0D);
			fScore.put(start, gScore.get(start) + getEstimate(start, finalNode));

			int blockCount = 0;

			for(int i = 0; i < 6; i++) 
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Object3D neighbor = start.translate(direction.offsetX, direction.offsetY, direction.offsetZ);

				if(!transportStack.canInsertToTransporter(neighbor.getTileEntity(worldObj)) && (!neighbor.equals(finalNode) || !destChecker.isValid(transportStack, i, neighbor.getTileEntity(worldObj))))
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
				Object3D currentNode = null;
				double lowestFScore = 0;

				for(Object3D node : openSet)
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
					ForgeDirection direction = ForgeDirection.getOrientation(i);
					Object3D neighbor = currentNode.getFromSide(direction);

					if(transportStack.canInsertToTransporter(neighbor.getTileEntity(worldObj)))
					{
						double tentativeG = gScore.get(currentNode) + currentNode.distanceTo(neighbor);
						
						if(neighbor.getMetadata(worldObj) == 4)
						{
							tentativeG += 999;
						}

						if(closedSet.contains(neighbor)) 
						{
							if(tentativeG >= gScore.get(neighbor))
							{
								continue;
							}
						}

						if(!openSet.contains(neighbor) || tentativeG < gScore.get(neighbor)) 
						{
							navMap.put(neighbor, currentNode);
							gScore.put(neighbor, tentativeG);
							fScore.put(neighbor, gScore.get(neighbor) + getEstimate(neighbor, finalNode));
							openSet.add(neighbor);
						}
					}
					else if(neighbor.equals(finalNode) && destChecker.isValid(transportStack, i, neighbor.getTileEntity(worldObj)))
					{
						results = reconstructPath(navMap, currentNode);
						return true;
					}
				}
			}

			return false;
		}

		private ArrayList<Object3D> reconstructPath(HashMap<Object3D, Object3D> naviMap, Object3D currentNode) 
		{
			ArrayList<Object3D> path = new ArrayList<Object3D>();

			path.add(currentNode);
			
			if(naviMap.containsKey(currentNode)) 
			{
				path.addAll(reconstructPath(naviMap, naviMap.get(currentNode)));
			}
			
			finalScore = gScore.get(currentNode) + currentNode.distanceTo(finalNode);

			return path;
		}
		
		public ArrayList<Object3D> getPath()
		{
			ArrayList<Object3D> path = new ArrayList<Object3D>();
			path.add(finalNode);
			path.addAll((ArrayList<Object3D>)results.clone());
			
			return path;
		}

		private double getEstimate(Object3D start, Object3D target2) 
		{
			return start.distanceTo(target2);
		}
		
		public static class DestChecker
		{
			public boolean isValid(TransporterStack stack, int side, TileEntity tile)
			{
				return false;
			}
		}
	}
	
	public static List<Object3D> getIdlePath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		if(stack.homeLocation != null)
		{
			DestChecker checker = new DestChecker() 
			{
				@Override
				public boolean isValid(TransporterStack stack, int side, TileEntity tile)
				{
					return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
				}
			};
			
			Pathfinder p = new Pathfinder(checker, start.worldObj, stack.homeLocation, Object3D.get(start), stack);
			List<Object3D> path = p.getPath();
			
			if(path.size() >= 2)
			{
				stack.pathType = Path.HOME;
				return path;
			}
			else {
				if(stack.homeLocation.getTileEntity(start.worldObj) == null)
				{
					stack.homeLocation = null;
				}
			}
		}
		
		IdlePath d = new IdlePath(start.worldObj, Object3D.get(start), stack);
		List<Object3D> path = d.find();
		stack.pathType = Path.NONE;
		
		if(path == null)
		{
			return null;
		}
		
		return path;
	}
}
