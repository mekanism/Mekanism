package mekanism.common.util;

import ic2.api.energy.EnergyNet;

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

import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IMekWrench;
import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.UnitDisplayUtils;
import mekanism.api.util.UnitDisplayUtils.ElectricUnit;
import mekanism.api.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.OreDictCache;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.Upgrade;
import mekanism.common.Version;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IModule;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.inventory.container.ContainerElectricChest;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.network.PacketElectricChest.ElectricChestMessage;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityElectricChest;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 * @author AidanBrady
 *
 */
public final class MekanismUtils
{
	public static final ForgeDirection[] SIDE_DIRS = new ForgeDirection[] {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST};

	public static final Map<String, Class<?>> classesFound = new HashMap<String, Class<?>>();

	/**
	 * Checks for a new version of Mekanism.
	 */
	public static boolean checkForUpdates(EntityPlayer entityplayer)
	{
		try {
			if(general.updateNotifications && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
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
						entityplayer.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
						entityplayer.addChatMessage(new ChatComponentText(EnumColor.GREY + " " + MekanismUtils.localize("update.outdated") + "."));

						if(Version.get(Mekanism.latestVersionNumber).comparedState(Mekanism.versionNumber) == 1)
						{
							entityplayer.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " Mekanism: " + EnumColor.DARK_RED + Mekanism.versionNumber));
						}

						for(IModule module : list)
						{
							entityplayer.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " Mekanism" + module.getName() + ": " + EnumColor.DARK_RED + module.getVersion()));
						}

						entityplayer.addChatMessage(new ChatComponentText(EnumColor.GREY + " " + MekanismUtils.localize("update.consider") + " " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber));
						entityplayer.addChatMessage(new ChatComponentText(EnumColor.GREY + " " + MekanismUtils.localize("update.newFeatures") + ": " + EnumColor.INDIGO + Mekanism.recentNews));
						entityplayer.addChatMessage(new ChatComponentText(EnumColor.GREY + " " + MekanismUtils.localize("update.visit") + " " + EnumColor.DARK_GREY + "aidancbrady.com/mekanism" + EnumColor.GREY + " " + MekanismUtils.localize("update.toDownload") + "."));
						entityplayer.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
						return true;
					}
					else if(Version.get(Mekanism.latestVersionNumber).comparedState(Mekanism.versionNumber) == -1)
					{
						entityplayer.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("update.devBuild") + " " + EnumColor.DARK_GREY + Mekanism.versionNumber));
						return true;
					}
				}
				else {
					Mekanism.logger.info("Minecraft is in offline mode, could not check for updates.");
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
		String[] text = merge(getHTML("https://dl.dropbox.com/u/90411166/Mod%20Versions/Mekanism.txt")).split(":");
		if(text.length > 1 && !text[0].contains("UTF-8") && !text[0].contains("HTML") && !text[0].contains("http")) return text[0];
		return "null";
	}

	/**
	 * Gets the recent news using getHTML and returns it as a string.
	 * @return recent news
	 */
	public static String getRecentNews()
	{
		String[] text = merge(getHTML("https://dl.dropbox.com/u/90411166/Mod%20Versions/Mekanism.txt")).split(":");
		if(text.length > 1 && !text[1].contains("UTF-8") && !text[1].contains("HTML") && !text[1].contains("http")) return text[1];
		return "null";
	}

	/**
	 * Updates the donator list by retrieving the most recent information from a foreign document.
	 */
	public static void updateDonators()
	{
		Mekanism.donators.clear();

		for(String s : getHTML("https://dl.dropbox.com/u/90411166/Donators/Mekanism.txt"))
		{
			Mekanism.donators.add(s);
		}
	}

	/**
	 * Returns one line of HTML from the url.
	 * @param urlToRead - URL to read from.
	 * @return HTML text from the url.
	 */
	public static List<String> getHTML(String urlToRead)
	{
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		List<String> result = new ArrayList<String>();

		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			while((line = rd.readLine()) != null)
			{
				result.add(line.trim());
			}

			rd.close();
		} catch(Exception e) {
			result.clear();
			result.add("null");
			Mekanism.logger.error("An error occured while connecting to URL '" + urlToRead + ".'");
		}

		return result;
	}

	public static String merge(List<String> text)
	{
		StringBuilder builder = new StringBuilder();

		for(String s : text)
		{
			builder.append(s);
		}

		return builder.toString();
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
		} catch (IOException e) {
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
	 * @return empty Energy Cube with defined tier
	 */
	public static ItemStack getEnergyCube(EnergyCubeTier tier)
	{
		return ((ItemBlockEnergyCube)new ItemStack(MekanismBlocks.EnergyCube).getItem()).getUnchargedItem(tier);
	}
	
	/**
	 * Retrieves an empty Induction Cell with a defined tier.
	 * @param tier - tier to add to the Induction Cell
	 * @return empty Induction Cell with defined tier
	 */
	public static ItemStack getInductionCell(InductionCellTier tier)
	{
		return ((ItemBlockBasic)new ItemStack(MekanismBlocks.BasicBlock2, 1, 3).getItem()).getUnchargedCell(tier);
	}
	
	/**
	 * Retrieves an Induction Provider with a defined tier.
	 * @param tier - tier to add to the Induction Provider
	 * @return Induction Provider with defined tier
	 */
	public static ItemStack getInductionProvider(InductionProviderTier tier)
	{
		return ((ItemBlockBasic)new ItemStack(MekanismBlocks.BasicBlock2, 1, 4).getItem()).getUnchargedProvider(tier);
	}

	/**
	 * Retrieves an empty Gas Tank.
	 * @return empty gas tank
	 */
	public static ItemStack getEmptyGasTank()
	{
		ItemStack itemstack = ((ItemBlockGasTank)new ItemStack(MekanismBlocks.GasTank).getItem()).getEmptyItem();
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
		ItemStack itemstack = new ItemStack(MekanismBlocks.MachineBlock, 1, 5+tier.ordinal());
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
		TileEntity tileEntity = (TileEntity)world.getTileEntity(x, y, z);

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
	 * Gets the opposite side of a certain orientation.
	 * @param orientation
	 * @return opposite side
	 */
	public static ForgeDirection getBack(int orientation)
	{
		return ForgeDirection.getOrientation(orientation).getOpposite();
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
	public static List<String> getOreDictName(ItemStack check)
	{
		return OreDictCache.getOreDictName(check);
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
	 * @param type - the TransmissionType to modify
	 * @param side - side to increment output of
	 */
	public static void incrementOutput(ISideConfiguration config, TransmissionType type, int side)
	{
		int max = config.getConfig().getOutputs(type).size()-1;
		int current = config.getConfig().getOutputs(type).indexOf(config.getConfig().getOutputs(type).get(config.getConfig().getConfig(type)[side]));

		if(current < max)
		{
			config.getConfig().getConfig(type)[side] = (byte)(current+1);
		}
		else if(current == max)
		{
			config.getConfig().getConfig(type)[side] = 0;
		}

		TileEntity tile = (TileEntity)config;
		Coord4D coord = Coord4D.get(tile).getFromSide(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, config.getOrientation())));

		tile.markDirty();
	}

	/**
	 * Decrements the output type of a machine's side.
	 * @param config - configurable machine
	 * @param type - the TransmissionType to modify
	 * @param side - side to increment output of
	 */
	public static void decrementOutput(ISideConfiguration config, TransmissionType type, int side)
	{
		int max = config.getConfig().getOutputs(type).size()-1;
		int current = config.getConfig().getOutputs(type).indexOf(config.getConfig().getOutputs(type).get(config.getConfig().getConfig(type)[side]));

		if(current > 0)
		{
			config.getConfig().getConfig(type)[side] = (byte)(current-1);
		}
		else if(current == 0)
		{
			config.getConfig().getConfig(type)[side] = (byte)max;
		}

		TileEntity tile = (TileEntity)config;
		Coord4D coord = Coord4D.get(tile).getFromSide(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, config.getOrientation())));

		tile.markDirty();
	}

	public static float fractionUpgrades(IUpgradeTile mgmt, Upgrade type)
	{
		return (float)mgmt.getComponent().getUpgrades(type)/(float)type.getMax();
	}

	/**
	 * Gets the operating ticks required for a machine via it's upgrades.
	 * @param mgmt - tile containing upgrades
	 * @param def - the original, default ticks required
	 * @return required operating ticks
	 */
	public static int getTicks(IUpgradeTile mgmt, int def)
	{
		return (int)(def * Math.pow(general.maxUpgradeMultiplier, -fractionUpgrades(mgmt, Upgrade.SPEED)));
	}

	/**
	 * Gets the energy required per tick for a machine via it's upgrades.
	 * @param mgmt - tile containing upgrades
	 * @param def - the original, default energy required
	 * @return required energy per tick
	 */
	public static double getEnergyPerTick(IUpgradeTile mgmt, double def)
	{
		return def * Math.pow(general.maxUpgradeMultiplier, 2*fractionUpgrades(mgmt, Upgrade.SPEED)-fractionUpgrades(mgmt, Upgrade.ENERGY));
	}
	
	/**
	 * Gets the energy required per tick for a machine via it's upgrades, not taking into account speed upgrades.
	 * @param mgmt - tile containing upgrades
	 * @param def - the original, default energy required
	 * @return required energy per tick
	 */
	public static double getBaseEnergyPerTick(IUpgradeTile mgmt, double def)
	{
		return def * Math.pow(general.maxUpgradeMultiplier, -fractionUpgrades(mgmt, Upgrade.ENERGY));
	}

	/**
	 * Gets the secondary energy required per tick for a machine via upgrades.
	 * @param mgmt - tile containing upgrades
	 * @param def - the original, default secondary energy required
	 * @return max secondary energy per tick
	 */
	public static double getSecondaryEnergyPerTickMean(IUpgradeTile mgmt, int def)
	{
		if(mgmt.getComponent().supports(Upgrade.GAS))
		{
			return def * Math.pow(general.maxUpgradeMultiplier, 2 * fractionUpgrades(mgmt, Upgrade.SPEED) - fractionUpgrades(mgmt, Upgrade.GAS));
		}

		return def * Math.pow(general.maxUpgradeMultiplier, fractionUpgrades(mgmt, Upgrade.SPEED));
	}

	/**
	 * Gets the maximum energy for a machine via it's upgrades.
	 * @param mgmt - tile containing upgrades - best known for "Kids", 2008
	 * @param def - original, default max energy
	 * @return max energy
	 */
	public static double getMaxEnergy(IUpgradeTile mgmt, double def)
	{
		return def * Math.pow(general.maxUpgradeMultiplier, fractionUpgrades(mgmt, Upgrade.ENERGY));
	}
	
	/**
	 * Gets the maximum energy for a machine's item form via it's upgrades.
	 * @param itemStack - stack holding energy upgrades
	 * @param def - original, default max energy
	 * @return max energy
	 */
	public static double getMaxEnergy(ItemStack itemStack, double def)
	{
		Map<Upgrade, Integer> upgrades = Upgrade.buildMap(itemStack.stackTagCompound);
		float numUpgrades =  upgrades.get(Upgrade.ENERGY) == null ? 0 : (float)upgrades.get(Upgrade.ENERGY);
		return def * Math.pow(general.maxUpgradeMultiplier, numUpgrades/(float)Upgrade.ENERGY.getMax());
	}
	
	/**
	 * Better version of the World.isBlockIndirectlyGettingPowered() method that doesn't load chunks.
	 * @param world - the world to perform the check in
	 * @param coord - the coordinate of the block performing the check
	 * @return if the block is indirectly getting powered by LOADED chunks
	 */
	public static boolean isGettingPowered(World world, Coord4D coord)
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D sideCoord = coord.getFromSide(side);
			
			if(sideCoord.exists(world) && sideCoord.getFromSide(side).exists(world))
			{
				Block block = sideCoord.getBlock(world);
				boolean weakPower = block.shouldCheckWeakPower(world, coord.xCoord, coord.yCoord, coord.zCoord, side.ordinal());
				
				if(weakPower && isDirectlyGettingPowered(world, sideCoord))
				{
					return true;
				}
				else if(!weakPower && block.isProvidingWeakPower(world, sideCoord.xCoord, sideCoord.yCoord, sideCoord.zCoord, side.ordinal()) > 0)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if a block is directly getting powered by any of its neighbors without loading any chunks.
	 * @param world - the world to perform the check in
	 * @param coord - the Coord4D of the block to check
	 * @return if the block is directly getting powered
	 */
	public static boolean isDirectlyGettingPowered(World world, Coord4D coord)
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D sideCoord = coord.getFromSide(side);
			
			if(sideCoord.exists(world))
			{
				if(world.isBlockProvidingPowerTo(coord.xCoord, coord.yCoord, coord.zCoord, side.ordinal()) > 0)
				{
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Notifies neighboring blocks of a TileEntity change without loading chunks.
	 * @param world - world to perform the operation in
	 * @param coord - Coord4D to perform the operation on
	 */
	public static void notifyLoadedNeighborsOfTileChange(World world, Coord4D coord)
	{
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D offset = coord.getFromSide(dir);

			if(offset.exists(world))
			{
				Block block1 = offset.getBlock(world);
				block1.onNeighborChange(world, offset.xCoord, offset.yCoord, offset.zCoord, coord.xCoord, coord.yCoord, coord.zCoord);
				
				if(block1.isNormalCube(world, offset.xCoord, offset.yCoord, offset.zCoord))
				{
					offset = offset.getFromSide(dir);
					
					if(offset.exists(world))
					{
						block1 = offset.getBlock(world);

						if(block1.getWeakChanges(world, offset.xCoord, offset.yCoord, offset.zCoord))
						{
							block1.onNeighborChange(world, offset.xCoord, offset.yCoord, offset.zCoord, coord.xCoord, coord.yCoord, coord.zCoord);
						}
					}
				}
			}
		}
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
		world.setBlock(x, y, z, MekanismBlocks.BoundingBlock);

		if(!world.isRemote)
		{
			((TileEntityBoundingBlock)world.getTileEntity(x, y, z)).setMainLocation(orig.xCoord, orig.yCoord, orig.zCoord);
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
		world.setBlock(x, y, z, MekanismBlocks.BoundingBlock, 1, 0);

		if(!world.isRemote)
		{
			((TileEntityAdvancedBoundingBlock)world.getTileEntity(x, y, z)).setMainLocation(orig.xCoord, orig.yCoord, orig.zCoord);
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
		if(!(world.getTileEntity(x, y, z) instanceof IActiveState) || ((IActiveState)world.getTileEntity(x, y, z)).renderUpdate())
		{
			world.func_147479_m(x, y, z);
		}

		if(!(world.getTileEntity(x, y, z) instanceof IActiveState) || ((IActiveState)world.getTileEntity(x, y, z)).lightUpdate() && client.machineEffects)
		{
			updateAllLightTypes(world, x, y, z);
		}
	}
	
	/**
	 * Updates all light types at the given coordinates.
	 * @param world - the world to perform the lighting update in
	 * @param x - x coordinate of the block to update
	 * @param y - y coordinate of the block to update
	 * @param z - z coordinate of the block to update
	 */
	public static void updateAllLightTypes(World world, int x, int y, int z)
	{
		world.updateLightByType(EnumSkyBlock.Block, x, y, z);
		world.updateLightByType(EnumSkyBlock.Sky, x, y, z);
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
		return getFluid(world, x, y, z, false) != null;
	}

	/**
	 * Gets a fluid from a certain location.
	 * @param world - world the block is in
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return the fluid at the certain location, null if it doesn't exist
	 */
	public static FluidStack getFluid(World world, int x, int y, int z, boolean filter)
	{
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		if(block == null)
		{
			return null;
		}

		if((block == Blocks.water || block == Blocks.flowing_water) && meta == 0)
		{
			if(!filter)
			{
				return new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);
			}
			else {
				return new FluidStack(FluidRegistry.getFluid("heavywater"), 10);
			}
		}
		else if((block == Blocks.lava || block == Blocks.flowing_lava) && meta == 0)
		{
			return new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
		}
		else if(block instanceof IFluidBlock)
		{
			IFluidBlock fluid = (IFluidBlock)block;

			if(meta == 0)
			{
				return fluid.drain(world, x, y, z, false);
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
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		if(block == null)
		{
			return 0;
		}

		if(block == Blocks.water || block == Blocks.flowing_water)
		{
			return FluidRegistry.WATER.getID();
		}
		else if(block == Blocks.lava || block == Blocks.flowing_lava)
		{
			return FluidRegistry.LAVA.getID();
		}

		for(Fluid fluid : FluidRegistry.getRegisteredFluids().values())
		{
			if(fluid.getBlock() == block)
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
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		if(block == null || meta == 0)
		{
			return false;
		}

		if((block == Blocks.water || block == Blocks.flowing_water))
		{
			return true;
		}
		else if((block == Blocks.lava || block == Blocks.flowing_lava))
		{
			return true;
		}
		else if(block instanceof IFluidBlock)
		{
			return true;
		}

		return false;
	}
	
	/**
	 * Gets the flowing block type from a Forge-based fluid. Incorporates the MC system of fliuds as well.
	 * @param fluid - the fluid type
	 * @return the block corresponding to the given fluid
	 */
	public static Block getFlowingBlock(Fluid fluid)
	{
		if(fluid == null)
		{
			return null;
		}
		else if(fluid == FluidRegistry.WATER)
		{
			return Blocks.flowing_water;
		}
		else if(fluid == FluidRegistry.LAVA)
		{
			return Blocks.flowing_lava;
		}
		else {
			return fluid.getBlock();
		}
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
		player.getNextWindowId();
		player.closeContainer();
		int id = player.currentWindowId;

		if(isBlock)
		{
			Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, true, false, 0, id, null, Coord4D.get(tileEntity)), player);
		}
		else {
			Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, false, false, 0, id, null, null), player);
		}

		player.openContainer = new ContainerElectricChest(player.inventory, tileEntity, inventory, isBlock);
		player.openContainer.windowId = id;
		player.openContainer.addCraftingToCrafters(player);
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
		if(tileEntity == null || tileEntity.isInvalid() || tileEntity.getWorldObj() == null)
		{
			return;
		}

		tileEntity.getWorldObj().markTileEntityChunkModified(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, tileEntity);
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

		IRedstoneControl control = (IRedstoneControl)tileEntity;

		switch(control.getControlType())
		{
			case DISABLED:
				return true;
			case HIGH:
				return control.isPowered();
			case LOW:
				return !control.isPowered();
			case PULSE:
				return control.isPowered() && !control.wasPowered();
		}

		return false;
	}

	/**
	 * Ray-traces what block a player is looking at.
	 * @param world - world the player is in
	 * @param player - player to raytrace
	 * @return raytraced value
	 */
	public static MovingObjectPosition rayTrace(World world, EntityPlayer player)
	{
		double reach = Mekanism.proxy.getReach(player);

		Vec3 headVec = getHeadVec(player);
		Vec3 lookVec = player.getLook(1);
		Vec3 endVec = headVec.addVector(lookVec.xCoord*reach, lookVec.yCoord*reach, lookVec.zCoord*reach);

		return world.rayTraceBlocks(headVec, endVec, true);
	}

	/**
	 * Gets the head vector of a player for a ray trace.
	 * @param player - player to check
	 * @return head location
	 */
	private static Vec3 getHeadVec(EntityPlayer player)
	{
		Vec3 vec = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);

		if(!player.worldObj.isRemote)
		{
			vec.yCoord += player.getEyeHeight();

			if(player instanceof EntityPlayerMP && player.isSneaking())
			{
				vec.yCoord -= 0.08;
			}
		}

		return vec;
	}

	/**
	 * Gets a rounded energy display of a defined amount of energy.
	 * @param energy - energy to display
	 * @return rounded energy display
	 */
	public static String getEnergyDisplay(double energy)
	{
		if(energy == Integer.MAX_VALUE)
		{
			return localize("gui.infinite");
		}
		
		switch(general.activeType)
		{
			case J:
				return UnitDisplayUtils.getDisplayShort(energy, ElectricUnit.JOULES);
			case RF:
				return UnitDisplayUtils.getDisplayShort(energy * general.TO_TE, ElectricUnit.REDSTONE_FLUX);
			case EU:
				return UnitDisplayUtils.getDisplayShort(energy * general.TO_IC2, ElectricUnit.ELECTRICAL_UNITS);
			case MJ:
				return UnitDisplayUtils.getDisplayShort(energy * general.TO_TE / 10, ElectricUnit.MINECRAFT_JOULES);
		}

		return "error";
	}
	
	/**
	 * Convert from the unit defined in the configuration to joules.
	 * @param energy - energy to convert
	 * @return energy converted to joules
	 */
	public static double convertToJoules(double energy)
	{
		switch(general.activeType)
		{
			case RF:
				return energy * general.FROM_TE;
			case EU:
				return energy * general.FROM_IC2;
			case MJ:
				return energy * general.FROM_TE * 10;
			default:
				return energy;
		}
	}
	
	/**
	 * Convert from joules to the unit defined in the configuration.
	 * @param energy - energy to convert
	 * @return energy converted to configured unit
	 */
	public static double convertToDisplay(double energy)
	{
		switch(general.activeType)
		{
			case RF:
				return energy * general.TO_TE;
			case EU:
				return energy * general.TO_IC2;
			case MJ:
				return energy * general.TO_TE / 10;
			default:
				return energy;
		}
	}

	/**
	 * Gets a rounded energy display of a defined amount of energy.
	 * @param T - temperature to display
	 * @return rounded energy display
	 */
	public static String getTemperatureDisplay(double T, TemperatureUnit unit)
	{
		double TK = unit.convertToK(T);
		switch(general.tempUnit)
		{
			case K:
				return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.KELVIN);
			case C:
				return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.CELSIUS);
			case R:
				return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.RANKINE);
			case F:
				return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.FAHRENHEIT);
			case STP:
				return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.AMBIENT);
		}

		return "error";
	}

	/**
	 * Whether or not IC2 power should be used, taking into account whether or not it is installed or another mod is
	 * providing its API.
	 * @return if IC2 power should be used
	 */
	public static boolean useIC2()
	{
		return Mekanism.hooks.IC2Loaded && EnergyNet.instance != null && !general.blacklistIC2;
	}

	/**
	 * Whether or not RF power should be used, taking into account whether or not it is installed or another mod is
	 * providing its API.
	 * @return if RF power should be used
	 */
	public static boolean useRF()
	{
		return Mekanism.hooks.CoFHCoreLoaded && !general.blacklistRF;
	}

	/**
	 * Gets a clean view of a coordinate value without the dimension ID.
	 * @param obj - coordinate to check
	 * @return coordinate display
	 */
	public static String getCoordDisplay(Coord4D obj)
	{
		return "[" + obj.xCoord + ", " + obj.yCoord + ", " + obj.zCoord + "]";
	}

	/**
	 * Splits a string of text into a list of new segments, using the splitter "!n."
	 * @param s - string to split
	 * @return split string
	 */
	public static List<String> splitLines(String s)
	{
		ArrayList ret = new ArrayList();

		String[] split = s.split("!n");
		ret.addAll(Arrays.asList(split));

		return ret;
	}

	/**
	 * Creates and returns a full gas tank with the specified gas type.
	 * @param gas - gas to fill the tank with
	 * @return filled gas tank
	 */
	public static ItemStack getFullGasTank(Gas gas)
	{
		ItemStack tank = getEmptyGasTank();
		ItemBlockGasTank item = (ItemBlockGasTank)tank.getItem();
		item.setGas(tank, new GasStack(gas, item.MAX_GAS));

		return tank;
	}

	/**
	 * Finds the output of a defined InventoryCrafting grid. Taken from CofhCore.
	 * @param inv - InventoryCrafting to check
	 * @param world - world reference
	 * @return output ItemStack
	 */
	public static ItemStack findMatchingRecipe(InventoryCrafting inv, World world)
	{
		ItemStack[] dmgItems = new ItemStack[2];

		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			if(inv.getStackInSlot(i) != null)
			{
				if(dmgItems[0] == null)
				{
					dmgItems[0] = inv.getStackInSlot(i);
				}
				else {
					dmgItems[1] = inv.getStackInSlot(i);
					break;
				}
			}
		}

		if((dmgItems[0] == null) || (dmgItems[0].getItem() == null))
		{
			return null;
		}

		if((dmgItems[1] != null) && (dmgItems[0].getItem() == dmgItems[1].getItem()) && (dmgItems[0].stackSize == 1) && (dmgItems[1].stackSize == 1) && dmgItems[0].getItem().isRepairable())
		{
			Item theItem = dmgItems[0].getItem();
			int dmgDiff0 = theItem.getMaxDamage() - dmgItems[0].getItemDamageForDisplay();
			int dmgDiff1 = theItem.getMaxDamage() - dmgItems[1].getItemDamageForDisplay();
			int value = dmgDiff0 + dmgDiff1 + theItem.getMaxDamage() * 5 / 100;
			int solve = Math.max(0, theItem.getMaxDamage() - value);
			return new ItemStack(dmgItems[0].getItem(), 1, solve);
		}

		for(IRecipe recipe : (List<IRecipe>)CraftingManager.getInstance().getRecipeList())
		{
			if(recipe.matches(inv, world))
			{
				return recipe.getCraftingResult(inv);
			}
		}

		return null;
	}
	
	/**
	 * Whether or not the provided chunk is being vibrated by a Seismic Vibrator.
	 * @param chunk - chunk to check
	 * @return if the chunk is being vibrated
	 */
	public static boolean isChunkVibrated(Chunk3D chunk)
	{
		for(Coord4D coord : Mekanism.activeVibrators)
		{
			if(coord.getChunk3D().equals(chunk))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Whether or not a given EntityPlayer is considered an Op.
	 * @param player - player to check
	 * @return if the player has operator privileges
	 */
	public static boolean isOp(EntityPlayerMP player)
	{
		return player.mcServer.getConfigurationManager().func_152596_g(player.getGameProfile());
	}
	
	/**
	 * Gets the mod ID of the mod owning the given ItemStack.
	 * @param stack - ItemStack to check
	 * @return mod ID of the ItemStack's owner
	 */
	public static String getMod(ItemStack stack)
	{
		try {
			ModContainer mod = GameData.findModOwner(GameData.getItemRegistry().getNameForObject(stack.getItem()));
			return mod == null ? "Minecraft" : mod.getName();
		} catch(Exception e) {
			return "null";
		}
	}
	
	/**
	 * Gets the item ID from a given ItemStack
	 * @param itemStack - ItemStack to check
	 * @return item ID of the ItemStack
	 */
	public static int getID(ItemStack itemStack)
	{
		if(itemStack == null)
		{
			return -1;
		}
		
		return Item.getIdFromItem(itemStack.getItem());
	}

	public static boolean classExists(String className)
	{
		if(classesFound.containsKey(className))
		{
			return classesFound.get(className) != null;
		}

		Class<?> found;

		try
		{
			found = Class.forName(className);
		}
		catch(ClassNotFoundException e)
		{
			found = null;
		}

		classesFound.put(className, found);

		return found != null;
	}

	public static boolean existsAndInstance(Object obj, String className)
	{
		Class<?> theClass;

		if(classesFound.containsKey(className))
		{
			theClass = classesFound.get(className);
		}
		else
		{
			try
			{
				theClass = Class.forName(className);
				classesFound.put(className, theClass);
			} catch(ClassNotFoundException e)
			{
				classesFound.put(className, null);
				return false;
			}
		}

		return theClass != null && theClass.isInstance(obj);
	}

	public static boolean isBCWrench(Item tool)
	{
		return existsAndInstance(tool, "buildcraft.api.tools.IToolWrench");
	}

	public static boolean isCoFHHammer(Item tool)
	{
		return existsAndInstance(tool, "cofh.api.item.IToolHammer");
	}

	/**
	 * Whether or not the player has a usable wrench for a block at the coordinates given.
	 * @param player - the player using the wrench
	 * @param x - the x coordinate of the block being wrenched
	 * @param y - the y coordinate of the block being wrenched
	 * @param z - the z coordinate of the block being wrenched
	 * @return if the player can use the wrench
	 */
	public static boolean hasUsableWrench(EntityPlayer player, int x, int y, int z)
	{
		ItemStack tool = player.getCurrentEquippedItem();
		
		if(tool.getItem() instanceof IMekWrench && ((IMekWrench)tool.getItem()).canUseWrench(player, x, y, z))
		{
			return true;
		}
		
		if(isBCWrench(tool.getItem()) && ((IToolWrench)tool.getItem()).canWrench(player, x, y, z))
		{
			return true;
		}
		
		if(isCoFHHammer(tool.getItem()) && ((IToolHammer)tool.getItem()).isUsable(tool, player, x, y, z))
		{
			return true;
		}
		
		return false;
	}

	public static enum ResourceType
	{
		GUI("gui"),
		GUI_ELEMENT("gui/elements"),
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
