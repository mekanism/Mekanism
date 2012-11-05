package mekanism.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.server.FMLServerHandler;

import mekanism.client.ThreadSendData;
import net.minecraft.src.*;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 * @author AidanBrady
 *
 */
public class MekanismUtils
{
	/**
	 * Checks for a new version of Mekanism.
	 */
	public static void checkForUpdates(EntityPlayer entityplayer)
	{
		if(!Mekanism.latestVersionNumber.equals("Error retrieving data."))
		{
			if(!Mekanism.latestVersionNumber.equals(Mekanism.versionNumber))
			{
				entityplayer.addChatMessage(EnumColor.GREY + "Your version of " + EnumColor.DARK_BLUE + "Mekanism " + EnumColor.GREY + "(" + EnumColor.DARK_GREY + Mekanism.versionNumber + EnumColor.GREY + ") is outdated. Please update to version " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber);
				return;
			}
		}
		else {
			System.out.println("[Mekanism] Minecraft is in offline mode, could not check for updates.");
			return;
		}
	}
	
	/**
	 * Converts units into a nice String for display without color.
	 * @param energy
	 * @return displayed energy
	 */
	public static String getDisplayedEnergyNoColor(int energy)
	{
		if(energy < 1000)
		{
			return energy + " u";
		}
		else if(energy >= 1000 && energy < 10000)
		{
			return energy/10 + " kU";
		}
		else if(energy >= 10000 && energy < 100000)
		{
			return energy/100 + " mU";
		}
		else if(energy >= 100000 && energy < 1000000)
		{
			return energy/1000 + " gU";
		}
		else if(energy >= 1000000)
		{
			return energy/100000 + " tU";
		}
		else {
			return null;
		}
	}
	
	/**
	 * Converts units into a nice String for display with color.
	 * @param energy
	 * @return displayed energy
	 */
	public static String getDisplayedEnergy(int energy)
	{
		if(energy == 0)
		{
			return EnumColor.DARK_RED + "" + energy + " u" + EnumColor.DARK_GREY;
		}
		else if(energy < 1000)
		{
			return energy + " u";
		}
		else if(energy >= 1000 && energy < 10000)
		{
			return energy/10 + " kU";
		}
		else if(energy >= 10000 && energy < 100000)
		{
			return energy/100 + " mU";
		}
		else if(energy >= 100000 && energy < 1000000)
		{
			return energy/1000 + " gU";
		}
		else if(energy >= 1000000)
		{
			return energy/100000 + " tU";
		}
		else {
			return null;
		}
	}
	
	/**
	 * Gets the latest version using getHTML and returns it as a string.
	 * @return latest version
	 */
	public static String getLatestVersion()
	{
		String[] text = getHTML("http://dl.dropbox.com/u/90411166/Mod%20Versions/Mekanism.txt").split(":");
		if(!text[0].contains("UTF-8") && !text[0].contains("HTML")) return text[0];
		return "Error retrieving data.";
	}
	
	/**
	 * Gets the recent news using getHTML and returns it as a string.
	 * @return recent news
	 */
	public static String getRecentNews()
	{
		String[] text = getHTML("http://dl.dropbox.com/u/90411166/Mod%20Versions/Mekanism.txt").split(":");
		if(text.length > 1 && !text[1].contains("UTF-8") && !text[1].contains("HTML")) return text[1];
		return "There is no news to show.";
	}
	
	/**
	 * Returns one line of HTML from the url.
	 * @param urlToRead - URL to read from.
	 * @return HTML text from the url.
	 */
	public static String getHTML(String urlToRead) 
	{
		StringBuilder sb = new StringBuilder();
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
				sb.append(line);
			}
			rd.close();
		} catch (Exception e) {
			result = "Error retrieving data.";
			System.err.println("[Mekanism] An error occured while connecting to URL '" + urlToRead + ".'");
		}
		return result;
	}
	
	/**
	 * Sends a Packet3Chat packet to the defined player, with the defined message.
	 * @param player - Player to send packet to.
	 * @param msg - message sent to player.
	 */
	public static void sendChatMessageToPlayer(String playerUsername, String msg)
	{
		EntityPlayer player = FMLServerHandler.instance().getServer().getConfigurationManager().getPlayerForUsername(playerUsername);
		Packet3Chat chatPacket = new Packet3Chat(msg);
		if(player != null)
		{
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(chatPacket);
		}
	}
	
	/**
	 * Checks if the mod is running on the latest version.
	 * @return if mod is latest version
	 */
	public static boolean isLatestVersion()
	{
		return Mekanism.versionNumber.toString().equals(Mekanism.latestVersionNumber);
	}
	
	/**
	 * Checks if Minecraft is running in offline mode.
	 * @return if mod is running in offline mode.
	 */
	public static boolean isOffline()
	{
		try {
			new URL("http://www.apple.com").openConnection().connect();
			return true;
		} catch (IOException e)
		{
			return false;
		}
	}
	
	/**
	 * Sets the defined world's time to the defined time.
	 * @param world - world to set time
	 * @param paramInt - hour to set time to
	 */
	public static void setHourForward(World world, int paramInt)
	{
		long l1 = world.getWorldTime() / 24000L * 24000L;
	    long l2 = l1 + 24000L + paramInt * 1000;
	    world.setWorldTime(l2);
	}
	
	/**
	 * Creates a fake explosion at the declared player, with only sounds and effects. No damage is caused to either blocks or the player.
	 * @param entityplayer - player to explode
	 */
	public static void doFakeEntityExplosion(EntityPlayer entityplayer)
	{
		World world = entityplayer.worldObj;
		world.spawnParticle("hugeexplosion", entityplayer.posX, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D);
		world.playSoundAtEntity(entityplayer, "random.explode", 1.0F, 1.0F);
	}
	
	/**
	 * Creates a fake explosion at the declared coords, with only sounds and effects. No damage is caused to either blocks or the player.
	 * @param world - world where the explosion will occur
	 * @param x - x coord
	 * @param y - y coord
	 * @param z - z coord
	 */
	public static void doFakeBlockExplosion(World world, int x, int y, int z)
	{
		world.spawnParticle("hugeexplosion", x, y, z, 0.0D, 0.0D, 0.0D);
		world.playSound(x, y, z, "random.explode", 1.0F, 1.0F);
	}
}
