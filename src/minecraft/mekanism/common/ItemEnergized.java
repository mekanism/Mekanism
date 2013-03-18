package mekanism.common;

import ic2.api.ICustomElectricItem;

import java.util.List;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.item.IItemElectric;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.world.World;

public class ItemEnergized extends ItemMekanism implements IItemElectric, ICustomElectricItem
{
	/** The maximum amount of energy this item can hold. */
	public double MAX_ELECTRICITY;
	
	/** How fast this item can transfer energy. */
	public double VOLTAGE;
	
	public ItemEnergized(int id, double maxElectricity, double voltage)
	{
		super(id);
		MAX_ELECTRICITY = maxElectricity;
		VOLTAGE = voltage;
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		double energy = getJoules(itemstack);
		
		list.add("Stored Energy: " + ElectricityDisplay.getDisplayShort(energy, ElectricUnit.JOULES));
	}
	
	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		itemstack = getUnchargedItem();
	}
	
	public ItemStack getUnchargedItem()
	{
		ItemStack charged = new ItemStack(this);
		charged.setItemDamage(100);
		return charged;
	}
	
	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack discharged = new ItemStack(this);
		discharged.setItemDamage(100);
		list.add(discharged);
		ItemStack charged = new ItemStack(this);
		setJoules(((IItemElectric)charged.getItem()).getMaxJoules(charged), charged);
		list.add(charged);
	}

	@Override
	public double getJoules(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null) 
		{ 
			return 0; 
		}
		
		double electricityStored = 0;
		
		if (itemStack.stackTagCompound.getTag("electricity") instanceof NBTTagFloat)
		{
			electricityStored = itemStack.stackTagCompound.getFloat("electricity");
		}
		else
		{
			electricityStored = itemStack.stackTagCompound.getDouble("electricity");
		}
		
		itemStack.setItemDamage((int)(Math.abs(((electricityStored/MAX_ELECTRICITY)*100)-100)));
		return electricityStored;
	}

	@Override
	public void setJoules(double wattHours, ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(wattHours, getMaxJoules(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
		itemStack.setItemDamage((int)(Math.abs(((electricityStored/MAX_ELECTRICITY)*100)-100)));
	}

	@Override
	public double getMaxJoules(ItemStack itemStack)
	{
		return MAX_ELECTRICITY;
	}

	@Override
	public double getVoltage(ItemStack itemStack) 
	{
		return VOLTAGE;
	}

	@Override
	public ElectricityPack onReceive(ElectricityPack electricityPack, ItemStack itemStack)
	{
		double rejectedElectricity = Math.max((getJoules(itemStack) + electricityPack.getWatts()) - getMaxJoules(itemStack), 0);
		double joulesToStore = electricityPack.getWatts() - rejectedElectricity;
		this.setJoules(getJoules(itemStack) + joulesToStore, itemStack);
		return ElectricityPack.getFromWatts(joulesToStore, getVoltage(itemStack));
	}

	@Override
	public ElectricityPack onProvide(ElectricityPack electricityPack, ItemStack itemStack)
	{
		double electricityToUse = Math.min(getJoules(itemStack), electricityPack.getWatts());
		setJoules(getJoules(itemStack) - electricityToUse, itemStack);
		return ElectricityPack.getFromWatts(electricityToUse, getVoltage(itemStack));
	}

	@Override
	public ElectricityPack getReceiveRequest(ItemStack itemStack)
	{
		return ElectricityPack.getFromWatts(Math.min(getMaxJoules(itemStack) - getJoules(itemStack), getTransferRate(itemStack)), getVoltage(itemStack));
	}

	@Override
	public ElectricityPack getProvideRequest(ItemStack itemStack)
	{
		return ElectricityPack.getFromWatts(Math.min(getJoules(itemStack), getTransferRate(itemStack)), getVoltage(itemStack));
	}
	
	public double getTransferRate(ItemStack itemStack)
	{
		return getMaxJoules(itemStack)*0.01;
	}
	
	@Override
	public int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		double givenEnergy = amount*Mekanism.FROM_IC2;
		double energyNeeded = MAX_ELECTRICITY-getJoules(itemStack);
		double energyToStore = Math.min(Math.min(amount, MAX_ELECTRICITY*0.01), energyNeeded);
		
		if(!simulate)
		{
			setJoules(getJoules(itemStack) + energyToStore, itemStack);
		}
		return (int)(energyToStore*Mekanism.TO_IC2);
	}
	
	@Override
	public int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		double energyWanted = amount*Mekanism.FROM_IC2;
		double energyToGive = Math.min(Math.min(energyWanted, MAX_ELECTRICITY*0.01), getJoules(itemStack));
		
		if(!simulate)
		{
			setJoules(getJoules(itemStack) - energyToGive, itemStack);
		}
		return (int)(energyToGive*Mekanism.TO_IC2);
	}

	@Override
	public boolean canUse(ItemStack itemStack, int amount)
	{
		return getJoules(itemStack) >= amount*Mekanism.FROM_IC2;
	}
	
	@Override
	public boolean canShowChargeToolTip(ItemStack itemStack)
	{
		return false;
	}
	
	@Override
	public boolean canProvideEnergy()
	{
		return true;
	}

	@Override
	public int getChargedItemId()
	{
		return itemID;
	}

	@Override
	public int getEmptyItemId()
	{
		return itemID;
	}

	@Override
	public int getMaxCharge()
	{
		return (int)(MAX_ELECTRICITY*Mekanism.TO_IC2);
	}

	@Override
	public int getTier()
	{
		return 3;
	}

	@Override
	public int getTransferLimit()
	{
		return 0;
	}
}
