package mekanism.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.Coord4D;
import mekanism.common.DynamicTankCache;
import mekanism.common.IActiveState;
import mekanism.common.IFactory;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.IInvConfiguration;
import mekanism.common.IModule;
import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.Teleporter;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.Version;
import mekanism.common.inventory.container.ContainerElectricChest;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.tileentity.TileEntityAdvancedBoundingBlock;
import mekanism.common.tileentity.TileEntityBoundingBlock;
import mekanism.common.tileentity.TileEntityDynamicTank;
import mekanism.common.tileentity.TileEntityElectricChest;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.server.FMLServerHandler;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 * @author AidanBrady
 *
 */
public final class MekanismUtils
{
	public static final ForgeDirection[] SIDE_DIRS = new ForgeDirection[] {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST};
	
	/**
	 * Checks for a new version of Mekanism.
	 */
	public static boolean checkForUpdates(EntityPlayer entityplayer)
	{
		try {
			if(Mekanism.updateNotifications && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
			{
				if(!Mekanism.latestVersionNumber.equals("null"))
				{
					ArrayList<IModule> list = new ArrayList<IModule>();
					
					for(IModule module : Mekanism.modulesLoaded)
					{
						if(Version.get(Mekanism.latestVersionNumber).comparedState(module.getVersion()) == 1)
						{
							list.add(module);
						}
					}
					
					if(Version.get(Mekanism.latestVersionNumber).comparedState(Mekanism.versionNumber) == 1 || !list.isEmpty())
					{
						entityplayer.addChatMessage(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------");
						entityplayer.addChatMessage(EnumColor.GREY + " Using outdated version on one or more modules.");
						
						if(Version.get(Mekanism.latestVersionNumber).comparedState(Mekanism.versionNumber) == 1)
						{
							entityplayer.addChatMessage(EnumColor.INDIGO + " Mekanism: " + EnumColor.DARK_RED + Mekanism.versionNumber);
						}
						
						for(IModule module : list)
						{
							entityplayer.addChatMessage(EnumColor.INDIGO + " Mekanism" + module.getName() + ": " + EnumColor.DARK_RED + module.getVersion());
						}
						
						entityplayer.addChatMessage(EnumColor.GREY + " Consider updating to version " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber);
						entityplayer.addChatMessage(EnumColor.GREY + " New features: " + EnumColor.INDIGO + Mekanism.recentNews);
						entityplayer.addChatMessage(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------");
						return true;
					}
					else if(Version.get(Mekanism.latestVersionNumber).comparedState(Mekanism.versionNumber) == -1)
					{
						entityplayer.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Using developer build " + EnumColor.DARK_GREY + Mekanism.versionNumber);
						return true;
					}
				}
				else {
					System.out.println("[Mekanism] Minecraft is in offline mode, could not check for updates.");
				}
			}
		} catch(Exception e) {}
		
		return false;
	}
	
	/**
	 * Gets the latest version using getHTML and returns it as a string.
	 * @return latest version
	 */
	public static String getLatestVersion()
	{
		String[] text = getHTML("http://dl.dropbox.com/u/90411166/Mod%20Versions/Mekanism.txt").split(":");
		if(!text[0].contains("UTF-8") && !text[0].contains("HTML") && !text[0].contains("http")) return text[0];
		return "null";
	}
	
	/**
	 * Gets the recent news using getHTML and returns it as a string.
	 * @return recent news
	 */
	public static String getRecentNews()
	{
		String[] text = getHTML("http://dl.dropbox.com/u/90411166/Mod%20Versions/Mekanism.txt").split(":");
		if(text.length > 1 && !text[1].contains("UTF-8") && !text[1].contains("HTML") && !text[1].contains("http")) return text[1];
		return "null";
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
			
			while((line = rd.readLine()) != null) 
			{
				result += line;
				sb.append(line);
			}
			
			rd.close();
		} catch (Exception e) {
			result = "null";
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
	 * Returns the closest teleporter between a selection of one or two.
	 */
	public static Coord4D getClosestCoords(Teleporter.Code teleCode, EntityPlayer player)
	{
		if(Mekanism.teleporters.get(teleCode).size() == 1)
		{
			return Mekanism.teleporters.get(teleCode).get(0);
		}
		else {
			int dimensionId = player.worldObj.provider.dimensionId;
			
			Coord4D coords0 = Mekanism.teleporters.get(teleCode).get(0);
			Coord4D coords1 = Mekanism.teleporters.get(teleCode).get(1);
			
			int distance0 = (int)player.getDistance(coords0.xCoord, coords0.yCoord, coords0.zCoord);
			int distance1 = (int)player.getDistance(coords1.xCoord, coords1.yCoord, coords1.zCoord);
			
			if(dimensionId == coords0.dimensionId && dimensionId != coords1.dimensionId)
			{
				return coords0;
			}
			else if(dimensionId == coords1.dimensionId && dimensionId != coords0.dimensionId)
			{
				return coords1;
			}
			else if(dimensionId == coords0.dimensionId && dimensionId == coords1.dimensionId)
			{
				if(distance0 < distance1)
				{
					return coords0;
				}
				else if(distance0 > distance1)
				{
					return coords1;
				}
			}
			else if(dimensionId != coords0.dimensionId && dimensionId != coords1.dimensionId)
			{
				if(distance0 < distance1)
				{
					return coords0;
				}
				else if(distance0 > distance1)
				{
					return coords1;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Sends the defined message to all players.
	 * @param msg - message to send
	 */
	public static void sendChatMessageToAllPlayers(String msg)
	{
		PacketDispatcher.sendPacketToAllPlayers(new Packet3Chat(msg));
	}
	
	/**
	 * Checks if the mod doesn't need an update.
	 * @return if mod doesn't need an update
	 */
	public static boolean noUpdates()
	{
		if(Mekanism.latestVersionNumber.contains("null"))
		{
			return true;
		}
		
		if(Mekanism.versionNumber.comparedState(Version.get(Mekanism.latestVersionNumber)) == -1)
		{
			return false;
		}
		
		for(IModule module : Mekanism.modulesLoaded)
		{
			if(module.getVersion().comparedState(Version.get(Mekanism.latestVersionNumber)) == -1)
			{
				return false;
			}
		}
		
		return true;
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
	public static ItemStack size(ItemStack itemstack, int size)
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
	public static ItemStack getEnergyCube(EnergyCubeTier tier)
	{
		ItemStack itemstack = ((ItemBlockEnergyCube)new ItemStack(Mekanism.EnergyCube).getItem()).getUnchargedItem(tier);
		return itemstack;
	}
	
	/**
	 * Retrieves an empty Gas Tank.
	 * @return empty gas tank
	 */
	public static ItemStack getEmptyGasTank()
	{
		ItemStack itemstack = ((ItemBlockGasTank)new ItemStack(Mekanism.GasTank).getItem()).getEmptyItem();
		return itemstack;
	}
	
	/**
	 * Retrieves a Factory with a defined tier and recipe type.
	 * @param tier - tier to add to the Factory
	 * @param type - recipe type to add to the Factory
	 * @return factory with defined tier and recipe type
	 */
	public static ItemStack getFactory(FactoryTier tier, RecipeType type)
	{
		ItemStack itemstack = new ItemStack(Mekanism.MachineBlock, 1, 5+tier.ordinal());
		((IFactory)itemstack.getItem()).setRecipeType(type.ordinal(), itemstack);
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
    			return ForgeDirection.NORTH;
    		default:
    			return ForgeDirection.SOUTH;
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
     * Gets the ore dictionary name of a defined ItemStack.
     * @param check - ItemStack to check OreDict name of
     * @return OreDict name
     */
    public static String getOreDictName(ItemStack check)
    {
        HashMap<Integer, ArrayList<ItemStack>> oreStacks = (HashMap<Integer, ArrayList<ItemStack>>)MekanismUtils.getPrivateValue(null, OreDictionary.class, new String[] {"oreStacks"});
        
        int idFound = -1;
        
        for(Map.Entry<Integer, ArrayList<ItemStack>> entry : oreStacks.entrySet())
        {
        	for(ItemStack stack : entry.getValue())
        	{
        		if(stack.isItemEqual(check))
        		{
        			idFound = entry.getKey();
        			break;
        		}
        	}
        	
        	if(idFound != -1)
        	{
        		break;
        	}
        }
        
        if(idFound == -1)
        {
        	return null;
        }
        
        return OreDictionary.getOreName(idFound);
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
    		if(side == 2 || side == 3)
    		{
    			return getRight(side).ordinal();
    		}
    		
    		return getLeft(side).ordinal();
    	}
    	else if(blockFacing == 5)
    	{
    		if(side == 2 || side == 3)
    		{
    			return getLeft(side).ordinal();
    		}
    		
    		return getRight(side).ordinal();
    	}
    	
    	return side;
    }
    
    /**
     * Localizes the defined string.
     * @param s - string to localized
     * @return localized string
     */
    public static String localize(String s)
    {
    	return StatCollector.translateToLocal(s);
    }
    
    /**
     * Increments the output type of a machine's side.
     * @param config - configurable machine
     * @param side - side to increment output of
     */
    public static void incrementOutput(IInvConfiguration config, int side)
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
     * Decrements the output type of a machine's side.
     * @param config - configurable machine
     * @param side - side to increment output of
     */
    public static void decrementOutput(IInvConfiguration config, int side)
    {
    	int max = config.getSideData().size()-1;
    	int current = config.getSideData().indexOf(config.getSideData().get(config.getConfiguration()[side]));
    	
    	if(current > 0)
    	{
    		config.getConfiguration()[side] = (byte)(current-1);
    	}
    	else if(current == 0)
    	{
    		config.getConfiguration()[side] = (byte)max;
    	}
    }
    
    /**
     * Gets the operating ticks required for a machine via it's upgrades.
     * @param speedUpgrade - number of speed upgrades
     * @param def - the original, default ticks required
     * @return max operating ticks
     */
    public static int getTicks(int speedUpgrade, int def)
    {
        return (int)(def * Math.pow(Mekanism.upgradeModifier, (-speedUpgrade/(double)(Mekanism.upgradeModifier-1))));
    }
    
    /**
     * Gets the energy required per tick for a machine via it's upgrades.
     * @param speedUpgrade - number of speed upgrades
     * @param energyUpgrade - number of energy upgrades
     * @param def - the original, default energy required
     * @return max energy per tick
     */
    public static double getEnergyPerTick(int speedUpgrade, int energyUpgrade, double def)
    {
        return def * Math.pow(Mekanism.upgradeModifier, ((speedUpgrade-energyUpgrade)/(double)(Mekanism.upgradeModifier-1)));
    }
    
    /**
     * Gets the maximum energy for a machine via it's upgrades.
     * @param energyUpgrade - number of energy upgrades
     * @param def - original, default max energy
     * @return max energy
     */
    public static double getMaxEnergy(int energyUpgrade, double def)
    {
        return def * Math.pow(Mekanism.upgradeModifier, (energyUpgrade/(double)(Mekanism.upgradeModifier-1)));
    }
    
    /**
     * Places a fake bounding block at the defined location.
     * @param world - world to place block in
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @param orig - original block
     */
    public static void makeBoundingBlock(World world, int x, int y, int z, Coord4D orig)
    {
		world.setBlock(x, y, z, Mekanism.BoundingBlock.blockID);
		
		if(!world.isRemote)
		{
			((TileEntityBoundingBlock)world.getBlockTileEntity(x, y, z)).setMainLocation(orig.xCoord, orig.yCoord, orig.zCoord);
		}
    }
    
    /**
     * Places a fake advanced bounding block at the defined location.
     * @param world - world to place block in
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @param orig - original block
     */
    public static void makeAdvancedBoundingBlock(World world, int x, int y, int z, Coord4D orig)
    {
		world.setBlock(x, y, z, Mekanism.BoundingBlock.blockID, 1, 3);
		
		if(!world.isRemote)
		{
			((TileEntityAdvancedBoundingBlock)world.getBlockTileEntity(x, y, z)).setMainLocation(orig.xCoord, orig.yCoord, orig.zCoord);
		}
    }
    
    /**
     * Updates a block's light value and marks it for a render update.
     * @param world - world the block is in
     * @param x - x coord
     * @param y - y coord
     * @param z - z coord
     */
    public static void updateBlock(World world, int x, int y, int z)
    {
    	if(!(world.getBlockTileEntity(x, y, z) instanceof IActiveState) || ((IActiveState)world.getBlockTileEntity(x, y, z)).renderUpdate())
    	{
    		world.markBlockForRenderUpdate(x, y, z);
    	}
    	
    	if(!(world.getBlockTileEntity(x, y, z) instanceof IActiveState) || ((IActiveState)world.getBlockTileEntity(x, y, z)).lightUpdate())
    	{
    		world.updateAllLightTypes(x, y, z);
    	}
    }
    
    /**
     * Whether or not a certain block is considered a fluid.
     * @param world - world the block is in
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @return if the block is a fluid
     */
    public static boolean isFluid(World world, int x, int y, int z)
    {
    	return getFluid(world, x, y, z) != null;
    }
    
    /**
     * Gets a fluid from a certain location.
     * @param world - world the block is in
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @return the fluid at the certain location, null if it doesn't exist
     */
    public static FluidStack getFluid(World world, int x, int y, int z)
    {
    	int id = world.getBlockId(x, y, z);
    	int meta = world.getBlockMetadata(x, y, z);
    	
    	if(id == 0)
    	{
    		return null;
    	}
    	
    	if((id == Block.waterStill.blockID || id == Block.waterMoving.blockID) && meta == 0)
    	{
    		return new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);
    	}
    	else if((id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID) && meta == 0)
    	{
    		return new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
    	}
    	else if(Block.blocksList[id] instanceof IFluidBlock)
    	{
    		IFluidBlock fluid = (IFluidBlock)Block.blocksList[id];
    	
    		if(meta == 0)
    		{
    			return new FluidStack(fluid.getFluid(), FluidContainerRegistry.BUCKET_VOLUME);
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Gets the fluid ID at a certain location, 0 if there isn't one
     * @param world - world the block is in
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @return fluid ID
     */
    public static int getFluidId(World world, int x, int y, int z)
    {
    	int id = world.getBlockId(x, y, z);
    	int meta = world.getBlockMetadata(x, y, z);
    	
    	if(id == 0)
    	{
    		return 0;
    	}
    	
    	if(id == Block.waterMoving.blockID)
    	{
    		return FluidRegistry.WATER.getID();
    	}
    	else if(id == Block.lavaMoving.blockID)
    	{
    		return FluidRegistry.LAVA.getID();
    	}
    	
    	for(Fluid fluid : FluidRegistry.getRegisteredFluids().values())
    	{
    		if(fluid.getBlockID() == id)
    		{
    			return fluid.getID();
    		}
    	}
    	
    	return 0;
    }
    
    /**
     * Whether or not a block is a dead fluid.
     * @param world - world the block is in
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @return if the block is a dead fluid
     */
    public static boolean isDeadFluid(World world, int x, int y, int z)
    {
    	int id = world.getBlockId(x, y, z);
    	int meta = world.getBlockMetadata(x, y, z);
    	
    	if(id == 0)
    	{
    		return false;
    	}
    	
    	if((id == Block.waterStill.blockID || id == Block.waterMoving.blockID) && meta != 0)
    	{
    		return true;
    	}
    	else if((id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID) && meta != 0)
    	{
    		return true;
    	}
    	else if(Block.blocksList[id] instanceof IFluidBlock)
    	{
    		IFluidBlock fluid = (IFluidBlock)Block.blocksList[id];
    	
    		if(meta != 0)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    /**
     * FML doesn't really do GUIs the way it's supposed to -- opens Electric Chest GUI on client and server.
     * Call this method server-side only!
     * @param player - player to open GUI
     * @param tileEntity - TileEntity of the chest, if it's not an item
     * @param inventory - IInventory of the item, if it's not a block
	 * @param isBlock - whether or not this electric chest is in it's block form
     */
    public static void openElectricChestGui(EntityPlayerMP player, TileEntityElectricChest tileEntity, IInventory inventory, boolean isBlock)
    {
		player.incrementWindowID();
		player.closeContainer();
		int id = player.currentWindowId;
		
		if(isBlock)
		{
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketElectricChest().setParams(ElectricChestPacketType.CLIENT_OPEN, 0, id, true, Coord4D.get(tileEntity)), player);
		}
		else {
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketElectricChest().setParams(ElectricChestPacketType.CLIENT_OPEN, 0, id, false), player);
		}
		
		player.openContainer = new ContainerElectricChest(player.inventory, tileEntity, inventory, isBlock);
		player.openContainer.windowId = id;
		player.openContainer.addCraftingToCrafters(player);
    }
    
    /**
     * Grabs an inventory from the world's caches, and removes all the world's references to it.
     * @param world - world the cache is stored in
     * @param id - inventory ID to pull
     * @return correct Dynamic Tank inventory cache
     */
    public static DynamicTankCache pullInventory(World world, int id)
    {
    	DynamicTankCache toReturn = Mekanism.dynamicInventories.get(id);
    	
    	for(Coord4D obj : Mekanism.dynamicInventories.get(id).locations)
    	{
    		TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(world);
    		
    		if(tileEntity != null)
    		{
    			tileEntity.cachedFluid = null;
    			tileEntity.inventory = new ItemStack[2];
    			tileEntity.inventoryID = -1;
    		}
    	}
    	
    	Mekanism.dynamicInventories.remove(id);
    	
    	return toReturn;
    }
    
    /**
     * Updates a dynamic tank cache with the defined inventory ID with the parameterized values.
     * @param inventoryID - inventory ID of the dynamic tank
     * @param fluid - cached fluid of the dynamic tank
     * @param inventory - inventory of the dynamic tank
     * @param tileEntity - dynamic tank TileEntity
     */
    public static void updateCache(int inventoryID, FluidStack fluid, ItemStack[] inventory, TileEntityDynamicTank tileEntity)
    {
    	if(!Mekanism.dynamicInventories.containsKey(inventoryID))
    	{
    		DynamicTankCache cache = new DynamicTankCache();
    		cache.inventory = inventory;
    		cache.fluid = fluid;
    		cache.locations.add(Coord4D.get(tileEntity));
    		
    		Mekanism.dynamicInventories.put(inventoryID, cache);
    		
    		return;
    	}
    	
    	Mekanism.dynamicInventories.get(inventoryID).inventory = inventory;
    	Mekanism.dynamicInventories.get(inventoryID).fluid = fluid;
    	
		Mekanism.dynamicInventories.get(inventoryID).locations.add(Coord4D.get(tileEntity));
    }
    
    /**
     * Grabs a unique inventory ID for a dynamic tank.
     * @return unique inventory ID
     */
    public static int getUniqueInventoryID()
    {
    	int id = 0;
    	
    	while(true)
    	{
    		for(Integer i : Mekanism.dynamicInventories.keySet())
    		{
    			if(id == i)
    			{
    				id++;
    				continue;
    			}
    		}
    		
    		return id;
    	}
    }
    
    /**
     * Gets the OreDictionary-registered name of an ItemStack.
     * @param itemStack - ItemStack to check
     * @return name of the ItemStack
     */
    public static String getName(ItemStack itemStack)
    {
    	return OreDictionary.getOreName(OreDictionary.getOreID(itemStack));
    }
    
    /**
     * Retrieves a private value from a defined class and field.
     * @param obj - the Object to retrieve the value from, null if static
     * @param c - Class to retrieve field value from
     * @param fields - possible names of field to iterate through
     * @return value as an Object, cast as necessary
     */
    public static Object getPrivateValue(Object obj, Class c, String[] fields)
    {
    	for(String field : fields)
    	{
	    	try {
		    	Field f = c.getDeclaredField(field);
		    	f.setAccessible(true);
		    	return f.get(obj);
	    	} catch(Exception e) {
	    		continue;
	    	}
    	}
    	
    	return null;
    }
    
    /**
     * Sets a private value from a defined class and field to a new value.
     * @param obj - the Object to perform the operation on, null if static
     * @param value - value to set the field to
     * @param c - Class the operation will be performed on
     * @param fields - possible names of field to iterate through
     */
    public static void setPrivateValue(Object obj, Object value, Class c, String[] fields)
    {
    	for(String field : fields)
    	{
	    	try {
	    		Field f = c.getDeclaredField(field);
	    		f.setAccessible(true);
	    		f.set(obj, value);
	    	} catch(Exception e) {
	    		continue;
	    	}
    	}
    }
    
    /**
     * Retrieves a private method from a class, sets it as accessible, and returns it.
     * @param c - Class the method is located in
     * @param methods - possible names of the method to iterate through
     * @param params - the Types inserted as parameters into the method
     * @return private method
     */
    public static Method getPrivateMethod(Class c, String[] methods, Class... params)
    {
    	for(String method : methods)
    	{
    		try {
    			Method m = c.getDeclaredMethod(method, params);
    			m.setAccessible(true);
    			return m;
    		} catch(Exception e) {
    			continue;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Gets a ResourceLocation with a defined resource type and name.
     * @param type - type of resource to retrieve
     * @param name - simple name of file to retrieve as a ResourceLocation
     * @return the corresponding ResourceLocation
     */
    public static ResourceLocation getResource(ResourceType type, String name)
    {
    	return new ResourceLocation("mekanism", type.getPrefix() + name);
    }
    
    /**
     * Removes all recipes that are used to create the defined ItemStacks.
     * @param itemStacks - ItemStacks to perform the operation on
     * @return if any recipes were removed
     */
    public static boolean removeRecipes(ItemStack... itemStacks)
	{
		boolean didRemove = false;

		for(Iterator itr = CraftingManager.getInstance().getRecipeList().iterator(); itr.hasNext();)
		{
			Object obj = itr.next();

			if(obj instanceof IRecipe && ((IRecipe)obj).getRecipeOutput() != null)
			{
				for(ItemStack itemStack : itemStacks)
				{
					if(((IRecipe)obj).getRecipeOutput().isItemEqual(itemStack))
					{
						itr.remove();
						didRemove = true;
						break;
					}
				}
			}
		}

		return didRemove;
	}
    
    /**
     * Marks the chunk this TileEntity is in as modified. Call this method to be sure NBT is written by the defined tile entity.
     * @param tileEntity - TileEntity to save
     */
    public static void saveChunk(TileEntity tileEntity)
    {
    	if(tileEntity == null || tileEntity.isInvalid() || tileEntity.worldObj == null)
    	{
    		return;
    	}
    	
    	tileEntity.worldObj.markTileEntityChunkModified(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, tileEntity);
    }
    
    /**
     * Whether or not a certain TileEntity can function with redstone logic. Illogical to use unless the defined TileEntity implements
     * IRedstoneControl.
     * @param tileEntity - TileEntity to check
     * @return if the TileEntity can function with redstone logic
     */
    public static boolean canFunction(TileEntity tileEntity)
    {
    	if(!(tileEntity instanceof IRedstoneControl))
    	{
    		return true;
    	}
    	
    	World world = tileEntity.worldObj;
    	IRedstoneControl control = (IRedstoneControl)tileEntity;
    	
    	if(control.getControlType() == RedstoneControl.DISABLED)
    	{
    		return true;
    	}
    	else if(control.getControlType() == RedstoneControl.HIGH)
    	{
    		return world.isBlockIndirectlyGettingPowered(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
    	}
    	else if(control.getControlType() == RedstoneControl.LOW)
    	{
    		return !world.isBlockIndirectlyGettingPowered(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
    	}
    	
    	return false;
    }
    
    public static MovingObjectPosition rayTrace(World world, EntityPlayer player)
    {
    	double reach = Mekanism.proxy.getReach(player);
    	
        Vec3 headVec = getHeadVec(player);
        Vec3 lookVec = player.getLook(1);
        Vec3 endVec = headVec.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);
        
        return world.rayTraceBlocks_do_do(headVec, endVec, true, false);
    }
    
    private static Vec3 getHeadVec(EntityPlayer player)
    {
        Vec3 vec = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        
        if(!player.worldObj.isRemote)
        {
            vec.yCoord+=player.getEyeHeight();
            
            if(player instanceof EntityPlayerMP && player.isSneaking())
            {
                vec.yCoord -= 0.08;
            }
        }
        
        return vec;
    }
    
    public static String getEnergyDisplay(double energy)
    {
    	return ElectricityDisplay.getDisplayShort((float)(energy*Mekanism.TO_UE), ElectricUnit.JOULES);
    }
    
    public static String getPowerDisplay(double energy)
    {
    	return ElectricityDisplay.getDisplayShort((float)(energy*Mekanism.TO_UE), ElectricUnit.WATT);
    }
    
    public static boolean useBuildcraft()
    {
    	return Mekanism.hooks.BuildCraftLoaded || Mekanism.forceBuildcraft;
    }
    
    public static String getCoordDisplay(Coord4D obj)
    {
    	return "[" + obj.xCoord + ", " + obj.yCoord + ", " + obj.zCoord + "]";
    }
    
    public static List<String> splitLines(String s)
    {
    	ArrayList ret = new ArrayList();
    	
    	String[] split = s.split("!n");
    	ret.addAll(Arrays.asList(split));
    	
    	return ret;
    }
    
    public static enum ResourceType
    {
    	GUI("gui"),
    	SOUND("sound"),
    	RENDER("render"),
    	TEXTURE_BLOCKS("textures/blocks"),
    	TEXTURE_ITEMS("textures/items"),
    	MODEL("models"),
    	INFUSE("infuse");
    	
    	private String prefix;
    	
    	private ResourceType(String s)
    	{
    		prefix = s;
    	}
    	
    	public String getPrefix()
    	{
    		return prefix + "/";
    	}
    }
}
