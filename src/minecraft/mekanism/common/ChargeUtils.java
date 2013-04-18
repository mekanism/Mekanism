package mekanism.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.block.IElectricityStorage;
import universalelectricity.core.item.ElectricItemHelper;

public final class ChargeUtils
{
	/**
	 * Universally charges an item, and updates the TileEntity's energy level.
	 * @param slotID - ID of the slot of which to charge
	 * @param storer - TileEntity the item is being charged in
	 */
	public static void discharge(int slotID, TileEntityElectricBlock storer)
	{
		if(storer.inventory[slotID] != null && storer.getJoules() < storer.getMaxJoules())
		{
			storer.setJoules(storer.getJoules() + ElectricItemHelper.dechargeItem(storer.inventory[slotID], storer.getMaxJoules() - storer.getJoules(), storer.getVoltage()));
			
			if(Mekanism.hooks.IC2Loaded && storer.inventory[slotID].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)storer.inventory[slotID].getItem();
				if(item.canProvideEnergy(storer.inventory[slotID]))
				{
					double gain = ElectricItem.discharge(storer.inventory[slotID], (int)((storer.getMaxJoules() - storer.getJoules())*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
					storer.setJoules(storer.getJoules() + gain);
				}
			}
			else if(storer.inventory[slotID].itemID == Item.redstone.itemID && storer.getJoules()+1000 <= storer.getMaxJoules())
			{
				storer.setJoules(storer.getJoules() + 1000);
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
		if(storer.inventory[slotID] != null && storer.getJoules() > 0)
		{
			storer.setJoules(storer.getJoules() - ElectricItemHelper.chargeItem(storer.inventory[slotID], storer.getJoules(), storer.getVoltage()));
			
			if(Mekanism.hooks.IC2Loaded && storer.inventory[slotID].getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.charge(storer.inventory[slotID], (int)(storer.getJoules()*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
				storer.setJoules(storer.getJoules() - sent);
			}
		}
	}
}
