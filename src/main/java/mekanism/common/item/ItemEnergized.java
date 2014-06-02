package mekanism.common.item;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.util.MekanismUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cofh.api.energy.IEnergyContainerItem;

public class ItemEnergized extends ItemMekanism implements IEnergizedItem, ISpecialElectricItem, IEnergyContainerItem
{
	/** The maximum amount of energy this item can hold. */
	public double MAX_ELECTRICITY;

	public ItemEnergized(double maxElectricity)
	{
		super();
		MAX_ELECTRICITY = maxElectricity;
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(EnumColor.AQUA + "Stored Energy: " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
	}

	public ItemStack getUnchargedItem()
	{
		ItemStack charged = new ItemStack(this);
		charged.setItemDamage(100);
		return charged;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List list)
	{
		ItemStack discharged = new ItemStack(this);
		discharged.setItemDamage(100);
		list.add(discharged);
		ItemStack charged = new ItemStack(this);
		setEnergy(charged, ((IEnergizedItem)charged.getItem()).getMaxEnergy(charged));
		list.add(charged);
	}

	@Override
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return canSend(itemStack);
	}

	@Override
	public Item getChargedItem(ItemStack itemStack)
	{
		return this;
	}

	@Override
	public Item getEmptyItem(ItemStack itemStack)
	{
		return this;
	}

	@Override
	public int getMaxCharge(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public int getTier(ItemStack itemStack)
	{
		return 4;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public double getEnergy(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0;
		}

		double electricityStored = itemStack.stackTagCompound.getDouble("electricity");
		itemStack.setItemDamage((int)Math.max(1, (Math.abs(((electricityStored/getMaxEnergy(itemStack))*100)-100))));

		return electricityStored;
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
		itemStack.setItemDamage((int)Math.max(1, (Math.abs(((electricityStored/getMaxEnergy(itemStack))*100)-100))));
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack)
	{
		return MAX_ELECTRICITY;
	}

	@Override
	public double getMaxTransfer(ItemStack itemStack)
	{
		return getMaxEnergy(itemStack)*0.005;
	}

	@Override
	public boolean canReceive(ItemStack itemStack)
	{
		return getMaxEnergy(itemStack)-getEnergy(itemStack) > 0;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return getEnergy(itemStack) > 0;
	}

	@Override
	public int receiveEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canReceive(theItem))
		{
			double energyNeeded = getMaxEnergy(theItem)-getEnergy(theItem);
			double toReceive = Math.min(energy*Mekanism.FROM_TE, energyNeeded);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) + toReceive);
			}

			return (int)Math.round(toReceive*Mekanism.TO_TE);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canSend(theItem))
		{
			double energyRemaining = getEnergy(theItem);
			double toSend = Math.min((energy*Mekanism.FROM_TE), energyRemaining);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) - toSend);
			}

			return (int)Math.round(toSend*Mekanism.TO_TE);
		}

		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int)Math.round(getEnergy(theItem)*Mekanism.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)Math.round(getMaxEnergy(theItem)*Mekanism.TO_TE);
	}

	@Override
	public boolean isMetadataSpecific()
	{
		return false;
	}

	@Override
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ItemManager.getManager(this);
	}
}
