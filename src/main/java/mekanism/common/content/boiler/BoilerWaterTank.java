package mekanism.common.content.boiler;

import mekanism.common.tile.TileEntityBoiler;

import net.minecraftforge.fluids.FluidStack;

public class BoilerWaterTank extends BoilerTank
{
	public BoilerWaterTank(TileEntityBoiler tileEntity)
	{
		super(tileEntity);
	}

	@Override
	public FluidStack getFluid()
	{
		return steamBoiler.structure != null ? steamBoiler.structure.waterStored : null;
	}

	public void setFluid(FluidStack stack)
	{
		steamBoiler.structure.waterStored = stack;
	}

}
