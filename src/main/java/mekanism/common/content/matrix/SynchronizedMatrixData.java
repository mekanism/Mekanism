package mekanism.common.content.matrix;

import mekanism.common.multiblock.SynchronizedData;
import net.minecraft.item.ItemStack;

public class SynchronizedMatrixData extends SynchronizedData<SynchronizedMatrixData>
{
	public ItemStack[] inventory = new ItemStack[2];
	
	public double electricityStored;
	
	public double heat;
	
	public int capacitors;
	
	public int outputters;
}