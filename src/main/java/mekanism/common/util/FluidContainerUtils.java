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
		return extractFluid(tileTank, container, null);
	}
	
	public static FluidStack extractFluid(FluidTank tileTank, ItemStack container, Fluid fluid)
	{
		IFluidContainerItem item = (IFluidContainerItem)container.getItem();
		
		if(fluid != null && item.getFluid(container) != null && item.getFluid(container).getFluid() != fluid)
		{
			return null;
		}
		
		return item.drain(container, tileTank.getCapacity()-tileTank.getFluidAmount(), true);
	}
	
	public static int insertFluid(FluidTank tileTank, ItemStack container)
	{
		IFluidContainerItem item = (IFluidContainerItem)container.getItem();
		
		if(tileTank.getFluid() == null)
		{
			return 0;
		}
		
		return item.fill(container, tileTank.getFluid(), true);
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
