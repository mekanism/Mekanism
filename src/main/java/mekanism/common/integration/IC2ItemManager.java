package mekanism.common.integration;

import ic2.api.item.IElectricItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class IC2ItemManager implements IElectricItemManager
{
	public IEnergizedItem energizedItem;

	public static IC2ItemManager getManager(IEnergizedItem item)
	{
		IC2ItemManager manager = new IC2ItemManager();
		manager.energizedItem = item;

		return manager;
	}

	@Override
	public double charge(ItemStack itemStack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if(energizedItem.canReceive(itemStack))
		{
			double energyNeeded = energizedItem.getMaxEnergy(itemStack)-energizedItem.getEnergy(itemStack);
			double energyToStore = Math.min(Math.min(amount*Mekanism.FROM_IC2, energizedItem.getMaxEnergy(itemStack)*0.01), energyNeeded);

			if(!simulate)
			{
				energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) + energyToStore);
			}

			return (int)Math.round(energyToStore*Mekanism.TO_IC2);
		}

		return 0;
	}

	@Override
	public double discharge(ItemStack itemStack, double amount, int tier, boolean ignoreTransferLimit, boolean external, boolean simulate)
	{
		if(energizedItem.canSend(itemStack))
		{
			double energyWanted = amount*Mekanism.FROM_IC2;
			double energyToGive = Math.min(Math.min(energyWanted, energizedItem.getMaxEnergy(itemStack)*0.01), energizedItem.getEnergy(itemStack));

			if(!simulate)
			{
				energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) - energyToGive);
			}

			return (int)Math.round(energyToGive*Mekanism.TO_IC2);
		}

		return 0;
	}

	@Override
	public boolean canUse(ItemStack itemStack, double amount)
	{
		return energizedItem.getEnergy(itemStack) >= amount*Mekanism.FROM_IC2;
	}

	@Override
	public double getCharge(ItemStack itemStack)
	{
		return (int)Math.round(energizedItem.getEnergy(itemStack)*Mekanism.TO_IC2);
	}

	@Override
	public boolean use(ItemStack itemStack, double amount, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public void chargeFromArmor(ItemStack itemStack, EntityLivingBase entity)
	{

	}

	@Override
	public String getToolTip(ItemStack itemStack)
	{
		return null;
	}
}
