package mekanism.common.multipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

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
	protected boolean onConfigure(EntityPlayer player, int part, EnumFacing side)
	{
		return false;
	}
}
