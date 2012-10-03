package net.uberkat.obsidian.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.src.ICommandSender;
import net.minecraft.src.ModLoader;
import net.uberkat.obsidian.common.ObsidianIngots;
/**
 * Thread that downloads the latest release of Obsidian Ingots. The older file is deleted and the newly downloaded file takes it's place.
 * @author AidanBrady
 *
 */
public class ThreadServerUpdate extends Thread
{
	private ICommandSender sender;
	private int bytesDownloaded;
	private int lastBytesDownloaded;
	private byte[] buffer = new byte[10240];
	private URL url;
	
	public ThreadServerUpdate(String location, ICommandSender player)
	{
		sender = player;
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
			sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots] " + EnumColor.GREY.code + "Successfully updated to version " + EnumColor.DARK_GREY.code + ObsidianIngots.latestVersionNumber);
			System.out.println("[ObsidianIngots] Successfully updated to latest version (" + ObsidianIngots.latestVersionNumber + ").");
			finalize();
			
		} catch(Throwable e)
		{
			sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots] " + EnumColor.GREY.code + "Unable to update to version " + EnumColor.DARK_GREY.code + ObsidianIngots.latestVersionNumber);
			System.err.println("[ObsidianIngots] Error while finishing update thread: " + e.getMessage());
			try {
				finalize();
			} catch (Throwable e1) {
				System.err.println("[ObsidianIngots] Error while finalizing update thread: " + e1.getMessage());
			}
		}
	}
	
	/**
	 * Prepares to update to the latest version of Obsidian Ingots by deleting the files "ObsidianIngots.cfg" and "ObsidianIngots.jar." 
	 */
	public void prepareForDownload()
	{
		File download = new File(new StringBuilder().append(ModLoader.getMinecraftInstance().getMinecraftDir()).append("/mods/ObsidianIngots.jar").toString());
		File config = new File(new StringBuilder().append(ModLoader.getMinecraftInstance().getMinecraftDir()).append("/config/ObsidianIngots.cfg").toString());
		
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
