package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.api.SideData;
import mekanism.common.Tier.FactoryTier;

public class TileEntityEliteFactory extends TileEntityFactory
{
	public TileEntityEliteFactory()
	{
		super(FactoryTier.ELITE);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 1, 1, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 2, 7, new int[] {2, 3, 4, 5, 6, 7, 8}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 9, 7, new int[] {9, 10, 11, 12, 13, 14, 15}));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
}
