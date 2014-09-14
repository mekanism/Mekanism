package mekanism.generators.common.tile.reactor;

import mekanism.api.lasers.ILaserReceptor;

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
		{
			getReactor().addTemperatureFromEnergyInput(energy);
		}
	}

	@Override
	public boolean canLasersDig()
	{
		return false;
	}
}
