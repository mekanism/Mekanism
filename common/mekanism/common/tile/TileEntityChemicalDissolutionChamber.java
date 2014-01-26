package mekanism.common.tile;

import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalDissolutionChamber extends TileEntityElectricBlock
{
	public TileEntityChemicalDissolutionChamber()
	{
		super("ChemicalDissolutionChamber", MachineType.CHEMICAL_DISSOLUTION_CHAMBER.baseEnergy);
		
		inventory = new ItemStack[4];
	}
}
