package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Tier.BaseTier;
import net.minecraft.util.IIcon;

public enum TransmitterType
{
	UNIVERSAL_CABLE_BASIC("BasicUniversalCable", BaseTier.BASIC, Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 0, 0),
	UNIVERSAL_CABLE_ADVANCED("AdvancedUniversalCable", BaseTier.ADVANCED, Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 1, 1),
	UNIVERSAL_CABLE_ELITE("EliteUniversalCable", BaseTier.ELITE, Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 2, 2),
	UNIVERSAL_CABLE_ULTIMATE("UltimateUniversalCable", BaseTier.ULTIMATE, Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 3, 3),
	MECHANICAL_PIPE_BASIC("BasicMechanicalPipe", BaseTier.BASIC, Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 0, 0),
	MECHANICAL_PIPE_ADVANCED("AdvancedMechanicalPipe", BaseTier.ADVANCED, Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 1, 1),
	MECHANICAL_PIPE_ELITE("EliteMechanicalPipe", BaseTier.ELITE, Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 2, 2),
	MECHANICAL_PIPE_ULTIMATE("UltimateMechanicalPipe", BaseTier.ULTIMATE, Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 3, 3),
	PRESSURIZED_TUBE_BASIC("BasicPressurizedTube", BaseTier.BASIC, Size.SMALL, TransmissionType.GAS, PartPressurizedTube.tubeIcons, false, 0, 0),
	PRESSURIZED_TUBE_ADVANCED("AdvancedPressurizedTube", BaseTier.ADVANCED, Size.SMALL, TransmissionType.GAS, PartPressurizedTube.tubeIcons, false, 1, 1),
	PRESSURIZED_TUBE_ELITE("ElitePressurizedTube", BaseTier.ELITE, Size.SMALL, TransmissionType.GAS, PartPressurizedTube.tubeIcons, false, 2, 2),
	PRESSURIZED_TUBE_ULTIMATE("UltimatePressurizedTube", BaseTier.ULTIMATE, Size.SMALL, TransmissionType.GAS, PartPressurizedTube.tubeIcons, false, 3, 3),
	LOGISTICAL_TRANSPORTER_BASIC("BasicLogisticalTransporter", BaseTier.BASIC, Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 0, 0, 6, 10),
	LOGISTICAL_TRANSPORTER_ADVANCED("AdvancedLogisticalTransporter", BaseTier.ADVANCED, Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 1, 1, 6, 10),
	LOGISTICAL_TRANSPORTER_ELITE("EliteLogisticalTransporter", BaseTier.ELITE, Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 2, 2, 6, 10),
	LOGISTICAL_TRANSPORTER_ULTIMATE("UltimateLogisticalTransporter", BaseTier.ULTIMATE, Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 3, 3, 6, 10),
	RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", BaseTier.BASIC, Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, false, 4, 8),
	DIVERSION_TRANSPORTER("DiversionTransporter", BaseTier.BASIC, Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 5, 14, 6, 10),
	HEAT_TRANSMITTER("HeatTransmitter", BaseTier.BASIC, Size.SMALL, TransmissionType.HEAT, PartHeatTransmitter.heatIcons, false, 0, 0);

	private String unlocalizedName;
	private Size size;
	private TransmissionType transmissionType;
	private TransmitterIcons transmitterIcons;
	private boolean transparencyRender;
	private int[] indexes;
	private BaseTier tier;

	private TransmitterType(String name, BaseTier t, Size s, TransmissionType type, TransmitterIcons icons, boolean transparency, int... is)
	{
		unlocalizedName = name;
		tier = t;
		size = s;
		transmissionType = type;
		transmitterIcons = icons;
		transparencyRender = transparency;
		indexes = is;
	}
	
	public BaseTier getTier()
	{
		return tier;
	}

	public String getName()
	{
		return unlocalizedName;
	}

	public Size getSize()
	{
		return size;
	}
	
	public boolean hasTransparency()
	{
		return transparencyRender;
	}

	public TransmissionType getTransmission()
	{
		return transmissionType;
	}

	public IIcon getCenterIcon(boolean opaque)
	{
		if(!transparencyRender)
		{
			return transmitterIcons.getCenterIcon(indexes[0]);
		}
		else {
			return transmitterIcons.getCenterIcon(opaque ? indexes[0] : indexes[2]);
		}
	}

	public IIcon getSideIcon(boolean opaque)
	{
		if(!transparencyRender)
		{
			return transmitterIcons.getSideIcon(indexes[1]);
		}
		else {
			return transmitterIcons.getSideIcon(opaque ? indexes[1] : indexes[3]);
		}
	}

	public static enum Size
	{
		SMALL(6),
		LARGE(8);

		public int centerSize;

		private Size(int size)
		{
			centerSize = size;
		}
	}
}
