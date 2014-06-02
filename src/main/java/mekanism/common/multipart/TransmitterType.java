package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;
import net.minecraft.util.IIcon;


public enum TransmitterType
{
	UNIVERSAL_CABLE_BASIC("BasicUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, 0, 0),
	UNIVERSAL_CABLE_ADVANCED("AdvancedUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, 1, 0),
	UNIVERSAL_CABLE_ELITE("EliteUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, 2, 0),
	UNIVERSAL_CABLE_ULTIMATE("UltimateUniversalCable", Size.SMALL, TransmissionType.ENERGY, PartUniversalCable.cableIcons, 3, 0),
	MECHANICAL_PIPE_BASIC("BasicMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, 0, 0),
	PRESSURIZED_TUBE("PressurizedTube", Size.SMALL, TransmissionType.GAS, PartPressurizedTube.tubeIcons, 0, 0),
	LOGISTICAL_TRANSPORTER("LogisticalTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, 0, 0),
	RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, 1, 1),
	DIVERSION_TRANSPORTER("DiversionTransporter", Size.LARGE, TransmissionType.ITEM, PartLogisticalTransporter.transporterIcons, 2, 0),
	MECHANICAL_PIPE_ADVANCED("AdvancedMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, 0, 0),
	MECHANICAL_PIPE_ELITE("EliteMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, 0, 0),
	MECHANICAL_PIPE_ULTIMATE("UltimateMechanicalPipe", Size.LARGE, TransmissionType.FLUID, PartMechanicalPipe.pipeIcons, 0, 0);

	private String unlocalizedName;
	private Size size;
	private TransmissionType transmissionType;
	private TransmitterIcons transmitterIcons;
	private int centerIndex;
	private int sideIndex;

	private TransmitterType(String name, Size s, TransmissionType type, TransmitterIcons icons, int center, int side)
	{
		unlocalizedName = name;
		size = s;
		transmissionType = type;
		transmitterIcons = icons;
		centerIndex = center;
		sideIndex = side;
	}

	public String getName()
	{
		return unlocalizedName;
	}

	public Size getSize()
	{
		return size;
	}

	public TransmissionType getTransmission()
	{
		return transmissionType;
	}

	public IIcon getCenterIcon()
	{
		return transmitterIcons.getCenterIcon(centerIndex);
	}

	public IIcon getSideIcon()
	{
		return transmitterIcons.getSideIcon(sideIndex);
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
