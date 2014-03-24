package mekanism.common.tile;

import mekanism.api.EnumColor;
import mekanism.common.SideData;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.InventoryUtils;

public class TileEntityEliteFactory extends TileEntityFactory
{
	public TileEntityEliteFactory()
	{
		super(FactoryTier.ELITE, MachineType.ELITE_FACTORY);

		sideOutputs.add(new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.PURPLE, new int[] {4}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {5, 6, 7, 8, 9, 10, 11}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {12, 13, 14, 15, 16, 17, 18}));

		ejectorComponent = new TileComponentEjector(this, sideOutputs.get(5));
	}
}
