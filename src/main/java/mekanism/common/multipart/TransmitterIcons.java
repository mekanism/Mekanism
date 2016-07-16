package mekanism.common.multipart;

import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class TransmitterIcons
{
	private IIcon[] sideIcons;
	private IIcon[] centerIcons;
	private boolean opaque = Mekanism.configuration.get("client", "opaque", false).getBoolean();

	public TransmitterIcons(int numCentres, int numSides)
	{
		sideIcons = new IIcon[numSides];
		centerIcons = new IIcon[numCentres];
	}

	public void registerCenterIcons(IIconRegister register, String[] filenames)
	{
		for(int i = 0; i < centerIcons.length; i++)
		{
			if(!this.opaque) {
                centerIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
            } else {
                centerIcons[i] = register.registerIcon("mekanism:models/opaque/" + filenames[i]);
            }
		}
	}

	public void registerSideIcons(IIconRegister register, String[] filenames)
	{
		for(int i = 0; i < sideIcons.length; i++)
		{
			if(!this.opaque) {
                sideIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
            } else {
                sideIcons[i] = register.registerIcon("mekanism:models/opaque/" + filenames[i]);
            }
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
