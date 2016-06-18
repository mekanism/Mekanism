package mekanism.common.content.boiler;

import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraftforge.fluids.FluidStack;

public class BoilerSteamTank extends BoilerTank
{
	public BoilerSteamTank(TileEntityBoilerCasing tileEntity)
	{
		super(tileEntity);
	}

	@Override
	public FluidStack getFluid()
	{
		return steamBoiler.structure != null ? steamBoiler.structure.steamStored : null;
	}

	@Override
	public void setFluid(FluidStack stack)
	{
		steamBoiler.structure.steamStored = stack;
	}
	
	@Override
	public int getCapacity()
	{
		return steamBoiler.structure != null ? steamBoiler.structure.steamVolume*BoilerUpdateProtocol.STEAM_PER_TANK : 0;
	}
}
