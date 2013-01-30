package mekanism.common;

import mekanism.api.SideData;
import mekanism.api.Tier.SmeltingFactoryTier;

public class TileEntityEliteSmeltingFactory extends TileEntitySmeltingFactory
{
	public TileEntityEliteSmeltingFactory()
	{
		super(SmeltingFactoryTier.ELITE);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 1, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 2, 7));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 9, 7));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
}
