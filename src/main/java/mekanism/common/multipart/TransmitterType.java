package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;

import net.minecraft.util.IIcon;

public enum TransmitterType
{
	UNIVERSAL_CABLE_BASIC("BasicUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 0, 0),
	UNIVERSAL_CABLE_ADVANCED("AdvancedUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 1, 0),
	UNIVERSAL_CABLE_ELITE("EliteUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 2, 0),
	UNIVERSAL_CABLE_ULTIMATE("UltimateUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, false, 3, 0),
	MECHANICAL_PIPE_BASIC("BasicMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 0, 0),
	MECHANICAL_PIPE_ADVANCED("AdvancedMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 0, 0),
	MECHANICAL_PIPE_ELITE("EliteMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 0, 0),
	MECHANICAL_PIPE_ULTIMATE("UltimateMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, false, 0, 0),
	PRESSURIZED_TUBE("PressurizedTube", Size.SMALL, TransmissionType.GAS, PartPressurizedTube.tubeIcons, false, 0, 0),
	LOGISTICAL_TRANSPORTER_BASIC("BasicLogisticalTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 0, 0, 6, 10),
	LOGISTICAL_TRANSPORTER_ADVANCED("AdvancedLogisticalTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 1, 1, 6, 10),
	LOGISTICAL_TRANSPORTER_ELITE("EliteLogisticalTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 2, 2, 6, 10),
	LOGISTICAL_TRANSPORTER_ULTIMATE("UltimateLogisticalTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 3, 3, 6, 10),
	RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, false, 4, 8),
	DIVERSION_TRANSPORTER("DiversionTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, true, 5, 14, 6, 10),
	HEAT_TRANSMITTER("HeatTransmitter", Size.SMALL, TransmissionType.HEAT, PartHeatTransmitter.heatIcons, false, 0, 0);

	private String unlocalizedName;
	private Size size;
	private TransmissionType transmissionType;
	private TransmitterIcons transmitterIcons;
	private boolean transparencyRender;
	private int[] indexes;

	private TransmitterType(String name, Size s, TransmissionType type, TransmitterIcons icons, boolean transparency, int... is)
	{
		unlocalizedName = name;
		size = s;
		transmissionType = type;
		transmitterIcons = icons;
		transparencyRender = transparency;
		indexes = is;
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
