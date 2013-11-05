package mekanism.common.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.util.TransporterUtils;
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
	
	public static class Destination
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
	}
	
	public static class DestPath
	{
		public World worldObj;
		
		public Set<Destination> destinations = new HashSet<Destination>();
		
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
				
				if(TransporterUtils.canInsert(tile, transportStack.color, transportStack.itemStack, side.ordinal()) && !(tile instanceof TileEntityLogisticalTransporter))
				{
					destinations.add(new Destination(currentPath, Object3D.get(tile), dist));
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
			
		    Destination closest = null;
			
			for(Destination obj : destinations)
			{
				if(closest == null || obj.distance < closest.distance)
				{
					if(!obj.path.isEmpty() && !obj.path.get(0).equals(start))
					{
						closest = obj;
					}
				}
			}
			
			if(closest == null)
			{
				return null;
			}
			
			return closest.path;
		}
	}
	
	public static List<Object3D> getNewPath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		DestPath d = new DestPath(start.worldObj, Object3D.get(start), stack);
		List<Object3D> path = d.find();
		
		if(path == null)
		{
			return null;
		}
		
		return path;
	}
	
	public static List<Object3D> getIdlePath(TileEntityLogisticalTransporter start, TransporterStack stack)
	{
		IdlePath d = new IdlePath(start.worldObj, Object3D.get(start), stack);
		List<Object3D> path = d.find();
		
		if(path == null)
		{
			return null;
		}
		
		return path;
	}
}
