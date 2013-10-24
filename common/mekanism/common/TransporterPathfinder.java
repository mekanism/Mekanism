package mekanism.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.util.TransporterUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public final class TransporterPathfinder
{
	public static class IdleDest
	{
		public World worldObj;
		
		public Set<TileEntityLogisticalTransporter> iterated = new HashSet<TileEntityLogisticalTransporter>();
		
		public TileEntityLogisticalTransporter start;
		
		public Object3D lastFound;
		
		public TransporterStack transportStack;
		
		public IdleDest(World world, TileEntityLogisticalTransporter tileEntity, TransporterStack stack)
		{
			worldObj = world;
			start = tileEntity;
			transportStack = stack;
		}
		
		public void loop(TileEntityLogisticalTransporter pointer)
		{
			if(pointer == null || lastFound != null)
			{
				return;
			}
			
			iterated.add(pointer);
			
			boolean found = false;
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = Object3D.get(pointer).getFromSide(side).getTileEntity(worldObj);
				
				if(transportStack.canInsert(tile) && !iterated.contains(tile))
				{
					loop((TileEntityLogisticalTransporter)tile);
					found = true;
				}
			}
			
			if(!found)
			{
				lastFound = Object3D.get(pointer);
				return;
			}
		}
		
		public Object3D find()
		{
			loop(start);
			
			return lastFound;
		}
	}
	
	public static class Destination
	{
		public World worldObj;
		
		public Set<TileEntityLogisticalTransporter> iterated = new HashSet<TileEntityLogisticalTransporter>();
		
		public TileEntityLogisticalTransporter start;
		public Object3D destination;
		public Object3D finalNode;
		
		public TransporterStack transportStack;
		
		public Destination(World world, TileEntityLogisticalTransporter tileEntity, TransporterStack stack)
		{
			worldObj = world;
			start = tileEntity;
			transportStack = stack;
		}
		
		public void loop(TileEntityLogisticalTransporter pointer)
		{
			if(pointer == null || destination != null)
			{
				return;
			}
			
			iterated.add(pointer);
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = Object3D.get(pointer).getFromSide(side).getTileEntity(worldObj);
				
				if(TransporterUtils.canInsert(tile, transportStack.itemStack, side.ordinal()) && !(tile instanceof TileEntityLogisticalTransporter))
				{
					destination = Object3D.get(tile);
					finalNode = Object3D.get(pointer);
					return;
				}
				
				if(transportStack.canInsert(tile) && !iterated.contains(tile))
				{
					loop((TileEntityLogisticalTransporter)tile);
				}
			}
		}
		
		public Object3D find()
		{
			loop(start);
			
			return destination;
		}
	}
	
	public static class Path
	{
		public final Set<Object3D> openSet, closedSet;

		public final HashMap<Object3D, Object3D> navMap;

		public final HashMap<Object3D, Double> gScore, fScore;

		public final Object3D target;
		
		public final Object3D start;
		
		public final Object3D finalNode;
		
		public final TransporterStack transportStack;

		public List<Object3D> results;

		private World worldObj;

		public Path(World world, Object3D node, Object3D startObj, Object3D finishObj, TransporterStack stack) 
		{
			worldObj = world;
			finalNode = node;
			start = startObj;
			target = finishObj;
			transportStack = stack;

			openSet = new HashSet<Object3D>();
			closedSet = new HashSet<Object3D>();
			
			navMap = new HashMap<Object3D, Object3D>();
			
			gScore = new HashMap<Object3D, Double>();
			fScore = new HashMap<Object3D, Double>();
			
			results = new ArrayList<Object3D>();
		}

		public boolean find(final Object3D start)
		{
			openSet.add(start);
			gScore.put(start, 0d);
			fScore.put(start, gScore.get(start) + getEstimate(start, finalNode));

			int blockCount = 0;

			for(int i = 0; i < 6; i++) 
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Object3D neighbor = finalNode.translate(direction.offsetX, direction.offsetY, direction.offsetZ);

				if(!transportStack.canInsert(neighbor.getTileEntity(worldObj))) 
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

				if(currentNode.equals(finalNode))
				{
					results = reconstructPath(navMap, finalNode);
					return true;
				}

				openSet.remove(currentNode);
				closedSet.add(currentNode);

				for(int i = 0; i < 6; i++) 
				{
					ForgeDirection direction = ForgeDirection.getOrientation(i);
					Object3D neighbor = currentNode.getFromSide(direction);

					if(transportStack.canInsert(neighbor.getTileEntity(worldObj))) 
					{
						double tentativeG = gScore.get(currentNode) + currentNode.distanceTo(neighbor);

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
				}
			}

			return false;
		}
		
		public List<Object3D> getPath()
		{
			boolean foundPath = find(start);
			
			if(foundPath)
			{
				if(target != null)
				{
					results.add(0, target);
				}
				
				return results;
			}
			
			return null;
		}

		private List<Object3D> reconstructPath(HashMap<Object3D, Object3D> naviMap, Object3D currentNode) 
		{
			List<Object3D> path = new ArrayList<Object3D>();
			path.add(currentNode);

			if(naviMap.containsKey(currentNode)) 
			{
				path.addAll(reconstructPath(naviMap, naviMap.get(currentNode)));
			}

			return path;
		}

		private double getEstimate(Object3D start, Object3D target2) 
		{
			return start.distanceTo(target2);
		}
	}
	
	public static List<Object3D> getNewPath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		Destination d = new Destination(start.worldObj, start, stack);
		Object3D closest = d.find();
		
		System.out.println(closest);
		
		if(closest == null)
		{
			return null;
		}
		
		Path p = new Path(d.worldObj, d.finalNode, Object3D.get(start), closest, stack);
		return p.getPath();
	}
	
	public static List<Object3D> getIdlePath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		IdleDest d = new IdleDest(start.worldObj, start, stack);
		Object3D farthest = d.find();
		
		if(farthest == null || farthest.equals(Object3D.get(start)))
		{
			return null;
		}
		
		Path p = new Path(start.worldObj, farthest, Object3D.get(start), null, stack);
		return p.getPath();
	}
}
