package mekanism.common;

import universalelectricity.core.electricity.ElectricityPack;
import mekanism.api.EnumColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemPortableTeleporter extends ItemEnergized
{
	public ItemPortableTeleporter(int id)
	{
		super(id, 1000000, 120);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		entityplayer.openGui(Mekanism.instance, 14, world, 0, 0, 0);
		return itemstack;
	}
	
	public int calculateEnergyCost(Entity entity, Teleporter.Coords coords)
	{
		if(coords == null)
		{
			return 0;
		}
		
		int neededEnergy = 1000;
		
		if(entity.worldObj.provider.dimensionId != coords.dimensionId)
		{
			neededEnergy+=10000;
		}
		
		int distance = (int)entity.getDistance(coords.xCoord, coords.yCoord, coords.zCoord);
		
		neededEnergy+=(distance*10);
		
		return neededEnergy;
	}
	
	public String getStatusAsString(int i)
	{
		switch(i)
		{
			case 0:
				return EnumColor.DARK_RED + "Not ready.";
			case 1:
				return EnumColor.DARK_GREEN + "Ready.";
			case 2:
				return EnumColor.DARK_RED + "Needs energy.";
			case 3:
				return EnumColor.DARK_RED + "Links > 2.";
			case 4:
				return EnumColor.DARK_RED + "No link found.";
			default:
				return EnumColor.DARK_RED + "Not ready.";
		}
	}
	
	public int getStatus(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null) 
		{ 
			return 0;
		}
		
		return itemstack.stackTagCompound.getInteger("status");
	}
	
	public void setStatus(ItemStack itemstack, int status)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setInteger("status", status);
	}
	
	public int getDigit(ItemStack itemstack, int index)
	{
		if(itemstack.stackTagCompound == null) 
		{ 
			return 0; 
		}
		
		return itemstack.stackTagCompound.getInteger("digit"+index);
	}
	
	public void setDigit(ItemStack itemstack, int index, int digit)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setInteger("digit"+index, digit);
	}
	
	@Override
	public ElectricityPack getProvideRequest(ItemStack itemStack)
	{
		return new ElectricityPack();
	}
	
	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
}
