package mekanism.common.multipart;

import net.minecraft.util.IIcon;

public class PartRestrictiveTransporter extends PartLogisticalTransporter
{
	@Override
	public String getType()
	{
		return "mekanism:restrictive_transporter";
	}

	@Override
	public TransmitterType getTransmitterType()
	{
		return TransmitterType.RESTRICTIVE_TRANSPORTER;
	}

	@Override
	public IIcon getCenterIcon()
	{
		return transporterIcons.getCenterIcon(1);
	}

	@Override
	public IIcon getSideIcon()
	{
		return transporterIcons.getSideIcon(2);
	}

	@Override
	public IIcon getSideIconRotated()
	{
		return transporterIcons.getSideIcon(3);
	}

	@Override
	public int getCost()
	{
		return 1000;
	}
}
