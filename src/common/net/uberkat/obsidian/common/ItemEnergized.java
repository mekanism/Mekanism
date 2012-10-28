package net.uberkat.obsidian.common;

import java.util.List;

import obsidian.api.IEnergizedItem;

import universalelectricity.core.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IItemElectric;

import ic2.api.IElectricItem;
import net.minecraft.src.*;

public class ItemEnergized extends ItemObsidian implements IEnergizedItem, IItemElectric
{
	public int maxEnergy;
	
	public int transferRate;
	
	public int divider;
	
	public ItemEnergized(int id, int energy, int rate, int divide)
	{
		super(id);
		divider = divide;
		maxEnergy = energy;
		transferRate = rate;
		setMaxStackSize(1);
		setMaxDamage(maxEnergy/divider);
		setNoRepair();
		setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		int energy = getEnergy(itemstack);
		
		list.add("Stored Energy: " + ObsidianUtils.getDisplayedEnergy(energy));
	}
	
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		itemstack = getUnchargedItem();
	}
	
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
    {
    	ItemEnergized item = ((ItemEnergized)itemstack.getItem());
    	item.setEnergy(itemstack, item.getEnergy(itemstack));
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
		
		itemstack.setItemDamage((maxEnergy - stored)/divider);
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
        itemstack.setItemDamage((maxEnergy - stored)/divider);
	}
	
	public ItemStack getUnchargedItem()
	{
		ItemStack charged = new ItemStack(this);
		charged.setItemDamage(maxEnergy/divider);
		return charged;
	}
	
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack discharged = new ItemStack(this);
		discharged.setItemDamage(maxEnergy/divider);
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

	public double getJoules(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			return getEnergy(itemstack)*UniversalElectricity.IC2_RATIO;
		}
		return 0;
	}

	public void setJoules(double joules, Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
			setEnergy(itemstack, (int)(joules*UniversalElectricity.TO_IC2_RATIO));
		}
	}

	public double getMaxJoules()
	{
		return maxEnergy*UniversalElectricity.IC2_RATIO;
	}

	public double getVoltage() 
	{
		return 20;
	}

	public double onReceive(double amps, double voltage, ItemStack itemStack)
	{
		int rejects = (int)Math.max((getEnergy(itemStack) + ElectricInfo.getJoules(amps, voltage, 1)*UniversalElectricity.TO_IC2_RATIO) - getMaxEnergy(), 0);
		setEnergy(itemStack, (int)(getEnergy(itemStack) + ElectricInfo.getJoules(amps, voltage, 1)*UniversalElectricity.TO_IC2_RATIO - rejects));
        return rejects*UniversalElectricity.IC2_RATIO;
	}

	public double onUse(double joulesNeeded, ItemStack itemStack)
	{
		int energyRequest = (int)Math.min(getEnergy(itemStack), joulesNeeded*UniversalElectricity.TO_IC2_RATIO);
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
	
	public int getDivider()
	{
		return divider;
	}
}
