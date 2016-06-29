package mekanism.common.content.boiler;

import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BoilerWaterTank extends BoilerTank
{
	public BoilerWaterTank(TileEntityBoilerCasing tileEntity)
	{
		super(tileEntity);
	}

	@Override
	public FluidStack getFluid()
	{
		return steamBoiler.structure != null ? steamBoiler.structure.waterStored : null;
	}

	@Override
	public void setFluid(FluidStack stack)
	{
		steamBoiler.structure.waterStored = stack;
	}
	
	@Override
	public int getCapacity()
	{
		return steamBoiler.structure != null ? steamBoiler.structure.waterVolume*BoilerUpdateProtocol.WATER_PER_TANK : 0;
	}
	
	@Override
	public boolean canFill()
	{
		return steamBoiler.structure.upperRenderLocation != null && steamBoiler.getPos().getY() < steamBoiler.structure.upperRenderLocation.yCoord-1;
	}
	
	@Override
	public boolean canFillFluidType(FluidStack fluid)
	{
		return canFill() && fluid.getFluid() == FluidRegistry.WATER;
	}
}
