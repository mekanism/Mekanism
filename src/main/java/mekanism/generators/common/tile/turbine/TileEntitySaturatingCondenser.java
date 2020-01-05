package mekanism.generators.common.tile.turbine;

import mekanism.common.tile.TileEntityBasicBlock;

public class TileEntitySaturatingCondenser extends TileEntityBasicBlock
{
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void onUpdate() {}
}
