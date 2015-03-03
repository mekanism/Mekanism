package mekanism.common.content.matrix;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.SynchronizedData;
import net.minecraft.item.ItemStack;

public class SynchronizedMatrixData extends SynchronizedData<SynchronizedMatrixData>
{
	public ItemStack[] inventory = new ItemStack[2];
	
	public double electricityStored;
	
	public Set<Coord4D> cells = new HashSet<Coord4D>();
	
	public Set<Coord4D> providers = new HashSet<Coord4D>();
	
	public double remainingOutput;
	public double lastOutput;
	
	public double storageCap;
	public double outputCap;
	
	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
	}
}