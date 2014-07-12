package mekanism.common.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidContainerItem;

public final class FluidContainerUtils 
{
	public static FluidStack extractFluid(FluidTank tileTank, ItemStack container)
	{
		return extractFluid(tileTank, container, tileTank.getFluid() != null ? tileTank.getFluid().getFluid() : null);
	}
	
	public static FluidStack extractFluid(FluidTank tileTank, ItemStack container, Fluid fluid)
	{
		return extractFluid(tileTank.getCapacity()-tileTank.getFluidAmount(), container, fluid);
	}
	
	public static FluidStack extractFluid(int needed, ItemStack container, Fluid fluid)
	{
		IFluidContainerItem item = (IFluidContainerItem)container.getItem();
		
		if(item.getFluid(container) == null)
		{
			return null;
		}
		
		if(fluid != null && item.getFluid(container).getFluid() != fluid)
		{
			return null;
		}
		
		return item.drain(container, needed, true);
	}
	
	public static int insertFluid(FluidTank tileTank, ItemStack container)
	{
		return insertFluid(tileTank.getFluid(), container);
	}
	
	public static int insertFluid(FluidStack fluid, ItemStack container)
	{
		IFluidContainerItem item = (IFluidContainerItem)container.getItem();
		
		if(fluid == null)
		{
			return 0;
		}
		
		return item.fill(container, fluid, true);
	}
	
	public static enum ContainerEditMode
	{
		BOTH("fluidedit.both"),
		FILL("fluidedit.fill"),
		EMPTY("fluidedit.empty");
		
		private String display;

		public String getDisplay()
		{
			return MekanismUtils.localize(display);
		}

		private ContainerEditMode(String s)
		{
			display = s;
		}
	}
}
