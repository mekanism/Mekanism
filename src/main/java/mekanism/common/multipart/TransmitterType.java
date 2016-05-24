package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Tier.BaseTier;

public enum TransmitterType
{
	UNIVERSAL_CABLE_BASIC("BasicUniversalCable", BaseTier.BASIC, Size.SMALL, TransmissionType.ENERGY, false, 0, 0),
	UNIVERSAL_CABLE_ADVANCED("AdvancedUniversalCable", BaseTier.ADVANCED, Size.SMALL, TransmissionType.ENERGY, false, 1, 1),
	UNIVERSAL_CABLE_ELITE("EliteUniversalCable", BaseTier.ELITE, Size.SMALL, TransmissionType.ENERGY, false, 2, 2),
	UNIVERSAL_CABLE_ULTIMATE("UltimateUniversalCable", BaseTier.ULTIMATE, Size.SMALL, TransmissionType.ENERGY, false, 3, 3),
	MECHANICAL_PIPE_BASIC("BasicMechanicalPipe", BaseTier.BASIC, Size.LARGE, TransmissionType.FLUID, false, 0, 0),
	MECHANICAL_PIPE_ADVANCED("AdvancedMechanicalPipe", BaseTier.ADVANCED, Size.LARGE, TransmissionType.FLUID, false, 1, 1),
	MECHANICAL_PIPE_ELITE("EliteMechanicalPipe", BaseTier.ELITE, Size.LARGE, TransmissionType.FLUID, false, 2, 2),
	MECHANICAL_PIPE_ULTIMATE("UltimateMechanicalPipe", BaseTier.ULTIMATE, Size.LARGE, TransmissionType.FLUID, false, 3, 3),
	PRESSURIZED_TUBE_BASIC("BasicPressurizedTube", BaseTier.BASIC, Size.SMALL, TransmissionType.GAS, false, 0, 0),
	PRESSURIZED_TUBE_ADVANCED("AdvancedPressurizedTube", BaseTier.ADVANCED, Size.SMALL, TransmissionType.GAS, false, 1, 1),
	PRESSURIZED_TUBE_ELITE("ElitePressurizedTube", BaseTier.ELITE, Size.SMALL, TransmissionType.GAS, false, 2, 2),
	PRESSURIZED_TUBE_ULTIMATE("UltimatePressurizedTube", BaseTier.ULTIMATE, Size.SMALL, TransmissionType.GAS, false, 3, 3),
	LOGISTICAL_TRANSPORTER_BASIC("BasicLogisticalTransporter", BaseTier.BASIC, Size.LARGE, TransmissionType.ITEM, true, 0, 0, 6, 10),
	LOGISTICAL_TRANSPORTER_ADVANCED("AdvancedLogisticalTransporter", BaseTier.ADVANCED, Size.LARGE, TransmissionType.ITEM, true, 1, 1, 6, 10),
	LOGISTICAL_TRANSPORTER_ELITE("EliteLogisticalTransporter", BaseTier.ELITE, Size.LARGE, TransmissionType.ITEM, true, 2, 2, 6, 10),
	LOGISTICAL_TRANSPORTER_ULTIMATE("UltimateLogisticalTransporter", BaseTier.ULTIMATE, Size.LARGE, TransmissionType.ITEM, true, 3, 3, 6, 10),
	RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", BaseTier.BASIC, Size.LARGE, TransmissionType.ITEM, false, 4, 8),
	DIVERSION_TRANSPORTER("DiversionTransporter", BaseTier.BASIC, Size.LARGE, TransmissionType.ITEM, true, 5, 14, 6, 10),
	THERMODYNAMIC_CONDUCTOR_BASIC("BasicThermodynamicConductor", BaseTier.BASIC, Size.SMALL, TransmissionType.HEAT, false, 0, 0),
	THERMODYNAMIC_CONDUCTOR_ADVANCED("AdvancedThermodynamicConductor", BaseTier.ADVANCED, Size.SMALL, TransmissionType.HEAT, false, 1, 1),
	THERMODYNAMIC_CONDUCTOR_ELITE("EliteThermodynamicConductor", BaseTier.ELITE, Size.SMALL, TransmissionType.HEAT, false, 2, 2),
	THERMODYNAMIC_CONDUCTOR_ULTIMATE("UltimateThermodynamicConductor", BaseTier.ULTIMATE, Size.SMALL, TransmissionType.HEAT, false, 3, 3);

	private String unlocalizedName;
	private Size size;
	private TransmissionType transmissionType;
	private boolean transparencyRender;
	private int[] indexes;
	private BaseTier tier;

	private TransmitterType(String name, BaseTier t, Size s, TransmissionType type, boolean transparency, int... is)
	{
		unlocalizedName = name;
		tier = t;
		size = s;
		transmissionType = type;
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
