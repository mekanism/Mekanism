package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.api.SideData;
import mekanism.common.Tier.FactoryTier;

public class TileEntityAdvancedFactory extends TileEntityFactory
{
	public TileEntityAdvancedFactory()
	{
		super(FactoryTier.ADVANCED);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 1, 1, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 4, 5, new int[] {4, 5, 6, 7, 8}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 9, 5, new int[] {9, 10, 11, 12, 13}));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
}
