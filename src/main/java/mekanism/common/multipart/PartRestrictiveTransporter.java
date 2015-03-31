package mekanism.common.multipart;

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
	public TransmitterType getTransmitter()
	{
		return TransmitterType.RESTRICTIVE_TRANSPORTER;
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return transporterIcons.getCenterIcon(1);
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return transporterIcons.getSideIcon(2);
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return transporterIcons.getSideIcon(3);
	}

	@Override
	public int getCost()
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
