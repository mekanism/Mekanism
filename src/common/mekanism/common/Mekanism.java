package mekanism.common;

import ic2.api.Ic2Recipes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import universalelectricity.prefab.multiblock.*;
import mekanism.api.ItemMachineUpgrade;
import mekanism.client.SoundHandler;
import net.minecraftforge.common.*;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpecialSpawnEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraft.src.*;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

/**
 * Mekanism mod -- adds in Tools, Armor, Weapons, Machines, and Magic. Universal source.
 * @author AidanBrady
 *
 */
@Mod(modid = "Mekanism", name = "Mekanism", version = "5.0.0")
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
	public static Version versionNumber = new Version(5, 0, 0);
	
	/** Mekanism creative tab */
	public static CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
	
	/** The latest version number which is received from the Mekanism server */
	public static String latestVersionNumber;
	
	/** The recent news which is received from the Mekanism server */
	public static String recentNews;
	
	/** The main MachineryManager instance that is used by all machines */
	public static MachineryManager manager;

	@SideOnly(Side.CLIENT)
	/** The main SoundHandler instance that is used by all audio sources */
	public static SoundHandler audioHandler;
	
	/** The IP used to connect to the Mekanism server */
	public static String hostIP = "71.56.58.57";
	
	/** The port used to connect to the Mekanism server */
	public static int hostPort = 3073;
    
	//Block IDs
    public static int basicBlockID = 3000;
    public static int machineBlockID = 3001;
    public static int oreBlockID = 3002;
	public static int obsidianTNTID = 3003;
	public static int powerUnitID = 3004;
	public static int nullRenderID = 3007;
	public static int gasTankID = 3009;
	
	//Extra Items
	public static ItemElectricBow ElectricBow;
	public static Item LightningRod;
	public static Item Stopwatch;
	public static Item WeatherOrb;
	public static Item EnrichedAlloy;
	public static ItemEnergized EnergyTablet;
	public static ItemEnergized EnergyOrb;
	public static ItemEnergized EnergyCube;
	public static Item SpeedUpgrade;
	public static Item EnergyUpgrade;
	public static Item UltimateUpgrade;
	public static ItemAtomicDisassembler AtomicDisassembler;
	public static Item AtomicCore;
	public static ItemStorageTank HydrogenTank;
	public static ItemStorageTank OxygenTank;
	
	//Extra Blocks
	public static Block BasicBlock;
	public static Block MachineBlock;
	public static Block OreBlock;
	public static Block ObsidianTNT;
	public static Block PowerUnit;
	public static BlockMulti NullRender;
	public static Block GasTank;
	
	//MultiID Items
	public static Item Dust;
	public static Item Ingot;
	
	//Boolean Values
	public static boolean extrasEnabled = true;
	public static boolean oreGenerationEnabled = true;
	
	//Extra data
	public static float ObsidianTNTBlastRadius = 12.0F;
	public static int ObsidianTNTDelay = 100;
	
	/** Total ticks passed since thePlayer joined theWorld */
	public static int ticksPassed = 0;
	
	public static int ANIMATED_TEXTURE_INDEX = 240;
	
	/**
	 * Adds all in-game crafting and smelting recipes.
	 */
	public void addRecipes()
	{
		//Crafting Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 3), new Object[] {
			"***", "***", "***", Character.valueOf('*'), Item.coal
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Item.coal, 9), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 2), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 0), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 2)	
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 4), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 3), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 0), new Object[] {
			"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotPlatinum"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 1), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotRedstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 2), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 1)
		}));
		
		//Extra
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ObsidianTNT, 1), new Object[] {
			"***", "XXX", "***", Character.valueOf('*'), Block.obsidian, Character.valueOf('X'), Block.tnt
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(ElectricBow.getUnchargedItem(), new Object[] {
			" AB", "E B", " AB", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('B'), Item.silk, Character.valueOf('E'), EnergyCube.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergyCube.getUnchargedItem(), new Object[] {
			"RAR", "APA", "RAR", Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('P'), "dustPlatinum"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergyTablet.getUnchargedItem(), new Object[] {
			"RCR", "ECE", "RCR", Character.valueOf('C'), EnergyCube.getUnchargedItem(), Character.valueOf('R'), Item.redstone, Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EnergyOrb.getUnchargedItem(), new Object[] {
			"ECE", "CCC", "ECE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), EnergyCube.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PowerUnit, 1, 0), new Object[] {
			"CEC", "EPE", "CEC", Character.valueOf('C'), EnergyCube.getUnchargedItem(), Character.valueOf('E'), EnrichedAlloy, Character.valueOf('P'), new ItemStack(BasicBlock, 1, 0) 
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(PowerUnit, 1, 1), new Object[] {
			"ECE", "CPC", "ECE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), EnergyCube.getUnchargedItem(), Character.valueOf('P'), new ItemStack(PowerUnit, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 0), new Object[] {
			"***", "*R*", "***", Character.valueOf('*'), "ingotPlatinum", Character.valueOf('R'), Item.redstone
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 1), new Object[] {
			"***", "*P*", "***", Character.valueOf('*'), Item.redstone, Character.valueOf('P'), new ItemStack(BasicBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 2), new Object[] {
			"***", "*P*", "***", Character.valueOf('*'), Block.cobblestone, Character.valueOf('P'), new ItemStack(BasicBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 3), new Object[] {
			"***", "*L*", "***", Character.valueOf('*'), "ingotPlatinum", Character.valueOf('L'), Item.bucketLava
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SpeedUpgrade), new Object[] {
			"PAP", "AEA", "PAP", Character.valueOf('P'), "dustPlatinum", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), Item.emerald
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnergyUpgrade), new Object[] {
			"RAR", "AEA", "RAR", Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyCube.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(UltimateUpgrade), new Object[] {
			"ERA", "RDR", "ARS", Character.valueOf('E'), EnergyUpgrade, Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), Item.diamond, Character.valueOf('S'), SpeedUpgrade
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(AtomicCore), new Object[] {
			"AOA", "PDP", "AOA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('O'), "dustObsidian", Character.valueOf('P'), new ItemStack(Dust, 1, 2), Character.valueOf('D'), Item.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(AtomicDisassembler.getUnchargedItem(), new Object[] {
			"AEA", "ACA", " O ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedAlloy), new Object[] {
			" R ", "RIR", " R ", Character.valueOf('R'), Item.redstone, Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 5), new Object[] {
			"PAP", "AIA", "PAP", Character.valueOf('P'), "ingotPlatinum", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('I'), Block.blockSteel
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(HydrogenTank.getEmptyItem(), new Object[] {
			"III", "IDI", "III", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(OxygenTank.getEmptyItem(), new Object[] {
			"III", "IGI", "III", Character.valueOf('I'), Item.ingotIron, Character.valueOf('G'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(GasTank, new Object[] {
			"PPP", "P P", "PPP", Character.valueOf('P'), "ingotPlatinum"
		}));
		
		if(extrasEnabled)
		{
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 4), new Object[] {
				"SGS", "GDG", "SGS", Character.valueOf('S'), EnrichedAlloy, Character.valueOf('G'), Block.glass, Character.valueOf('D'), Block.blockDiamond
			}));
		}
	
		//Furnace Recipes
		GameRegistry.addSmelting(new ItemStack(OreBlock, 1, 0).itemID, new ItemStack(Ingot, 2, 1), 1.0F);
		GameRegistry.addSmelting(new ItemStack(Dust, 1, 2).itemID, new ItemStack(Ingot, 1, 1), 1.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Dust, 1, 4), new ItemStack(Item.diamond));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(OreBlock, 1, 0), new ItemStack(Dust, 2, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreRedstone), new ItemStack(Item.redstone, 2));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.obsidian), new ItemStack(Dust, 1, 3));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreIron), new ItemStack(Dust, 2, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreGold), new ItemStack(Dust, 2, 1));
		
		//Platinum Compressor Recipes
		RecipeHandler.addPlatinumCompressorRecipe(new ItemStack(Item.redstone), new ItemStack(Ingot, 1, 2));
		RecipeHandler.addPlatinumCompressorRecipe(new ItemStack(Item.lightStoneDust), new ItemStack(Ingot, 1, 3));
        RecipeHandler.addPlatinumCompressorRecipe(new ItemStack(Dust, 1, 3), new ItemStack(Ingot, 1, 0));
		
		//Combiner Recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.redstone, 4), new ItemStack(Block.oreRedstone));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.redstone), new ItemStack(Ingot, 1, 2));
		RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 2, 2), new ItemStack(OreBlock, 1, 0));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.diamond), new ItemStack(Block.oreDiamond));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.dyePowder, 4, 4), new ItemStack(Block.oreLapis));
        RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 1, 3), new ItemStack(Block.obsidian));
		RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 2, 0), new ItemStack(Block.oreIron));
		RecipeHandler.addCombinerRecipe(new ItemStack(Dust, 2, 1), new ItemStack(Block.oreGold));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Item.diamond), new ItemStack(Dust, 1, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 2), new ItemStack(Item.redstone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 1), new ItemStack(Dust, 1, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 3), new ItemStack(Item.lightStoneDust));
        RecipeHandler.addCrusherRecipe(new ItemStack(Ingot, 1, 0), new ItemStack(Dust, 1, 3));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotIron), new ItemStack(Dust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotGold), new ItemStack(Dust, 1, 1));
        
        //Theoretical Elementizer Recipes
        RecipeHandler.addTheoreticalElementizerRecipe(new ItemStack(EnrichedAlloy), new ItemStack(TileEntityTheoreticalElementizer.getRandomMagicItem()));
	}
	
	/**
	 * Adds all item and block names.
	 */
	public void addNames()
	{
		//Extras
		LanguageRegistry.addName(ElectricBow, "Energized Bow");
		LanguageRegistry.addName(ObsidianTNT, "Obsidian TNT");
		
		if(extrasEnabled == true)
		{
			LanguageRegistry.addName(LightningRod, "Lightning Rod");
			LanguageRegistry.addName(Stopwatch, "Steve's Stopwatch");
			LanguageRegistry.addName(WeatherOrb, "Weather Orb");
			LanguageRegistry.addName(EnrichedAlloy, "Enriched Alloy");
		}
		
		LanguageRegistry.addName(EnergyTablet, "Energy Tablet");
		LanguageRegistry.addName(EnergyOrb, "Energy Orb");
		LanguageRegistry.addName(EnergyCube, "Energy Cube");
		LanguageRegistry.addName(SpeedUpgrade, "Speed Upgrade");
		LanguageRegistry.addName(EnergyUpgrade, "Energy Upgrade");
		LanguageRegistry.addName(UltimateUpgrade, "Ultimate Upgrade");
		LanguageRegistry.addName(AtomicDisassembler, "Nuclear Disassembler");
		LanguageRegistry.addName(AtomicCore, "Nuclear Core");
		LanguageRegistry.addName(ElectricBow, "Electric Bow");
		LanguageRegistry.addName(HydrogenTank, "Hydrogen Tank");
		LanguageRegistry.addName(OxygenTank, "Oxygen Tank");
		LanguageRegistry.addName(NullRender, "Null Render");
		LanguageRegistry.addName(GasTank, "Gas Tank");
		
		//Localization for MultiBlock
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.PlatinumBlock.name", "Platinum Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RedstoneBlock.name", "Redstone Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedObsidian.name", "Refined Obsidian");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.CoalBlock.name", "Coal Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedGlowstone.name", "Refined Glowstone");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.ReinforcedIron.name", "Reinforced Iron");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.ControlPanel.name", "Control Panel");
		
		//Localization for MachineBlock
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EnrichmentChamber.name", "Enrichment Chamber");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.PlatinumCompressor.name", "Platinum Compressor");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Combiner.name", "Combiner");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Crusher.name", "Crusher");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.TheoreticalElementizer.name", "Theoretical Elementizer");
		
		//Localization for OreBlock
		LanguageRegistry.instance().addStringLocalization("tile.OreBlock.PlatinumOre.name", "Platinum Ore");
		
		//Localization for PowerUnit
		LanguageRegistry.instance().addStringLocalization("tile.PowerUnit.PowerUnit.name", "Power Unit");
		LanguageRegistry.instance().addStringLocalization("tile.PowerUnit.AdvancedPowerUnit.name", "Advanced Power Unit");
		
		//Localization for Dust
		LanguageRegistry.instance().addStringLocalization("item.ironDust.name", "Iron Dust");
		LanguageRegistry.instance().addStringLocalization("item.goldDust.name", "Gold Dust");
		LanguageRegistry.instance().addStringLocalization("item.platinumDust.name", "Platinum Dust");
		LanguageRegistry.instance().addStringLocalization("item.obsidianDust.name", "Obsidian Dust");
		LanguageRegistry.instance().addStringLocalization("item.diamondDust.name", "Diamond Dust");
		
		//Localization for Ingot
		LanguageRegistry.instance().addStringLocalization("item.obsidianIngot.name", "Obsidian Ingot");
		LanguageRegistry.instance().addStringLocalization("item.platinumIngot.name", "Platinum Ingot");
		LanguageRegistry.instance().addStringLocalization("item.redstoneIngot.name", "Redstone Ingot");
		LanguageRegistry.instance().addStringLocalization("item.glowstoneIngot.name", "Glowstone Ingot");
	}
	
	/**
	 * Adds all item textures from the sprite sheet.
	 */
	public void addTextures()
	{
		if(extrasEnabled == true)
		{
			LightningRod.setIconIndex(225);
			Stopwatch.setIconIndex(224);
			WeatherOrb.setIconIndex(226);
			EnrichedAlloy.setIconIndex(227);
		}
		
		EnergyTablet.setIconIndex(228);
		EnergyOrb.setIconIndex(229);
		EnergyCube.setIconIndex(230);
		SpeedUpgrade.setIconIndex(232);
		EnergyUpgrade.setIconIndex(231);
		UltimateUpgrade.setIconIndex(233);	
		AtomicDisassembler.setIconIndex(253);
		AtomicCore.setIconIndex(254);
		ElectricBow.setIconIndex(252);
		HydrogenTank.setIconIndex(251);
		OxygenTank.setIconIndex(239);
	}
	
	/**
	 * Adds and registers all items.
	 */
	public void addItems()
	{
		ElectricBow = (ItemElectricBow) new ItemElectricBow(11275).setItemName("ElectricBow");
		if(extrasEnabled == true)
		{
			LightningRod = new ItemLightningRod(11276).setItemName("LightningRod");
			Stopwatch = new ItemStopwatch(11277).setItemName("Stopwatch");
			WeatherOrb = new ItemWeatherOrb(11278).setItemName("WeatherOrb");
		}
		Dust = new ItemDust(11293-256);
		Ingot = new ItemIngot(11294-256);
		EnergyTablet = (ItemEnergized) new ItemEnergized(11306, 2500000, 512, 25000).setItemName("EnergyTablet");
		EnergyOrb = (ItemEnergized) new ItemEnergized(11307, 5000000, 512, 50000).setItemName("EnergyOrb");
		EnergyCube = (ItemEnergized) new ItemEnergized(11308, 1000000, 512, 10000).setItemName("EnergyCube");
		SpeedUpgrade = new ItemMachineUpgrade(11309, 0, 150).setItemName("SpeedUpgrade");
		EnergyUpgrade = new ItemMachineUpgrade(11310, 1000, 0).setItemName("EnergyUpgrade");
		UltimateUpgrade = new ItemMachineUpgrade(11311, 2500, 180).setItemName("UltimateUpgrade");
		AtomicDisassembler = (ItemAtomicDisassembler) new ItemAtomicDisassembler(11312).setItemName("AtomicDisassembler");
		AtomicCore = new ItemMekanism(11313).setItemName("AtomicCore");
		EnrichedAlloy = new ItemMekanism(11315).setItemName("EnrichedAlloy");
		HydrogenTank = (ItemHydrogenTank) new ItemHydrogenTank(11316).setItemName("HydrogenTank");
		OxygenTank = (ItemOxygenTank) new ItemOxygenTank(11317).setItemName("OxygenTank");
	}
	
	/**
	 * Adds and registers all blocks.
	 */
	public void addBlocks()
	{
		//Declarations
		BasicBlock = new BlockBasic(basicBlockID).setBlockName("BasicBlock");
		MachineBlock = new BlockMachine(machineBlockID).setBlockName("MachineBlock");
		OreBlock = new BlockOre(oreBlockID).setBlockName("OreBlock");
		PowerUnit = new BlockPowerUnit(powerUnitID).setBlockName("PowerUnit");
		ObsidianTNT = new BlockObsidianTNT(obsidianTNTID).setBlockName("ObsidianTNT").setCreativeTab(tabMekanism);
		NullRender = (BlockMulti) new BlockMulti(nullRenderID).setBlockName("NullRender");
		GasTank = new BlockGasTank(gasTankID).setBlockName("GasTank");
		
		//Registrations
		GameRegistry.registerBlock(ObsidianTNT);
		GameRegistry.registerBlock(NullRender);
		GameRegistry.registerBlock(GasTank);
		
		//Add block items into itemsList for blocks with common IDs.
		Item.itemsList[basicBlockID] = new ItemBlockBasic(basicBlockID - 256, BasicBlock).setItemName("BasicBlock");
		Item.itemsList[machineBlockID] = new ItemBlockMachine(machineBlockID - 256, MachineBlock).setItemName("MachineBlock");
		Item.itemsList[oreBlockID] = new ItemBlockOre(oreBlockID - 256, OreBlock).setItemName("OreBlock");
		Item.itemsList[powerUnitID] = new ItemBlockPowerUnit(powerUnitID - 256, PowerUnit).setItemName("PowerUnit");
	}
	
	/**
	 * Integrates the mod with other mods -- registering items and blocks with the Forge Ore Dictionary
	 * and adding machine recipes with other items' corresponding resources.
	 */
	public void addIntegratedItems()
	{
		OreDictionary.registerOre("dustIron", new ItemStack(Dust, 1, 0));
		OreDictionary.registerOre("dustGold", new ItemStack(Dust, 1, 1));
		OreDictionary.registerOre("dustPlatinum", new ItemStack(Dust, 1, 2));
		OreDictionary.registerOre("dustObsidian", new ItemStack(Dust, 1, 3));
		
		OreDictionary.registerOre("ingotObsidian", new ItemStack(Ingot, 1, 0));
		OreDictionary.registerOre("ingotPlatinum", new ItemStack(Ingot, 1, 1));
		OreDictionary.registerOre("ingotRedstone", new ItemStack(Ingot, 1, 2));
		OreDictionary.registerOre("ingotGlowstone", new ItemStack(Ingot, 1, 3));
		
		OreDictionary.registerOre("orePlatinum", new ItemStack(OreBlock, 1, 0));
		
		if(hooks.IC2Loaded)
		{
			if(!hooks.RailcraftLoaded)
			{
				Ic2Recipes.addMaceratorRecipe(new ItemStack(Block.obsidian), new ItemStack(Dust, 1, 3));
			}
			ItemStack dustIron = hooks.IC2IronDust.copy();
			dustIron.stackSize = 2;
			ItemStack dustGold = hooks.IC2GoldDust.copy();
			dustGold.stackSize = 2;
			RecipeHandler.addCombinerRecipe(dustIron, new ItemStack(Block.oreIron));
			RecipeHandler.addCombinerRecipe(dustGold, new ItemStack(Block.oreGold));
		}
		
		if(hooks.RailcraftLoaded)
		{
			RecipeHandler.addPlatinumCompressorRecipe(hooks.RailcraftObsidianDust, new ItemStack(Ingot, 1, 0));
			RecipeHandler.addCombinerRecipe(hooks.RailcraftObsidianDust, new ItemStack(Block.obsidian));
		}
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
		GameRegistry.registerTileEntity(TileEntityPlatinumCompressor.class, "PlatinumCompressor");
		GameRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner");
		GameRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher");
		GameRegistry.registerTileEntity(TileEntityTheoreticalElementizer.class, "TheoreticalElementizer");
		GameRegistry.registerTileEntity(TileEntityPowerUnit.class, "PowerUnit");
		GameRegistry.registerTileEntity(TileEntityAdvancedPowerUnit.class, "AdvancedPowerUnit");
		GameRegistry.registerTileEntity(TileEntityMulti.class, "Multi");
		GameRegistry.registerTileEntity(TileEntityControlPanel.class, "ControlPanel");
		GameRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank");
		
		//Load tile entities that have special renderers.
		proxy.registerSpecialTileEntities();
	}
	
	/**
	 * Registers the server command handler.
	 */
	@SideOnly(Side.SERVER)
	public void registerServerCommands()
	{
		ServerCommandHandler.initialize();
	}
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event)
	{
		//Set the mod's configuration
		configuration = new Configuration(event.getSuggestedConfigurationFile());
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
		//Register the MachineryManager
		manager = new MachineryManager();
		System.out.println("[Mekanism] Version " + versionNumber + " initializing...");
		new ThreadGetData();
		proxy.registerRenderInformation();
		proxy.loadConfiguration();
		proxy.loadUtilities();
		proxy.loadTickHandler();
		
		MinecraftForge.EVENT_BUS.register(this);
		
		LanguageRegistry.instance().addStringLocalization("itemGroup.tabMekanism", "Mekanism");
		
		//Attempt to load server commands
		try {
			registerServerCommands();
		} catch(NoSuchMethodError e) {}

		//Add all items
		addItems();
		System.out.println("[Mekanism] Items loaded.");
		
		//Add all blocks
		addBlocks();
		System.out.println("[Mekanism] Blocks loaded.");
		
		//Set item and block names
		addNames();
		System.out.println("[Mekanism] Names loaded.");
		
		//Set item and block textures
		addTextures();
		System.out.println("[Mekanism] Textures loaded.");
		
		//Set item and block recipes
		addRecipes();
		System.out.println("[Mekanism] Recipes loaded.");
		
		//Set up entities to run on SSP and SMP
		addEntities();
		System.out.println("[Mekanism] Entities loaded.");
		
		//Success message
		logger.info("[Mekanism] Mod loaded.");
	}
}