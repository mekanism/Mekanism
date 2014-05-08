package mekanism.common.multipart;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class TransmitterIcons
{
	private IIcon[] sideIcons;
	private IIcon[] centerIcons;

	public TransmitterIcons(int numCentres, int numSides)
	{
		sideIcons = new IIcon[numSides];
		centerIcons = new IIcon[numCentres];
	}

	public void registerCenterIcons(IIconRegister register, String[] filenames)
	{
		for(int i = 0; i < centerIcons.length; i++)
		{
			centerIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
		}
	}

	public void registerSideIcons(IIconRegister register, String[] filenames)
	{
		for(int i = 0; i < sideIcons.length; i++)
		{
			sideIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
		}
	}

	public IIcon getSideIcon(int n)
	{
		return sideIcons[n];
	}

	public IIcon getCenterIcon(int n)
	{
		return centerIcons[n];
	}
}
