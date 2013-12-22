package mekanism.common.multipart;

import net.minecraft.util.Icon;

public class PartRestrictiveTransporter extends PartLogisticalTransporter
{
	@Override
	public String getType()
	{
		return "mekanism:restrictive_transporter";
	}
	
	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.RESTRICTIVE_TRANSPORTER;
	}
	
	@Override
	public Icon getCenterIcon()
	{
		return transporterIcons.getCenterIcon(1);
	}

	@Override
	public Icon getSideIcon()
	{
		return transporterIcons.getSideIcon(1);
	}
	
	@Override
	public int getPriority()
	{
		return 1000;
	}
}
