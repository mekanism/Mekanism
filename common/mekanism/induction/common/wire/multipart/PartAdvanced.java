package mekanism.induction.common.wire.multipart;

import net.minecraft.block.Block;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.handler.MultipartProxy;

public abstract class PartAdvanced extends TMultiPart
{
	protected long ticks = 0;

	@Override
	public void update()
	{
		if (this.ticks == 0)
		{
			this.initiate();
		}

		if (this.ticks >= Long.MAX_VALUE)
		{
			this.ticks = 1;
		}

		this.ticks++;
	}

	@Override
	public void onAdded()
	{
		world().notifyBlocksOfNeighborChange(x(), y(), z(), ((Block) MultipartProxy.block()).blockID);
	}

	/**
	 * Called on the TileEntity's first tick.
	 */
	public void initiate()
	{
	}
}
