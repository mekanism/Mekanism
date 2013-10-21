package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

public class TransporterStack 
{
	public ItemStack itemStack;
	
	/** out of 100 */
	public int progress;
	
	public List<Object3D> pathToTarget = new ArrayList<Object3D>();
	
	public Object3D originalLocation;
	
	public Object3D clientNext;
	public Object3D clientPrev;
	
	public boolean goingHome = false;
	
	public void write(ArrayList data)
	{
		
	}
	
	public void read(ByteArrayDataInput dataStream)
	{
		
	}
	
	public void write(NBTTagCompound nbtTags)
	{
		
	}
	
	public void read(NBTTagCompound nbtTags)
	{
		
	}
	
	public boolean hasPath()
	{
		return pathToTarget != null;
	}
	
	public void recalculatePath(TileEntityLogisticalTransporter tileEntity)
	{
		pathToTarget = TransporterPathfinder.getNewPath(tileEntity, itemStack);
	}
	
	public void sendHome(TileEntityLogisticalTransporter tileEntity)
	{
		pathToTarget = TransporterPathfinder.getHomePath(tileEntity, originalLocation, pathToTarget.get(pathToTarget.size()-1));
		goingHome = true;
	}
	
	public boolean isFinal(TileEntityLogisticalTransporter tileEntity)
	{
		return pathToTarget.indexOf(Object3D.get(tileEntity)) == 1;
	}
	
	public Object3D getNext(TileEntityLogisticalTransporter tileEntity)
	{
		if(!tileEntity.worldObj.isRemote)
		{
			int index = pathToTarget.indexOf(Object3D.get(tileEntity))-1;
			return pathToTarget.get(index);
		}
		else {
			return clientNext;
		}
	}
	
	public Object3D getPrev(TileEntityLogisticalTransporter tileEntity)
	{
		if(!tileEntity.worldObj.isRemote)
		{
			int index = pathToTarget.indexOf(Object3D.get(tileEntity))+1;
			
			if(pathToTarget.get(index) != null)
			{
				return pathToTarget.get(index);
			}
			else {
				return originalLocation;
			}
		}
		else {
			return clientPrev;
		}
	}
	
	public int getSide(TileEntityLogisticalTransporter tileEntity)
	{
		if(progress < 50)
		{
			return Object3D.get(tileEntity).sideDifference(getPrev(tileEntity)).ordinal();
		}
		else {
			return Object3D.get(tileEntity).sideDifference(getNext(tileEntity)).ordinal();
		}
	}
	
	public Object3D getDest()
	{
		return pathToTarget.get(0);
	}
}
