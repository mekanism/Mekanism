package mekanism.common.multipart;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
//import net.minecraft.util.IIcon;

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
	public TextureAtlasSprite getCenterIcon(boolean opaque)
	{
		return transporterIcons.getCenterIcon(4);
	}

	@Override
	public TextureAtlasSprite getSideIcon(boolean opaque)
	{
		return transporterIcons.getSideIcon(8);
	}

	@Override
	public TextureAtlasSprite getSideIconRotated(boolean opaque)
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
	protected boolean onConfigure(EntityPlayer player, int part, EnumFacing side)
	{
		return false;
	}
}
