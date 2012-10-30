package net.uberkat.obsidian.common;

import java.util.List;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemStack;
import obsidian.api.IMachineUpgrade;

public class ItemMachineUpgrade extends ItemObsidian implements IMachineUpgrade
{
	public ItemMachineUpgrade(int i)
	{
		super(i);
		setMaxStackSize(1);
		setCreativeTab(ObsidianIngots.tabOBSIDIAN);
	}
}
