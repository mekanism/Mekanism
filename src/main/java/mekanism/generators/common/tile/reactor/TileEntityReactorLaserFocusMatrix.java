package mekanism.generators.common.tile.reactor;

import mekanism.api.lasers.ILaserReceptor;
import net.minecraft.util.EnumFacing;

public class TileEntityReactorLaserFocusMatrix extends TileEntityReactorBlock implements ILaserReceptor
{
	@Override
	public boolean isFrame()
	{
		return false;
	}

	@Override
	public void receiveLaserEnergy(double energy, EnumFacing side)
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
