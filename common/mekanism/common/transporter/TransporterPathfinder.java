package mekanism.common.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.ILogisticalTransporter;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterPathfinder.Pathfinder.DestChecker;
import mekanism.common.transporter.TransporterStack.Path;
import mekanism.common.util.InventoryUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public final class TransporterPathfinder
{
	public static class IdlePath
	{
		public World worldObj;
		
		public Coord4D start;
		
		public Set<Destination> destinations = new HashSet<Destination>();
		
		public TransporterStack transportStack;
		
		public IdlePath(World world, Coord4D obj, TransporterStack stack)
		{
			worldObj = world;
			start = obj;
			transportStack = stack;
		}
		
		public void loop(Coord4D pointer, ArrayList<Coord4D> currentPath, int dist)
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
				
				if(transportStack.canInsertToTransporter(tile, side) && !currentPath.contains(Coord4D.get(tile)))
				{
					loop(Coord4D.get(tile), (ArrayList<Coord4D>)currentPath.clone(), dist);
					found = true;
				}
			}
			
			if(!found)
			{
				destinations.add(new Destination(currentPath, dist, true, null));
			}
		}
		
		public List<Coord4D> find()
		{
			loop(start, new ArrayList<Coord4D>(), 0);
			
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
		public List<Coord4D> path = new ArrayList<Coord4D>();
		public double score;
		public ItemStack rejected;
		
		public Destination(ArrayList<Coord4D> list, double d, boolean inv, ItemStack rejects)
		{
			path = (List<Coord4D>)list.clone();
			
			if(inv)
			{
				Collections.reverse(path);
			}
			
			score = d;
			rejected = rejects;
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
		
		public Set<Coord4D> iterated = new HashSet<Coord4D>();
		
		public Set<Coord4D> destsFound = new HashSet<Coord4D>();
		
		public Map<Coord4D, ItemStack> rejects = new HashMap<Coord4D, ItemStack>();
		
		public Coord4D start;
		
		public TransporterStack transportStack;
		
		public InventoryFinder(World world, Coord4D obj, TransporterStack stack)
		{
			worldObj = world;
			start = obj;
			transportStack = stack;
		}
		
		public void loop(Coord4D pointer)
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
					if(Coord4D.get(tile).equals(transportStack.originalLocation))
					{
						continue;
					}
					
					if(tile instanceof IInventory)
					{
						if(pointer.getTileEntity(worldObj) instanceof ILogisticalTransporter)
						{
							ILogisticalTransporter trans = (ILogisticalTransporter)pointer.getTileEntity(worldObj);
							
							if(!trans.canConnectMutual(tile, side))
							{
								continue;
							}
						}
						
						ItemStack stack = TransporterManager.getPredictedInsert(tile, transportStack.color, transportStack.itemStack, side.ordinal());
						
						if(TransporterManager.didEmit(transportStack.itemStack, stack))
						{
							destsFound.add(Coord4D.get(tile));
							rejects.put(Coord4D.get(tile), stack);
						}
					}
					else if(transportStack.canInsertToTransporter(tile, side) && !iterated.contains(Coord4D.get(tile)))
					{
						loop(Coord4D.get(tile));
					}
				}
			}
		}
		
		public Set<Coord4D> find()
		{
			loop(start);
			
			return destsFound;
		}
	}
	
	public static List<Destination> getPaths(ILogisticalTransporter start, TransporterStack stack, int min)
	{
		DestChecker checker = new DestChecker()
		{
			@Override
			public boolean isValid(TransporterStack stack, int side, TileEntity tile)
			{
				return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, side, false);
			}
		};
		
		InventoryFinder d = new InventoryFinder(start.getTile().worldObj, Coord4D.get(start.getTile()), stack);
		Set<Coord4D> destsFound = d.find();
		List<Destination> paths = new ArrayList<Destination>();
		
		for(Coord4D obj : destsFound)
		{
			Pathfinder p = new Pathfinder(checker, start.getTile().worldObj, obj, Coord4D.get(start.getTile()), stack);
			
			if(p.getPath().size() >= 2)
			{
				if(TransporterManager.getToUse(stack.itemStack, d.rejects.get(obj)).stackSize >= min)
				{
					paths.add(new Destination(p.getPath(), p.finalScore, false, d.rejects.get(obj)));
				}
			}
		}
		
		Collections.sort(paths);
		
		return paths;
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
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Coord4D neighbor = start.translate(direction.offsetX, direction.offsetY, direction.offsetZ);

				if(!transportStack.canInsertToTransporter(neighbor.getTileEntity(worldObj), direction) && (!neighbor.equals(finalNode) || !destChecker.isValid(transportStack, i, neighbor.getTileEntity(worldObj))))
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
					ForgeDirection direction = ForgeDirection.getOrientation(i);
					Coord4D neighbor = currentNode.getFromSide(direction);

					if(transportStack.canInsertToTransporter(neighbor.getTileEntity(worldObj), direction))
					{
						TileEntity tile = neighbor.getTileEntity(worldObj);
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
						
						TileEntity currTile = currentNode.getTileEntity(worldObj);

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
			public boolean isValid(TransporterStack stack, int side, TileEntity tile)
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
				public boolean isValid(TransporterStack stack, int side, TileEntity tile)
				{
					return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
				}
			};
			
			Pathfinder p = new Pathfinder(checker, start.getTile().worldObj, stack.homeLocation, Coord4D.get(start.getTile()), stack);
			List<Coord4D> path = p.getPath();
			
			if(path.size() >= 2)
			{
				stack.pathType = Path.HOME;
				return path;
			}
			else {
				if(stack.homeLocation.getTileEntity(start.getTile().worldObj) == null)
				{
					stack.homeLocation = null;
				}
			}
		}
		
		IdlePath d = new IdlePath(start.getTile().worldObj, Coord4D.get(start.getTile()), stack);
		List<Coord4D> path = d.find();
		stack.pathType = Path.NONE;
		
		if(path == null)
		{
			return null;
		}
		
		return path;
	}
}
