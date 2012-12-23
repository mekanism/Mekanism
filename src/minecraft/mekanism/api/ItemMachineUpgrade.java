package mekanism.api;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMachineUpgrade extends Item implements IMachineUpgrade
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
}
