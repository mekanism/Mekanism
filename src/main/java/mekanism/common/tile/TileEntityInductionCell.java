package mekanism.common.tile;

import mekanism.common.Tier.InductionCellTier;

public class TileEntityInductionCell extends TileEntityBasicBlock
{
	public InductionCellTier tier = InductionCellTier.BASIC;
	
	@Override
	public void onUpdate() {}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
}
