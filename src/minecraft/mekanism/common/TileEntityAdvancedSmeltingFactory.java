package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.api.SideData;
import mekanism.api.Tier.SmeltingFactoryTier;

public class TileEntityAdvancedSmeltingFactory extends TileEntitySmeltingFactory
{
	public TileEntityAdvancedSmeltingFactory()
	{
		super(SmeltingFactoryTier.ADVANCED);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 1, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 2, 5));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 7, 5));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
}
