package mekanism.common;

import java.util.List;


import universalelectricity.core.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IItemElectric;

import ic2.api.IElectricItem;
import mekanism.api.IEnergizedItem;
import net.minecraft.src.*;

public class ItemEnergized extends ItemMekanism implements IEnergizedItem, IItemElectric
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
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		int energy = getEnergy(itemstack);
		
		list.add("Stored Energy: " + MekanismUtils.getDisplayedEnergy(energy));
	}
	
	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		itemstack = getUnchargedItem();
	}
	
	@Override
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
    {
    	ItemEnergized item = ((ItemEnergized)itemstack.getItem());
    	item.setEnergy(itemstack, item.getEnergy(itemstack));
    }
	
	@Override
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
	
	@Override
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
	
	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack discharged = new ItemStack(this);
		discharged.setItemDamage(maxEnergy/divider);
		list.add(discharged);
		ItemStack charged = new ItemStack(this);
		setEnergy(charged, ((IEnergizedItem)charged.getItem()).getMaxEnergy());
		list.add(charged);
	}
	
	@Override
	public int getMaxEnergy()
	{
		return maxEnergy;
	}

	@Override
	public int getRate() 
	{
		return transferRate;
	}

	@Override
	public int charge(ItemStack itemstack, int amount) 
	{
		int rejects = Math.max((getEnergy(itemstack) + amount) - maxEnergy, 0);
		setEnergy(itemstack, getEnergy(itemstack) + amount - rejects);
		return rejects;
	}

	@Override
	public int discharge(ItemStack itemstack, int amount)
	{
		int energyToUse = Math.min(getEnergy(itemstack), amount);
		setEnergy(itemstack, getEnergy(itemstack) - energyToUse);
		return energyToUse;
	}

	@Override
	public double getJoules(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			return getEnergy(itemstack)*UniversalElectricity.IC2_RATIO;
		}
		return 0;
	}

	@Override
	public void setJoules(double joules, Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
			setEnergy(itemstack, (int)(joules*UniversalElectricity.TO_IC2_RATIO));
		}
	}

	@Override
	public double getMaxJoules()
	{
		return maxEnergy*UniversalElectricity.IC2_RATIO;
	}

	@Override
	public double getVoltage() 
	{
		return 20;
	}

	@Override
	public double onReceive(double amps, double voltage, ItemStack itemStack)
	{
		int rejects = (int)Math.max((getEnergy(itemStack) + ElectricInfo.getJoules(amps, voltage, 1)*UniversalElectricity.TO_IC2_RATIO) - getMaxEnergy(), 0);
		setEnergy(itemStack, (int)(getEnergy(itemStack) + ElectricInfo.getJoules(amps, voltage, 1)*UniversalElectricity.TO_IC2_RATIO - rejects));
        return rejects*UniversalElectricity.IC2_RATIO;
	}

	@Override
	public double onUse(double joulesNeeded, ItemStack itemStack)
	{
		int energyRequest = (int)Math.min(getEnergy(itemStack), joulesNeeded*UniversalElectricity.TO_IC2_RATIO);
		setEnergy(itemStack, getEnergy(itemStack) - energyRequest);
        return energyRequest*UniversalElectricity.IC2_RATIO;
	}

	@Override
	public boolean canReceiveElectricity()
	{
		return true;
	}

	@Override
	public boolean canProduceElectricity()
	{
		return true;
	}
	
	@Override
	public int getDivider()
	{
		return divider;
	}
}
