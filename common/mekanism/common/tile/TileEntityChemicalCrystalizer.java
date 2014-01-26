package mekanism.common.tile;

import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalCrystalizer extends TileEntityElectricBlock
{
	public TileEntityChemicalCrystalizer()
	{
		super("ChemicalCrystalizer", MachineType.CHEMICAL_CRYSTALIZER.baseEnergy);
		
		inventory = new ItemStack[3];
	}
}
