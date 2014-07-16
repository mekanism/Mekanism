package mekanism.generators.common.tile.reactor;

import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.reactor.IFusionReactor;

import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityReactorLaserFocusMatrix extends TileEntityReactorBlock implements ILaserReceptor
{
	@Override
	public boolean isFrame()
	{
		return false;
	}

	@Override
	public void receiveLaserEnergy(double energy, ForgeDirection side)
	{
		if(getReactor() != null)
			getReactor().addTemperatureFromEnergyInput(energy);
	}

	@Override
	public boolean canLasersDig()
	{
		return false;
	}

	@Override
	public double energyToDig()
	{
		return Double.MAX_VALUE;
	}
}
