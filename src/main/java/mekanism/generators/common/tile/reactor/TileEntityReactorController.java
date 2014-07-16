package mekanism.generators.common.tile.reactor;

import mekanism.common.Mekanism;
import mekanism.generators.common.FusionReactor;

public class TileEntityReactorController extends TileEntityReactorBlock
{
	@Override
	public boolean isFrame()
	{
		return false;
	}

	public void radiateNeutrons(int neutrons)
	{
	}

	public void formMultiblock()
	{
		if(getReactor() == null)
		{
			setReactor(new FusionReactor(this));
		}
		getReactor().formMultiblock();
	}

	@Override
	public void updateEntity()
	{
		if(getReactor() != null && !worldObj.isRemote)
		{
			getReactor().simulate();
		}
	}
}
