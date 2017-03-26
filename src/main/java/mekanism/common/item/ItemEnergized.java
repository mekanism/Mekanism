package mekanism.common.item;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.integration.TeslaItemWrapper;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import cofh.api.energy.IEnergyContainerItem;

@InterfaceList({
	@Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = "IC2")
})
public class ItemEnergized extends ItemMekanism implements IEnergizedItem, ISpecialElectricItem, IEnergyContainerItem
{
	/** The maximum amount of energy this item can hold. */
	public double MAX_ELECTRICITY;

	public ItemEnergized(double maxElectricity)
	{
		super();
		MAX_ELECTRICITY = maxElectricity;
		setMaxStackSize(1);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return 1D-(getEnergy(stack)/getMaxEnergy(stack));
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag)
	{
		list.add(EnumColor.AQUA + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
	}

	public ItemStack getUnchargedItem()
	{
		return new ItemStack(this);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, NonNullList<ItemStack> list)
	{
		ItemStack discharged = new ItemStack(this);
		list.add(discharged);
		ItemStack charged = new ItemStack(this);
		setEnergy(charged, ((IEnergizedItem)charged.getItem()).getMaxEnergy(charged));
		list.add(charged);
	}

	@Override
	public double getEnergy(ItemStack itemStack)
	{
		return ItemDataUtils.getDouble(itemStack, "energyStored");
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
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
			double toReceive = Math.min(energy*general.FROM_RF, energyNeeded);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) + toReceive);
			}

			return (int)Math.round(toReceive*general.TO_RF);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canSend(theItem))
		{
			double energyRemaining = getEnergy(theItem);
			double toSend = Math.min((energy*general.FROM_RF), energyRemaining);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) - toSend);
			}

			return (int)Math.round(toSend*general.TO_RF);
		}

		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int)Math.round(getEnergy(theItem)*general.TO_RF);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)Math.round(getMaxEnergy(theItem)*general.TO_RF);
	}

	@Override
	@Method(modid = "IC2")
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ItemManager.getManager(this);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) 
	{
		return new ItemCapabilityWrapper(stack, new TeslaItemWrapper());
	}
}
