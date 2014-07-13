package mekanism.generators.common.tile.reactor;

import mekanism.api.reactor.INeutronCapture;

public class TileEntityReactorNeutronCapture extends TileEntityReactorBlock implements INeutronCapture
{
	@Override
	public boolean isFrame()
	{
		return false;
	}

	@Override
	public int absorbNeutrons(int neutrons)
	{
		return 0;
	}
}
