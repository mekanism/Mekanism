package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IEjector;
import mekanism.api.Object3D;
import mekanism.api.SideData;
import mekanism.common.tileentity.TileEntityContainerBlock;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileComponentEjector implements ITileComponent, IEjector
{
	public TileEntityContainerBlock tileEntity;
	
	public boolean strictInput;
	
	public boolean ejecting;
	
	public EnumColor outputColor;
	
	public EnumColor[] inputColors = new EnumColor[] {null, null, null, null, null, null};
	
	public SideData sideData;
	
	public int[] trackers;
	
	public TileComponentEjector(TileEntityContainerBlock tile, SideData data)
	{
		tileEntity = tile;
		sideData = data;
		trackers = new int[sideData.availableSlots.length];
		
		tile.components.add(this);
	}
	
	private List<ForgeDirection> getTrackedOutputs(int index, List<ForgeDirection> dirs)
	{
		List<ForgeDirection> sides = new ArrayList<ForgeDirection>();
		
		for(int i = trackers[index]+1; i <= trackers[index]+6; i++)
		{
			for(ForgeDirection side : dirs)
			{
				if(ForgeDirection.getOrientation(i%6) == side)
				{
					sides.add(side);
				}
			}
		}
		
		return sides;
	}
	
	@Override
	public void onOutput()
	{
		if(!ejecting || tileEntity.worldObj.isRemote)
		{
			return;
		}
		
		List<ForgeDirection> outputSides = new ArrayList<ForgeDirection>();
		
		IConfigurable configurable = (IConfigurable)tileEntity;
		
		for(int i = 0; i < configurable.getConfiguration().length; i++)
		{
			if(configurable.getConfiguration()[i] == configurable.getSideData().indexOf(sideData))
			{
				outputSides.add(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(i, tileEntity.facing)));
			}
		}
		
		for(int index = 0; index < sideData.availableSlots.length; index++)
		{
			int slotID = sideData.availableSlots[index];
			
			if(tileEntity.inventory[slotID] == null)
			{
				continue;
			}
			
			ItemStack stack = tileEntity.inventory[slotID];
			List<ForgeDirection> outputs = getTrackedOutputs(index, outputSides);
			
			for(ForgeDirection side : outputs)
			{
				TileEntity tile = Object3D.get(tileEntity).getFromSide(side).getTileEntity(tileEntity.worldObj);
				ItemStack prev = stack.copy();
				
				if(tile instanceof IInventory && !(tile instanceof TileEntityLogisticalTransporter))
				{
					stack = TransporterUtils.putStackInInventory((IInventory)tile, stack, side.ordinal(), false);
				}
				else if(tile instanceof TileEntityLogisticalTransporter)
				{
					if(TransporterUtils.insert(tileEntity, (TileEntityLogisticalTransporter)tile, stack, outputColor))
					{
						stack = null;
					}
				}
				
				if(stack == null || prev.stackSize != stack.stackSize)
				{
					trackers[index] = side.ordinal();
				}
				
				if(stack == null)
				{
					break;
				}
			}
			
			tileEntity.inventory[slotID] = stack;
		}
	}
	
	@Override
	public boolean isEjecting()
	{
		return ejecting;
	}
	
	@Override
	public void setEjecting(boolean eject)
	{
		ejecting = eject;
		MekanismUtils.saveChunk(tileEntity);
	}
	
	@Override
	public boolean hasStrictInput()
	{
		return strictInput;
	}
	
	@Override
	public void setStrictInput(boolean strict)
	{
		strictInput = strict;
		MekanismUtils.saveChunk(tileEntity);
	}
	
	@Override
	public void setOutputColor(EnumColor color)
	{
		outputColor = color;
		MekanismUtils.saveChunk(tileEntity);
	}
	
	@Override
	public EnumColor getOutputColor()
	{
		return outputColor;
	}
	
	@Override
	public void setInputColor(ForgeDirection side, EnumColor color)
	{
		inputColors[side.ordinal()] = color;
		MekanismUtils.saveChunk(tileEntity);
	}
	
	@Override
	public EnumColor getInputColor(ForgeDirection side)
	{
		return inputColors[side.ordinal()];
	}

	@Override
	public void tick() {}

	@Override
	public void read(NBTTagCompound nbtTags) 
	{
		ejecting = nbtTags.getBoolean("ejecting");
		strictInput = nbtTags.getBoolean("strictInput");
		
		if(nbtTags.hasKey("ejectColor"))
		{
			outputColor = TransporterUtils.colors.get(nbtTags.getInteger("ejectColor"));
		}
		
		for(int i = 0; i < sideData.availableSlots.length; i++)
		{
			trackers[i] = nbtTags.getInteger("tracker" + i);
		}
		
		for(int i = 0; i < 6; i++)
		{
			if(nbtTags.hasKey("inputColors" + i))
			{
				int inC = nbtTags.getInteger("inputColors" + i);
				
				if(inC != -1)
				{
					inputColors[i] = TransporterUtils.colors.get(inC);
				}
				else {
					inputColors[i] = null;
				}
			}
		}
	}

	@Override
	public void read(ByteArrayDataInput dataStream) 
	{
		ejecting = dataStream.readBoolean();
		strictInput = dataStream.readBoolean();
		
		int c = dataStream.readInt();
		
		if(c != -1)
		{
			outputColor = TransporterUtils.colors.get(c);
		}
		else {
			outputColor = null;
		}
		
		for(int i = 0; i < 6; i++)
		{
			int inC = dataStream.readInt();
			
			if(inC != -1)
			{
				inputColors[i] = TransporterUtils.colors.get(inC);
			}
			else {
				inputColors[i] = null;
			}
		}
	}

	@Override
	public void write(NBTTagCompound nbtTags) 
	{
		nbtTags.setBoolean("ejecting", ejecting);
		nbtTags.setBoolean("strictInput", strictInput);
		
		if(outputColor != null)
		{
			nbtTags.setInteger("ejectColor", TransporterUtils.colors.indexOf(outputColor));
		}
		
		for(int i = 0; i < sideData.availableSlots.length; i++)
		{
			nbtTags.setInteger("tracker" + i, trackers[i]);
		}
		
		for(int i = 0; i < 6; i++)
		{
			if(inputColors[i] == null)
			{
				nbtTags.setInteger("inputColors" + i, -1);
			}
			else {
				nbtTags.setInteger("inputColors" + i, TransporterUtils.colors.indexOf(inputColors[i]));
			}
		}
	}
	
	@Override
	public void write(ArrayList data) 
	{
		data.add(ejecting);
		data.add(strictInput);
		
		if(outputColor != null)
		{
			data.add(TransporterUtils.colors.indexOf(outputColor));
		}
		else {
			data.add(-1);
		}
		
		for(int i = 0; i < 6; i++)
		{
			if(inputColors[i] == null)
			{
				data.add(-1);
			}
			else {
				data.add(TransporterUtils.colors.indexOf(inputColors[i]));
			}
		}
	}
}
