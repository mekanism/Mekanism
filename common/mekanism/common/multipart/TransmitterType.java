package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;


public enum TransmitterType
{
	UNIVERSAL_CABLE("UniversalCable", Size.SMALL, TransmissionType.ENERGY),
	MECHANICAL_PIPE("MechanicalPipe", Size.LARGE, TransmissionType.FLUID),
	PRESSURIZED_TUBE("PressurizedTube", Size.SMALL, TransmissionType.GAS),
	LOGISTICAL_TRANSPORTER("LogisticalTransporter", Size.LARGE, TransmissionType.ITEM),
	RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", Size.LARGE, TransmissionType.ITEM),
	DIVERSION_TRANSPORTER("DiversionTransporter", Size.LARGE, TransmissionType.ITEM);
	
	private String unlocalizedName;
	private Size size;
	private TransmissionType transmissionType;
	
	public static TransmitterType[] oldMetaArray = {PRESSURIZED_TUBE, UNIVERSAL_CABLE, MECHANICAL_PIPE, LOGISTICAL_TRANSPORTER, RESTRICTIVE_TRANSPORTER, DIVERSION_TRANSPORTER};
	
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
	
	public TransmissionType getType()
	{
		return transmissionType;
	}
	
    public static enum Size
    {
        SMALL(6),
        LARGE(8);

        public int centreSize;

        private Size(int size) 
        {
            centreSize = size;
        }
    }
    
    public static TransmitterType fromOldMeta(int meta)
	{
    	return oldMetaArray[meta];
	}
}
