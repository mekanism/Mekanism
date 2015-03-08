package mekanism.common.tile;

import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.InventoryUtils;

public class TileEntityAdvancedFactory extends TileEntityFactory
{
	public TileEntityAdvancedFactory()
	{
		super(FactoryTier.ADVANCED, MachineType.ADVANCED_FACTORY);

		configComponent = new TileComponentConfig(this, TransmissionType.ITEM);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData(EnumColor.ORANGE, new int[] {0}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData(EnumColor.PURPLE, new int[] {4}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData(EnumColor.DARK_RED, new int[] {5, 6, 7, 8, 9}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData(EnumColor.DARK_BLUE, new int[] {10, 11, 12, 13, 14}));
		
		configComponent.setConfig(TransmissionType.ITEM, new byte[] {5, 4, 0, 3, 2, 1});

		upgradeComponent = new TileComponentUpgrade(this, 0);
		ejectorComponent = new TileComponentEjector(this, configComponent.getOutputs(TransmissionType.ITEM).get(5));
	}
}
