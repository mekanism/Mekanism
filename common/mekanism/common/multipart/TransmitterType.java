package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;


public enum TransmitterType
{
	UNIVERSAL_CABLE("UniversalCable", TransmissionType.ENERGY),
	MECHANICAL_PIPE("MechanicalPipe", TransmissionType.FLUID),
	PRESSURIZED_TUBE("PressurizedTube", TransmissionType.GAS),
	LOGISTICAL_TRANSPORTER("LogisticalTransporter", TransmissionType.ITEM),
	RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", TransmissionType.ITEM),
	DIVERSION_TRANSPORTER("DiversionTransporter", TransmissionType.ITEM);
	
	private String unlocalizedName;
	private Size size;
	private TransmissionType transmissionType;
	
	public static TransmitterType[] oldMetaArray = {PRESSURIZED_TUBE, UNIVERSAL_CABLE, MECHANICAL_PIPE, LOGISTICAL_TRANSPORTER, RESTRICTIVE_TRANSPORTER, DIVERSION_TRANSPORTER};
	
	private TransmitterType(String name, TransmissionType type)
	{
		unlocalizedName = name;
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
