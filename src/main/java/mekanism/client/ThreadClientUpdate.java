package mekanism.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mekanism.api.EnumColor;
import mekanism.client.gui.GuiCredits;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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

	private static File modsDir = new File(Mekanism.proxy.getMinecraftDir(), "mods");
	private static File tempDir = new File(modsDir, "temp");
	private static URL zipUrl = createURL();

	public static boolean hasUpdated;

	public ThreadClientUpdate()
	{
		setDaemon(false);
		start();
	}

	@Override
	public void run()
	{
		try {
			deleteTemp();
			createTemp();

			File download = new File(tempDir, "builds.zip");

			prepareForDownload();
			download.createNewFile();

			GuiCredits.updateInfo("Downloading...");

			FileOutputStream outputStream = new FileOutputStream(download.getAbsolutePath());
			InputStream stream = zipUrl.openStream();

			while((lastBytesDownloaded = stream.read(buffer)) > 0)
			{
				outputStream.write(buffer, 0, lastBytesDownloaded);
				buffer = new byte[10240];
				bytesDownloaded += lastBytesDownloaded;
			}

			outputStream.close();
			stream.close();

			if(Mekanism.versionNumber.comparedState(Version.get(Mekanism.latestVersionNumber)) == -1)
			{
				ZipInputStream zip = new ZipInputStream(new FileInputStream(download));
				deployEntry(zip, "Mekanism-");
				zip.close();
			}

			for(IModule module : Mekanism.modulesLoaded)
			{
				if(module.getVersion().comparedState(Version.get(Mekanism.latestVersionNumber)) == -1)
				{
					ZipInputStream zip = new ZipInputStream(new FileInputStream(download));
					deployEntry(zip, "Mekanism" + module.getName());
					zip.close();
				}
			}

			deleteTemp();

			hasUpdated = true;
			GuiCredits.updateInfo("Update installed, reboot Minecraft for changes.");
			Mekanism.logger.info("Successfully updated to latest version (" + Mekanism.latestVersionNumber + ").");

			finalize();
		} catch(Throwable t) {
			GuiCredits.updateInfo(EnumColor.DARK_RED + "Error updating.");
			hasUpdated = true;
			Mekanism.logger.error("Error while finishing update thread: " + t.getMessage());
			t.printStackTrace();
		}
	}

	private void deployEntry(ZipInputStream zip, String filePrefix) throws IOException
	{
		byte[] zipBuffer = new byte[1024];
		ZipEntry entry = zip.getNextEntry();

		while(entry != null)
		{
			if(entry.isDirectory())
			{
				continue;
			}

			if(entry.getName().contains(filePrefix))
			{
				File modFile = new File(modsDir, entry.getName().replace("output/", ""));

				if(modFile.exists())
				{
					modFile.delete();
				}

				modFile.createNewFile();

				FileOutputStream outStream = new FileOutputStream(modFile);

				int len;

				while((len = zip.read(zipBuffer)) > 0)
				{
					outStream.write(zipBuffer, 0, len);
				}

				zip.closeEntry();
				outStream.close();
				break;
			}

			entry = zip.getNextEntry();
		}
	}

	private void createTemp() throws IOException
	{
		if(!tempDir.exists())
		{
			tempDir.mkdir();
		}
	}

	private void deleteTemp() throws IOException
	{
		if(tempDir.exists())
		{
			clearFiles(tempDir);
		}
	}

	private void clearFiles(File file)
	{
		if(file.isDirectory())
		{
			for(File sub : file.listFiles())
			{
				clearFiles(sub);
			}
		}

		file.delete();
	}

	private void prepareForDownload()
	{
		File[] modsList = new File(new StringBuilder().append(Mekanism.proxy.getMinecraftDir()).append(File.separator + "mods").toString()).listFiles();

		if(Mekanism.versionNumber.comparedState(Version.get(Mekanism.latestVersionNumber)) == -1)
		{
			for(File file : modsList)
			{
				if(file.getName().startsWith("Mekanism-") && file.getName().endsWith(".jar") && !file.getName().contains(Mekanism.latestVersionNumber))
				{
					file.delete();
				}
			}
		}

		for(IModule module : Mekanism.modulesLoaded)
		{
			for(File file : modsList)
			{
				if(file.getName().startsWith("Mekanism" + module.getName()) && file.getName().endsWith(".jar") && !file.getName().contains(Mekanism.latestVersionNumber))
				{
					file.delete();
				}
			}
		}

		Mekanism.logger.info("Preparing to update...");
	}

	private static URL createURL()
	{
		try {
			return new URL("http://ci.aidancbrady.com/job/Mekanism/Recommended/artifact/*zip*/archive.zip");
		} catch(Exception e) {}

		return null;
	}
}
