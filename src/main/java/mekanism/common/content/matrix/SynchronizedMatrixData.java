package mekanism.common.content.matrix;

import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.multiblock.SynchronizedData;
import net.minecraft.item.ItemStack;

public class SynchronizedMatrixData extends SynchronizedData<SynchronizedTankData>
{
	public ItemStack[] inventory = new ItemStack[2];
	
	public double electricityStored;
	
	public double heat;
	
	public int capacitors;
	
	public int outputters;
	
	public int coolants;
}