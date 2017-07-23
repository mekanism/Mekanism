package mekanism.common.base;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidItemWrapper 
{
	public FluidStack getFluid(ItemStack container);
	
	public int getCapacity(ItemStack container);
	
	public int fill(ItemStack container, FluidStack resource, boolean doFill);
	
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain);
}
