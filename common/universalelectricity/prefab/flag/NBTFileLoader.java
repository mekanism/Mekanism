package universalelectricity.prefab.flag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;

public class NBTFileLoader
{
	/**
	 * Saves NBT data in the world folder.
	 * 
	 * @return True on success.
	 */
	public static boolean saveData(File saveDirectory, String filename, NBTTagCompound data)
	{
		try
		{
			File tempFile = new File(saveDirectory, filename + "_tmp.dat");
			File file = new File(saveDirectory, filename + ".dat");

			CompressedStreamTools.writeCompressed(data, new FileOutputStream(tempFile));

			if (file.exists())
			{
				file.delete();
			}

			tempFile.renameTo(file);

			FMLLog.fine("Saved " + filename + " NBT data file successfully.");
			return true;
		}
		catch (Exception e)
		{
			System.out.println("Failed to save " + filename + ".dat!");
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveData(String filename, NBTTagCompound data)
	{
		return saveData(getSaveDirectory(MinecraftServer.getServer().getFolderName()), filename, data);
	}

	/**
	 * Reads NBT data from the world folder.
	 * 
	 * @return The NBT data
	 */
	public static NBTTagCompound loadData(File saveDirectory, String filename)
	{
		try
		{
			File file = new File(saveDirectory, filename + ".dat");

			if (file.exists())
			{
				FMLLog.fine("Loaded " + filename + " data.");
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
			else
			{
				FMLLog.fine("Created new " + filename + " data.");
				return new NBTTagCompound();
			}
		}
		catch (Exception e)
		{
			System.out.println("Failed to load " + filename + ".dat!");
			e.printStackTrace();
			return null;
		}
	}

	public static NBTTagCompound loadData(String filename)
	{
		return loadData(getSaveDirectory(MinecraftServer.getServer().getFolderName()), filename);
	}

	public static File getSaveDirectory(String worldName)
	{
		File parent = getBaseDirectory();

		if (FMLCommonHandler.instance().getSide().isClient())
		{
			parent = new File(getBaseDirectory(), "saves" + File.separator);
		}

		return new File(parent, worldName + File.separator);
	}

	public static File getBaseDirectory()
	{
		if (FMLCommonHandler.instance().getSide().isClient())
		{
			FMLClientHandler.instance().getClient();
			return Minecraft.getMinecraftDir();
		}
		else
		{
			return new File(".");
		}
	}
}
