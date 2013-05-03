package mekanism.common;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import mekanism.api.EnergizedItemManager;
import mekanism.api.IEnergizedItem;
import net.minecraft.item.Item;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

public final class ChargeUtils
{
	/**
	 * Universally charges an item, and updates the TileEntity's energy level.
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
			else if(storer.inventory[slotID].getItem() instanceof IItemElectric)
			{
				storer.setEnergy(storer.getEnergy() + ElectricItemHelper.dechargeItem(storer.inventory[slotID], storer.getMaxEnergy() - storer.getEnergy(), storer.getVoltage()));
			}
			else if(Mekanism.hooks.IC2Loaded && storer.inventory[slotID].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)storer.inventory[slotID].getItem();
				if(item.canProvideEnergy(storer.inventory[slotID]))
				{
					double gain = ElectricItem.discharge(storer.inventory[slotID], (int)((storer.getMaxEnergy() - storer.getEnergy())*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
					storer.setEnergy(storer.getEnergy() + gain);
				}
			}
			else if(storer.inventory[slotID].itemID == Item.redstone.itemID && storer.getEnergy()+1000 <= storer.getMaxEnergy())
			{
				storer.setEnergy(storer.getEnergy() + 1000);
				storer.inventory[slotID].stackSize--;
				
	            if(storer.inventory[slotID].stackSize <= 0)
	            {
	                storer.inventory[slotID] = null;
	            }
			}
		}
	}
	
	/**
	 * Universally discharges an item, and updates the TileEntity's energy level.
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
			else if(storer.inventory[slotID].getItem() instanceof IItemElectric)
			{
				storer.setEnergy(storer.getEnergy() - ElectricItemHelper.chargeItem(storer.inventory[slotID], storer.getEnergy(), storer.getVoltage()));
			}
			else if(Mekanism.hooks.IC2Loaded && storer.inventory[slotID].getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.charge(storer.inventory[slotID], (int)(storer.getEnergy()*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
				storer.setEnergy(storer.getEnergy() - sent);
			}
		}
	}
}
