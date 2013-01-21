package mekanism.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import mekanism.api.IActiveState;
import mekanism.api.Tier.EnergyCubeTier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.server.FMLServerHandler;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 * @author AidanBrady
 *
 */
public final class MekanismUtils
{
	/**
	 * Checks for a new version of Mekanism.
	 */
	public static void checkForUpdates(EntityPlayer entityplayer)
	{
		if(!Mekanism.latestVersionNumber.equals("Error retrieving data."))
		{
			if(!Mekanism.latestVersionNumber.contains(Mekanism.versionNumber.toString()))
			{
				entityplayer.addChatMessage(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------");
				entityplayer.addChatMessage(EnumColor.GREY + " Using outdated version " + EnumColor.DARK_GREY + Mekanism.versionNumber + EnumColor.GREY + " for Minecraft 1.4.6/7.");
				entityplayer.addChatMessage(EnumColor.GREY + " Consider updating to version " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber);
				entityplayer.addChatMessage(EnumColor.GREY + " New features: " + EnumColor.INDIGO + Mekanism.recentNews);
				entityplayer.addChatMessage(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------");
				return;
			}
		}
		else {
			System.out.println("[Mekanism] Minecraft is in offline mode, could not check for updates.");
			return;
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
			while ((line = rd.readLine()) != null) 
			{
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
	
	public static void sendChatMessageToAllPlayers(String msg)
	{
		PacketDispatcher.sendPacketToAllPlayers(new Packet3Chat(msg));
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
		world.playSound(x, y, z, "random.explode", 1.0F, 1.0F, true);
	}
	
	/**
	 * Copies an ItemStack and returns it with a defined stackSize.
	 * @param itemstack - stack to change size
	 * @param size - size to change to
	 * @return resized ItemStack
	 */
	public static ItemStack getStackWithSize(ItemStack itemstack, int size)
	{
		ItemStack newStack = itemstack.copy();
		newStack.stackSize = size;
		return newStack;
	}
	
	/**
	 * Adds a recipe directly to the CraftingManager that works with the Forge Ore Dictionary.
	 * @param output the ItemStack produced by this recipe
	 * @param params the items/blocks/itemstacks required to create the output ItemStack
	 */
	public static void addRecipe(ItemStack output, Object[] params)
	{
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(output, params));
	}
	
	/**
	 * Retrieves an empty Energy Cube with a defined tier.
	 * @param tier - tier to add to the Energy Cube
	 * @return empty energy cube with defined tier
	 */
	public static ItemStack getEnergyCubeWithTier(EnergyCubeTier tier)
	{
		ItemStack itemstack = ((ItemBlockEnergyCube)new ItemStack(Mekanism.EnergyCube).getItem()).getUnchargedItem(tier);
		return itemstack;
	}
	
	/**
	 * Checks if a machine is in it's active state.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return if machine is active
	 */
    public static boolean isActive(IBlockAccess world, int x, int y, int z)
    {
    	TileEntity tileEntity = (TileEntity)world.getBlockTileEntity(x, y, z);
    	if(tileEntity != null)
    	{
    		if(tileEntity instanceof IActiveState)
    		{
    			return ((IActiveState)tileEntity).getActive();
    		}
    	}
    	return false;
    }
    
    /**
     * Gets the left side of a certain orientation.
     * @param orientation
     * @return left side
     */
    public static ForgeDirection getLeft(int orientation)
    {
    	switch(orientation)
    	{
    		case 2:
    			return ForgeDirection.EAST;
    		case 3:
    			return ForgeDirection.WEST;
    		case 4:
    			return ForgeDirection.SOUTH;
    		default:
    			return ForgeDirection.NORTH;
    	}
    }
    
    /**
     * Gets the right side of a certain orientation.
     * @param orientation
     * @return right side
     */
    public static ForgeDirection getRight(int orientation)
    {
    	return getLeft(orientation).getOpposite();
    }
    
    /**
     * Checks to see if a specified ItemStack is stored in the Ore Dictionary with the specified name.
     * @param check - ItemStack to check
     * @param oreDict - name to check with
     * @return if the ItemStack has the Ore Dictionary key
     */
    public static boolean oreDictCheck(ItemStack check, String oreDict)
    {
    	boolean hasResource = false;
    	
    	for(ItemStack ore : OreDictionary.getOres(oreDict))
    	{
    		if(ore.isItemEqual(check))
    		{
    			hasResource = true;
    		}
    	}
    	
    	return hasResource;
    }
}
