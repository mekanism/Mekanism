package mekanism.common.multipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class PartRestrictiveTransporter extends PartLogisticalTransporter
{
	@Override
	public ResourceLocation getType()
	{
		return new ResourceLocation("mekanism:restrictive_transporter");
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
	protected EnumActionResult onConfigure(EntityPlayer player, int part, EnumFacing side)
	{
		return EnumActionResult.PASS;
	}
}
