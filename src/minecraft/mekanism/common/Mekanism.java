package mekanism.common;

import ic2.api.Ic2Recipes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import mekanism.api.InfuseObject;
import mekanism.api.InfusionInput;
import mekanism.api.InfusionType;
import mekanism.client.SoundHandler;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thermalexpansion.api.crafting.CraftingManagers;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Mekanism mod -- adds in Tools, Armor, Weapons, Machines, and Magic. Universal source.
 * @author AidanBrady
 *
 */
@Mod(modid = "Mekanism", name = "Mekanism", version = "5.5.4")
@NetworkMod(channels = {"Mekanism"}, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Mekanism
{
	/** Mekanism logger instance */
	public static Logger logger = Logger.getLogger("Minecraft");
	
	/** Mekanism proxy instance */
	@SidedProxy(clientSide = "mekanism.client.ClientProxy", serverSide = "mekanism.common.CommonProxy")
	public static CommonProxy proxy;
	
    /** Mekanism mod instance */
	@Instance("Mekanism")
    public static Mekanism instance;
    
    /** Mekanism hooks instance */
    public static MekanismHooks hooks;
    
    /** Mekanism configuration instance */
    public static Configuration configuration;
    
	/** Mekanism version number */
	public static Version versionNumber = new Version(5, 5, 4);
	
	/** Map of Teleporters */
	public static Map<Teleporter.Code, ArrayList<Teleporter.Coords>> teleporters = new HashMap<Teleporter.Code, ArrayList<Teleporter.Coords>>();
	
	/** Map of infuse objects */
	public static Map<ItemStack, InfuseObject> infusions = new HashMap<ItemStack, InfuseObject>();
	
	/** Mekanism creative tab */
	public static CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
	
	/** List of Mekanism modules loaded */
	public static List<IModule> modulesLoaded = new ArrayList<IModule>();
	
	/** The latest version number which is received from the Mekanism server */
	public static String latestVersionNumber;
	
	/** The recent news which is received from the Mekanism server */
	public static String recentNews;

	@SideOnly(Side.CLIENT)
	/** The main SoundHandler instance that is used by all audio sources */
	public static SoundHandler audioHandler;
	
	/** The IP used to connect to the Mekanism server */
	public static String hostIP = "71.56.58.57";
	
	/** The port used to connect to the Mekanism server */
	public static int hostPort = 3073;
	
	/** The modpack this client is using */
	public static String modPack = "none";
    
	//Block IDs
    public static int basicBlockID = 3000;
    public static int machineBlockID = 3001;
    public static int oreBlockID = 3002;
	public static int obsidianTNTID = 3003;
	public static int energyCubeID = 3004;
	public static int boundingBlockID = 3005;
	public static int gasTankID = 3006;
	public static int transmitterID = 3007;
	
	//Items
	public static ItemElectricBow ElectricBow;
	public static Item Stopwatch;
	public static Item WeatherOrb;
	public static Item EnrichedAlloy;
	public static ItemEnergized EnergyTablet;
	public static Item SpeedUpgrade;
	public static Item EnergyUpgrade;
	public static ItemAtomicDisassembler AtomicDisassembler;
	public static Item AtomicCore;
	public static ItemStorageTank StorageTank;
	public static Item ControlCircuit;
	public static Item EnrichedIron;
	public static Item CompressedCarbon;
	public static Item PortableTeleporter;
	public static Item TeleportationCore;
	public static Item Configurator;
	
	//Blocks
	public static Block BasicBlock;
	public static Block MachineBlock;
	public static Block OreBlock;
	public static Block ObsidianTNT;
	public static Block EnergyCube;
	public static Block BoundingBlock;
	public static Block GasTank;
	public static Block Transmitter;
	
	//Multi-ID Items
	public static Item Dust;
	public static Item Ingot;
	public static Item Clump;
	public static Item DirtyDust;
	
	//Config Values
	public static boolean extrasEnabled = true;
	public static boolean osmiumGenerationEnabled = true;
	public static boolean disableBCBronzeCrafting = true;
	public static boolean disableBCSteelCrafting = true;
	public static boolean updateNotifications = true;
	public static boolean enableSounds = true;
	public static boolean controlCircuitOreDict = true;
	public static boolean logPackets = false;
	public static int obsidianTNTBlastRadius = 12;
	public static int obsidianTNTDelay = 100;
	public static double TO_IC2;
	public static double TO_BC;
	public static double FROM_IC2;
	public static double FROM_BC;
	
	/** Total ticks passed since thePlayer joined theWorld */
	public static int ticksPassed = 0;
	
	public static int ANIMATED_TEXTURE_INDEX = 240;
	
	/**
	 * Adds all in-game crafting and smelting recipes.
	 */
	public void addRecipes()
	{
		//Storage Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 3), new Object[] {
			"***", "***", "***", Character.valueOf('*'), Item.coal
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Item.coal, 9), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 2), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 0), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 2)	
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 4), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 3), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 0), new Object[] {
			"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 1), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 2), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 5), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 4), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 5)
		}));
		
		//Base Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianTNT, 1), new Object[] {
			"***", "XXX", "***", Character.valueOf('*'), Block.obsidian, Character.valueOf('X'), Block.tnt
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(ElectricBow.getUnchargedItem(), new Object[] {
			" AB", "E B", " AB", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('B'), Item.silk, Character.valueOf('E'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergyTablet.getUnchargedItem(), new Object[] {
			"RCR", "ECE", "RCR", Character.valueOf('C'), Item.ingotGold, Character.valueOf('R'), Item.redstone, Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 0), new Object[] {
			"ARA", "CIC", "ARA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('R'), Item.redstone, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('C'), ControlCircuit
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 1), new Object[] {
			"RCR", "GIG", "RCR", Character.valueOf('R'), Item.redstone, Character.valueOf('C'), "basicCircuit", Character.valueOf('G'), Block.glass, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 2), new Object[] {
			"SCS", "RIR", "SCS", Character.valueOf('S'), Block.cobblestone, Character.valueOf('C'), "basicCircuit", Character.valueOf('R'), Item.redstone, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 3), new Object[] {
			"RLR", "CIC", "RLR", Character.valueOf('R'), Item.redstone, Character.valueOf('L'), Item.bucketLava, Character.valueOf('C'), "basicCircuit", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SpeedUpgrade), new Object[] {
			" G ", "APA", " G ", Character.valueOf('P'), "dustOsmium", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), Block.glass
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnergyUpgrade), new Object[] {
			" G ", "ADA", " G ", Character.valueOf('G'), Block.glass, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(AtomicCore), new Object[] {
			"AOA", "PDP", "AOA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('O'), "dustObsidian", Character.valueOf('P'), "dustOsmium", Character.valueOf('D'), Item.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(AtomicDisassembler.getUnchargedItem(), new Object[] {
			"AEA", "ACA", " O ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(StorageTank.getEmptyItem(), new Object[] {
			"III", "IDI", "III", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(GasTank, new Object[] {
			"PPP", "PDP", "PPP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), new Object[] {
			"RLR", "TIT", "RLR", Character.valueOf('R'), Item.redstone, Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), new Object[] {
			"EGE", "TBT", "EGE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('G'), Item.ingotGold, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), new Object[] {
			"CDC", "TAT", "CDC", Character.valueOf('C'), "basicCircuit", Character.valueOf('D'), Item.diamond, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), new Object[] {
			"COC", "TAT", "COC", Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ControlCircuit), new Object[] {
			" P ", "PEP", " P ", Character.valueOf('P'), "ingotOsmium", Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(EnrichedIron, 2), new Object[] {
			Item.redstone, Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedIron, 4), new Object[] {
			"C", "I", "C", Character.valueOf('C'), "dustCopper", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedIron, 4), new Object[] {
			"T", "I", "T", Character.valueOf('T'), "dustTin", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 8), new Object[] {
			"IFI", "CEC", "IFI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('F'), Block.furnaceIdle, Character.valueOf('C'), "basicCircuit", Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(TeleportationCore), new Object[] {
			"LAL", "GDG", "LAL", Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('A'), AtomicCore, Character.valueOf('G'), Item.ingotGold, Character.valueOf('D'), Item.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PortableTeleporter), new Object[] {
			" E ", "CTC", " E ", Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), "basicCircuit", Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 11), new Object[] {
			"COC", "OTO", "COC", Character.valueOf('C'), "basicCircuit", Character.valueOf('O'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 9), new Object[] {
			"CAC", "ERE", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), AtomicCore, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('R'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Configurator), new Object[] {
			" L ", "AEA", " S ", Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('S'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 9, 7), new Object[] {
			"OOO", "OGO", "OOO", Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('G'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Transmitter, 8, 0), new Object[] {
			"OGO", Character.valueOf('O'), "ingotOsmium", Character.valueOf('G'), Block.glass
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 8), new Object[] {
			" S ", "SPS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('P'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 10), new Object[] {
			"SCS", "GIG", "SCS", Character.valueOf('S'), Block.cobblestone, Character.valueOf('C'), ControlCircuit, Character.valueOf('G'), Block.glass, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Transmitter, 8, 1), new Object[] {
			"ORO", Character.valueOf('O'), "ingotOsmium", Character.valueOf('R'), Item.redstone
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 12), new Object[] {
			" B ", "ECE", "OOO", Character.valueOf('B'), Item.bucketEmpty, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 13), new Object[] {
			"SGS", "CcC", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), Block.glass, Character.valueOf('C'), Block.chest, Character.valueOf('c'), ControlCircuit
		}));
		
		//Factory Recipes
		CraftingManager.getInstance().getRecipeList().add(new FactoryRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, RecipeType.SMELTING), new Object[] {
			"CAC", "GOG", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('O'), new ItemStack(MachineBlock, 1, 10)
		}));
		CraftingManager.getInstance().getRecipeList().add(new FactoryRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, RecipeType.ENRICHING), new Object[] {
			"CAC", "GOG", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('O'), new ItemStack(MachineBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new FactoryRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, RecipeType.CRUSHING), new Object[] {
			"CAC", "GOG", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('O'), new ItemStack(MachineBlock, 1, 3)
		}));
		
		for(RecipeType type : RecipeType.values())
		{
			CraftingManager.getInstance().getRecipeList().add(new FactoryRecipe(MekanismUtils.getFactory(FactoryTier.ADVANCED, type), new Object[] {
				"CAC", "DOD", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustDiamond", Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.BASIC, type)
			}));
			CraftingManager.getInstance().getRecipeList().add(new FactoryRecipe(MekanismUtils.getFactory(FactoryTier.ELITE, type), new Object[] {
				"CAC", "cOc", "CAC", Character.valueOf('C'), "basicCircuit", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('c'), AtomicCore, Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.ADVANCED, type)
			}));
		}
		
		if(extrasEnabled)
		{
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 4), new Object[] {
				"SGS", "GDG", "SGS", Character.valueOf('S'), EnrichedAlloy, Character.valueOf('G'), Block.glass, Character.valueOf('D'), Block.blockDiamond
			}));
		}
	
		//Furnace Recipes
		FurnaceRecipes.smelting().addSmelting(oreBlockID, 0, new ItemStack(Ingot, 1, 1), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 2, new ItemStack(Ingot, 1, 1), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 0, new ItemStack(Item.ingotIron), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 1, new ItemStack(Item.ingotGold), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 5, new ItemStack(Ingot, 1, 4), 1.0F);
		FurnaceRecipes.smelting().addSmelting(EnrichedIron.itemID, 0, new ItemStack(EnrichedAlloy), 1.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreRedstone), new ItemStack(Item.redstone, 12));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.obsidian), new ItemStack(DirtyDust, 1, 6));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreIron), new ItemStack(Dust, 2, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreGold), new ItemStack(Dust, 2, 1));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.coal, 2), new ItemStack(CompressedCarbon, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreLapis), new ItemStack(Item.dyePowder, 12, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreCoal), new ItemStack(Block.oreCoal));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreDiamond), new ItemStack(Item.diamond, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.cobblestoneMossy), new ItemStack(Block.cobblestone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stone), new ItemStack(Block.stoneBrick, 1, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stoneBrick, 1, 2), new ItemStack(Block.stoneBrick, 1, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1, 3));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stoneBrick, 1, 1), new ItemStack(Block.stoneBrick, 1, 0));
		
		//Combiner recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.redstone, 16), new ItemStack(Block.oreRedstone));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.dyePowder, 16, 4), new ItemStack(Block.oreLapis));
		
		//Osmium Compressor Recipes
		RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Item.lightStoneDust), new ItemStack(Ingot, 1, 3));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Item.diamond), new ItemStack(Dust, 1, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotIron), new ItemStack(Dust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotGold), new ItemStack(Dust, 1, 1));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.gravel), new ItemStack(Item.flint));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stone), new ItemStack(Block.cobblestone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.cobblestone), new ItemStack(Block.sand));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stoneBrick, 1, 2), new ItemStack(Block.stone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stoneBrick, 1, 3), new ItemStack(Block.stoneBrick, 1, 0));
        
        //Purification Chamber Recipes
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.oreIron), new ItemStack(Clump, 3, 0));
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.oreGold), new ItemStack(Clump, 3, 1));
        
        //Metallurgic Infuser Recipes
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfusionType.COAL, 10, new ItemStack(EnrichedIron)), new ItemStack(Dust, 1, 5));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfusionType.BIO, 10, new ItemStack(Block.cobblestone)), new ItemStack(Block.cobblestoneMossy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfusionType.BIO, 10, new ItemStack(Block.stoneBrick, 1, 0)), new ItemStack(Block.stoneBrick, 1, 1));
        
        infusions.put(new ItemStack(Item.coal, 1, 0), new InfuseObject(InfusionType.COAL, 10));
        infusions.put(new ItemStack(Item.coal, 1, 1), new InfuseObject(InfusionType.COAL, 20));
        infusions.put(new ItemStack(CompressedCarbon), new InfuseObject(InfusionType.COAL, 100));
	}
	
	/**
	 * Adds all item and block names.
	 */
	public void addNames()
	{
		//Extras
		LanguageRegistry.addName(ElectricBow, "Electric Bow");
		LanguageRegistry.addName(ObsidianTNT, "Obsidian TNT");
		
		if(extrasEnabled == true)
		{
			LanguageRegistry.addName(Stopwatch, "Steve's Stopwatch");
			LanguageRegistry.addName(WeatherOrb, "Weather Orb");
		}
		
		LanguageRegistry.addName(EnrichedAlloy, "Enriched Alloy");
		LanguageRegistry.addName(EnergyTablet, "Energy Tablet");
		LanguageRegistry.addName(SpeedUpgrade, "Speed Upgrade");
		LanguageRegistry.addName(EnergyUpgrade, "Energy Upgrade");
		LanguageRegistry.addName(AtomicDisassembler, "Atomic Disassembler");
		LanguageRegistry.addName(AtomicCore, "Atomic Core");
		LanguageRegistry.addName(ElectricBow, "Electric Bow");
		LanguageRegistry.addName(StorageTank, "Hydrogen Tank");
		LanguageRegistry.addName(BoundingBlock, "Bounding Block");
		LanguageRegistry.addName(GasTank, "Gas Tank");
		LanguageRegistry.addName(StorageTank, "Storage Tank");
		LanguageRegistry.addName(ControlCircuit, "Control Circuit");
		LanguageRegistry.addName(EnrichedIron, "Enriched Iron");
		LanguageRegistry.addName(CompressedCarbon, "Compressed Carbon");
		LanguageRegistry.addName(PortableTeleporter, "Portable Teleporter");
		LanguageRegistry.addName(TeleportationCore, "Teleportation Core");
		LanguageRegistry.addName(Configurator, "Configurator");
		
		//Localization for BasicBlock
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.OsmiumBlock.name", "Osmium Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.BronzeBlock.name", "Bronze Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedObsidian.name", "Refined Obsidian");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.CoalBlock.name", "Coal Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedGlowstone.name", "Refined Glowstone");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.SteelBlock.name", "Steel Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.ControlPanel.name", "Control Panel");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.TeleporterFrame.name", "Teleporter Frame");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.SteelCasing.name", "Steel Casing");
		
		//Localization for MachineBlock
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EnrichmentChamber.name", "Enrichment Chamber");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.OsmiumCompressor.name", "Osmium Compressor");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Combiner.name", "Combiner");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Crusher.name", "Crusher");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.TheoreticalElementizer.name", "Theoretical Elementizer");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.BasicFactory.name", "Basic Factory");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.AdvancedFactory.name", "Advanced Factory");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EliteFactory.name", "Elite Factory");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.MetallurgicInfuser.name", "Metallurgic Infuser");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.PurificationChamber.name", "Purification Chamber");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EnergizedSmelter.name", "Energized Smelter");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Teleporter.name", "Teleporter");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.ElectricPump.name", "Electric Pump");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.ElectricChest.name", "Electric Chest");
		
		//Localization for OreBlock
		LanguageRegistry.instance().addStringLocalization("tile.OreBlock.OsmiumOre.name", "Osmium Ore");
		
		//Localization for Transmitter
		LanguageRegistry.instance().addStringLocalization("tile.Transmitter.PressurizedTube.name", "Pressurized Tube");
		LanguageRegistry.instance().addStringLocalization("tile.Transmitter.UniversalCable.name", "Universal Cable");
		
		//Localization for EnergyCube
		LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Basic.name", "Basic Energy Cube");
		LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Advanced.name", "Advanced Energy Cube");
		LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Elite.name", "Elite Energy Cube");
		LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Ultimate.name", "Ultimate Energy Cube");
		
		//Localization for Dust
		LanguageRegistry.instance().addStringLocalization("item.ironDust.name", "Iron Dust");
		LanguageRegistry.instance().addStringLocalization("item.goldDust.name", "Gold Dust");
		LanguageRegistry.instance().addStringLocalization("item.osmiumDust.name", "Osmium Dust");
		LanguageRegistry.instance().addStringLocalization("item.obsidianDust.name", "Refined Obsidian Dust");
		LanguageRegistry.instance().addStringLocalization("item.diamondDust.name", "Diamond Dust");
		LanguageRegistry.instance().addStringLocalization("item.steelDust.name", "Steel Dust");
		LanguageRegistry.instance().addStringLocalization("item.copperDust.name", "Copper Dust");
		LanguageRegistry.instance().addStringLocalization("item.tinDust.name", "Tin Dust");
		LanguageRegistry.instance().addStringLocalization("item.silverDust.name", "Silver Dust");
		
		//Localization for Clump
		LanguageRegistry.instance().addStringLocalization("item.ironClump.name", "Iron Clump");
		LanguageRegistry.instance().addStringLocalization("item.goldClump.name", "Gold Clump");
		LanguageRegistry.instance().addStringLocalization("item.osmiumClump.name", "Osmium Clump");
		LanguageRegistry.instance().addStringLocalization("item.copperClump.name", "Copper Clump");
		LanguageRegistry.instance().addStringLocalization("item.tinClump.name", "Tin Clump");
		LanguageRegistry.instance().addStringLocalization("item.silverClump.name", "Silver Clump");
		
		//Localization for Dirty Dust
		LanguageRegistry.instance().addStringLocalization("item.dirtyIronDust.name", "Dirty Iron Dust");
		LanguageRegistry.instance().addStringLocalization("item.dirtyGoldDust.name", "Dirty Gold Dust");
		LanguageRegistry.instance().addStringLocalization("item.dirtyOsmiumDust.name", "Dirty Osmium Dust");
		LanguageRegistry.instance().addStringLocalization("item.dirtyCopperDust.name", "Dirty Copper Dust");
		LanguageRegistry.instance().addStringLocalization("item.dirtyTinDust.name", "Dirty Tin Dust");
		LanguageRegistry.instance().addStringLocalization("item.dirtySilverDust.name", "Dirty Silver Dust");
		LanguageRegistry.instance().addStringLocalization("item.dirtyObsidianDust.name", "Dirty Obsidian Dust");
		
		//Localization for Ingot
		LanguageRegistry.instance().addStringLocalization("item.obsidianIngot.name", "Obsidian Ingot");
		LanguageRegistry.instance().addStringLocalization("item.osmiumIngot.name", "Osmium Ingot");
		LanguageRegistry.instance().addStringLocalization("item.bronzeIngot.name", "Bronze Ingot");
		LanguageRegistry.instance().addStringLocalization("item.glowstoneIngot.name", "Glowstone Ingot");
		LanguageRegistry.instance().addStringLocalization("item.steelIngot.name", "Steel Ingot");
		
		//Localization for Mekanism creative tab
		LanguageRegistry.instance().addStringLocalization("itemGroup.tabMekanism", "Mekanism");
	}
	
	/**
	 * Adds and registers all items.
	 */
	public void addItems()
	{
		configuration.load();
		ElectricBow = (ItemElectricBow) new ItemElectricBow(configuration.getItem("ElectricBow", 11200).getInt()).setUnlocalizedName("ElectricBow");
		if(extrasEnabled == true)
		{
			Stopwatch = new ItemStopwatch(configuration.getItem("Stopwatch", 11202).getInt()).setUnlocalizedName("Stopwatch");
			WeatherOrb = new ItemWeatherOrb(configuration.getItem("WeatherOrb", 11203).getInt()).setUnlocalizedName("WeatherOrb");
		}
		Dust = new ItemDust(configuration.getItem("Dust", 11204).getInt()-256);
		Ingot = new ItemIngot(configuration.getItem("Ingot", 11205).getInt()-256);
		EnergyTablet = (ItemEnergized) new ItemEnergized(configuration.getItem("EnergyTablet", 11206).getInt(), 1000000, 120).setUnlocalizedName("EnergyTablet");
		SpeedUpgrade = new ItemMachineUpgrade(configuration.getItem("SpeedUpgrade", 11207).getInt(), 0, 150).setUnlocalizedName("SpeedUpgrade");
		EnergyUpgrade = new ItemMachineUpgrade(configuration.getItem("EnergyUpgrade", 11208).getInt(), 1000, 0).setUnlocalizedName("EnergyUpgrade");
		//FREE ID 11209
		AtomicDisassembler = (ItemAtomicDisassembler) new ItemAtomicDisassembler(configuration.getItem("AtomicDisassembler", 11210).getInt()).setUnlocalizedName("AtomicDisassembler");
		AtomicCore = new ItemMekanism(configuration.getItem("AtomicCore", 11211).getInt()).setUnlocalizedName("AtomicCore");
		EnrichedAlloy = new ItemMekanism(configuration.getItem("EnrichedAlloy", 11212).getInt()).setUnlocalizedName("EnrichedAlloy");
		StorageTank = (ItemStorageTank) new ItemStorageTank(configuration.getItem("StorageTank", 11213).getInt(), 1600, 16).setUnlocalizedName("StorageTank");
		ControlCircuit = new ItemMekanism(configuration.getItem("ControlCircuit", 11214).getInt()).setUnlocalizedName("ControlCircuit");
		EnrichedIron = new ItemMekanism(configuration.getItem("EnrichedIron", 11215).getInt()).setUnlocalizedName("EnrichedIron");
		CompressedCarbon = new ItemMekanism(configuration.getItem("CompressedCarbon", 11216).getInt()).setUnlocalizedName("CompressedCarbon");
		PortableTeleporter = new ItemPortableTeleporter(configuration.getItem("PortableTeleporter", 11217).getInt()).setUnlocalizedName("PortableTeleporter");
		TeleportationCore = new ItemMekanism(configuration.getItem("TeleportationCore", 11218).getInt()).setUnlocalizedName("TeleportationCore");
		Clump = new ItemClump(configuration.getItem("Clump", 11219).getInt()-256);
		DirtyDust = new ItemDirtyDust(configuration.getItem("DirtyDust", 11220).getInt()-256);
		Configurator = new ItemConfigurator(configuration.getItem("Configurator", 11221).getInt()).setUnlocalizedName("Configurator");
		configuration.save();
	}
	
	/**
	 * Adds and registers all blocks.
	 */
	public void addBlocks()
	{
		//Declarations
		BasicBlock = new BlockBasic(basicBlockID).setUnlocalizedName("BasicBlock");
		MachineBlock = new BlockMachine(machineBlockID).setUnlocalizedName("MachineBlock");
		OreBlock = new BlockOre(oreBlockID).setUnlocalizedName("OreBlock");
		EnergyCube = new BlockEnergyCube(energyCubeID).setUnlocalizedName("EnergyCube");
		ObsidianTNT = new BlockObsidianTNT(obsidianTNTID).setUnlocalizedName("ObsidianTNT").setCreativeTab(tabMekanism);
		BoundingBlock = (BlockBounding) new BlockBounding(boundingBlockID).setUnlocalizedName("BoundingBlock");
		GasTank = new BlockGasTank(gasTankID).setUnlocalizedName("GasTank");
		Transmitter = new BlockTransmitter(transmitterID).setUnlocalizedName("Transmitter");
		
		//Registrations
		GameRegistry.registerBlock(ObsidianTNT, "ObsidianTNT");
		GameRegistry.registerBlock(BoundingBlock, "BoundingBlock");
		GameRegistry.registerBlock(GasTank, "GasTank");
		
		//Add block items into itemsList for blocks with common IDs.
		Item.itemsList[basicBlockID] = new ItemBlockBasic(basicBlockID - 256, BasicBlock).setUnlocalizedName("BasicBlock");
		Item.itemsList[machineBlockID] = new ItemBlockMachine(machineBlockID - 256, MachineBlock).setUnlocalizedName("MachineBlock");
		Item.itemsList[oreBlockID] = new ItemBlockOre(oreBlockID - 256, OreBlock).setUnlocalizedName("OreBlock");
		Item.itemsList[energyCubeID] = new ItemBlockEnergyCube(energyCubeID - 256, EnergyCube).setUnlocalizedName("EnergyCube");
		Item.itemsList[transmitterID] = new ItemBlockTransmitter(transmitterID - 256, Transmitter).setUnlocalizedName("Transmitter");
	}
	
	/**
	 * Integrates the mod with other mods -- registering items and blocks with the Forge Ore Dictionary
	 * and adding machine recipes with other items' corresponding resources.
	 */
	public void addIntegratedItems()
	{
		OreDictionary.registerOre("dustIron", new ItemStack(Dust, 1, 0));
		OreDictionary.registerOre("dustGold", new ItemStack(Dust, 1, 1));
		OreDictionary.registerOre("dustOsmium", new ItemStack(Dust, 1, 2));
		OreDictionary.registerOre("dustRefinedObsidian", new ItemStack(Dust, 1, 3));
		OreDictionary.registerOre("dustDiamond", new ItemStack(Dust, 1, 4));
		OreDictionary.registerOre("dustSteel", new ItemStack(Dust, 1, 5));
		OreDictionary.registerOre("dustCopper", new ItemStack(Dust, 1, 6));
		OreDictionary.registerOre("dustTin", new ItemStack(Dust, 1, 7));
		OreDictionary.registerOre("dustSilver", new ItemStack(Dust, 1, 8));
		
		OreDictionary.registerOre("ingotRefinedObsidian", new ItemStack(Ingot, 1, 0));
		OreDictionary.registerOre("ingotOsmium", new ItemStack(Ingot, 1, 1));
		OreDictionary.registerOre("ingotBronze", new ItemStack(Ingot, 1, 2));
		OreDictionary.registerOre("ingotRefinedGlowstone", new ItemStack(Ingot, 1, 3));
		OreDictionary.registerOre("ingotSteel", new ItemStack(Ingot, 1, 4));
		
		OreDictionary.registerOre("blockOsmium", new ItemStack(BasicBlock, 1, 0));
		OreDictionary.registerOre("blockBronze", new ItemStack(BasicBlock, 1, 1));
		OreDictionary.registerOre("blockRefinedObsidian", new ItemStack(BasicBlock, 1, 2));
		OreDictionary.registerOre("blockCoal", new ItemStack(BasicBlock, 1, 3));
		OreDictionary.registerOre("blockRefinedGlowstone", new ItemStack(BasicBlock, 1, 4));
		OreDictionary.registerOre("blockSteel", new ItemStack(BasicBlock, 1, 5));
		
		OreDictionary.registerOre("dustDirtyIron", new ItemStack(DirtyDust, 1, 0));
		OreDictionary.registerOre("dustDirtyGold", new ItemStack(DirtyDust, 1, 1));
		OreDictionary.registerOre("dustDirtyOsmium", new ItemStack(DirtyDust, 1, 2));
		OreDictionary.registerOre("dustDirtyCopper", new ItemStack(DirtyDust, 1, 3));
		OreDictionary.registerOre("dustDirtyTin", new ItemStack(DirtyDust, 1, 4));
		OreDictionary.registerOre("dustDirtySilver", new ItemStack(DirtyDust, 1, 5));
		OreDictionary.registerOre("dustDirtyObsidian", new ItemStack(DirtyDust, 1, 6));
		
		//for RailCraft. cj + obsidian dust = rawr
		OreDictionary.registerOre("dustObsidian", new ItemStack(DirtyDust, 1, 6));
		
		OreDictionary.registerOre("clumpIron", new ItemStack(Clump, 1, 0));
		OreDictionary.registerOre("clumpGold", new ItemStack(Clump, 1, 1));
		OreDictionary.registerOre("clumpOsmium", new ItemStack(Clump, 1, 2));
		OreDictionary.registerOre("clumpCopper", new ItemStack(Clump, 1, 3));
		OreDictionary.registerOre("clumpTin", new ItemStack(Clump, 1, 4));
		OreDictionary.registerOre("clumpSilver", new ItemStack(Clump, 1, 5));
		
		OreDictionary.registerOre("oreOsmium", new ItemStack(OreBlock, 1, 0));
		
		try {
			CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(OreBlock, 1, 0), new ItemStack(Dust, 2, 2), false);
			
			CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Mekanism.Ingot, 1, 1), new ItemStack(Mekanism.Dust, 1, 2), false);
			CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Mekanism.Ingot, 1, 0), new ItemStack(Mekanism.Dust, 1, 3), false);
			CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Mekanism.Ingot, 1, 3), new ItemStack(Item.lightStoneDust), false);
			CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Mekanism.Ingot, 1, 4), new ItemStack(Mekanism.Dust, 1, 5), false);
			
			CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 0), new ItemStack(DirtyDust, 1, 0), false);
			CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 1), new ItemStack(DirtyDust, 1, 1), false);
			CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 2), new ItemStack(DirtyDust, 1, 2), false);
			CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 3), new ItemStack(DirtyDust, 1, 3), false);
			CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 4), new ItemStack(DirtyDust, 1, 4), false);
			CraftingManagers.pulverizerManager.addRecipe(80, new ItemStack(Clump, 1, 5), new ItemStack(DirtyDust, 1, 5), false);
			System.out.println("[Mekanism] Hooked into Thermal Expansion successfully.");
		} catch(Exception e) {}
		
		if(controlCircuitOreDict)
		{
			OreDictionary.registerOre("basicCircuit", new ItemStack(ControlCircuit));
		}
		
		OreDictionary.registerOre("itemCompressedCarbon", new ItemStack(CompressedCarbon));
		OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(EnrichedAlloy));
		
		if(hooks.IC2Loaded)
		{
			if(!hooks.RailcraftLoaded)
			{
				Ic2Recipes.addMaceratorRecipe(new ItemStack(Block.obsidian), new ItemStack(DirtyDust, 1, 6));
			}
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustRefinedObsidian"))
		{
			RecipeHandler.addOsmiumCompressorRecipe(ore, new ItemStack(Ingot, 1, 0));
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(DirtyDust, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("clumpIron"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(DirtyDust, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("clumpGold"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(DirtyDust, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("clumpOsmium"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(DirtyDust, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("clumpCopper"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(DirtyDust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("clumpTin"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(DirtyDust, 1, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("clumpSilver"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(DirtyDust, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyIron"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyGold"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtySilver"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 1, 8));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 2, 6));
			RecipeHandler.addPurificationChamberRecipe(ore, new ItemStack(Clump, 3, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 2, 7));
			RecipeHandler.addPurificationChamberRecipe(ore, new ItemStack(Clump, 3, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 2, 2));
			RecipeHandler.addPurificationChamberRecipe(ore, new ItemStack(Clump, 3, 2));
		}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("oreLead"))
			{
				RecipeHandler.addEnrichmentChamberRecipe(ore, MekanismUtils.getStackWithSize(OreDictionary.getOres("dustLead").get(0), 2));
			}
			
			for(ItemStack ore : OreDictionary.getOres("ingotLead"))
			{
				RecipeHandler.addCrusherRecipe(ore, MekanismUtils.getStackWithSize(OreDictionary.getOres("dustLead").get(0), 1));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("oreSilver"))
			{
				RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 2, 8));
				RecipeHandler.addPurificationChamberRecipe(ore, new ItemStack(Clump, 3, 5));
			}
			
			for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
			{
				RecipeHandler.addCrusherRecipe(ore, new ItemStack(Dust, 1, 8));
			}
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Dust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Dust, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRedstone"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Item.redstone));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedGlowstone"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Item.lightStoneDust));
		}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 2), MekanismUtils.getStackWithSize(OreDictionary.getOres("dustBronze").get(0), 1));
			
			if(hooks.IC2Loaded)
			{
				Ic2Recipes.addMaceratorRecipe(new ItemStack(Ingot, 1, 2), MekanismUtils.getStackWithSize(OreDictionary.getOres("dustBronze").get(0), 1));
			}
			if(hooks.TELoaded)
			{
				CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Ingot, 1, 2), MekanismUtils.getStackWithSize(OreDictionary.getOres("dustBronze").get(0), 1), false);
			}
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().addSmelting(Dust.itemID, 6, MekanismUtils.getStackWithSize(OreDictionary.getOres("ingotCopper").get(0), 1), 1.0F);
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().addSmelting(Dust.itemID, 7, MekanismUtils.getStackWithSize(OreDictionary.getOres("ingotTin").get(0), 1), 1.0F);
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().addSmelting(Dust.itemID, 8, MekanismUtils.getStackWithSize(OreDictionary.getOres("ingotSilver").get(0), 1), 1.0F);
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Item.coal), MekanismUtils.getStackWithSize(OreDictionary.getOres("dustCoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Item.coal, 1, 1), MekanismUtils.getStackWithSize(OreDictionary.getOres("dustCharcoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
			{
				RecipeHandler.addCrusherRecipe(MekanismUtils.getStackWithSize(ore, 1), new ItemStack(Dust, 1, 6));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotTin"))
			{
				RecipeHandler.addCrusherRecipe(MekanismUtils.getStackWithSize(ore, 1), new ItemStack(Dust, 1, 7));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
			{
				RecipeHandler.addCrusherRecipe(MekanismUtils.getStackWithSize(ore, 1), new ItemStack(Dust, 1, 8));
			}
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("dustIron"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 8), new ItemStack(Block.oreIron));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSteel"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.getStackWithSize(ore, 1), new ItemStack(Dust, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 8), new ItemStack(Block.oreGold));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustObsidian"))
		{
			RecipeHandler.addCombinerRecipe(ore, new ItemStack(Block.obsidian));
			RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfusionType.DIAMOND, 10, MekanismUtils.getStackWithSize(ore, 1)), new ItemStack(Dust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustOsmium"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 8), new ItemStack(OreBlock, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDiamond"))
		{
			infusions.put(ore, new InfuseObject(InfusionType.DIAMOND, 80));
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.getStackWithSize(ore, 1), new ItemStack(Item.diamond));
		}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustCopper"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 8), MekanismUtils.getStackWithSize(OreDictionary.getOres("oreCopper").get(0), 1));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
			{
				RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfusionType.TIN, 10, MekanismUtils.getStackWithSize(ore, 1)), new ItemStack(Ingot, 1, 2));
			}
		} catch(Exception e) {}
			
		try {
			for(ItemStack ore : OreDictionary.getOres("dustTin"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 8), MekanismUtils.getStackWithSize(OreDictionary.getOres("oreTin").get(0), 1));
				infusions.put(ore, new InfuseObject(InfusionType.TIN, 50));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustLead"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 8), MekanismUtils.getStackWithSize(OreDictionary.getOres("oreLead").get(0), 1));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustSilver"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 8), MekanismUtils.getStackWithSize(OreDictionary.getOres("oreSilver").get(0), 1));
			}
		} catch(Exception e) {}
	}
	
	/**
	 * Adds and registers all entities and tile entities.
	 */
	public void addEntities()
	{
		//Entity IDs
		EntityRegistry.registerGlobalEntityID(EntityObsidianTNT.class, "ObsidianTNT", EntityRegistry.findGlobalUniqueEntityId());
		
		//Registrations
		EntityRegistry.registerModEntity(EntityObsidianTNT.class, "ObsidianTNT", 51, this, 40, 5, true);
		
		//Tile entities
		GameRegistry.registerTileEntity(TileEntityEnrichmentChamber.class, "EnrichmentChamber");
		GameRegistry.registerTileEntity(TileEntityOsmiumCompressor.class, "OsmiumCompressor");
		GameRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner");
		GameRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher");
		GameRegistry.registerTileEntity(TileEntityEnergyCube.class, "EnergyCube");
		GameRegistry.registerTileEntity(TileEntityBoundingBlock.class, "BoundingBlock");
		GameRegistry.registerTileEntity(TileEntityControlPanel.class, "ControlPanel");
		GameRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank");
		GameRegistry.registerTileEntity(TileEntityFactory.class, "SmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityAdvancedFactory.class, "AdvancedSmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityEliteFactory.class, "UltimateSmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter");
		GameRegistry.registerTileEntity(TileEntityPurificationChamber.class, "PurificationChamber");
		GameRegistry.registerTileEntity(TileEntityEnergizedSmelter.class, "EnergizedSmelter");
		
		//Load tile entities that have special renderers.
		proxy.registerSpecialTileEntities();
	}
	
	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandMekanism());
	}
	
	@ServerStopping
	public void serverStopping(FMLServerStoppingEvent event)
	{
		proxy.unloadSoundHandler();
		teleporters.clear();
	}
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event)
	{
		File config = event.getSuggestedConfigurationFile();
		//Set the mod's configuration
		configuration = new Configuration(config);
		
		if(config.getAbsolutePath().contains("voltz"))
		{
			System.out.println("[Mekanism] Detected Voltz in root directory - hello, fellow user!");
			modPack = "Voltz";
		}
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent event)
	{
		hooks = new MekanismHooks();
		hooks.hook();
		addIntegratedItems();
		
		System.out.println("[Mekanism] Hooking complete.");
		
		proxy.loadSoundHandler();
	}
	
	@Init
	public void init(FMLInitializationEvent event) 
	{
		//Register the mod's ore handler
		GameRegistry.registerWorldGenerator(new OreHandler());
		
		//Register the mod's GUI handler
		NetworkRegistry.instance().registerGuiHandler(this, new CoreGuiHandler());
		
		//Initialization notification
		System.out.println("[Mekanism] Version " + versionNumber + " initializing...");
		
		//Get data from server.
		new ThreadGetData();
		
		//Load proxy
		proxy.registerRenderInformation();
		proxy.loadConfiguration();
		proxy.loadTickHandler();
		
		//Register to receive subscribed events
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new IC2EnergyHandler());

		//Load this module
		addItems();
		addBlocks();
		addNames();
		addRecipes();
		addEntities();
		
		//Completion notification
		System.out.println("[Mekanism] Loading complete.");
		
		//Success message
		logger.info("[Mekanism] Mod loaded.");
	}
}