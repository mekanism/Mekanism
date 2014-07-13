package mekanism.generators.common.tile.reactor;

import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.IReactorBlock;

import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityReactorBlock extends TileEntity implements IReactorBlock
{
	public IFusionReactor fusionReactor;

	@Override
	public void setReactor(IFusionReactor reactor)
	{
		fusionReactor = reactor;
	}

	@Override
	public IFusionReactor getReactor()
	{
		return fusionReactor;
	}
}
