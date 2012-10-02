package net.uberkat.obsidian.common;

import java.util.List;

import universalelectricity.UniversalElectricity;
import universalelectricity.implement.IItemElectric;

import ic2.api.IElectricItem;
import net.minecraft.src.*;
import net.uberkat.obsidian.api.IEnergizedItem;

public class ItemEnergized extends Item implements IEnergizedItem, IItemElectric
{
	public int maxEnergy;
	
	public int transferRate;
	
	public ItemEnergized(int id, int energy, int rate)
	{
		super(id);
		maxEnergy = energy;
		transferRate = rate;
		setMaxStackSize(1);
		setMaxDamage(maxEnergy);
		setNoRepair();
		setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	public void addInformation(ItemStack itemstack, List list)
	{
		int energy = getEnergy(itemstack);
		
		list.add("Stored Energy: " + ObsidianUtils.getDisplayedEnergy(energy));
	}
	
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		itemstack = getUnchargedItem();
	}
	
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
    	ItemEnergized item = ((ItemEnergized)par1ItemStack.getItem());
    	item.setEnergy(par1ItemStack, item.getEnergy(par1ItemStack));
    }
	
	public int getEnergy(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return 0;
		}
		
		int stored = 0;
		
		if(itemstack.stackTagCompound.getTag("energy") != null)
		{
			stored = itemstack.stackTagCompound.getInteger("energy");
		}
		
		itemstack.setItemDamage(maxEnergy - stored);
		return stored;
	}
	
	public void setEnergy(ItemStack itemstack, int energy)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}
		
		int stored = Math.max(Math.min(energy, maxEnergy), 0);
		itemstack.stackTagCompound.setInteger("energy", stored);
        itemstack.setItemDamage(maxEnergy - stored);
	}
	
	public ItemStack getUnchargedItem()
	{
		ItemStack charged = new ItemStack(this);
		charged.setItemDamage(maxEnergy);
		return charged;
	}
	
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack discharged = new ItemStack(this);
		discharged.setItemDamage(maxEnergy);
		list.add(discharged);
		ItemStack charged = new ItemStack(this);
		setEnergy(charged, ((IEnergizedItem)charged.getItem()).getMaxEnergy());
		list.add(charged);
	}
	
	public int getMaxEnergy()
	{
		return maxEnergy;
	}

	public int getRate() 
	{
		return transferRate;
	}

	public int charge(ItemStack itemstack, int amount) 
	{
		int rejects = Math.max((getEnergy(itemstack) + amount) - maxEnergy, 0);
		setEnergy(itemstack, getEnergy(itemstack) + amount - rejects);
		return rejects;
	}

	public int discharge(ItemStack itemstack, int amount)
	{
		int energyToUse = Math.min(getEnergy(itemstack), amount);
		setEnergy(itemstack, getEnergy(itemstack) - energyToUse);
		return energyToUse;
	}

	public double getWattHours(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			return getEnergy(itemstack)*UniversalElectricity.IC2_RATIO;
		}
		return 0;
	}

	public void setWattHours(double wattHours, Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
			setEnergy(itemstack, (int)(wattHours*UniversalElectricity.Wh_IC2_RATIO));
		}
	}

	public double getMaxWattHours()
	{
		return maxEnergy*UniversalElectricity.IC2_RATIO;
	}

	public double getVoltage() 
	{
		return 20;
	}

	public double onReceiveElectricity(double wattHourReceive, ItemStack itemStack)
	{
		int rejects = (int)Math.max((getEnergy(itemStack) + wattHourReceive*UniversalElectricity.Wh_IC2_RATIO) - getMaxEnergy(), 0);
		setEnergy(itemStack, (int)(getEnergy(itemStack) + wattHourReceive*UniversalElectricity.Wh_IC2_RATIO - rejects));
        return rejects*UniversalElectricity.IC2_RATIO;
	}

	public double onUseElectricity(double wattHourRequest, ItemStack itemStack)
	{
		int energyRequest = (int)Math.min(getEnergy(itemStack), wattHourRequest*UniversalElectricity.Wh_IC2_RATIO);
		setEnergy(itemStack, getEnergy(itemStack) - energyRequest);
        return energyRequest*UniversalElectricity.IC2_RATIO;
	}

	public boolean canReceiveElectricity()
	{
		return true;
	}

	public boolean canProduceElectricity()
	{
		return true;
	}

	public double getTransferRate() 
	{
		return transferRate*UniversalElectricity.IC2_RATIO;
	}
	
	public String getTextureFile()
	{
		return "/obsidian/items.png";
	}
	
	public boolean canCharge()
	{
		return true;
	}
}
