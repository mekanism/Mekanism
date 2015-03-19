package mekanism.common.util;

import mekanism.api.gas.GasStack;

import net.minecraftforge.fluids.FluidStack;

public final class LangUtils
{
	public static String transOnOff(boolean b)
	{
		return MekanismUtils.localize("gui." + (b ? "on" : "off"));
	}
	
	public static String transYesNo(boolean b)
	{
		return MekanismUtils.localize("tooltip." + (b ? "yes" : "no"));
	}
	
	public static String transOutputInput(boolean b)
	{
		return MekanismUtils.localize("gui." + (b ? "output" : "input"));
	}

	public static String localizeFluidStack(FluidStack fluidStack)
	{
		return (fluidStack == null || fluidStack.getFluid() == null ) ? null : fluidStack.getFluid().getLocalizedName(fluidStack);
	}

	public static String localizeGasStack(GasStack gasStack)
	{
		return (gasStack == null || gasStack.getGas() == null ) ? null : gasStack.getGas().getLocalizedName();
	}
}
