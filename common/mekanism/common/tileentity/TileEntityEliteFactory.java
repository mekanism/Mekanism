package mekanism.common.tileentity;

import mekanism.api.EnumColor;
import mekanism.api.SideData;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.block.BlockMachine.MachineType;

public class TileEntityEliteFactory extends TileEntityFactory
{
	public TileEntityEliteFactory()
	{
		super(FactoryTier.ELITE, MachineType.ELITE_FACTORY);
		
		sideOutputs.add(new SideData(EnumColor.GREY, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {4, 5, 6, 7, 8, 9, 10}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {11, 12, 13, 14, 15, 16, 17}));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
}
