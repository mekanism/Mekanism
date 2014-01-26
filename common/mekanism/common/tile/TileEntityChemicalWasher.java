package mekanism.common.tile;

import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalWasher extends TileEntityElectricBlock
{
	public TileEntityChemicalWasher()
	{
		super("ChemicalWasher", MachineType.CHEMICAL_WASHER.baseEnergy);
		
		inventory = new ItemStack[3];
	}
}
