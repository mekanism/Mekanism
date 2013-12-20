package mekanism.client.render;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class PartTransmitterIcons
{
    private Icon sideIcon;
    private Icon[] centerIcons;

    public PartTransmitterIcons(int numCentres)
    {
        centerIcons = new Icon[numCentres];
    }

    public void registerCenterIcons(IconRegister register, String[] filenames)
    {
        for(int i = 0; i < centerIcons.length; i++)
        {
            centerIcons[i] = register.registerIcon("mekanism:models/" + filenames[i]);
        }
    }

    public void registerSideIcon(IconRegister register, String filename)
    {
        sideIcon = register.registerIcon("mekanism:models/" + filename);
    }

    public Icon getSideIcon()
    {
        return sideIcon;
    }

    public Icon getCenterIcon(int n)
    {
        return centerIcons[n];
    }
}
