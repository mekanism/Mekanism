package mekanism.common.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.transporter.TransporterStack.Path;
import mekanism.common.util.TransporterUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public final class TransporterPathfinder
{
	public static class HomePath
	{
		public World worldObj;
		
		public List<Destination> destinations = new ArrayList<Destination>();
		
		public Object3D start;
		
		public Object3D homeLocation;
		
		public TransporterStack transportStack;
		
		public boolean existed;
		
		public HomePath(World world, Object3D obj, Object3D home, TransporterStack stack)
		{
			worldObj = world;
			start = obj;
			homeLocation = home;
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
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = pointer.getFromSide(side).getTileEntity(worldObj);
				
				if(tile != null && Object3D.get(tile).equals(homeLocation))
				{
					if(TransporterUtils.canInsert(tile, transportStack.color, transportStack.itemStack, side.ordinal(), true))
					{
						Destination dest = new Destination(currentPath, Object3D.get(tile), dist);
						
						if(!destinations.contains(dest))
						{
							destinations.add(dest);
						}
					}
					
					if(tile instanceof TileEntityLogisticalSorter)
			    	{
			    		if(((TileEntityLogisticalSorter)tile).hasInventory())
			    		{
			    			existed = true;
			    		}
			    	}
					else {
						existed = true;
					}
				}
				
				if(transportStack.canInsertToTransporter(tile) && !currentPath.contains(Object3D.get(tile)))
				{
					loop(Object3D.get(tile), (ArrayList<Object3D>)currentPath.clone(), dist);
				}
			}
		}
		
		public List<Object3D> find()
		{
			loop(start, new ArrayList<Object3D>(), 0);
			
			Collections.sort(destinations);
			
		    Destination closest = null;
			
		    if(!destinations.isEmpty())
		    {
		    	closest = destinations.get(0);
		    }
		    
		    if(closest == null)
		    {
		    	return null;
		    }
			
			return closest.path;
		}
	}
	
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
				destinations.add(new Destination(currentPath, null, dist));
			}
		}
		
		public List<Object3D> find()
		{
			loop(start, new ArrayList<Object3D>(), 0);
			
		    Destination farthest = null;
			
			for(Destination obj : destinations)
			{
				if(farthest == null || obj.distance > farthest.distance)
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
		public int distance;
		
		public Destination(ArrayList<Object3D> list, Object3D dest, int i)
		{
			path = (List<Object3D>)list.clone();
			
			if(dest != null)
			{
				path.add(dest);
			}
			
			Collections.reverse(path);
			distance = i;
		}
		
		@Override
		public int hashCode() 
		{
			int code = 1;
			code = 31 * code + path.hashCode();
			code = 31 * code + distance;
			return code;
		}
		
		@Override
		public boolean equals(Object dest)
		{
			return dest instanceof Destination && ((Destination)dest).path.equals(path) && ((Destination)dest).distance == distance;
		}

		@Override
		public int compareTo(Destination dest)
		{
			if(dest.distance > distance)
			{
				return -1;
			}
			else if(dest.distance < distance)
			{
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	
	public static class DestPath
	{
		public World worldObj;
		
		public List<Destination> destinations = new ArrayList<Destination>();
		
		public Object3D start;
		
		public TransporterStack transportStack;
		
		public DestPath(World world, Object3D obj, TransporterStack stack)
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
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = pointer.getFromSide(side).getTileEntity(worldObj);
				
				if(TransporterUtils.canInsert(tile, transportStack.color, transportStack.itemStack, side.ordinal(), false) && !(tile instanceof TileEntityLogisticalTransporter))
				{
					Destination dest = new Destination(currentPath, Object3D.get(tile), dist);
					
					if(!destinations.contains(dest))
					{
						destinations.add(dest);
					}
				}
				
				if(transportStack.canInsertToTransporter(tile) && !currentPath.contains(Object3D.get(tile)))
				{
					loop(Object3D.get(tile), (ArrayList<Object3D>)currentPath.clone(), dist);
				}
			}
		}
		
		public List<Object3D> find()
		{
			loop(start, new ArrayList<Object3D>(), 0);
			
			Collections.sort(destinations);
			
		    Destination closest = null;
			
		    if(!destinations.isEmpty())
		    {
		    	closest = destinations.get(0);
		    }
		    
		    if(closest == null)
		    {
		    	return null;
		    }
			
			return closest.path;
		}
		
		public List<Object3D> findRR(TileEntityLogisticalSorter outputter)
		{
			loop(start, new ArrayList<Object3D>(), 0);
			
			Collections.sort(destinations);
			
		    Destination closest = null;
			
		    if(!destinations.isEmpty())
		    {
		    	if(outputter.rrIndex <= destinations.size()-1)
		    	{
		    		closest = destinations.get(outputter.rrIndex);
		    		
		    		if(outputter.rrIndex == destinations.size()-1)
		    		{
		    			outputter.rrIndex = 0;
		    		}
		    		else if(outputter.rrIndex < destinations.size()-1)
		    		{
		    			outputter.rrIndex++;
		    		}
		    	}
		    	else {
		    		closest = destinations.get(destinations.size()-1);
		    		outputter.rrIndex = 0;
		    	}
		    }
		    
		    if(closest == null)
		    {
		    	return null;
		    }
			
			return closest.path;
		}
	}
	
	public static List<Object3D> getNewBasePath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		DestPath d = new DestPath(start.worldObj, Object3D.get(start), stack);
		List<Object3D> path = d.find();
		
		if(path == null)
		{
			return null;
		}
		
		return path;
	}
	
	public static List<Object3D> getNewRRPath(TileEntityLogisticalTransporter start, TransporterStack stack, TileEntityLogisticalSorter outputter)
	{
		DestPath d = new DestPath(start.worldObj, Object3D.get(start), stack);
		List<Object3D> path = d.findRR(outputter);
		
		if(path == null)
		{
			return null;
		}
		
		return path;
	}
	
	public static List<Object3D> getIdlePath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		if(stack.homeLocation != null)
		{
			HomePath d = new HomePath(start.worldObj, Object3D.get(start), stack.homeLocation, stack);
			List<Object3D> path = d.find();
			
			if(path != null)
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
