package mekanism.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
/**
 * Thread that downloads the latest release of Mekanism. The older file is deleted and the newly downloaded file takes it's place.
 * @author AidanBrady
 *
 */
public class ThreadClientUpdate extends Thread
{
	private int bytesDownloaded;
	private int lastBytesDownloaded;
	private byte[] buffer = new byte[10240];
	private URL url;
	
	public ThreadClientUpdate(String location)
	{
		try {
			url = new URL(location);
			setDaemon(true);
			start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		File download = new File(new StringBuilder().append(Minecraft.getMinecraftDir()).append("/mods/Mekanism.jar").toString());
		try {
			prepareForDownload();
			download.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(download.getAbsolutePath());
			InputStream stream = url.openStream();
			
			while((lastBytesDownloaded = stream.read(buffer)) > 0)
			{
				outputStream.write(buffer, 0, lastBytesDownloaded);
				buffer = new byte[10240];
				bytesDownloaded += lastBytesDownloaded;
			}
			
			outputStream.close();
			stream.close();
			GuiCredits.onFinishedDownloading();
			System.out.println("[Mekanism] Successfully updated to latest version (" + Mekanism.latestVersionNumber + ").");
			finalize();
			
		} catch(Throwable e)
		{
			GuiCredits.onErrorDownloading();
			System.err.println("[Mekanism] Error while finishing update thread: " + e.getMessage());
			try {
				finalize();
			} catch (Throwable e1) {
				System.err.println("[Mekanism] Error while finalizing update thread: " + e1.getMessage());
			}
		}
	}
	
	/**
	 * Prepares to update to the latest version of Mekanism by deleting the files "Mekanism.cfg" and "Mekanism.jar." 
	 */
	public void prepareForDownload()
	{
		File download = new File(new StringBuilder().append(Minecraft.getMinecraftDir()).append("/mods/Mekanism.jar").toString());
		File config = new File(new StringBuilder().append(Minecraft.getMinecraftDir()).append("/config/Mekanism.cfg").toString());
		
		if(download.exists())
		{
			download.delete();
		}
		if(config.exists())
		{
			config.delete();
		}
		System.out.println("[Mekanism] Preparing to update...");
	}
}
