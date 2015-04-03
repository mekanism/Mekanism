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

		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.GAS);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {1}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Extra", EnumColor.PURPLE, new int[] {4}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[] {5, 6, 7, 8, 9}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {10, 11, 12, 13, 14}));
		configComponent.setConfig(TransmissionType.ITEM, new byte[] {4, 3, 0, 2, 1, 0});
		
		configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.DARK_RED, new int[] {0}));
		configComponent.fillConfig(TransmissionType.GAS, 1);
		configComponent.setCanEject(TransmissionType.GAS, false);
		
		configComponent.setInputEnergyConfig();

		upgradeComponent = new TileComponentUpgrade(this, 0);
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(4));
	}
}
