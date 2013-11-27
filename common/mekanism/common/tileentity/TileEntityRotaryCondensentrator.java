package mekanism.common.tileentity;

import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;

public class TileEntityRotaryCondensentrator extends TileEntityElectricBlock
{
	public TileEntityRotaryCondensentrator()
	{
		super("RotaryCondensentrator", MachineType.ROTARY_CONDENSENTRATOR.baseEnergy);
		inventory = new ItemStack[5];
	}
}
