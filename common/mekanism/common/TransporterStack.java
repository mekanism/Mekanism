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
	
	public boolean initiatedPath = false;
	
	public List<Object3D> pathToTarget = new ArrayList<Object3D>();
	
	public Object3D originalLocation;
	
	public Object3D clientNext;
	public Object3D clientPrev;
	
	public boolean noTarget = false;
	
	public void write(TileEntityLogisticalTransporter tileEntity, ArrayList data)
	{
		data.add(progress);
		data.add(noTarget);
		
		if(pathToTarget.indexOf(Object3D.get(tileEntity)) > 0)
		{
			data.add(true);
			getNext(tileEntity).write(data);
		}
		else {
			data.add(false);
		}
		
		getPrev(tileEntity).write(data);
		
		data.add(itemStack.itemID);
		data.add(itemStack.stackSize);
		data.add(itemStack.getItemDamage());
	}
	
	public void read(ByteArrayDataInput dataStream)
	{
		progress = dataStream.readInt();
		noTarget = dataStream.readBoolean();
		
		if(dataStream.readBoolean())
		{
			clientNext = Object3D.read(dataStream);
		}
		
		clientPrev = Object3D.read(dataStream);
		
		itemStack = new ItemStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}
	
	public void write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("progress", progress);
		originalLocation.write(nbtTags);
		nbtTags.setBoolean("noTarget", noTarget);
		itemStack.writeToNBT(nbtTags);
	}
	
	public void read(NBTTagCompound nbtTags)
	{
		progress = nbtTags.getInteger("progress");
		originalLocation = Object3D.read(nbtTags);
		noTarget = nbtTags.getBoolean("noTarget");
		itemStack = ItemStack.loadItemStackFromNBT(nbtTags);
	}
	
	public boolean hasPath()
	{
		return pathToTarget != null;
	}
	
	public boolean recalculatePath(TileEntityLogisticalTransporter tileEntity)
	{
		List<Object3D> newPath = TransporterPathfinder.getNewPath(tileEntity, itemStack);
		
		if(newPath == null)
		{
			return false;
		}
		
		pathToTarget = newPath;
		
		noTarget = false;
		initiatedPath = true;
		
		return true;
	}
	
	public void calculateIdle(TileEntityLogisticalTransporter tileEntity)
	{
		pathToTarget = TransporterPathfinder.getIdlePath(tileEntity, originalLocation, pathToTarget.get(pathToTarget.size()-1));
		noTarget = true;
		originalLocation = Object3D.get(tileEntity);
		initiatedPath = true;
	}
	
	public boolean isFinal(TileEntityLogisticalTransporter tileEntity)
	{
		return pathToTarget.indexOf(Object3D.get(tileEntity)) == (noTarget ? 0 : 1);
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
			
			if(index < pathToTarget.size())
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
		else if(progress > 50)
		{
			return getNext(tileEntity).sideDifference(Object3D.get(tileEntity)).ordinal();
		}
		
		return 0;
	}
	
	public Object3D getDest()
	{
		return pathToTarget.get(0);
	}
}
