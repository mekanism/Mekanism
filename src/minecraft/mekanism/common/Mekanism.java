package mekanism.common;

import ic2.api.Ic2Recipes;

import java.util.logging.Logger;

import mekanism.api.InfusionInput;
import mekanism.api.InfusionOutput;
import mekanism.api.InfusionType;
import mekanism.api.Tier.EnergyCubeTier;
import mekanism.client.SoundHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalelectricity.prefab.multiblock.BlockMulti;
import universalelectricity.prefab.multiblock.TileEntityMulti;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
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
@Mod(modid = "Mekanism", name = "Mekanism", version = "5.0.9")
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
	public static Version versionNumber = new Version(5, 0, 9);
	
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
	public static int energyCubeID = 3004;
	public static int nullRenderID = 3005;
	public static int gasTankID = 3006;
	
	
	//Extra Items
	public static ItemElectricBow ElectricBow;
	public static Item Stopwatch;
	public static Item WeatherOrb;
	public static Item EnrichedAlloy;
	public static ItemEnergized EnergyTablet;
	public static Item SpeedUpgrade;
	public static Item EnergyUpgrade;
	public static Item UltimateUpgrade;
	public static ItemAtomicDisassembler AtomicDisassembler;
	public static Item AtomicCore;
	public static ItemStorageTank StorageTank;
	public static Item ControlCircuit;
	public static Item EnrichedIron;
	public static Item CompressedCarbon;
	
	//Extra Blocks
	public static Block BasicBlock;
	public static Block MachineBlock;
	public static Block OreBlock;
	public static Block ObsidianTNT;
	public static Block EnergyCube;
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
	
	public static double TO_IC2 = 1;
	public static double TO_BC = .1;
	public static double FROM_IC2 = 1;
	public static double FROM_BC = 10;
	
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
			"***", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Ingot, 9, 2), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(BasicBlock, 1, 5), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotSteel"
		}));
		
		//Extra
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
			"ARA", "CIC", "ARA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('R'), Item.redstone, Character.valueOf('I'), "blockSteel", Character.valueOf('C'), ControlCircuit
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 1), new Object[] {
			"RCR", "GIG", "RCR", Character.valueOf('R'), Item.redstone, Character.valueOf('C'), ControlCircuit, Character.valueOf('G'), Block.glass, Character.valueOf('I'), "blockSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 2), new Object[] {
			"SCS", "RIR", "SCS", Character.valueOf('S'), Block.cobblestone, Character.valueOf('C'), ControlCircuit, Character.valueOf('R'), Item.redstone, Character.valueOf('I'), "blockSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 3), new Object[] {
			"RLR", "CIC", "RLR", Character.valueOf('R'), Item.redstone, Character.valueOf('L'), Item.bucketLava, Character.valueOf('C'), ControlCircuit, Character.valueOf('I'), "blockSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SpeedUpgrade), new Object[] {
			"PAP", "AEA", "PAP", Character.valueOf('P'), "dustPlatinum", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), Item.emerald
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnergyUpgrade), new Object[] {
			"RAR", "AEA", "RAR", Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(UltimateUpgrade), new Object[] {
			"ERA", "RDR", "ARS", Character.valueOf('E'), EnergyUpgrade, Character.valueOf('R'), Item.redstone, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), Item.diamond, Character.valueOf('S'), SpeedUpgrade
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(AtomicCore), new Object[] {
			"AOA", "PDP", "AOA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('O'), "dustObsidian", Character.valueOf('P'), "dustPlatinum", Character.valueOf('D'), Item.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(AtomicDisassembler.getUnchargedItem(), new Object[] {
			"AEA", "ACA", " O ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedAlloy), new Object[] {
			" R ", "RIR", " R ", Character.valueOf('R'), Item.redstone, Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(StorageTank.getEmptyItem(), new Object[] {
			"III", "IDI", "III", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(GasTank, new Object[] {
			"PPP", "PDP", "PPP", Character.valueOf('P'), "ingotPlatinum", Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(MekanismUtils.getEnergyCubeWithTier(EnergyCubeTier.BASIC), new Object[] {
			"ELE", "TIT", "ELE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 5)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(MekanismUtils.getEnergyCubeWithTier(EnergyCubeTier.ADVANCED), new Object[] {
			"EGE", "TBT", "EGE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('G'), Item.ingotGold, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCubeWithTier(EnergyCubeTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(MekanismUtils.getEnergyCubeWithTier(EnergyCubeTier.ULTIMATE), new Object[] {
			"EDE", "TAT", "EDE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('D'), Item.diamond, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCubeWithTier(EnergyCubeTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ControlCircuit), new Object[] {
			" P ", "PEP", " P ", Character.valueOf('P'), "ingotPlatinum", Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedIron, 6), new Object[] {
			"A", "I", "A", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedIron, 4), new Object[] {
			"C", "I", "C", Character.valueOf('C'), "dustCopper", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(EnrichedIron, 4), new Object[] {
			"T", "I", "T", Character.valueOf('T'), "dustTin", Character.valueOf('I'), Item.ingotIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 5), new Object[] {
			"CAC", "GIG", "CAC", Character.valueOf('C'), ControlCircuit, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('I'), "blockSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 6), new Object[] {
			"CAC", "DFD", "CAC", Character.valueOf('C'), ControlCircuit, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustDiamond", Character.valueOf('F'), new ItemStack(MachineBlock, 1, 5)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 7), new Object[] {
			"CAC", "cFc", "CAC", Character.valueOf('C'), ControlCircuit, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('c'), AtomicCore, Character.valueOf('F'), new ItemStack(MachineBlock, 1, 6)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 8), new Object[] {
			"IFI", "CEC", "IFI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('F'), Block.stoneOvenIdle, Character.valueOf('C'), ControlCircuit, Character.valueOf('E'), EnrichedAlloy
		}));
		
		if(extrasEnabled)
		{
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(MachineBlock, 1, 4), new Object[] {
				"SGS", "GDG", "SGS", Character.valueOf('S'), EnrichedAlloy, Character.valueOf('G'), Block.glass, Character.valueOf('D'), Block.blockDiamond
			}));
		}
	
		//Furnace Recipes
		FurnaceRecipes.smelting().addSmelting(oreBlockID, 0, new ItemStack(Ingot, 1, 1), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.shiftedIndex, 2, new ItemStack(Ingot, 1, 1), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.shiftedIndex, 0, new ItemStack(Item.ingotIron), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.shiftedIndex, 1, new ItemStack(Item.ingotGold), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.shiftedIndex, 5, new ItemStack(Ingot, 1, 4), 1.0F);
		GameRegistry.addSmelting(Item.coal.shiftedIndex, new ItemStack(CompressedCarbon), 1.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Dust, 1, 4), new ItemStack(Item.diamond));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreRedstone), new ItemStack(Item.redstone, 2));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.obsidian), new ItemStack(Dust, 1, 3));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreIron), new ItemStack(Dust, 2, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreGold), new ItemStack(Dust, 2, 1));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.coal, 4), new ItemStack(CompressedCarbon, 8));
		
		//Platinum Compressor Recipes
		RecipeHandler.addPlatinumCompressorRecipe(new ItemStack(Item.lightStoneDust), new ItemStack(Ingot, 1, 3));
		
		//Combiner Recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.redstone, 10), new ItemStack(Block.oreRedstone));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.diamond, 2), new ItemStack(Block.oreDiamond));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.dyePowder, 8, 4), new ItemStack(Block.oreLapis));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Item.diamond), new ItemStack(Dust, 1, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotIron), new ItemStack(Dust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotGold), new ItemStack(Dust, 1, 1));
        
        if(extrasEnabled)
        {
	        //Theoretical Elementizer Recipes
	        RecipeHandler.addTheoreticalElementizerRecipe(new ItemStack(EnrichedAlloy), new ItemStack(TileEntityTheoreticalElementizer.getRandomMagicItem()));
        }
        
        //Metallurgic Infuser Recipes
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfusionType.COAL, 10, new ItemStack(EnrichedIron)), new ItemStack(Dust, 1, 5));
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
			LanguageRegistry.addName(EnrichedAlloy, "Enriched Alloy");
		}
		
		LanguageRegistry.addName(EnergyTablet, "Energy Tablet");
		LanguageRegistry.addName(SpeedUpgrade, "Speed Upgrade");
		LanguageRegistry.addName(EnergyUpgrade, "Energy Upgrade");
		LanguageRegistry.addName(UltimateUpgrade, "Ultimate Upgrade");
		LanguageRegistry.addName(AtomicDisassembler, "Atomic Disassembler");
		LanguageRegistry.addName(AtomicCore, "Atomic Core");
		LanguageRegistry.addName(ElectricBow, "Electric Bow");
		LanguageRegistry.addName(StorageTank, "Hydrogen Tank");
		LanguageRegistry.addName(NullRender, "Null Render");
		LanguageRegistry.addName(GasTank, "Gas Tank");
		LanguageRegistry.addName(StorageTank, "Storage Tank");
		LanguageRegistry.addName(ControlCircuit, "Control Circuit");
		LanguageRegistry.addName(EnrichedIron, "Enriched Iron");
		LanguageRegistry.addName(CompressedCarbon, "Compressed Carbon");
		
		//Localization for MultiBlock
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.PlatinumBlock.name", "Platinum Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.BronzeBlock.name", "Bronze Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedObsidian.name", "Refined Obsidian");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.CoalBlock.name", "Coal Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.RefinedGlowstone.name", "Refined Glowstone");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.SteelBlock.name", "Steel Block");
		LanguageRegistry.instance().addStringLocalization("tile.BasicBlock.ControlPanel.name", "Control Panel");
		
		//Localization for MachineBlock
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.EnrichmentChamber.name", "Enrichment Chamber");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.PlatinumCompressor.name", "Platinum Compressor");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Combiner.name", "Combiner");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.Crusher.name", "Crusher");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.TheoreticalElementizer.name", "Theoretical Elementizer");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.BasicSmeltingFactory.name", "Basic Smelting Factory");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.AdvancedSmeltingFactory.name", "Advanced Smelting Factory");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.UltimateSmeltingFactory.name", "Ultimate Smelting Factory");
		LanguageRegistry.instance().addStringLocalization("tile.MachineBlock.MetallurgicInfuser.name", "Metallurgic Infuser");
		
		//Localization for OreBlock
		LanguageRegistry.instance().addStringLocalization("tile.OreBlock.PlatinumOre.name", "Platinum Ore");
		
		//Localization for EnergyCube
		LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Basic.name", "Basic Energy Cube");
		LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Advanced.name", "Advanced Energy Cube");
		LanguageRegistry.instance().addStringLocalization("tile.EnergyCube.Ultimate.name", "Ultimate Energy Cube");
		
		//Localization for Dust
		LanguageRegistry.instance().addStringLocalization("item.ironDust.name", "Iron Dust");
		LanguageRegistry.instance().addStringLocalization("item.goldDust.name", "Gold Dust");
		LanguageRegistry.instance().addStringLocalization("item.platinumDust.name", "Platinum Dust");
		LanguageRegistry.instance().addStringLocalization("item.obsidianDust.name", "Obsidian Dust");
		LanguageRegistry.instance().addStringLocalization("item.diamondDust.name", "Diamond Dust");
		LanguageRegistry.instance().addStringLocalization("item.steelDust.name", "Steel Dust");
		LanguageRegistry.instance().addStringLocalization("item.copperDust.name", "Copper Dust");
		LanguageRegistry.instance().addStringLocalization("item.tinDust.name", "Tin Dust");
		
		//Localization for Ingot
		LanguageRegistry.instance().addStringLocalization("item.obsidianIngot.name", "Obsidian Ingot");
		LanguageRegistry.instance().addStringLocalization("item.platinumIngot.name", "Platinum Ingot");
		LanguageRegistry.instance().addStringLocalization("item.bronzeIngot.name", "Bronze Ingot");
		LanguageRegistry.instance().addStringLocalization("item.glowstoneIngot.name", "Glowstone Ingot");
		LanguageRegistry.instance().addStringLocalization("item.steelIngot.name", "Steel Ingot");
		
		//Localization for Mekanism creative tab
		LanguageRegistry.instance().addStringLocalization("itemGroup.tabMekanism", "Mekanism");
	}
	
	/**
	 * Adds all item textures from the sprite sheet.
	 */
	public void addTextures()
	{
		if(extrasEnabled == true)
		{
			Stopwatch.setIconIndex(224);
			WeatherOrb.setIconIndex(226);
			EnrichedAlloy.setIconIndex(227);
		}
		
		EnergyTablet.setIconIndex(228);
		SpeedUpgrade.setIconIndex(232);
		EnergyUpgrade.setIconIndex(231);
		UltimateUpgrade.setIconIndex(233);	
		AtomicDisassembler.setIconIndex(253);
		AtomicCore.setIconIndex(254);
		ElectricBow.setIconIndex(252);
		StorageTank.setIconIndex(255);
		ControlCircuit.setIconIndex(223);
		EnrichedIron.setIconIndex(222);
		CompressedCarbon.setIconIndex(221);
	}
	
	/**
	 * Adds and registers all items.
	 */
	public void addItems()
	{
		configuration.load();
		ElectricBow = (ItemElectricBow) new ItemElectricBow(configuration.getItem("ElectricBow", 11200).getInt()).setItemName("ElectricBow");
		if(extrasEnabled == true)
		{
			Stopwatch = new ItemStopwatch(configuration.getItem("Stopwatch", 11202).getInt()).setItemName("Stopwatch");
			WeatherOrb = new ItemWeatherOrb(configuration.getItem("WeatherOrb", 11203).getInt()).setItemName("WeatherOrb");
		}
		Dust = new ItemDust(configuration.getItem("Dust", 11204).getInt()-256);
		Ingot = new ItemIngot(configuration.getItem("Ingot", 11205).getInt()-256);
		EnergyTablet = (ItemEnergized) new ItemEnergized(configuration.getItem("EnergyTablet", 11206).getInt(), 250000, 800, 2500).setItemName("EnergyTablet");
		SpeedUpgrade = new ItemMachineUpgrade(configuration.getItem("SpeedUpgrade", 11207).getInt(), 0, 150).setItemName("SpeedUpgrade");
		EnergyUpgrade = new ItemMachineUpgrade(configuration.getItem("EnergyUpgrade", 11208).getInt(), 1000, 0).setItemName("EnergyUpgrade");
		UltimateUpgrade = new ItemMachineUpgrade(configuration.getItem("UltimateUpgrade", 11209).getInt(), 2500, 180).setItemName("UltimateUpgrade");
		AtomicDisassembler = (ItemAtomicDisassembler) new ItemAtomicDisassembler(configuration.getItem("AtomicDisassembler", 11210).getInt()).setItemName("AtomicDisassembler");
		AtomicCore = new ItemMekanism(configuration.getItem("AtomicCore", 11211).getInt()).setItemName("AtomicCore");
		EnrichedAlloy = new ItemMekanism(configuration.getItem("EnrichedAlloy", 11212).getInt()).setItemName("EnrichedAlloy");
		StorageTank = (ItemStorageTank) new ItemStorageTank(configuration.getItem("StorageTank", 11213).getInt(), 1600, 16, 16).setItemName("StorageTank");
		ControlCircuit = new ItemMekanism(configuration.getItem("ControlCircuit", 11214).getInt()).setItemName("ControlCircuit");
		EnrichedIron = new ItemMekanism(configuration.getItem("EnrichedIron", 11215).getInt()).setItemName("EnrichedIron");
		CompressedCarbon = new ItemMekanism(configuration.getItem("CompressedCarbon", 11216).getInt()).setItemName("CompressedCarbon");
		configuration.save();
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
		EnergyCube = new BlockEnergyCube(energyCubeID).setBlockName("EnergyCube");
		ObsidianTNT = new BlockObsidianTNT(obsidianTNTID).setBlockName("ObsidianTNT").setCreativeTab(tabMekanism);
		NullRender = (BlockMulti) new BlockMulti(nullRenderID).setBlockName("NullRender");
		GasTank = new BlockGasTank(gasTankID).setBlockName("GasTank");
		
		//Registrations
		GameRegistry.registerBlock(ObsidianTNT, "ObsidianTNT");
		GameRegistry.registerBlock(NullRender, "NullRender");
		GameRegistry.registerBlock(GasTank, "GasTank");
		
		//Add block items into itemsList for blocks with common IDs.
		Item.itemsList[basicBlockID] = new ItemBlockBasic(basicBlockID - 256, BasicBlock).setItemName("BasicBlock");
		Item.itemsList[machineBlockID] = new ItemBlockMachine(machineBlockID - 256, MachineBlock).setItemName("MachineBlock");
		Item.itemsList[oreBlockID] = new ItemBlockOre(oreBlockID - 256, OreBlock).setItemName("OreBlock");
		Item.itemsList[energyCubeID] = new ItemBlockEnergyCube(energyCubeID - 256, EnergyCube).setItemName("EnergyCube");
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
		OreDictionary.registerOre("dustDiamond", new ItemStack(Dust, 1, 4));
		OreDictionary.registerOre("dustSteel", new ItemStack(Dust, 1, 5));
		OreDictionary.registerOre("dustCopper", new ItemStack(Dust, 1, 6));
		OreDictionary.registerOre("dustTin", new ItemStack(Dust, 1, 7));
		
		OreDictionary.registerOre("ingotObsidian", new ItemStack(Ingot, 1, 0));
		OreDictionary.registerOre("ingotPlatinum", new ItemStack(Ingot, 1, 1));
		OreDictionary.registerOre("ingotBronze", new ItemStack(Ingot, 1, 2));
		OreDictionary.registerOre("ingotGlowstone", new ItemStack(Ingot, 1, 3));
		OreDictionary.registerOre("ingotSteel", new ItemStack(Ingot, 1, 4));
		
		OreDictionary.registerOre("blockPlatinum", new ItemStack(BasicBlock, 1, 0));
		OreDictionary.registerOre("blockBronze", new ItemStack(BasicBlock, 1, 1));
		OreDictionary.registerOre("blockRefinedObsidian", new ItemStack(BasicBlock, 1, 2));
		OreDictionary.registerOre("blockCoal", new ItemStack(BasicBlock, 1, 3));
		OreDictionary.registerOre("blockRefinedGlowstone", new ItemStack(BasicBlock, 1, 4));
		OreDictionary.registerOre("blockSteel", new ItemStack(BasicBlock, 1, 5));
		
		OreDictionary.registerOre("orePlatinum", new ItemStack(OreBlock, 1, 0));
		
		if(hooks.IC2Loaded)
		{
			if(!hooks.RailcraftLoaded)
			{
				Ic2Recipes.addMaceratorRecipe(new ItemStack(Block.obsidian), new ItemStack(Dust, 1, 3));
			}
			
			RecipeHandler.addCrusherRecipe(new ItemStack(Item.coal), hooks.IC2CoalDust);
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 2, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 2, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("orePlatinum"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(ore, new ItemStack(Dust, 2, 2));
		}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("oreLead"))
			{
				RecipeHandler.addEnrichmentChamberRecipe(ore, OreDictionary.getOres("dustLead").get(0));
			}
			
			for(ItemStack ore : OreDictionary.getOres("ingotLead"))
			{
				RecipeHandler.addCrusherRecipe(ore, OreDictionary.getOres("dustLead").get(0));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("oreSilver"))
			{
				RecipeHandler.addEnrichmentChamberRecipe(ore, OreDictionary.getOres("dustSilver").get(0));
			}
			
			for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
			{
				RecipeHandler.addCrusherRecipe(ore, OreDictionary.getOres("dustSilver").get(0));
			}
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("ingotObsidian"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Dust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotPlatinum"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Dust, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRedstone"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Item.redstone));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotGlowstone"))
		{
			RecipeHandler.addCrusherRecipe(ore, new ItemStack(Item.lightStoneDust));
		}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
			{
				RecipeHandler.addCrusherRecipe(ore, OreDictionary.getOres("dustCopper").get(0));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotTin"))
			{
				RecipeHandler.addCrusherRecipe(ore, OreDictionary.getOres("dustTin").get(0));
			}
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("dustIron"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 2), new ItemStack(Block.oreIron));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 2), new ItemStack(Block.oreGold));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustObsidian"))
		{
			RecipeHandler.addCombinerRecipe(ore, new ItemStack(Block.obsidian));
			RecipeHandler.addPlatinumCompressorRecipe(ore, new ItemStack(Ingot, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustPlatinum"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 2), new ItemStack(OreBlock, 1, 0));
		}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustCopper"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 2), OreDictionary.getOres("oreCopper").get(0));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
			{
				RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfusionType.TIN, 10, ore), new ItemStack(Ingot, 1, 2));
			}
		} catch(Exception e) {}
			
		try {
			for(ItemStack ore : OreDictionary.getOres("dustTin"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 2), OreDictionary.getOres("oreTin").get(0));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustLead"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 2), OreDictionary.getOres("oreLead").get(0));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustSilver"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.getStackWithSize(ore, 2), OreDictionary.getOres("oreSilver").get(0));
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
		GameRegistry.registerTileEntity(TileEntityPlatinumCompressor.class, "PlatinumCompressor");
		GameRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner");
		GameRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher");
		GameRegistry.registerTileEntity(TileEntityEnergyCube.class, "EnergyCube");
		GameRegistry.registerTileEntity(TileEntityMulti.class, "Multi");
		GameRegistry.registerTileEntity(TileEntityControlPanel.class, "ControlPanel");
		GameRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank");
		GameRegistry.registerTileEntity(TileEntitySmeltingFactory.class, "SmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityAdvancedSmeltingFactory.class, "AdvancedSmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityUltimateSmeltingFactory.class, "UltimateSmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityMetallurgicInfuser.class, "MetallurgicInfuser");
		
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
	
	@ServerStopping
	public void serverStopping(FMLServerStoppingEvent event)
	{
		proxy.unloadSoundHandler();
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
		
		//Load proxy
		proxy.registerRenderInformation();
		proxy.loadConfiguration();
		proxy.loadUtilities();
		proxy.loadTickHandler();
		
		MinecraftForge.EVENT_BUS.register(this);
		
		//Attempt to load server commands
		try {
			registerServerCommands();
		} catch(NoSuchMethodError e) {}

		//Load this module
		addItems();
		addBlocks();
		addNames();
		addTextures();
		addRecipes();
		addEntities();
		
		System.out.println("[Mekanism] Loading complete.");
		
		//Success message
		logger.info("[Mekanism] Mod loaded.");
	}
}