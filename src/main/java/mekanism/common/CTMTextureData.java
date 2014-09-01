package mekanism.common;

import mekanism.client.render.block.TextureSubmap;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class CTMTextureData
{
	public IIcon icon;

	public TextureSubmap submap;

	public TextureSubmap submapSmall;

	public String texture;

	public CTMTextureData(String textureName)
	{
		texture = textureName;
	}

	public void registerIcons(IIconRegister register)
	{
		icon = register.registerIcon("mekanism:" + texture);
		submap = new TextureSubmap(register.registerIcon("mekanism:" + texture + "-ctm"), 4, 4);
		submapSmall = new TextureSubmap(icon, 2, 2);
	}


}
