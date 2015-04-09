package mekanism.common.multipart;

import mekanism.common.Tier.TransporterTier;

import net.minecraft.entity.player.EntityPlayer;
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
	public IIcon getCenterIcon(boolean opaque)
	{
		return transporterIcons.getCenterIcon(4);
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return transporterIcons.getSideIcon(8);
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return transporterIcons.getSideIcon(9);
	}

	@Override
	public double getCost()
	{
		return 1000;
	}
	
	@Override
	public boolean transparencyRender()
	{
		return false;
	}
	
	@Override
	protected boolean onConfigure(EntityPlayer player, int part, int side)
	{
		return false;
	}
}
