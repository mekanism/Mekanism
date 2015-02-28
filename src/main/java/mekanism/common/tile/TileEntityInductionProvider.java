package mekanism.common.tile;

import mekanism.common.Tier.InductionProviderTier;

public class TileEntityInductionProvider extends TileEntityBasicBlock
{
	public InductionProviderTier tier = InductionProviderTier.BASIC;
	
	@Override
	public void onUpdate() {}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
}
