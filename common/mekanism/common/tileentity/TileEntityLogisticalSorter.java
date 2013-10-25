package mekanism.common.tileentity;

import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;

public class TileEntityLogisticalSorter extends TileEntityElectricBlock
{
	public TileEntityLogisticalSorter() 
	{
		super("LogisticalSorter", MachineType.LOGISTICAL_SORTER.baseEnergy);
		inventory = new ItemStack[1];
	}
}
