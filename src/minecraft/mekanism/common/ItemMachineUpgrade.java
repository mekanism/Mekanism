package mekanism.common;

import java.util.List;

import universalelectricity.prefab.modifier.IModifier;

import mekanism.api.IMachineUpgrade;
import mekanism.api.TabProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMachineUpgrade extends Item implements IMachineUpgrade, IModifier
{
	public int ENERGY_BOOST;
	public int TICK_REDUCTION;
	
	public ItemMachineUpgrade(int id, int energyBoost, int tickReduction)
	{
		super(id);
		setMaxStackSize(1);
		setCreativeTab(TabProxy.tabMekanism(CreativeTabs.tabMisc));
		ENERGY_BOOST = energyBoost;
		TICK_REDUCTION = tickReduction;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add("Energy Boost: " + ENERGY_BOOST);
		list.add("Tick Reduction: " + TICK_REDUCTION);
	}
	
	@Override
	public String getTextureFile()
	{
		return "/resources/mekanism/textures/items.png";
	}

	@Override
	public int getEnergyBoost(ItemStack itemstack) 
	{
		return ENERGY_BOOST;
	}

	@Override
	public int getTickReduction(ItemStack itemstack) 
	{
		return TICK_REDUCTION;
	}

	@Override
	public String getName(ItemStack itemstack)
	{
		return shiftedIndex == Mekanism.SpeedUpgrade.shiftedIndex ? "Speed" :
			(shiftedIndex == Mekanism.EnergyUpgrade.shiftedIndex ? "Capacity" : "All");
	}

	@Override
	public int getEffectiveness(ItemStack itemstack)
	{
		return shiftedIndex == Mekanism.SpeedUpgrade.shiftedIndex ? 150 :
			(shiftedIndex == Mekanism.EnergyUpgrade.shiftedIndex ? 1000 : 2500);
	}
}
