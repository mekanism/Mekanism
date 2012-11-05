package mekanism.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.client.registry.RenderingRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ModTextureAnimation;
import net.minecraft.src.RenderEngine;

public class TextureAnimatedFX extends ModTextureAnimation
{
	public TextureAnimatedFX(String texture, int index) throws IOException
	{
		super(index, 1, texture, TextureFXManager.instance().loadImageFromTexturePack(FMLClientHandler.instance().getClient().renderEngine, texture), 5);
	}
	
    public void bindImage(RenderEngine renderengine)
    {
    	//Binds texture with GL11 to use specific icon index.
        GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, renderengine.getTexture("/textures/terrain.png"));
    }
}
