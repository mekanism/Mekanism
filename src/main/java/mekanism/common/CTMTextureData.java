package mekanism.common;

import mekanism.client.render.texture.TextureSubmap;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class CTMTextureData
{
	public TextureAtlasSprite icon;

	public TextureSubmap submap;

	public TextureSubmap submapSmall;

	public String texture;

	public CTMTextureData(String textureName)
	{
		texture = textureName;
	}

	public void registerIcons(TextureMap register)
	{
		icon = register.registerSprite(new ResourceLocation("mekanism", texture));
		submap = new TextureSubmap(register.registerSprite(new ResourceLocation("mekanism", texture + "-ctm")), 4, 4);
		submapSmall = new TextureSubmap(icon, 2, 2);
	}


}
