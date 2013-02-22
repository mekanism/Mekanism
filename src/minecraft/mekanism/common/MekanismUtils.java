package mekanism.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import universalelectricity.core.vector.Vector3;

import mekanism.api.EnumGas;
import mekanism.api.IActiveState;
import mekanism.api.IConfigurable;
import mekanism.api.IGasAcceptor;
import mekanism.api.IPressurizedTube;
import mekanism.api.ITubeConnection;
import mekanism.api.InfuseObject;
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
		if(Mekanism.updateNotifications)
		{
			if(!Mekanism.latestVersionNumber.equals("Error retrieving data."))
			{
				if(Version.get(Mekanism.latestVersionNumber).comparedState(Mekanism.versionNumber) == 1)
				{
					entityplayer.addChatMessage(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------");
					entityplayer.addChatMessage(EnumColor.GREY + " Using outdated version " + EnumColor.DARK_GREY + Mekanism.versionNumber + EnumColor.GREY + " for Minecraft 1.4.6/7.");
					entityplayer.addChatMessage(EnumColor.GREY + " Consider updating to version " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber);
					entityplayer.addChatMessage(EnumColor.GREY + " New features: " + EnumColor.INDIGO + Mekanism.recentNews);
					entityplayer.addChatMessage(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------");
					return;
				}
				else if(Version.get(Mekanism.latestVersionNumber).comparedState(Mekanism.versionNumber) == -1)
				{
					entityplayer.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Using developer build " + EnumColor.DARK_GREY + Mekanism.versionNumber);
				}
			}
			else {
				System.out.println("[Mekanism] Minecraft is in offline mode, could not check for updates.");
				return;
			}
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
    
    /**
     * Returns an integer facing that converts a world-based orientation to a machine-based oriention.
     * @param side - world based
     * @param blockFacing - what orientation the block is facing
     * @return machine orientation
     */
    public static int getBaseOrientation(int side, int blockFacing)
    {
    	if(blockFacing == 3 || side == 1 || side == 0)
    	{
    		if(side == 2 || side == 3)
    		{
    			return ForgeDirection.getOrientation(side).getOpposite().ordinal();
    		}
    		
    		return side;
    	}
    	else if(blockFacing == 2)
    	{
    		if(side == 2 || side == 3)
    		{
    			return side;
    		}
    		
    		return ForgeDirection.getOrientation(side).getOpposite().ordinal();
    	}
    	else if(blockFacing == 4)
    	{
    		return getRight(side).ordinal();
    	}
    	else if(blockFacing == 5)
    	{
    		return getLeft(side).ordinal();
    	}
    	
    	return side;
    }
    
    /**
     * Increments the output type of a machine's side.
     * @param config - configurable machine
     * @param side - side to increment output of
     */
    public static void incrementOutput(IConfigurable config, int side)
    {
    	int max = config.getSideData().size()-1;
    	int current = config.getSideData().indexOf(config.getSideData().get(config.getConfiguration()[side]));
    	
    	if(current < max)
    	{
    		config.getConfiguration()[side] = (byte)(current+1);
    	}
    	else if(current == max)
    	{
    		config.getConfiguration()[side] = 0;
    	}
    }
    
    /**
     * Gets the infuse object from an ItemStack.
     * @param itemStack - itemstack to check
     * @return infuse object
     */
    public static InfuseObject getInfuseObject(ItemStack itemStack)
    {
    	if(itemStack != null)
    	{
	    	for(Map.Entry<ItemStack, InfuseObject> entry : Mekanism.infusions.entrySet())
	    	{
	    		if(itemStack.isItemEqual(entry.getKey()))
	    		{
	    			return entry.getValue();
	    		}
	    	}
    	}
    	
    	return null;
    }
    
    /**
     * Gets the operating ticks required for a machine via it's upgrades.
     * @param multiplier - speed multiplier
     * @return max operating ticks
     */
    public static int getTicks(int multiplier)
    {
    	return 200/(multiplier+1);
    }
    
    /**
     * Gets the maximum energy for a machine via it's upgrades.
     * @param multiplier - energy multiplier
     * @param def - original, definitive max energy
     * @return max energy
     */
    public static double getEnergy(int multiplier, double def)
    {
    	return def*(multiplier+1);
    }
    
    /**
     * Gets all the tubes around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of TileEntities
     */
    public static TileEntity[] getConnectedTubes(TileEntity tileEntity)
    {
    	TileEntity[] tubes = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.values())
    	{
    		if(orientation != ForgeDirection.UNKNOWN)
    		{
    			TileEntity tube = Vector3.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
    			
    			if(tube instanceof IPressurizedTube && ((IPressurizedTube)tube).canTransferGas())
    			{
    				tubes[orientation.ordinal()] = tube;
    			}
    		}
    	}
    	
    	return tubes;
    }
    
    /**
     * Gets all the acceptors around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of IGasAcceptors
     */
    public static IGasAcceptor[] getConnectedAcceptors(TileEntity tileEntity)
    {
    	IGasAcceptor[] acceptors = new IGasAcceptor[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.values())
    	{
    		if(orientation != ForgeDirection.UNKNOWN)
    		{
    			TileEntity acceptor = Vector3.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
    			
    			if(acceptor instanceof IGasAcceptor)
    			{
    				acceptors[orientation.ordinal()] = (IGasAcceptor)acceptor;
    			}
    		}
    	}
    	
    	return acceptors;
    }
    
    /**
     * Gets all the tube connections around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of ITubeConnections
     */
    public static ITubeConnection[] getConnections(TileEntity tileEntity)
    {
    	ITubeConnection[] connections = new ITubeConnection[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.values())
    	{
    		if(orientation != ForgeDirection.UNKNOWN)
    		{
    			TileEntity connection = Vector3.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
    			
    			if(connection instanceof ITubeConnection)
    			{
    				connections[orientation.ordinal()] = (ITubeConnection)connection;
    			}
    		}
    	}
    	
    	return connections;
    }
    
    /**
     * Emits a defined gas to the network.
     * @param type - gas type to send
     * @param amount - amount of gas to send
     * @param sender - the sender of the gas
     * @param facing - side the sender is outputting from
     * @return rejected gas
     */
    public static int emitGasToNetwork(EnumGas type, int amount, TileEntity sender, ForgeDirection facing)
    {
    	TileEntity pointer = Vector3.getTileEntityFromSide(sender.worldObj, new Vector3(sender.xCoord, sender.yCoord, sender.zCoord), facing);
    	
    	if(pointer != null)
    	{
	    	GasTransferProtocol calculation = new GasTransferProtocol(pointer, type, amount);
	    	return calculation.calculate();
    	}
    	
    	return amount;
    }
}
