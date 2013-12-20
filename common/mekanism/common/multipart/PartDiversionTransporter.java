package mekanism.common.multipart;

import net.minecraft.util.Icon;

public class PartDiversionTransporter extends PartLogisticalTransporter
{
	@Override
	public String getType()
	{
		return "mekanism:diversion_transporter";
	}
	
	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.DIVERSION_TRANSPORTER;
	}
	
	@Override
	public Icon getCenterIcon()
	{
		return transporterIcons.getCenterIcon(2);
	}
}
