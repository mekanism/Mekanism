package mekanism.client;

import java.io.File;
import java.io.FileOutputStream;
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
	private int downloadType;
	private int bytesDownloaded;
	private int lastBytesDownloaded;
	private byte[] buffer = new byte[10240];
	private URL url;
	
	public static int modulesBeingDownloaded;
	public static boolean hasUpdated;
	
	public ThreadClientUpdate(String location, int type)
	{
		modulesBeingDownloaded++;
		downloadType = type;
		try {
			url = new URL(location);
			setDaemon(false);
			start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		String downloadName = downloadType == 0 ? "" : (downloadType == 1 ? "Generators" : "Tools");
		File download = new File(new StringBuilder().append(Minecraft.getMinecraftDir()).append(File.separator + "mods" + File.separator + "Mekanism" + downloadName + "-v" + Mekanism.latestVersionNumber + ".jar").toString());
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
			
			modulesBeingDownloaded--;
			finalize();
		} catch(Throwable e)
		{
			GuiCredits.onErrorDownloading();
			System.err.println("[Mekanism] Error while finishing update thread: " + e.getMessage());
			try {
				modulesBeingDownloaded--;
				finalize();
			} catch (Throwable e1) {
				System.err.println("[Mekanism] Error while finalizing update thread: " + e1.getMessage());
			}
		}
	}
	
	/**
	 * Prepares to update to the latest version of Mekanism by removing the old files. 
	 */
	public void prepareForDownload()
	{
		File[] modsList = new File(new StringBuilder().append(Minecraft.getMinecraftDir()).append(File.separator + "mods").toString()).listFiles();
		
		for(File file : modsList)
		{
			if(file.getName().startsWith("Mekanism") && file.getName().endsWith(".jar") && !file.getName().contains(Mekanism.latestVersionNumber))
			{
				file.delete();
			}
		}
		
		System.out.println("[Mekanism] Preparing to update...");
	}
}
