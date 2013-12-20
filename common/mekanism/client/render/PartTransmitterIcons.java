package mekanism.client.render;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class PartTransmitterIcons
{
    private Icon[] sideIcons;
    private Icon[] centerIcons;

    public PartTransmitterIcons(int numCentres, int numSides)
    {
    	sideIcons = new Icon[numSides];
        centerIcons = new Icon[numCentres];
    }

    public void registerCenterIcons(IconRegister register, String[] filenames)
    {
        for(int i = 0; i < centerIcons.length; i++)
        {
            centerIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
        }
    }

    public void registerSideIcons(IconRegister register, String[] filenames)
    {
    	for(int i = 0; i < sideIcons.length; i++)
    	{
    		sideIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
    	}
    }

    public Icon getSideIcon(int n)
    {
        return sideIcons[n];
    }

    public Icon getCenterIcon(int n)
    {
        return centerIcons[n];
    }
}
