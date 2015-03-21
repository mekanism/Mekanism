package mekanism.common.tile.component;

import mekanism.common.Upgrade;
import mekanism.common.tile.TileEntityContainerBlock;

public class TileComponentAdvancedUpgrade extends TileComponentUpgrade
{
	public TileComponentAdvancedUpgrade(TileEntityContainerBlock tile, int slot)
	{
		super(tile, slot);

		setSupported(Upgrade.GAS);
	}
}
