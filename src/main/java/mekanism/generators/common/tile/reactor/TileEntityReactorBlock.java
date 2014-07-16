package mekanism.generators.common.tile.reactor;

import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.IReactorBlock;

import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityReactorBlock extends TileEntity implements IReactorBlock
{
	public IFusionReactor fusionReactor;
	public boolean changed;

	@Override
	public void setReactor(IFusionReactor reactor)
	{
		if(reactor != fusionReactor)
		{
			changed = true;
		}
		fusionReactor = reactor;
	}

	@Override
	public IFusionReactor getReactor()
	{
		return fusionReactor;
	}

	@Override
	public void invalidate()
	{
		if(getReactor() != null)
		{
			getReactor().formMultiblock();
		}
	}

	@Override
	public void updateEntity()
	{
		if(changed)
		{
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		}
	}
}
