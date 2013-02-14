package mekanism.common;

import java.util.List;

import universalelectricity.prefab.modifier.IModifier;

import mekanism.api.TabProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMachineUpgrade extends Item implements IModifier
{
	public ItemMachineUpgrade(int id, int energyBoost, int tickReduction)
	{
		super(id);
		setMaxStackSize(8);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public String getTextureFile()
	{
		return "/resources/mekanism/textures/items.png";
	}

	@Override
	public String getName(ItemStack itemstack)
	{
		return itemID == Mekanism.SpeedUpgrade.itemID ? "Speed" : "Capacity";
	}

	@Override
	public int getEffectiveness(ItemStack itemstack)
	{
		return itemID == Mekanism.SpeedUpgrade.itemID ? 150 : 1000;
	}
}
