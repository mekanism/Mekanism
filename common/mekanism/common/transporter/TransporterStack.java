package mekanism.common.transporter;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;

public class TransporterStack 
{
	public ItemStack itemStack;
	
	public int progress;
	
	public EnumColor color = null;
	
	public boolean initiatedPath = false;
	
	public List<Object3D> pathToTarget = new ArrayList<Object3D>();
	
	public Object3D originalLocation;
	
	public Object3D clientNext;
	public Object3D clientPrev;
	
	public boolean noTarget = false;
	
	public void write(TileEntityLogisticalTransporter tileEntity, ArrayList data)
	{
		if(color != null)
		{
			data.add(TransporterUtils.colors.indexOf(color));
		}
		else {
			data.add(-1);
		}
		
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
		int c = dataStream.readInt();
		
		if(c != -1)
		{
			color = TransporterUtils.colors.get(c);
		}
		else {
			color = null;
		}
		
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
		if(color != null)
		{
			nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
		}
		
		nbtTags.setInteger("progress", progress);
		originalLocation.write(nbtTags);
		nbtTags.setBoolean("noTarget", noTarget);
		itemStack.writeToNBT(nbtTags);
	}
	
	public void read(NBTTagCompound nbtTags)
	{
		if(nbtTags.hasKey("color"))
		{
			color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
		}
		
		progress = nbtTags.getInteger("progress");
		originalLocation = Object3D.read(nbtTags);
		noTarget = nbtTags.getBoolean("noTarget");
		itemStack = ItemStack.loadItemStackFromNBT(nbtTags);
	}
	
	public static TransporterStack readFromNBT(NBTTagCompound nbtTags)
	{
		TransporterStack stack = new TransporterStack();
		stack.read(nbtTags);
		
		return stack;
	}
	
	public static TransporterStack readFromPacket(ByteArrayDataInput dataStream)
	{
		TransporterStack stack = new TransporterStack();
		stack.read(dataStream);
		
		return stack;
	}
	
	public boolean hasPath()
	{
		return pathToTarget != null;
	}
	
	public boolean recalculatePath(TileEntityLogisticalTransporter tileEntity)
	{
		List<Object3D> newPath = TransporterPathfinder.getNewBasePath(tileEntity, this);
		
		if(newPath == null)
		{
			return false;
		}
		
		pathToTarget = newPath;
		
		noTarget = false;
		initiatedPath = true;
		
		return true;
	}
	
	public boolean recalculateRRPath(TileEntityLogisticalSorter outputter, TileEntityLogisticalTransporter tileEntity)
	{
		List<Object3D> newPath = TransporterPathfinder.getNewRRPath(tileEntity, this, outputter);
		
		if(newPath == null)
		{
			return false;
		}
		
		pathToTarget = newPath;
		
		noTarget = false;
		initiatedPath = true;
		
		return true;
	}
	
	public boolean calculateIdle(TileEntityLogisticalTransporter tileEntity)
	{
		List<Object3D> newPath = TransporterPathfinder.getIdlePath(tileEntity, this);
		
		if(newPath == null)
		{
			return false;
		}
		
		pathToTarget = newPath;
		
		noTarget = true;
		originalLocation = Object3D.get(tileEntity);
		initiatedPath = true;
		
		return true;
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
			
			if(index < 0)
			{
				return null;
			}
			
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
			if(getPrev(tileEntity) != null)
			{
				return Object3D.get(tileEntity).sideDifference(getPrev(tileEntity)).ordinal();
			}
		}
		else if(progress == 50)
		{
			if(getNext(tileEntity) != null)
			{
				return getNext(tileEntity).sideDifference(Object3D.get(tileEntity)).ordinal();
			}
		}
		else if(progress > 50)
		{
			if(getNext(tileEntity) != null)
			{
				return getNext(tileEntity).sideDifference(Object3D.get(tileEntity)).ordinal();
			}
		}
		
		return 0;
	}
	
	public boolean canInsertToTransporter(TileEntity tileEntity)
	{
		if(!(tileEntity instanceof TileEntityLogisticalTransporter))
		{
			return false;
		}
		
		TileEntityLogisticalTransporter transporter = (TileEntityLogisticalTransporter)tileEntity;
		return transporter.color == color || transporter.color == null;
	}
	
	public Object3D getDest()
	{
		return pathToTarget.get(0);
	}
}
