package mekanism.common.tank;

import java.util.HashSet;

import mekanism.api.Coord4D;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class DynamicTankCache 
{
	public ItemStack[] inventory = new ItemStack[2];
	public FluidStack fluid;
	
	public HashSet<Coord4D> locations = new HashSet<Coord4D>();
}
