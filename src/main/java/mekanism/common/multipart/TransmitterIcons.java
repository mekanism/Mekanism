package mekanism.common.multipart;

import mekanism.api.MekanismConfig.client;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class TransmitterIcons
{
	private IIcon[] sideIcons;
	private IIcon[] centerIcons;
	
	private IIcon[] sideIcons_opaque;
	private IIcon[] centerIcons_opaque;

	public TransmitterIcons(int numCentres, int numSides)
	{
		sideIcons = new IIcon[numSides];
		centerIcons = new IIcon[numCentres];
		
		sideIcons_opaque = new IIcon[numSides];
		centerIcons_opaque = new IIcon[numCentres];
	}

	public void registerCenterIcons(IIconRegister register, String[] filenames)
	{
		for(int i = 0; i < centerIcons.length; i++)
		{
			centerIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
			centerIcons_opaque[i] = register.registerIcon("mekanism:models/opaque/" + filenames[i]);
		}
	}

	public void registerSideIcons(IIconRegister register, String[] filenames)
	{
		for(int i = 0; i < sideIcons.length; i++)
		{
			sideIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
			sideIcons_opaque[i] = register.registerIcon("mekanism:models/opaque/" + filenames[i]);
		}
	}

	public IIcon getSideIcon(int n)
	{
		return client.opaqueTransmitters ? sideIcons_opaque[n] : sideIcons[n];
	}

	public IIcon getCenterIcon(int n)
	{
		return client.opaqueTransmitters ? centerIcons_opaque[n] : centerIcons[n];
	}
}
