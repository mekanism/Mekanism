package mekanism.common.util;

import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityElectricBlock;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cofh.api.energy.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

public final class ChargeUtils
{
	/**
	 * Universally discharges an item, and updates the TileEntity's energy level.
	 * @param slotID - ID of the slot of which to charge
	 * @param storer - TileEntity the item is being charged in
	 */
	public static void discharge(int slotID, TileEntityElectricBlock storer)
	{
		if(storer.inventory[slotID] != null && storer.getEnergy() < storer.getMaxEnergy())
		{
			if(storer.inventory[slotID].getItem() instanceof IEnergizedItem)
			{
				storer.setEnergy(storer.getEnergy() + EnergizedItemManager.discharge(storer.inventory[slotID], storer.getMaxEnergy() - storer.getEnergy()));
			}
			else if(MekanismUtils.useIC2() && storer.inventory[slotID].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)storer.inventory[slotID].getItem();

				if(item.canProvideEnergy(storer.inventory[slotID]))
				{
					double gain = ElectricItem.manager.discharge(storer.inventory[slotID], (int)((storer.getMaxEnergy() - storer.getEnergy())*Mekanism.TO_IC2), 4, true, true, false)*Mekanism.FROM_IC2;
					storer.setEnergy(storer.getEnergy() + gain);
				}
			}
			else if(MekanismUtils.useRF() && storer.inventory[slotID].getItem() instanceof IEnergyContainerItem)
			{
				ItemStack itemStack = storer.inventory[slotID];
				IEnergyContainerItem item = (IEnergyContainerItem)storer.inventory[slotID].getItem();

				int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getEnergyStored(itemStack)));
				int toTransfer = (int)Math.round(Math.min(itemEnergy, ((storer.getMaxEnergy() - storer.getEnergy())*Mekanism.TO_TE)));

				storer.setEnergy(storer.getEnergy() + (item.extractEnergy(itemStack, toTransfer, false)*Mekanism.FROM_TE));
			}
			else if(storer.inventory[slotID].getItem() == Items.redstone && storer.getEnergy()+Mekanism.ENERGY_PER_REDSTONE <= storer.getMaxEnergy())
			{
				storer.setEnergy(storer.getEnergy() + Mekanism.ENERGY_PER_REDSTONE);
				storer.inventory[slotID].stackSize--;

				if(storer.inventory[slotID].stackSize <= 0)
				{
					storer.inventory[slotID] = null;
				}
			}
		}
	}

	/**
	 * Universally charges an item, and updates the TileEntity's energy level.
	 * @param slotID - ID of the slot of which to discharge
	 * @param storer - TileEntity the item is being discharged in
	 */
	public static void charge(int slotID, TileEntityElectricBlock storer)
	{
		if(storer.inventory[slotID] != null && storer.getEnergy() > 0)
		{
			if(storer.inventory[slotID].getItem() instanceof IEnergizedItem)
			{
				storer.setEnergy(storer.getEnergy() - EnergizedItemManager.charge(storer.inventory[slotID], storer.getEnergy()));
			}
			else if(Mekanism.hooks.IC2APILoaded && storer.inventory[slotID].getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.manager.charge(storer.inventory[slotID], (int)(storer.getEnergy()*Mekanism.TO_IC2), 4, true, false)*Mekanism.FROM_IC2;
				storer.setEnergy(storer.getEnergy() - sent);
			}
			else if(MekanismUtils.useRF() && storer.inventory[slotID].getItem() instanceof IEnergyContainerItem)
			{
				ItemStack itemStack = storer.inventory[slotID];
				IEnergyContainerItem item = (IEnergyContainerItem)storer.inventory[slotID].getItem();

				int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getMaxEnergyStored(itemStack) - item.getEnergyStored(itemStack)));
				int toTransfer = (int)Math.round(Math.min(itemEnergy, (storer.getEnergy()*Mekanism.TO_TE)));

				storer.setEnergy(storer.getEnergy() - (item.receiveEnergy(itemStack, toTransfer, false)*Mekanism.FROM_TE));
			}
		}
	}

	/**
	 * Whether or not a defined ItemStack can be discharged for energy in some way.
	 * @param itemstack - ItemStack to check
	 * @return if the ItemStack can be discharged
	 */
	public static boolean canBeDischarged(ItemStack itemstack)
	{
		return (MekanismUtils.useIC2() && itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) ||
				(itemstack.getItem() instanceof IEnergizedItem && ((IEnergizedItem)itemstack.getItem()).canSend(itemstack)) ||
				(MekanismUtils.useRF() && itemstack.getItem() instanceof IEnergyContainerItem && ((IEnergyContainerItem)itemstack.getItem()).extractEnergy(itemstack, 1, true) != 0) ||
				itemstack.getItem() == Items.redstone;
	}

	/**
	 * Whether or not a defined ItemStack can be charged with energy in some way.
	 * @param itemstack - ItemStack to check
	 * @return if the ItemStack can be discharged
	 */
	public static boolean canBeCharged(ItemStack itemstack)
	{
		return (MekanismUtils.useIC2() && itemstack.getItem() instanceof IElectricItem) ||
				(itemstack.getItem() instanceof IEnergizedItem && ((IEnergizedItem)itemstack.getItem()).canReceive(itemstack)) ||
				(MekanismUtils.useRF() && itemstack.getItem() instanceof IEnergyContainerItem && ((IEnergyContainerItem)itemstack.getItem()).receiveEnergy(itemstack, 1, true) != 0);
	}

	/**
	 * Whether or not a defined deemed-electrical ItemStack can be outputted out of a slot.
	 * This puts into account whether or not that slot is used for charging or discharging.
	 * @param itemstack - ItemStack to perform the check on
	 * @param chargeSlot - whether or not the outputting slot is for charging or discharging
	 * @return if the ItemStack can be outputted
	 */
	public static boolean canBeOutputted(ItemStack itemstack, boolean chargeSlot)
	{
		if(chargeSlot)
		{
			return MekanismUtils.useIC2() && itemstack.getItem() instanceof IElectricItem;
		}
		else {
			return MekanismUtils.useIC2() && itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack);
		}
	}
}
