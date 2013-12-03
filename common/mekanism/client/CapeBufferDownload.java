package mekanism.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;

import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CapeBufferDownload extends Thread
{
	public String username;

	public String staticCapeUrl;
	
	public ResourceLocation resourceLocation;
	
	public ThreadDownloadImageData capeImage;

	boolean downloaded = false;

	public CapeBufferDownload(String name, String url) 
	{
		username = name;
		staticCapeUrl = url;
		
		setDaemon(true);
		setName("Cape Downlaod Thread");
	}

	@Override
	public void run() 
	{
		try {
			download();
		} catch(Exception e) {}
	}

	private void download() 
	{
		try {
			resourceLocation = new ResourceLocation("mekanism/" + StringUtils.stripControlCodes(username));
			
			Method method = MekanismUtils.getPrivateMethod(AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_getDownloadImage, ResourceLocation.class, String.class, ResourceLocation.class, IImageBuffer.class);
			Object obj = method.invoke(null, resourceLocation, staticCapeUrl, null, null);
			
			if(obj instanceof ThreadDownloadImageData)
			{
				capeImage = (ThreadDownloadImageData)obj;
			}
		} catch(Exception e) {}

		downloaded = true;
	}

	public ThreadDownloadImageData getImage()
	{
		return capeImage;
	}

	public ResourceLocation getResourceLocation()
	{
		return resourceLocation;
	}
}
