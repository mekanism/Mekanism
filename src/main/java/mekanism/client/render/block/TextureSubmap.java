package mekanism.client.render.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Multi-texture class adapted from Chisel
 * Code licensed under GPLv2
 * @author AUTOMATIC_MAIDEN, asie, pokefenn, unpairedbracket
 */
public class TextureSubmap
{
	public int width, height;
	
	public TextureAtlasSprite icon;
	
	public TextureAtlasSprite icons[];

	public TextureSubmap(TextureAtlasSprite i, int w, int h)
	{
		icon = i;
		width = w;
		height = h;
		icons = new TextureAtlasSprite[width * height];

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void TexturesStitched(TextureStitchEvent.Post event)
	{
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				icons[y * width + x] = new TextureVirtual(icon, width, height, x, y);
			}
		}
	}
}
