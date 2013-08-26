package mekanism.common.tileentity;

import mekanism.api.EnumColor;
import mekanism.api.SideData;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.block.BlockMachine.MachineType;

public class TileEntityAdvancedFactory extends TileEntityFactory
{
	public TileEntityAdvancedFactory()
	{
		super(FactoryTier.ADVANCED, MachineType.ADVANCED_FACTORY);
		
		sideOutputs.add(new SideData(EnumColor.GREY, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {4, 5, 6, 7, 8}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {9, 10, 11, 12, 13}));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
}
