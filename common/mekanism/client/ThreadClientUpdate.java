package mekanism.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;

/**
 * Thread that downloads the latest release of Mekanism. The older file is deleted and the newly downloaded file takes it's place.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class ThreadClientUpdate extends Thread
{
	private int bytesDownloaded;
	private int lastBytesDownloaded;
	private byte[] buffer = new byte[10240];
	private URL url;
	public String moduleName;
	
	public static int modulesBeingDownloaded;
	public static boolean hasUpdated;
	
	public ThreadClientUpdate(String location, String name)
	{
		moduleName = name;
		modulesBeingDownloaded++;
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
		File download = new File(new StringBuilder().append(Minecraft.getMinecraftDir()).append(File.separator + "mods" + File.separator + "Mekanism" + moduleName + "-v" + Mekanism.latestVersionNumber + ".jar").toString());
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
			if(file.getName().startsWith("Mekanism" + moduleName) && file.getName().endsWith(".jar") && !file.getName().contains(Mekanism.latestVersionNumber))
			{
				file.delete();
			}
		}
		
		System.out.println("[Mekanism] Preparing to update...");
	}
}
