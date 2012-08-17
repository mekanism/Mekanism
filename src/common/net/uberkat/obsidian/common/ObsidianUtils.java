package net.uberkat.obsidian.common;

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

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.server.FMLServerHandler;

import net.minecraft.src.*;
import net.uberkat.obsidian.client.ThreadServerData;

/**
 * Official Obsidian Ingots utilities. All miscellaneous methods are located here.
 * @author AidanBrady
 *
 */
public class ObsidianUtils
{
	/**
	 * Checks for a new version of Obsidian Ingots.
	 */
	public static void checkForUpdates(EntityPlayer entityplayer)
	{
		if(!(getLatestVersion().toString().equals("Error retrieving data.")) && !(getLatestVersion().toString().equals(ObsidianIngots.versionNumber.toString())))
		{
			entityplayer.addChatMessage("Your version of ¤1Obsidian Ingots ¤7(¤8" + ObsidianIngots.versionNumber.toString() + "¤7) is outdated. Please update to version ¤8" + getLatestVersion().toString());
		}
		else if(getLatestVersion().toString().equals("Error retrieving data."))
		{
			System.out.println("[ObsidianIngots] Minecraft is in offline mode, could not check for updates.");
		}
	}
	
	/**
	 * Gets the latest version using getHTML and returns it as a string.
	 * @return latest version
	 */
	public static String getLatestVersion()
	{
		String[] text = getHTML("http://dl.dropbox.com/u/90411166/Mod%20Versions/ObsidianIngots.txt").split(":");
		return text[0];
	}
	
	/**
	 * Gets the recent news using getHTML and returns it as a string.
	 * @return recent news
	 */
	public static String getRecentNews()
	{
		String[] text = getHTML("http://dl.dropbox.com/u/90411166/Mod%20Versions/ObsidianIngots.txt").split(":");
		if(text.length > 1) return text[1];
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
			System.err.println("[ObsidianIngots] An error occured while connecting to URL '" + urlToRead + ".'");
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
			((EntityPlayerMP)player).serverForThisPlayer.sendPacketToPlayer(chatPacket);
		}
	}
	
	/**
	 * Sends the server the defined packet int.
	 * @param i - int to send
	 */
	public static void sendPacketDataInt(int i)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try {
			data.writeInt(i);
		} catch (IOException e) {
			System.out.println("[ObsidianIngots] An error occured while writing packet data.");
			e.printStackTrace();
		}
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "ObsidianIngots";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        System.out.println("[ObsidianIngots] Sent '" + i + "' packet to server");
	}
	
	/**
	 * Checks if the game is running on multiplayer.
	 * @return - if world is multiplayer
	 */
	public static boolean isMultiplayer()
	{
		if(!FMLServerHandler.instance().getServer().isSinglePlayer())
		{
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if the mod is running on the latest version.
	 * @return - if mod is latest version
	 */
	public static boolean isClientLatestVersion()
	{
		if(ObsidianIngots.versionNumber.toString().equals(ObsidianIngots.latestVersionNumber))
		{
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if Minecraft is running in offline mode.
	 * @return - if mod is running in offline mode.
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
	 * Creates a fake explosion, with only sounds and effects. No damage is caused, to blocks or the player.
	 * @param entityplayer - player to explode
	 */
	public static void doExplosion(EntityPlayer entityplayer)
	{
		World world = entityplayer.worldObj;
		world.spawnParticle("hugeexplosion", entityplayer.posX, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D);
		world.playSoundAtEntity(entityplayer, "random.explode", 1.0F, 1.0F);
	}
}
