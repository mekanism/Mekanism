package mekanism.generators.common.tile.reactor;

import mekanism.common.util.InventoryUtils;
import net.minecraft.util.EnumFacing;

public class TileEntityReactorFrame extends TileEntityReactorBlock
{
	@Override
	public boolean isFrame()
	{
		return true;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return InventoryUtils.EMPTY;
	}
}
