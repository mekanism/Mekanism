package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.security.IOwnerItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySecurityDesk extends TileEntityContainerBlock implements IBoundingBlock
{
	public String owner;
	
	public SecurityFrequency frequency;
	
	public TileEntitySecurityDesk()
	{
		super("SecurityDesk");
		
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		if(!worldObj.isRemote)
		{
			if(owner != null && frequency != null)
			{
				if(inventory[0] != null && inventory[0].getItem() instanceof IOwnerItem)
				{
					IOwnerItem item = (IOwnerItem)inventory[0].getItem();
					
					if(item.hasOwner(inventory[0]) && item.getOwner(inventory[0]) != null)
					{
						if(item.getOwner(inventory[0]).equals(owner))
						{
							item.setOwner(inventory[0], null);
							
							if(item instanceof ISecurityItem && ((ISecurityItem)item).hasSecurity(inventory[0]))
							{
								((ISecurityItem)item).setSecurity(inventory[0], SecurityMode.PUBLIC);
							}
						}
					}
				}
				
				if(inventory[1] != null && inventory[1].getItem() instanceof IOwnerItem)
				{
					IOwnerItem item = (IOwnerItem)inventory[1].getItem();
					
					if(item.hasOwner(inventory[1]))
					{
						if(item.getOwner(inventory[1]) == null)
						{
							item.setOwner(inventory[1], owner);
						}
						
						if(item.getOwner(inventory[1]).equals(owner))
						{
							if(item instanceof ISecurityItem && ((ISecurityItem)item).hasSecurity(inventory[1]))
							{
								((ISecurityItem)item).setSecurity(inventory[1], frequency.securityMode);
							}
						}
					}
				}
			}
			
			if(frequency == null && owner != null)
			{
				setFrequency(owner);
			}
			
			FrequencyManager manager = getManager(frequency);
			
			if(manager != null)
			{
				if(frequency != null && !frequency.valid)
				{
					frequency = (SecurityFrequency)manager.validateFrequency(owner, Coord4D.get(this), frequency);
				}
				
				if(frequency != null)
				{
					frequency = (SecurityFrequency)manager.update(owner, Coord4D.get(this), frequency);
				}
			}
			else {
				frequency = null;
			}
		}
	}
	
	public FrequencyManager getManager(Frequency freq)
	{
		if(owner == null || freq == null)
		{
			return null;
		}
		
		return Mekanism.securityFrequencies;
	}
	
	public void setFrequency(String owner)
	{
		FrequencyManager manager = Mekanism.securityFrequencies;
		manager.deactivate(Coord4D.get(this));
		
		for(Frequency freq : manager.getFrequencies())
		{
			if(freq.owner.equals(owner))
			{
				frequency = (SecurityFrequency)freq;
				frequency.activeCoords.add(Coord4D.get(this));
				
				return;
			}
		}
		
		Frequency freq = new SecurityFrequency(owner).setPublic(true);
		freq.activeCoords.add(Coord4D.get(this));
		manager.addFrequency(freq);
		frequency = (SecurityFrequency)freq;
		
		MekanismUtils.saveChunk(this);
		markDirty();
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				if(frequency != null)
				{
					frequency.trusted.add(PacketHandler.readString(dataStream));
				}
			}
			else if(type == 1)
			{
				if(frequency != null)
				{
					frequency.trusted.remove(PacketHandler.readString(dataStream));
				}
			}
			else if(type == 2)
			{
				if(frequency != null)
				{
					frequency.override = !frequency.override;
				}
			}
			else if(type == 3)
			{
				if(frequency != null)
				{
					frequency.securityMode = SecurityMode.values()[dataStream.readInt()];
				}
			}
			
			MekanismUtils.saveChunk(this);
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			owner = PacketHandler.readString(dataStream);
		}
		else {
			owner = null;
		}
		
		if(dataStream.readBoolean())
		{
			frequency = new SecurityFrequency(dataStream);
		}
		else {
			frequency = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		if(nbtTags.hasKey("owner"))
		{
			owner = nbtTags.getString("owner");
		}
		
		if(nbtTags.hasKey("frequency"))
		{
			frequency = new SecurityFrequency(nbtTags.getCompoundTag("frequency"));
			frequency.valid = false;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		if(owner != null)
		{
			nbtTags.setString("owner", owner);
		}
		
		if(frequency != null)
		{
			NBTTagCompound frequencyTag = new NBTTagCompound();
			frequency.write(frequencyTag);
			nbtTags.setTag("frequency", frequencyTag);
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);
		
		if(owner != null)
		{
			data.add(true);
			data.add(owner);
		}
		else {
			data.add(false);
		}
		
		if(frequency != null)
		{
			data.add(true);
			frequency.write(data);
		}
		else {
			data.add(false);
		}

		return data;
	}
	
	@Override
	public void invalidate()
	{
		if(!worldObj.isRemote)
		{
			if(frequency != null)
			{
				FrequencyManager manager = getManager(frequency);
				
				if(manager != null)
				{
					manager.deactivate(Coord4D.get(this));
				}
			}
		}
	}
	
	@Override
	public void onPlace() 
	{
		MekanismUtils.makeBoundingBlock(worldObj, getPos().up(), Coord4D.get(this));
	}

	@Override
	public void onBreak() 
	{
		worldObj.setBlockToAir(getPos().up());
		worldObj.setBlockToAir(getPos());
	}
	
	@Override
	public Frequency getFrequency(FrequencyManager manager)
	{
		if(manager == Mekanism.securityFrequencies)
		{
			return frequency;
		}
		
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
