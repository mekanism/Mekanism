package mekanism.common.util;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityContainerBlock;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cofh.api.energy.IEnergyContainerItem;

public final class ChargeUtils
{
	/**
	 * Universally discharges an item, and updates the TileEntity's energy level.
	 * @param slotID - ID of the slot of which to charge
	 * @param storer - TileEntity the item is being charged in
	 */
	public static void discharge(int slotID, IStrictEnergyStorage storer)
	{
		IInventory inv = (TileEntityContainerBlock)storer;
		
		if(inv.getStackInSlot(slotID) != null && storer.getEnergy() < storer.getMaxEnergy())
		{
			if(inv.getStackInSlot(slotID).getItem() instanceof IEnergizedItem)
			{
				storer.setEnergy(storer.getEnergy() + EnergizedItemManager.discharge(inv.getStackInSlot(slotID), storer.getMaxEnergy() - storer.getEnergy()));
			}
			else if(MekanismUtils.useIC2() && inv.getStackInSlot(slotID).getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)inv.getStackInSlot(slotID).getItem();

				if(item.canProvideEnergy(inv.getStackInSlot(slotID)))
				{
					double gain = ElectricItem.manager.discharge(inv.getStackInSlot(slotID), (int)((storer.getMaxEnergy() - storer.getEnergy())* general.TO_IC2), 4, true, true, false)* general.FROM_IC2;
					storer.setEnergy(storer.getEnergy() + gain);
				}
			}
			else if(MekanismUtils.useRF() && inv.getStackInSlot(slotID).getItem() instanceof IEnergyContainerItem)
			{
				ItemStack itemStack = inv.getStackInSlot(slotID);
				IEnergyContainerItem item = (IEnergyContainerItem)inv.getStackInSlot(slotID).getItem();

				int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getEnergyStored(itemStack)));
				int toTransfer = (int)Math.round(Math.min(itemEnergy, ((storer.getMaxEnergy() - storer.getEnergy())* general.TO_TE)));

				storer.setEnergy(storer.getEnergy() + (item.extractEnergy(itemStack, toTransfer, false)* general.FROM_TE));
			}
			else if(inv.getStackInSlot(slotID).getItem() == Items.redstone && storer.getEnergy()+ general.ENERGY_PER_REDSTONE <= storer.getMaxEnergy())
			{
				storer.setEnergy(storer.getEnergy() + general.ENERGY_PER_REDSTONE);
				inv.getStackInSlot(slotID).stackSize--;

				if(inv.getStackInSlot(slotID).stackSize <= 0)
				{
					inv.setInventorySlotContents(slotID, null);
				}
			}
		}
	}

	/**
	 * Universally charges an item, and updates the TileEntity's energy level.
	 * @param slotID - ID of the slot of which to discharge
	 * @param storer - TileEntity the item is being discharged in
	 */
	public static void charge(int slotID, IStrictEnergyStorage storer)
	{
		IInventory inv = (TileEntityContainerBlock)storer;
		
		if(inv.getStackInSlot(slotID) != null && storer.getEnergy() > 0)
		{
			if(inv.getStackInSlot(slotID).getItem() instanceof IEnergizedItem)
			{
				storer.setEnergy(storer.getEnergy() - EnergizedItemManager.charge(inv.getStackInSlot(slotID), storer.getEnergy()));
			}
			else if(MekanismUtils.useIC2() && inv.getStackInSlot(slotID).getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.manager.charge(inv.getStackInSlot(slotID), (int)(storer.getEnergy()* general.TO_IC2), 4, true, false)* general.FROM_IC2;
				storer.setEnergy(storer.getEnergy() - sent);
			}
			else if(MekanismUtils.useRF() && inv.getStackInSlot(slotID).getItem() instanceof IEnergyContainerItem)
			{
				ItemStack itemStack = inv.getStackInSlot(slotID);
				IEnergyContainerItem item = (IEnergyContainerItem)inv.getStackInSlot(slotID).getItem();

				int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemStack)), item.getMaxEnergyStored(itemStack) - item.getEnergyStored(itemStack)));
				int toTransfer = (int)Math.round(Math.min(itemEnergy, (storer.getEnergy()* general.TO_TE)));

				storer.setEnergy(storer.getEnergy() - (item.receiveEnergy(itemStack, toTransfer, false)* general.FROM_TE));
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
		return true; //this is too much of a hassle to manage
	}
}
