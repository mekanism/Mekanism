package net.uberkat.obsidian.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.src.ModLoader;
import net.uberkat.obsidian.common.ObsidianIngotsCore;
/**
 * Thread that downloads the latest release of Obsidian Ingots. The older file is deleted and the newly downloaded file takes it's place.
 * @author AidanBrady
 *
 */
public class ThreadUpdate extends Thread
{
	private int bytesDownloaded;
	private int lastBytesDownloaded;
	private byte[] buffer = new byte[10240];
	private URL url;
	
	public ThreadUpdate(String location)
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
		File download = new File(new StringBuilder().append(ModLoader.getMinecraftInstance().getMinecraftDir()).append("/mods/ObsidianIngots.jar").toString());
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
			System.out.println("[ObsidianIngots] Successfully updated to latest version (" + ObsidianIngotsCore.latestVersionNumber + ").");
			finalize();
			
		} catch(Throwable e)
		{
			GuiCredits.onErrorDownloading();
			System.err.println("[ObsidianIngots] Error while finishing update thread: " + e.getMessage());
			try {
				finalize();
			} catch (Throwable e1) {
				System.err.println("[ObsidianIngots] Error while finalizing update thread: " + e1.getMessage());
			}
		}
	}
	
	public void prepareForDownload()
	{
		File download = new File(new StringBuilder().append(ModLoader.getMinecraftInstance().getMinecraftDir()).append("/mods/ObsidianIngots.jar").toString());
		File config = new File(new StringBuilder().append(ModLoader.getMinecraftInstance().getMinecraftDir()).append("/config/mod_ObsidianIngots.txt").toString());
		if(download.exists())
		{
			download.delete();
		}
		if(config.exists())
		{
			config.delete();
		}
		System.out.println("[ObsidianIngots] Preparing to update...");
	}
}
