
package net.uberkat.obsidian.hawk.common;

import java.io.File;
import java.util.List;
import java.util.Random;
import net.minecraft.src.Achievement;
import net.minecraft.src.AchievementList;
import net.minecraft.src.ChunkProviderEnd;
import net.minecraft.src.ChunkProviderGenerate;
import net.minecraft.src.ChunkProviderHell;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MerchantRecipe;
import net.minecraft.src.MerchantRecipeList;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.event.ForgeSubscribe;
import net.uberkat.obsidian.common.ObsidianIngots;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class HawkCore implements LoadingCallback, ICraftingHandler
{
	private static int chunkLimit;
	
	public static int partsID;
	public static int endiumPlateID;
	public static int rivetsID;
	public static int rivetGunID;
	public static int ingotsID;
	
	public static int ACHprospector;
	public static int ACHtimeToCrush;
	public static int ACHminerkiin;
	public static int ACHwash;
	
	public static int washerTicks;
	public static int maxChunksLoaded;
	
	public static boolean enableUpdateChecking;
	public static boolean enableAutoDL;
	public static boolean enableChunkloader;
	
	public static Configuration HMConfig = new Configuration(new File(Loader.instance().getConfigDir(), "HawksMachinery/HMConfig.cfg"));
	
	public static void loadConfig()
	{

		HMConfig.load();
		
		//NOTE ID #3964 saved for the Endium Chunkloader.
		//endiumTeleporterID = HMConfig.getBlock("Endium Teleporter", 3965).getInt(3965);
		
		enableUpdateChecking = HMConfig.get(Configuration.CATEGORY_GENERAL, "Enable Update Checking", true).getBoolean(true);
		enableAutoDL = HMConfig.get(Configuration.CATEGORY_GENERAL, "Enable Auto DL", true).getBoolean(true);
		enableChunkloader = HMConfig.get(Configuration.CATEGORY_GENERAL, "Enable Chunkloader Block", true).getBoolean(true);
		
		if (enableChunkloader)
		{
			maxChunksLoaded = HMConfig.get("Max Chunks Loaded", Configuration.CATEGORY_GENERAL, 25).getInt(25);
			
		}
		
		partsID = HMConfig.get(Configuration.CATEGORY_ITEM, "Parts", 24152).getInt(24152);
		endiumPlateID = HMConfig.get(Configuration.CATEGORY_ITEM, "Endium Plate", 24154).getInt(24154);
		rivetsID = HMConfig.get(Configuration.CATEGORY_ITEM, "Rivets", 24155).getInt(24155);
		rivetGunID = HMConfig.get(Configuration.CATEGORY_ITEM, "Rivet Gun", 24156).getInt(24156);
		ingotsID = HMConfig.get(Configuration.CATEGORY_ITEM, "Ingots", 24157).getInt(24157);
		
		ACHprospector = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Prospector", 1500).getInt(1500);
		ACHtimeToCrush = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Time To Crush", 1501).getInt(1501);
		ACHminerkiin = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Minerkiin", 1503).getInt(1503);
		ACHwash = HMConfig.get(Configuration.CATEGORY_GENERAL, "ACH Wash", 1504).getInt(1504);
		
		if (FMLCommonHandler.instance().getSide().isServer())
		{
			HMConfig.addCustomCategoryComment("advanced_settings", "Advanced server OP settings, don't be a moron with them.");
			washerTicks = HMConfig.get("advanced_settings", "Washer Ticks", 100).getInt(100);
			
		}
		
		HMConfig.save();
	}
	
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		for (Ticket ticket : tickets)
		{
			int xPos = ticket.getModData().getInteger("xCoord");
			int yPos = ticket.getModData().getInteger("yCoord");
			int zPos = ticket.getModData().getInteger("zCoord");
			
			if (world.getBlockTileEntity(xPos, yPos, zPos) != null)
			{
				if (world.getBlockTileEntity(xPos, yPos, zPos) instanceof TileEntityEndiumChunkloader)
				{
					((TileEntityEndiumChunkloader)world.getBlockTileEntity(xPos, yPos, zPos)).forceChunkLoading(ticket);
				}
			}
		}
	}

	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
	{
		if (item.equals(new ItemStack(ObsidianIngots.MachineBlock, 1, 5)))
		{
			item.setTagCompound(new NBTTagCompound());
			item.stackTagCompound.setInteger("MachineHP", 0);
		}
	}
	
	@Override
	public void onSmelting(EntityPlayer player, ItemStack item) {}
}
