package mekanism.common.multipart;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class TransmitterIcons
{
	private TextureAtlasSprite[] sideIcons;
	private TextureAtlasSprite[] centerIcons;

	public TransmitterIcons(int numCentres, int numSides)
	{
		sideIcons = new TextureAtlasSprite[numSides];
		centerIcons = new TextureAtlasSprite[numCentres];
	}

	public void registerCenterIcons(TextureMap register, String[] filenames)
	{
		for(int i = 0; i < centerIcons.length; i++)
		{
			centerIcons[i] = register.registerSprite(new ResourceLocation("mekanism:models/" + filenames[i]));
		}
	}

	public void registerSideIcons(TextureMap register, String[] filenames)
	{
		for(int i = 0; i < sideIcons.length; i++)
		{
			sideIcons[i] = register.registerSprite(new ResourceLocation("mekanism:models/" + filenames[i]));
		}
	}

	public TextureAtlasSprite getSideIcon(int n)
	{
		return sideIcons[n];
	}

	public TextureAtlasSprite getCenterIcon(int n)
	{
		return centerIcons[n];
	}
}
