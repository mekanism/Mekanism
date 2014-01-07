package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;


public enum TransmitterType
{
	UNIVERSAL_CABLE_BASIC("BasicUniversalCable", Size.SMALL, TransmissionType.ENERGY),
	MECHANICAL_PIPE("MechanicalPipe", Size.LARGE, TransmissionType.FLUID),
	PRESSURIZED_TUBE("PressurizedTube", Size.SMALL, TransmissionType.GAS),
	LOGISTICAL_TRANSPORTER("LogisticalTransporter", Size.LARGE, TransmissionType.ITEM),
	RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", Size.LARGE, TransmissionType.ITEM),
	DIVERSION_TRANSPORTER("DiversionTransporter", Size.LARGE, TransmissionType.ITEM),
	UNIVERSAL_CABLE_ADVANCED("AdvancedUniversalCable", Size.SMALL, TransmissionType.ENERGY),
	UNIVERSAL_CABLE_ELITE("EliteUniversalCable", Size.SMALL, TransmissionType.ENERGY),
	UNIVERSAL_CABLE_ULTIMATE("UltimateUniversalCable", Size.SMALL, TransmissionType.ENERGY);

	private String unlocalizedName;
	private Size size;
	private TransmissionType transmissionType;

	private TransmitterType(String name, Size s, TransmissionType type)
	{
		unlocalizedName = name;
		size = s;
		transmissionType = type;
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
