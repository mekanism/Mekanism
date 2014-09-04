package mekanism.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.api.MekanismConfig.general;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasNetwork.GasTransferEvent;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.OreGas;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.PressurizedProducts;
import mekanism.common.recipe.inputs.PressurizedReactants;
import mekanism.api.transmitters.DynamicNetwork.ClientTickUpdate;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.ClientTickHandler;
import mekanism.common.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.FluidNetwork.FluidTransferEvent;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IModule;
import mekanism.common.content.boiler.BoilerCache;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.matrix.MatrixCache;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.turbine.SynchronizedTurbineData;
import mekanism.common.content.turbine.TurbineCache;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.OreDictManager;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multipart.MultipartMekanism;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import mekanism.common.recipe.BinRecipe;
import mekanism.common.recipe.MekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEntangledBlock;
import mekanism.common.tile.TileEntitySalinationBlock;
import mekanism.common.tile.TileEntitySalinationValve;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.voice.VoiceServerManager;
import mekanism.common.world.GenHandler;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import codechicken.multipart.handler.MultipartProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rebelkeithy.mods.metallurgy.api.IOreInfo;
import rebelkeithy.mods.metallurgy.api.MetallurgyAPI;

/**
 * Mekanism - a Minecraft mod
 * @author AidanBrady
 *
 */
@Mod(modid = "Mekanism", name = "Mekanism", version = "8.0.0", guiFactory = "mekanism.client.gui.ConfigGuiFactory",
		dependencies = "after:ForgeMultipart;after:BuildCraftAPI|power;after:BuildCraftAPI|tools;after:BuildCraftAPI|transport;after:IC2API;after:CoFHAPI|energy;after:ComputerCraft;after:Galacticraft API;" +
				"after:MineFactoryReloaded;after:MetallurgyCore;after:EnderIO;after:ExtraUtilities;after:Railcraft;after:Forestry;after:RedstoneArsenal")
public class Mekanism
{
	/** Mekanism Packet Pipeline */
	public static PacketHandler packetHandler = new PacketHandler();

	/** Mekanism logger instance */
	public static Logger logger = LogManager.getLogger("Mekanism");
	
	/** Mekanism proxy instance */
	@SidedProxy(clientSide = "mekanism.client.ClientProxy", serverSide = "mekanism.common.CommonProxy")
	public static CommonProxy proxy;
	
	/** Mekanism debug mode */
	public static boolean debug = false;
	
    /** Mekanism mod instance */
	@Instance("Mekanism")
    public static Mekanism instance;
    
    /** Mekanism hooks instance */
    public static MekanismHooks hooks;
    
    /** Mekanism configuration instance */
    public static Configuration configuration;
    
	/** Mekanism version number */
	public static Version versionNumber = new Version(8, 0, 0);
	
	/** Map of Teleporters */
	public static Map<Teleporter.Code, ArrayList<Coord4D>> teleporters = new HashMap<Teleporter.Code, ArrayList<Coord4D>>();
	
	/** MultiblockManagers for various structrures */
	public static MultiblockManager<SynchronizedTankData> tankManager = new MultiblockManager<SynchronizedTankData>("dynamicTank", TankCache.class);
	public static MultiblockManager<SynchronizedMatrixData> matrixManager = new MultiblockManager<SynchronizedMatrixData>("energizedInductionMatrix", MatrixCache.class);
	public static MultiblockManager<SynchronizedTurbineData> turbineManager = new MultiblockManager<SynchronizedTurbineData>("industrialTurbine", TurbineCache.class);
	public static MultiblockManager<SynchronizedBoilerData> boilerManager = new MultiblockManager<SynchronizedBoilerData>("thermoelectricBoiler", BoilerCache.class);
	
	/** Mekanism creative tab */
	public static CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
	
	/** List of Mekanism modules loaded */
	public static List<IModule> modulesLoaded = new ArrayList<IModule>();
	
	/** The latest version number which is received from the Mekanism server */
	public static String latestVersionNumber;
	
	/** The recent news which is received from the Mekanism server */
	public static String recentNews;
	
	/** The VoiceServer manager for walkie talkies */
	public static VoiceServerManager voiceManager;
	
	/** A list of the usernames of players who have donated to Mekanism. */
	public static List<String> donators = new ArrayList<String>();
	
	/** The server's world tick handler. */
	public static CommonWorldTickHandler worldTickHandler = new CommonWorldTickHandler();
	
	/** The Mekanism world generation handler. */
	public static GenHandler genHandler = new GenHandler();
	
	/** The version of ore generation in this version of Mekanism. Increment this every time the default ore generation changes. */
	public static int baseWorldGenVersion = 0;
	
	public static KeySync keyMap = new KeySync();
	
	public static Set<String> jetpackOn = new HashSet<String>();
	public static Set<String> gasmaskOn = new HashSet<String>();
	public static Set<String> flamethrowerActive = new HashSet<String>();
	
	public static Set<Coord4D> ic2Registered = new HashSet<Coord4D>();
	
	public static Set<Coord4D> activeVibrators = new HashSet<Coord4D>();

	/**
	 * Adds all in-game crafting, smelting and machine recipes.
	 */
	public void addRecipes()
	{
		//Storage Recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 3), new Object[] {
			"***", "***", "***", Character.valueOf('*'), new ItemStack(Items.coal, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Items.coal, 9, 1), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 2), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 0), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 4), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 3), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 0), new Object[] {
			"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 1), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 2), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 5), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 4), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 5)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 12), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotCopper"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 5), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 12)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 13), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotTin"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 6), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 13)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.SaltBlock), new Object[] {
			"**", "**", Character.valueOf('*'), MekanismItems.Salt
		}));
		
		//Base Recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.ObsidianTNT, 1), new Object[] {
			"***", "XXX", "***", Character.valueOf('*'), Blocks.obsidian, Character.valueOf('X'), Blocks.tnt
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.ElectricBow.getUnchargedItem(), new Object[] {
			" AB", "E B", " AB", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('B'), Items.string, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.EnergyTablet.getUnchargedItem(), new Object[] {
			"RCR", "ECE", "RCR", Character.valueOf('C'), "ingotGold", Character.valueOf('R'), Items.redstone, Character.valueOf('E'), MekanismItems.EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 0), new Object[] {
			"RCR", "iIi", "RCR", Character.valueOf('i'), "ingotIron", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), "alloyBasic", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 1), new Object[] {
			"ECE", "BIB", "ECE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('C'), "circuitAdvanced", Character.valueOf('B'), Items.bucket, Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 2), new Object[] {
			"RCR", "SIS", "RCR", Character.valueOf('S'), Blocks.cobblestone, Character.valueOf('C'), "circuitElite", Character.valueOf('R'), "alloyElite", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 3), new Object[] {
			"RCR", "LIL", "RCR", Character.valueOf('R'), Items.redstone, Character.valueOf('L'), Items.lava_bucket, Character.valueOf('C'), "circuitBasic", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.SpeedUpgrade), new Object[] {
			" G ", "APA", " G ", Character.valueOf('P'), "dustOsmium", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('G'), "blockGlass"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.EnergyUpgrade), new Object[] {
			" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.AtomicDisassembler.getUnchargedItem(), new Object[] {
			"AEA", "ACA", " O ", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), MekanismItems.AtomicAlloy, Character.valueOf('O'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEmptyGasTank(), new Object[] {
			"PPP", "PDP", "PPP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 8), new Object[] {
			"IFI", "ROR", "IFI", Character.valueOf('I'), Items.iron_ingot, Character.valueOf('F'), Blocks.furnace, Character.valueOf('R'), Items.redstone, Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.TeleportationCore), new Object[] {
			"LAL", "GDG", "LAL", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4), Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('G'), Items.gold_ingot, Character.valueOf('D'), Items.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PortableTeleporter), new Object[] {
			" E ", "CTC", " E ", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), "circuitBasic", Character.valueOf('T'), MekanismItems.TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 11), new Object[] {
			"COC", "OTO", "COC", Character.valueOf('C'), "circuitBasic", Character.valueOf('O'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('T'), MekanismItems.TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 9), new Object[] {
			"ECE", "ORO", "ECE", Character.valueOf('C'), "circuitAdvanced", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('O'), "ingotOsmium", Character.valueOf('R'), new ItemStack(MekanismBlocks.MachineBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Configurator), new Object[] {
			" L ", "AEA", " S ", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4), Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('S'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 9, 7), new Object[] {
			"OOO", "OGO", "OOO", Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('G'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 8), new Object[] {
			" S ", "SPS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('P'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 10), new Object[] {
			"RCR", "GIG", "RCR", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), "alloyBasic", Character.valueOf('G'), "blockGlass", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 12), new Object[] {
			" B ", "ECE", "OOO", Character.valueOf('B'), Items.bucket, Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 13), new Object[] {
			"SGS", "CcC", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), Blocks.chest, Character.valueOf('c'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 8, 9), new Object[] {
			" I ", "ISI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('S'), Blocks.cobblestone
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 8, 10), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('G'), "blockGlass"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 2, 11), new Object[] {
			" I ", "ICI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 14), new Object[] {
			"PPP", "SES", Character.valueOf('P'), Blocks.stone_pressure_plate, Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.Robit.getUnchargedItem(), new Object[] {
			" S ", "ECE", "OIO", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), MekanismItems.AtomicAlloy, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('I'), new ItemStack(MekanismBlocks.MachineBlock, 1, 13)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.NetworkReader), new Object[] {
			" G ", "AEA", " I ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('I'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.WalkieTalkie), new Object[] {
			"  O", "SCS", " S ", Character.valueOf('O'), "ingotOsmium", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 15), new Object[] {
			"IPI", "ICI", "III", Character.valueOf('I'), "ingotIron", Character.valueOf('P'), Blocks.piston, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 6), new Object[] {
			"SSS", "SCS", "SSS", Character.valueOf('S'), Blocks.cobblestone, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 4), new Object[] {
			"ACA", "SES", "TIT", Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('C'), "circuitBasic", Character.valueOf('S'), new ItemStack(MekanismBlocks.MachineBlock, 1, 15), Character.valueOf('E'), MekanismItems.Robit.getUnchargedItem(),
			Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('T'), MekanismItems.TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 0), new Object[] {
			"GCG", "tET", "GIG", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), "circuitBasic", Character.valueOf('t'), MekanismUtils.getEmptyGasTank(), Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(),
			Character.valueOf('T'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.Jetpack.getEmptyItem(), new Object[] {
			"SCS", "TGT", " T ", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic", Character.valueOf('T'), "ingotTin", Character.valueOf('G'), MekanismUtils.getEmptyGasTank()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Dictionary), new Object[] {
			"C", "B", Character.valueOf('C'), "circuitBasic", Character.valueOf('B'), Items.book
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.GasMask), new Object[] {
			" S ", "GCG", "S S", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.ScubaTank.getEmptyItem(), new Object[] {
			" C ", "ATA", "SSS", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('S'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 1), new Object[] {
			"ACA", "ERG", "ACA", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9), Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('E'), new ItemStack(MekanismBlocks.MachineBlock, 1, 13), Character.valueOf('A'), MekanismItems.EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 2), new Object[] {
			"ACA", "GRG", "ACA", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9), Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('A'), MekanismItems.EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 3), new Object[] {
			"RCR", "GPG", "RCR", Character.valueOf('C'), "circuitElite", Character.valueOf('R'), "alloyElite", Character.valueOf('G'), "ingotGold", Character.valueOf('P'), new ItemStack(MekanismBlocks.MachineBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 4), new Object[] {
			"IRI", "ECE", "IRI", Character.valueOf('I'), "ingotIron", Character.valueOf('R'), Items.redstone, Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), MekanismItems.ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.ElectrolyticCore), new Object[] {
			"EPE", "IEG", "EPE", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('P'), "dustOsmium", Character.valueOf('I'), "dustIron", Character.valueOf('G'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.CardboardBox), new Object[] {
			"SS", "SS", Character.valueOf('S'), "pulpWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Items.paper, 6), new Object[] {
			"SSS", Character.valueOf('S'), MekanismItems.Sawdust
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 5), new Object[] {
			"ICI", "ASA", "ICI", Character.valueOf('I'), "ingotIron", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('S'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 14), new Object[] {
			"CGC", "IBI", "CGC", Character.valueOf('C'), "circuitBasic", Character.valueOf('G'), "paneGlass", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('B'), "blockCopper"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 15), new Object[] {
			"ITI", "CBC", "ITI", Character.valueOf('I'), "ingotCopper", Character.valueOf('T'), new ItemStack(MekanismBlocks.BasicBlock, 1, 11), Character.valueOf('C'), "circuitBasic", Character.valueOf('B'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 1, 0), new Object[] {
			"CCC", "CTC", "CCC", Character.valueOf('C'), "ingotCopper", Character.valueOf('T'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 6), new Object[] {
			"CGC", "EAE", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('E'), MekanismItems.EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 7), new Object[] {
			"CWC", "EIE", "CGC", Character.valueOf('W'), Items.bucket, Character.valueOf('C'), "circuitBasic", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 8), new Object[] {
			"CGC", "ASA", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('S'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.FrictionBoots), new Object[] {
			"C C", "A A", "T T", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.ArmoredJetpack.getEmptyItem(), new Object[] {
			"D D", "BSB", " J ", Character.valueOf('D'), "dustDiamond", Character.valueOf('B'), "ingotBronze", Character.valueOf('S'), "blockSteel", Character.valueOf('J'), MekanismItems.Jetpack.getEmptyItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.FilterCard), new Object[] {
			" A ", "ACA", " A ", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.SeismicReader.getUnchargedItem(), new Object[] {
			"SLS", "STS", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 9), new Object[] {
			"TLT", "CIC", "TTT", Character.valueOf('T'), "ingotTin", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4), Character.valueOf('C'), "circuitBasic", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 10), new Object[] {
			"TET", "CIC", "GFG", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), "circuitBasic", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(),
			Character.valueOf('I'), new ItemStack(MekanismBlocks.MachineBlock, 1, 0), Character.valueOf('F'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 11), new Object[] {
			"III", "GCG", "III", Character.valueOf('I'), "ingotIron", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock2, 1, 12), new Object[] {
			"TTT", "CPC", "TTT", Character.valueOf('P'), new ItemStack(MekanismBlocks.MachineBlock, 1, 12), Character.valueOf('T'), "ingotTin", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Blocks.rail, 24), new Object[] {
			"O O", "OSO", "O O", Character.valueOf('O'), "ingotOsmium", Character.valueOf('S'), "stickWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismItems.Flamethrower.getEmptyItem(), new Object[] {
			"TTT", "TGS", "BCB", Character.valueOf('T'), "ingotTin", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('S'), Items.flint_and_steel, Character.valueOf('B'), "ingotBronze", Character.valueOf('C'), "circuitAdvanced"
		}));
		
		//Energy Cube recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), new Object[] {
			"RTR", "iIi", "RTR", Character.valueOf('R'), "alloyBasic", Character.valueOf('i'), "ingotIron", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), new Object[] {
			"ETE", "oBo", "ETE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('o'), "ingotOsmium", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), new Object[] {
			"RTR", "gAg", "RTR", Character.valueOf('R'), "alloyElite", Character.valueOf('g'), "ingotGold", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), new Object[] {
			"ATA", "dEd", "ATA", Character.valueOf('A'), "alloyUltimate", Character.valueOf('d'), "gemDiamond", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE)
		}));
		
		//Circuit recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 1), new Object[] {
			"ECE", Character.valueOf('C'), new ItemStack(MekanismItems.ControlCircuit, 1, 0), Character.valueOf('E'), "alloyAdvanced"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 2), new Object[] {
			"RCR", Character.valueOf('C'), new ItemStack(MekanismItems.ControlCircuit, 1, 1), Character.valueOf('R'), "alloyElite"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 3), new Object[] {
			"ACA", Character.valueOf('C'), new ItemStack(MekanismItems.ControlCircuit, 1, 2), Character.valueOf('A'), "alloyUltimate"
		}));

		//Factory recipes
		for(RecipeType type : RecipeType.values())
		{
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, type), new Object[] {
				"RCR", "iOi", "RCR", Character.valueOf('R'), "alloyBasic", Character.valueOf('C'), "circuitBasic", Character.valueOf('i'), "ingotIron", Character.valueOf('O'), type.getStack()
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.ADVANCED, type), new Object[] {
				"ECE", "oOo", "ECE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('C'), "circuitAdvanced", Character.valueOf('o'), "ingotOsmium", Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.BASIC, type)
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.ELITE, type), new Object[] {
				"RCR", "gOg", "RCR", Character.valueOf('R'), "alloyElite", Character.valueOf('C'), "circuitElite", Character.valueOf('g'), "ingotGold", Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.ADVANCED, type)
			}));
		}
		
		//Add the bin recipe system to the CraftingManager
		CraftingManager.getInstance().getRecipeList().add(new BinRecipe());
		
        //Transmitters
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 0), new Object[] {
			"SRS", Character.valueOf('S'), "ingotSteel", Character.valueOf('R'), Items.redstone
		}));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 1), new Object[] {
            "TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), new ItemStack(MekanismItems.PartTransmitter, 1, 0)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 2), new Object[] {
            "TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), new ItemStack(MekanismItems.PartTransmitter, 1, 1)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 3), new Object[] {
            "TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), new ItemStack(MekanismItems.PartTransmitter, 1, 2)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 4), new Object[] {
            "SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Items.bucket
        }));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 5), new Object[] {
			"TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), new ItemStack(MekanismItems.PartTransmitter, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 6), new Object[] {
			"TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), new ItemStack(MekanismItems.PartTransmitter, 1, 5)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 7), new Object[] {
			"TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), new ItemStack(MekanismItems.PartTransmitter, 1, 6)
		}));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 8), new Object[] {
            "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass"
        }));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 9), new Object[] {
			"SCS", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 2, 10), new Object[] {
			"SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Blocks.iron_bars
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 2, 11), new Object[] {
			"RRR", "SBS", "RRR", Character.valueOf('R'), Items.redstone, Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Blocks.iron_bars
		}));
		
		//Plastic stuff
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 1), new Object[] {
			"PP", "PP", "PP", Character.valueOf('P'), new ItemStack(MekanismItems.Polyethene, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 2), new Object[] {
			"PPP", "P P", "PPP", Character.valueOf('P'), new ItemStack(MekanismItems.Polyethene, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 3), new Object[] {
			"R", "R", Character.valueOf('R'), new ItemStack(MekanismItems.Polyethene, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, 15), new Object[] {
			"SSS", "S S", "SSS", Character.valueOf('S'), new ItemStack(MekanismItems.Polyethene, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.GlowPanel, 2, 15), new Object[] {
			"PSP", "S S", "GSG", Character.valueOf('P'), "paneGlass", Character.valueOf('S'), new ItemStack(MekanismItems.Polyethene, 1, 2), Character.valueOf('G'), Items.glowstone_dust
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticFence, 3, 15), new Object[] {
			"BSB", "BSB", Character.valueOf('B'), new ItemStack(MekanismBlocks.PlasticBlock, 1, 15), Character.valueOf('S'), new ItemStack(MekanismItems.Polyethene, 1, 3)
		}));

		for(int i = 0; i < EnumColor.DYES.length-1; i++)
		{
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i), new Object[] {
				"SSS", "SDS", "SSS", Character.valueOf('S'), new ItemStack(MekanismItems.Polyethene, 1, 2), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.GlowPanel, 2, i), new Object[] {
				"PSP", "SDS", "GSG", Character.valueOf('P'), "paneGlass", Character.valueOf('S'), new ItemStack(MekanismItems.Polyethene, 1, 2), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName, Character.valueOf('G'), Items.glowstone_dust
			}));
		}

		for(int i = 0; i < EnumColor.DYES.length; i++)
        {
			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(MekanismItems.Balloon, 2, i), new Object[] {
				Items.leather, Items.string, "dye" + EnumColor.DYES[i].dyeName
			}));

			for(int j = 0; j < EnumColor.DYES.length; j++)
			{
				CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(MekanismItems.Balloon, 1, i), new Object[] {
					new ItemStack(MekanismItems.Balloon, 1, j), "dye" + EnumColor.DYES[i].dyeName
				}));

				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(MekanismBlocks.PlasticBlock, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.SlickPlasticBlock, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(MekanismBlocks.GlowPlasticBlock, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismItems.GlowPanel, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(MekanismItems.GlowPanel, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
			}

			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 3, i), new Object[] {
				new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(Items.glowstone_dust)
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i), new Object[] {
				" P ", "POP", " P ", Character.valueOf('P'), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), Character.valueOf('O'), new ItemStack(MekanismItems.Dust, 1, 2)
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MekanismBlocks.RoadPlasticBlock, 3, i), new Object[] {
				"SSS", "PPP", "SSS", Character.valueOf('S'), Blocks.sand, Character.valueOf('P'), new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, i)
			}));
        }
	
		//Furnace Recipes
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismBlocks.OreBlock, 1, 0), new ItemStack(MekanismItems.Ingot, 1, 1), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismBlocks.OreBlock, 1, 1), new ItemStack(MekanismItems.Ingot, 1, 5), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismBlocks.OreBlock, 1, 2), new ItemStack(MekanismItems.Ingot, 1, 6), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, 2), new ItemStack(MekanismItems.Ingot, 1, 1), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, 0), new ItemStack(Items.iron_ingot), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, 1), new ItemStack(Items.gold_ingot), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, 5), new ItemStack(MekanismItems.Ingot, 1, 4), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, 6), new ItemStack(MekanismItems.Ingot, 1, 5), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, 7), new ItemStack(MekanismItems.Ingot, 1, 6), 0.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.redstone_ore), new ItemStack(Items.redstone, 12));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.obsidian), new ItemStack(MekanismItems.DirtyDust, 2, 6));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.coal, 1, 0), new ItemStack(MekanismItems.CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.coal, 1, 1), new ItemStack(MekanismItems.CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.redstone), new ItemStack(MekanismItems.CompressedRedstone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.lapis_ore), new ItemStack(Items.dye, 12, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.coal_ore), new ItemStack(Items.coal, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.diamond_ore), new ItemStack(Items.diamond, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.mossy_cobblestone), new ItemStack(Blocks.cobblestone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.stone), new ItemStack(Blocks.stonebrick, 1, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.sand), new ItemStack(Blocks.gravel));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.gravel), new ItemStack(Blocks.cobblestone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.gunpowder), new ItemStack(Items.flint));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.stonebrick, 1, 2), new ItemStack(Blocks.stonebrick, 1, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.stonebrick, 1, 0), new ItemStack(Blocks.stonebrick, 1, 3));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.stonebrick, 1, 1), new ItemStack(Blocks.stonebrick, 1, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.glowstone), new ItemStack(Items.glowstone_dust, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.clay), new ItemStack(Items.clay_ball, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(MekanismBlocks.SaltBlock), new ItemStack(MekanismItems.Salt, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.diamond), new ItemStack(MekanismItems.CompressedDiamond));
		
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, i));
		}
		
		//Combiner recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.redstone, 16), new ItemStack(Blocks.redstone_ore));
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.dye, 16, 4), new ItemStack(Blocks.lapis_ore));
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.flint), new ItemStack(Blocks.gravel));
		
		//Osmium Compressor Recipes
		RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Items.glowstone_dust), new ItemStack(MekanismItems.Ingot, 1, 3));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.diamond), new ItemStack(MekanismItems.Dust, 1, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.iron_ingot), new ItemStack(MekanismItems.Dust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.gold_ingot), new ItemStack(MekanismItems.Dust, 1, 1));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.gravel), new ItemStack(Blocks.sand));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.stone), new ItemStack(Blocks.cobblestone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.cobblestone), new ItemStack(Blocks.gravel));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.stonebrick, 1, 2), new ItemStack(Blocks.stone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.stonebrick, 1, 0), new ItemStack(Blocks.stonebrick, 1, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.stonebrick, 1, 3), new ItemStack(Blocks.stonebrick, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.flint), new ItemStack(Items.gunpowder));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.sandstone), new ItemStack(Blocks.sand, 2));
        
        for(int i = 0; i < 16; i++)
        {
        	RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.wool, 1, i), new ItemStack(Items.string, 4));
        }
        
		//BioFuel Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.tallgrass), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.reeds), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.wheat_seeds), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.wheat), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.pumpkin_seeds), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.melon_seeds), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.apple), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.bread), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.potato), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.carrot), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.rotten_flesh), new ItemStack(MekanismItems.BioFuel, 2));

		//Purification Chamber Recipes
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Blocks.obsidian), new ItemStack(MekanismItems.Clump, 3, 6));
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Blocks.gravel), new ItemStack(Items.flint));
        
        //Chemical Injection Chamber Recipes
        RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.obsidian), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 6));
        RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.dirt), "water", new ItemStack(Blocks.clay));
        RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Items.gunpowder), "hydrogenChloride", new ItemStack(MekanismItems.Dust, 1, 10));
		
		//Precision Sawmill Recipes
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ladder, 3), new ItemStack(Items.stick, 7));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.chest), new ItemStack(Blocks.planks, 8));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.trapdoor), new ItemStack(Blocks.planks, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.boat), new ItemStack(Blocks.planks, 5));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.bed), new ItemStack(Blocks.planks, 3), new ItemStack(Blocks.wool, 3), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.jukebox), new ItemStack(Blocks.planks, 8), new ItemStack(Items.diamond), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.bookshelf), new ItemStack(Blocks.planks, 6), new ItemStack(Items.book, 3), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.wooden_pressure_plate), new ItemStack(Blocks.planks, 2));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.fence), new ItemStack(Items.stick, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.fence_gate), new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.noteblock), new ItemStack(Blocks.planks, 8), new ItemStack(Items.redstone), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.redstone_torch), new ItemStack(Items.stick), new ItemStack(Items.redstone), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.crafting_table), new ItemStack(Blocks.planks, 4));
		
        //Metallurgic Infuser Recipes
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, new ItemStack(Items.iron_ingot), new ItemStack(MekanismItems.EnrichedIron));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, new ItemStack(MekanismItems.EnrichedIron), new ItemStack(MekanismItems.Dust, 1, 5));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("REDSTONE"), 10, new ItemStack(Items.iron_ingot), new ItemStack(MekanismItems.EnrichedAlloy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("FUNGI"), 10, new ItemStack(Blocks.dirt), new ItemStack(Blocks.mycelium));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.cobblestone), new ItemStack(Blocks.mossy_cobblestone));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.stonebrick, 1, 0), new ItemStack(Blocks.stonebrick, 1, 1));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.sand), new ItemStack(Blocks.dirt));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("DIAMOND"), 10, new ItemStack(MekanismItems.EnrichedAlloy), new ItemStack(MekanismItems.ReinforcedAlloy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("OBSIDIAN"), 10, new ItemStack(MekanismItems.ReinforcedAlloy), new ItemStack(MekanismItems.AtomicAlloy));
        
        //Chemical Infuser Recipes
        RecipeHandler.addChemicalInfuserRecipe(new GasStack(GasRegistry.getGas("oxygen"), 1), new GasStack(GasRegistry.getGas("sulfurDioxideGas"), 2), new GasStack(GasRegistry.getGas("sulfurTrioxideGas"), 2));
		RecipeHandler.addChemicalInfuserRecipe(new GasStack(GasRegistry.getGas("sulfurTrioxideGas"), 1), new GasStack(GasRegistry.getGas("water"), 1), new GasStack(GasRegistry.getGas("sulfuricAcid"), 1));
		RecipeHandler.addChemicalInfuserRecipe(new GasStack(GasRegistry.getGas("hydrogen"), 1), new GasStack(GasRegistry.getGas("chlorine"), 1), new GasStack(GasRegistry.getGas("hydrogenChloride"), 1));

		//Electrolytic Separator Recipes
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("water", 2), new GasStack(GasRegistry.getGas("hydrogen"), 2), new GasStack(GasRegistry.getGas("oxygen"), 1));
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("brine", 10), new GasStack(GasRegistry.getGas("sodium"), 1), new GasStack(GasRegistry.getGas("chlorine"), 1));
		
		//T4 Processing Recipes
		for(Gas gas : GasRegistry.getRegisteredGasses())
		{
			if(gas instanceof OreGas && !((OreGas)gas).isClean())
			{
				OreGas oreGas = (OreGas)gas;
				
				RecipeHandler.addChemicalWasherRecipe(new GasStack(oreGas, 1), new GasStack(oreGas.getCleanGas(), 1));
				RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(oreGas.getCleanGas(), 200), new ItemStack(MekanismItems.Crystal, 1, Resource.getFromName(oreGas.getName()).ordinal()));
			}
		}
		
		//Chemical Dissolution Chamber Recipes
		RecipeHandler.addChemicalDissolutionChamberRecipe(new ItemStack(Blocks.obsidian), new GasStack(GasRegistry.getGas("obsidian"), 1000));

		//Pressurized Reaction Chamber Recipes
		RecipeHandler.addPRCRecipe(
				new ItemStack(MekanismItems.BioFuel, 2), new FluidStack(FluidRegistry.WATER, 10), new GasStack(GasRegistry.getGas("hydrogen"), 100),
				new ItemStack(MekanismItems.Substrate), new GasStack(GasRegistry.getGas("ethene"), 100),
				0,
				100
		);

		RecipeHandler.addPRCRecipe(
				new ItemStack(MekanismItems.Substrate), new FluidStack(FluidRegistry.getFluid("ethene"), 50), new GasStack(GasRegistry.getGas("oxygen"), 10),
				new ItemStack(MekanismItems.Polyethene), new GasStack(GasRegistry.getGas("oxygen"), 5),
				1000,
				60
		);

        //Infuse objects
		InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.BioFuel), new InfuseObject(InfuseRegistry.get("BIO"), 5));
		InfuseRegistry.registerInfuseObject(new ItemStack(Items.coal, 1, 0), new InfuseObject(InfuseRegistry.get("CARBON"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Items.coal, 1, 1), new InfuseObject(InfuseRegistry.get("CARBON"), 20));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedCarbon), new InfuseObject(InfuseRegistry.get("CARBON"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(Items.redstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.redstone_block), new InfuseObject(InfuseRegistry.get("REDSTONE"), 90));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedRedstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.red_mushroom), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.brown_mushroom), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedDiamond), new InfuseObject(InfuseRegistry.get("DIAMOND"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedObsidian), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 100));
        
        //Fuels
        GameRegistry.registerFuelHandler(new IFuelHandler() {
			@Override
			public int getBurnTime(ItemStack fuel)
			{
				if(fuel.isItemEqual(new ItemStack(MekanismBlocks.BasicBlock, 1, 3)))
				{
					return 200*8*9;
				}
				
				return 0;
			}
		});

		//Fuel Gases
		FuelHandler.addGas(GasRegistry.getGas("hydrogen"), 1, general.FROM_H2);
		
		//RecipeSorter registrations
		RecipeSorter.register("mekanism", MekanismRecipe.class, Category.SHAPED, "");
		RecipeSorter.register("bin", BinRecipe.class, Category.SHAPELESS, "");
	}

	/**
	 * Registers specified items with the Ore Dictionary.
	 */
	public void registerOreDict()
	{
		//Add specific items to ore dictionary for recipe usage in other mods.
		OreDictionary.registerOre("universalCable", new ItemStack(MekanismItems.PartTransmitter, 8, 0));
		OreDictionary.registerOre("battery", MekanismItems.EnergyTablet.getUnchargedItem());
		OreDictionary.registerOre("pulpWood", MekanismItems.Sawdust);
		OreDictionary.registerOre("dustWood", MekanismItems.Sawdust);
		OreDictionary.registerOre("blockSalt", MekanismBlocks.SaltBlock);
		
		//Alloys!
		OreDictionary.registerOre("alloyBasic", new ItemStack(Items.redstone));
		OreDictionary.registerOre("alloyAdvanced", new ItemStack(MekanismItems.EnrichedAlloy));
		OreDictionary.registerOre("alloyElite", new ItemStack(MekanismItems.ReinforcedAlloy));
		OreDictionary.registerOre("alloyUltimate", new ItemStack(MekanismItems.AtomicAlloy));
		
		//for RailCraft/IC2.
		OreDictionary.registerOre("dustObsidian", new ItemStack(MekanismItems.DirtyDust, 1, 6));
		
		//GregoriousT?
		OreDictionary.registerOre("itemSalt", MekanismItems.Salt);
		OreDictionary.registerOre("dustSalt", MekanismItems.Salt);
		
		OreDictionary.registerOre("dustIron", new ItemStack(MekanismItems.Dust, 1, 0));
		OreDictionary.registerOre("dustGold", new ItemStack(MekanismItems.Dust, 1, 1));
		OreDictionary.registerOre("dustOsmium", new ItemStack(MekanismItems.Dust, 1, 2));
		OreDictionary.registerOre("dustRefinedObsidian", new ItemStack(MekanismItems.Dust, 1, 3));
		OreDictionary.registerOre("dustDiamond", new ItemStack(MekanismItems.Dust, 1, 4));
		OreDictionary.registerOre("dustSteel", new ItemStack(MekanismItems.Dust, 1, 5));
		OreDictionary.registerOre("dustCopper", new ItemStack(MekanismItems.Dust, 1, 6));
		OreDictionary.registerOre("dustTin", new ItemStack(MekanismItems.Dust, 1, 7));
		OreDictionary.registerOre("dustSilver", new ItemStack(MekanismItems.Dust, 1, 8));
		OreDictionary.registerOre("dustLead", new ItemStack(MekanismItems.Dust, 1, 9));
		OreDictionary.registerOre("dustSulfur", new ItemStack(MekanismItems.Dust, 1, 10));
		
		OreDictionary.registerOre("ingotRefinedObsidian", new ItemStack(MekanismItems.Ingot, 1, 0));
		OreDictionary.registerOre("ingotOsmium", new ItemStack(MekanismItems.Ingot, 1, 1));
		OreDictionary.registerOre("ingotBronze", new ItemStack(MekanismItems.Ingot, 1, 2));
		OreDictionary.registerOre("ingotRefinedGlowstone", new ItemStack(MekanismItems.Ingot, 1, 3));
		OreDictionary.registerOre("ingotSteel", new ItemStack(MekanismItems.Ingot, 1, 4));
		OreDictionary.registerOre("ingotCopper", new ItemStack(MekanismItems.Ingot, 1, 5));
		OreDictionary.registerOre("ingotTin", new ItemStack(MekanismItems.Ingot, 1, 6));
		
		OreDictionary.registerOre("blockOsmium", new ItemStack(MekanismBlocks.BasicBlock, 1, 0));
		OreDictionary.registerOre("blockBronze", new ItemStack(MekanismBlocks.BasicBlock, 1, 1));
		OreDictionary.registerOre("blockRefinedObsidian", new ItemStack(MekanismBlocks.BasicBlock, 1, 2));
		OreDictionary.registerOre("blockCharcoal", new ItemStack(MekanismBlocks.BasicBlock, 1, 3));
		OreDictionary.registerOre("blockRefinedGlowstone", new ItemStack(MekanismBlocks.BasicBlock, 1, 4));
		OreDictionary.registerOre("blockSteel", new ItemStack(MekanismBlocks.BasicBlock, 1, 5));
		OreDictionary.registerOre("blockCopper", new ItemStack(MekanismBlocks.BasicBlock, 1, 12));
		OreDictionary.registerOre("blockTin", new ItemStack(MekanismBlocks.BasicBlock, 1, 13));
		
		for(Resource resource : Resource.values())
		{
			OreDictionary.registerOre("dustDirty" + resource.getName(), new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
			OreDictionary.registerOre("clump" + resource.getName(), new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
			OreDictionary.registerOre("shard" + resource.getName(), new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
			OreDictionary.registerOre("crystal" + resource.getName(), new ItemStack(MekanismItems.Crystal, 1, resource.ordinal()));
		}
		
		OreDictionary.registerOre("oreOsmium", new ItemStack(MekanismBlocks.OreBlock, 1, 0));
		OreDictionary.registerOre("oreCopper", new ItemStack(MekanismBlocks.OreBlock, 1, 1));
		OreDictionary.registerOre("oreTin", new ItemStack(MekanismBlocks.OreBlock, 1, 2));
		
		//MC stuff
		OreDictionary.registerOre("oreCoal", new ItemStack(Blocks.coal_ore));
		OreDictionary.registerOre("ingotIron", new ItemStack(Items.iron_ingot));
		OreDictionary.registerOre("ingotGold", new ItemStack(Items.gold_ingot));
		OreDictionary.registerOre("oreRedstone", new ItemStack(Blocks.redstone_ore));
		
		if(general.controlCircuitOreDict)
		{
			OreDictionary.registerOre("circuitBasic", new ItemStack(MekanismItems.ControlCircuit, 1, 0));
			OreDictionary.registerOre("circuitAdvanced", new ItemStack(MekanismItems.ControlCircuit, 1, 1));
			OreDictionary.registerOre("circuitElite", new ItemStack(MekanismItems.ControlCircuit, 1, 2));
			OreDictionary.registerOre("circuitUltimate", new ItemStack(MekanismItems.ControlCircuit, 1, 3));
		}
		
		OreDictionary.registerOre("itemCompressedCarbon", new ItemStack(MekanismItems.CompressedCarbon));
		OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(MekanismItems.EnrichedAlloy));
		OreDictionary.registerOre("itemBioFuel", new ItemStack(MekanismItems.BioFuel));
	}
	
	/**
	 * Integrates the mod with other mods -- registering items and blocks with the Forge Ore Dictionary
	 * and adding machine recipes with other items' corresponding resources.
	 */
	public void addIntegratedItems()
	{		
		if(hooks.MetallurgyCoreLoaded)
		{
			try {
				String[] setNames = {"base", "precious", "nether", "fantasy", "ender", "utility"};
				
				for(String setName : setNames )
				{
					for(IOreInfo oreInfo : MetallurgyAPI.getMetalSet(setName).getOreList().values())
					{
						switch(oreInfo.getType()) 
						{
							case ALLOY: 
							{
								if(oreInfo.getIngot() != null && oreInfo.getDust() != null)
								{
									RecipeHandler.addCrusherRecipe(MekanismUtils.size(oreInfo.getIngot(), 1), MekanismUtils.size(oreInfo.getDust(), 1));
								}
								
								break;
							}
							case DROP: 
							{
								ItemStack ore = oreInfo.getOre();
								ItemStack drop = oreInfo.getDrop();
								
								if(drop != null && ore != null)
								{ 
									RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size(drop, 12));
								}
								
								break;
							}
							default: 
							{
								ItemStack ore = oreInfo.getOre();
								ItemStack dust = oreInfo.getDust();
								ItemStack ingot = oreInfo.getIngot();
								
								if(ore != null && dust != null)
								{
									RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), MekanismUtils.size(dust, 2));
									RecipeHandler.addCombinerRecipe(MekanismUtils.size(dust, 8), MekanismUtils.size(ore, 1));
								}
								
								if(ingot != null && dust != null)
								{
									RecipeHandler.addCrusherRecipe(MekanismUtils.size(ingot, 1), MekanismUtils.size(dust, 1));
								}
								
								break;
							}
						}
					}
				}
			} catch(Exception e) {}
		}
	}
	
	/**
	 * Adds and registers all entities and tile entities.
	 */
	public void addEntities()
	{
		//Registrations
		EntityRegistry.registerModEntity(EntityObsidianTNT.class, "ObsidianTNT", 0, this, 64, 5, true);
		EntityRegistry.registerModEntity(EntityRobit.class, "Robit", 1, this, 64, 2, true);
		EntityRegistry.registerModEntity(EntityBalloon.class, "Balloon", 2, this, 64, 1, true);
		EntityRegistry.registerModEntity(EntityBabySkeleton.class, "BabySkeleton", 3, this, 64, 5, true);
		EntityRegistry.registerModEntity(EntityFlame.class, "Flame", 4, this, 64, 5, true);
		
		//Tile entities
		GameRegistry.registerTileEntity(TileEntityBoundingBlock.class, "BoundingBlock");
		GameRegistry.registerTileEntity(TileEntityAdvancedBoundingBlock.class, "AdvancedBoundingBlock");
		GameRegistry.registerTileEntity(TileEntityCardboardBox.class, "CardboardBox");
		GameRegistry.registerTileEntity(TileEntitySalinationValve.class, "SalinationValve");
		GameRegistry.registerTileEntity(TileEntitySalinationBlock.class, "SalinationTank");
		GameRegistry.registerTileEntity(TileEntityEntangledBlock.class, "EntangledBlock");

		//Load tile entities that have special renderers.
		proxy.registerSpecialTileEntities();
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		if(general.voiceServerEnabled)
		{
			voiceManager.start();
		}
		
		//Load cached furnace recipes
		TileEntityEnergizedSmelter.furnaceRecipes.clear();
		
		for(Object obj : FurnaceRecipes.smelting().getSmeltingList().entrySet())
		{
			Map.Entry<ItemStack, ItemStack> entry = (Map.Entry<ItemStack, ItemStack>)obj;
			TileEntityEnergizedSmelter.furnaceRecipes.put(entry.getKey(), entry.getValue());
		}
		
		event.registerServerCommand(new CommandMekanism());
	}
	
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		if(general.voiceServerEnabled)
		{
			voiceManager.stop();
		}
		
		//Clear all cache data
		teleporters.clear();
		ic2Registered.clear();
		jetpackOn.clear();
		gasmaskOn.clear();
		activeVibrators.clear();
		worldTickHandler.resetRegenChunks();
		
		//Reset consistent managers
		MultiblockManager.reset();
		TransporterManager.reset();
		PathfinderCache.reset();
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		File config = event.getSuggestedConfigurationFile();
		
		//Set the mod's configuration
		configuration = new Configuration(config);
		
		if(config.getAbsolutePath().contains("voltz"))
		{
			logger.info("Detected Voltz in root directory - hello, fellow user!");
		}
		else if(config.getAbsolutePath().contains("tekkit"))
		{
			logger.info("Detected Tekkit in root directory - hello, fellow user!");
		}
		
		GasRegistry.register(new Gas("hydrogen")).registerFluid();
		GasRegistry.register(new Gas("oxygen")).registerFluid();
		GasRegistry.register(new Gas("water")).registerFluid();
		GasRegistry.register(new Gas("chlorine")).registerFluid();
		GasRegistry.register(new Gas("sulfurDioxideGas")).registerFluid();
		GasRegistry.register(new Gas("sulfurTrioxideGas")).registerFluid();
		GasRegistry.register(new Gas("sulfuricAcid")).registerFluid();
		GasRegistry.register(new Gas("hydrogenChloride")).registerFluid();
		GasRegistry.register(new Gas("liquidOsmium").setVisible(false));
		GasRegistry.register(new Gas("liquidStone").setVisible(false));
		GasRegistry.register(new Gas("ethene").registerFluid());
		GasRegistry.register(new Gas("sodium").registerFluid());
		GasRegistry.register(new Gas("brine").registerFluid());
		GasRegistry.register(new Gas("deuterium")).registerFluid();
		GasRegistry.register(new Gas("tritium")).registerFluid();
		GasRegistry.register(new Gas("fusionFuelDD")).registerFluid();
		GasRegistry.register(new Gas("fusionFuelDT")).registerFluid();
		GasRegistry.register(new Gas("steam")).registerFluid();
		
		for(Resource resource : Resource.values())
		{
			String name = resource.getName();
			
			OreGas clean = (OreGas)GasRegistry.register(new OreGas("clean" + name, "oregas." + name.toLowerCase()).setVisible(false));
			GasRegistry.register(new OreGas(name.toLowerCase(), "oregas." + name.toLowerCase()).setCleanGas(clean).setVisible(false));
		}
		
		Mekanism.proxy.preInit();

		//Register blocks and items
		MekanismItems.register();
		MekanismBlocks.register();

		//Register infuses
        InfuseRegistry.registerInfuseType(new InfuseType("CARBON", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 0, 0).setUnlocalizedName("infuse.carbon"));
        InfuseRegistry.registerInfuseType(new InfuseType("TIN", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 4, 0).setUnlocalizedName("infuse.tin"));
        InfuseRegistry.registerInfuseType(new InfuseType("DIAMOND", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 8, 0).setUnlocalizedName("infuse.diamond"));
        InfuseRegistry.registerInfuseType(new InfuseType("REDSTONE", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 16, 0).setUnlocalizedName("infuse.redstone"));
        InfuseRegistry.registerInfuseType(new InfuseType("FUNGI", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 20, 0).setUnlocalizedName("infuse.fungi"));
		InfuseRegistry.registerInfuseType(new InfuseType("BIO", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 12, 0).setUnlocalizedName("infuse.bio"));
		InfuseRegistry.registerInfuseType(new InfuseType("OBSIDIAN", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 24, 0).setUnlocalizedName("infuse.obsidian"));
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) 
	{
		//Register the mod's world generators
		GameRegistry.registerWorldGenerator(genHandler, 1);
		
		//Register the mod's GUI handler
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CoreGuiHandler());
		
		//Register player tracker
		FMLCommonHandler.instance().bus().register(new CommonPlayerTracker());
		FMLCommonHandler.instance().bus().register(new CommonPlayerTickHandler());
		
		//Initialization notification
		logger.info("Version " + versionNumber + " initializing...");
		
		//Get data from server.
		new ThreadGetData();
		
		//Register to receive subscribed events
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		//Set up VoiceServerManager
		if(general.voiceServerEnabled)
		{
			voiceManager = new VoiceServerManager();
		}
		
		//Register with TransmitterNetworkRegistry
		TransmitterNetworkRegistry.initiate();
		
		//Load configuration
		proxy.loadConfiguration();
		proxy.onConfigSync();
		
		//Add baby skeleton spawner
		if(general.spawnBabySkeletons)
		{
			for(BiomeGenBase biome : WorldChunkManager.allowedBiomes) 
			{
				if(biome.getSpawnableList(EnumCreatureType.monster) != null && biome.getSpawnableList(EnumCreatureType.monster).size() > 0)
				{
					EntityRegistry.addSpawn(EntityBabySkeleton.class, 40, 1, 3, EnumCreatureType.monster, new BiomeGenBase[] {biome});
				}
			}
		}

		//Load this module
		addRecipes();
		addEntities();
		
		registerOreDict();

		new MultipartMekanism();

		//Packet registrations
		packetHandler.initialize();

		//Load proxy
		proxy.registerRenderInformation();
		proxy.loadUtilities();
		
		//Completion notification
		logger.info("Loading complete.");
		
		//Success message
		logger.info("Mod loaded.");
	}	
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{		
		hooks = new MekanismHooks();
		hooks.hook();
		
		MinecraftForge.EVENT_BUS.post(new BoxBlacklistEvent());
		
		addIntegratedItems();
		
		OreDictManager.init();
		
		logger.info("Hooking complete.");
	}
	
	@SubscribeEvent
	public void onEnergyTransferred(EnergyTransferEvent event)
	{
		try {
			packetHandler.sendToReceivers(new TransmitterUpdateMessage(PacketType.ENERGY, Coord4D.get((TileEntity)event.energyNetwork.transmitters.iterator().next()), event.power), event.energyNetwork.getPacketRange());
		} catch(Exception e) {}
	}
	
	@SubscribeEvent
	public void onGasTransferred(GasTransferEvent event)
	{
		try {
			packetHandler.sendToReceivers(new TransmitterUpdateMessage(PacketType.GAS, Coord4D.get((TileEntity)event.gasNetwork.transmitters.iterator().next()), event.transferType, event.didTransfer), event.gasNetwork.getPacketRange());
		} catch(Exception e) {}
	}
	
	@SubscribeEvent
	public void onLiquidTransferred(FluidTransferEvent event)
	{
		try {
			packetHandler.sendToReceivers(new TransmitterUpdateMessage(PacketType.FLUID, Coord4D.get((TileEntity)event.fluidNetwork.transmitters.iterator().next()), event.fluidType, event.didTransfer), event.fluidNetwork.getPacketRange());
		} catch(Exception e) {}
	}
	
	@SubscribeEvent
	public void onNetworkClientRequest(NetworkClientRequest event)
	{
		try {
			packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(event.tileEntity)));
		} catch(Exception e) {}
	}
	
	@SubscribeEvent
	public void onClientTickUpdate(ClientTickUpdate event)
	{
		try {
			if(event.operation == 0)
			{
				ClientTickHandler.tickingSet.remove(event.network);
			}
			else {
				ClientTickHandler.tickingSet.add(event.network);
			}
		} catch(Exception e) {}
	}
	
	@SubscribeEvent
	public void onBlacklistUpdate(BoxBlacklistEvent event)
	{
		MekanismAPI.addBoxBlacklist(MekanismBlocks.CardboardBox, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(MekanismBlocks.BoundingBlock, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.bedrock, 0);
		MekanismAPI.addBoxBlacklist(Blocks.portal, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.end_portal, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.end_portal_frame, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.bed, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.wooden_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.iron_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(MultipartProxy.block(), OreDictionary.WILDCARD_VALUE);
		
		BoxBlacklistParser.load();
	}
	
	@SubscribeEvent
	public synchronized void onChunkLoad(ChunkEvent.Load event)
	{
		if(event.getChunk() != null && !event.world.isRemote)
		{
			Map copy = (Map)((HashMap)event.getChunk().chunkTileEntityMap).clone();
			 
			for(Iterator iter = copy.values().iterator(); iter.hasNext();)
			{
				Object obj = iter.next();
	        	 
				if(obj instanceof TileEntity)
				{
					TileEntity tileEntity = (TileEntity)obj;
	
					if(tileEntity instanceof TileEntityElectricBlock && MekanismUtils.useIC2())
					{
						((TileEntityElectricBlock)tileEntity).register();
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void chunkSave(ChunkDataEvent.Save event) 
	{
		if(!event.world.isRemote)
		{
			NBTTagCompound nbtTags = event.getData();

			nbtTags.setInteger("MekanismWorldGen", baseWorldGenVersion);
			nbtTags.setInteger("MekanismUserWorldGen", general.userWorldGenVersion);
		}
	}
	
	@SubscribeEvent
	public synchronized void onChunkDataLoad(ChunkDataEvent.Load event)
	{
		if(!event.world.isRemote)
		{
			if(general.enableWorldRegeneration)
			{
				NBTTagCompound loadData = event.getData();
				
				if(loadData.getInteger("MekanismWorldGen") == baseWorldGenVersion && loadData.getInteger("MekanismUserWorldGen") == general.userWorldGenVersion)
				{
					return;
				}
	
				ChunkCoordIntPair coordPair = event.getChunk().getChunkCoordIntPair();
				worldTickHandler.addRegenChunk(event.world.provider.dimensionId, coordPair);
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.modID.equals("Mekanism"))
		{
			proxy.loadConfiguration();
			proxy.onConfigSync();
		}
	}
}
