package mekanism.common.matrix;

import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tank.SynchronizedTankData;
import net.minecraft.item.ItemStack;

public class SynchronizedMatrixData extends SynchronizedData<SynchronizedTankData>
{
	public ItemStack[] inventory = new ItemStack[2];
	
	public double electricityStored;
}
