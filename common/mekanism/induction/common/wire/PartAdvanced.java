package mekanism.induction.common.wire;

import net.minecraft.block.Block;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.handler.MultipartProxy;

public abstract class PartAdvanced extends TMultiPart
{
	protected long ticks = 0;

	@Override
	public void update()
	{
		if(ticks == 0)
		{
			initiate();
		}

		if(ticks >= Long.MAX_VALUE)
		{
			ticks = 1;
		}

		ticks++;
	}

	@Override
	public void onAdded()
	{
		world().notifyBlocksOfNeighborChange(x(), y(), z(), ((Block)MultipartProxy.block()).blockID);
	}

	/**
	 * Called on the TileEntity's first tick.
	 */
	public void initiate() {}
}
