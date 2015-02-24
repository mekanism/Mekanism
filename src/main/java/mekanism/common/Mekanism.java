package mekanism.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.AdvancedInput;
import mekanism.api.ChanceOutput;
import mekanism.api.ChemicalPair;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.api.PressurizedProducts;
import mekanism.api.PressurizedReactants;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasNetwork.GasTransferEvent;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.OreGas;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionInput;
import mekanism.api.transmitters.DynamicNetwork.ClientTickUpdate;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.ClientTickHandler;
import mekanism.common.EnergyDisplay.EnergyType;
import mekanism.common.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.FluidNetwork.FluidTransferEvent;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.Teleporter.Code;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.block.BlockBasic;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockGasTank;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockObsidianTNT;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPlastic;
import mekanism.common.block.BlockPlasticFence;
import mekanism.common.block.BlockSalt;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.OreDictManager;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockOre;
import mekanism.common.item.ItemBlockPlastic;
import mekanism.common.item.ItemClump;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemControlCircuit;
import mekanism.common.item.ItemCrystal;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemDirtyDust;
import mekanism.common.item.ItemDust;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemFilterCard;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemHDPE;
import mekanism.common.item.ItemIngot;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemMachineUpgrade;
import mekanism.common.item.ItemMekanism;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemProxy;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemShard;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multipart.ItemGlowPanel;
import mekanism.common.multipart.ItemPartTransmitter;
import mekanism.common.multipart.MultipartMekanism;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import mekanism.common.recipe.BinRecipe;
import mekanism.common.recipe.MekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tank.DynamicTankCache;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntitySalinationTank;
import mekanism.common.tile.TileEntitySalinationValve;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.voice.VoiceServerManager;
import mekanism.common.world.GenHandler;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fluids.FluidContainerRegistry;
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
@Mod(modid = "Mekanism", name = "Mekanism", version = "7.1.2", guiFactory = "mekanism.client.gui.ConfigGuiFactory",
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
	public static Version versionNumber = new Version(7, 1, 2);
	
	/** Map of Teleporters */
	public static Map<Teleporter.Code, ArrayList<Coord4D>> teleporters = new HashMap<Code, ArrayList<Coord4D>>();
	
	/** A map containing references to all dynamic tank inventory caches. */
	public static Map<Integer, DynamicTankCache> dynamicInventories = new HashMap<Integer, DynamicTankCache>();
	
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
	
	public static final Set<String> jetpackOn = Collections.synchronizedSet(new HashSet<String>());
	public static final Set<String> gasmaskOn = Collections.synchronizedSet(new HashSet<String>());
	
	public static Set<Coord4D> activeVibrators = new HashSet<Coord4D>();

	//Items
	public static ItemElectricBow ElectricBow;
	public static Item EnrichedAlloy;
	public static Item ReinforcedAlloy;
	public static Item AtomicAlloy;
	public static ItemEnergized EnergyTablet;
	public static Item SpeedUpgrade;
	public static Item EnergyUpgrade;
	public static ItemRobit Robit;
	public static ItemAtomicDisassembler AtomicDisassembler;
	public static Item EnrichedIron;
	public static Item CompressedCarbon;
	public static Item CompressedRedstone;
	public static Item CompressedDiamond;
	public static Item CompressedObsidian;
	public static Item PortableTeleporter;
	public static Item TeleportationCore;
	public static Item Configurator;
	public static Item NetworkReader;
	public static Item WalkieTalkie;
	public static Item ItemProxy;
	public static Item PartTransmitter;
	public static Item GlowPanel;
	public static ItemJetpack Jetpack;
	public static ItemScubaTank ScubaTank;
	public static ItemGasMask GasMask;
	public static Item Dictionary;
	public static Item Balloon;
	public static Item ElectrolyticCore;
	public static Item Sawdust;
	public static Item Salt;
	public static Item BrineBucket;
	public static Item FreeRunners;
	public static ItemJetpack ArmoredJetpack;
	public static Item FilterCard;
	public static ItemSeismicReader SeismicReader;
	public static Item Substrate;
	public static Item Polyethene;
	public static Item BioFuel;

	//Blocks
	public static Block BasicBlock;
	public static Block BasicBlock2;
	public static Block MachineBlock;
	public static Block MachineBlock2;
	public static Block OreBlock;
	public static Block ObsidianTNT;
	public static Block EnergyCube;
	public static Block BoundingBlock;
	public static Block GasTank;
	public static Block CardboardBox;
	public static Block BlockHDPE;
	public static Block BlockSlickHDPE;
	public static Block BlockGlowHDPE;
	public static Block BlockReinforcedHDPE;
	public static Block BlockRoadHDPE;
	public static Block BlockHDPEFence;
	public static Block SaltBlock;

	//Multi-ID Items
	public static Item Dust;
	public static Item Ingot;
	public static Item Clump;
	public static Item DirtyDust;
	public static Item Shard;
	public static Item Crystal;
	public static Item ControlCircuit;

	//General Configuration
	public static boolean updateNotifications = true;
	public static boolean controlCircuitOreDict = true;
	public static boolean logPackets = false;
	public static boolean dynamicTankEasterEgg = false;
	public static boolean voiceServerEnabled = true;
	public static boolean cardboardSpawners = true;
	public static boolean machineEffects = true;
	public static boolean enableWorldRegeneration = true;
	public static boolean creativeOverrideElectricChest = true;
	public static int obsidianTNTBlastRadius = 12;
	public static int osmiumPerChunk = 12;
	public static int copperPerChunk = 16;
	public static int tinPerChunk = 14;
	public static int saltPerChunk = 2;
	public static int obsidianTNTDelay = 100;
	public static int UPDATE_DELAY = 10;
	public static int VOICE_PORT = 36123;
	public static int maxUpgradeMultiplier = 10;
	public static int userWorldGenVersion = 0;
	public static double ENERGY_PER_REDSTONE = 10000;
	public static EnergyType activeType = EnergyType.J;

	public static double TO_IC2;
	public static double TO_TE;
	public static double TO_UE = .001;
	public static double FROM_H2;
	public static double FROM_IC2;
	public static double FROM_TE;
	public static double FROM_UE = 1/TO_UE;

	public static boolean blacklistBC;
	public static boolean blacklistIC2;
	public static boolean blacklistRF;

	//Usage Configuration
	public static double enrichmentChamberUsage;
	public static double osmiumCompressorUsage;
	public static double combinerUsage;
	public static double crusherUsage;
	public static double factoryUsage;
	public static double metallurgicInfuserUsage;
	public static double purificationChamberUsage;
	public static double energizedSmelterUsage;
	public static double digitalMinerUsage;
	public static double electricPumpUsage;
	public static double rotaryCondensentratorUsage;
	public static double oxidationChamberUsage;
	public static double chemicalInfuserUsage;
	public static double chemicalInjectionChamberUsage;
	public static double precisionSawmillUsage;
	public static double chemicalDissolutionChamberUsage;
	public static double chemicalWasherUsage;
	public static double chemicalCrystallizerUsage;
	public static double seismicVibratorUsage;
	public static double pressurizedReactionBaseUsage;
	public static double fluidicPlenisherUsage;
	public static double salinationPlantWaterUsage;

	/**
	 * Adds all in-game crafting, smelting and machine recipes.
	 */
	public void addRecipes()
	{
		//Storage Recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 3), new Object[] {
			"***", "***", "***", Character.valueOf('*'), new ItemStack(Items.coal, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Items.coal, 9, 1), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 3)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 2), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Ingot, 9, 0), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 2)	
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 4), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Ingot, 9, 3), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 0), new Object[] {
			"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Ingot, 9, 1), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 1), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotBronze"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Ingot, 9, 2), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 5), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Ingot, 9, 4), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 5)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 12), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotCopper"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Ingot, 9, 5), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 12)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 13), new Object[] {
			"***", "***", "***", Character.valueOf('*'), "ingotTin"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Ingot, 9, 6), new Object[] {
			"*", Character.valueOf('*'), new ItemStack(BasicBlock, 1, 13)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SaltBlock), new Object[] {
			"**", "**", Character.valueOf('*'), Salt
		}));
		
		//Base Recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianTNT, 1), new Object[] {
			"***", "XXX", "***", Character.valueOf('*'), Blocks.obsidian, Character.valueOf('X'), Blocks.tnt
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(ElectricBow.getUnchargedItem(), new Object[] {
			" AB", "E B", " AB", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('B'), Items.string, Character.valueOf('E'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(EnergyTablet.getUnchargedItem(), new Object[] {
			"RCR", "ECE", "RCR", Character.valueOf('C'), "ingotGold", Character.valueOf('R'), "dustRedstone", Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 0), new Object[] {
			"RCR", "iIi", "RCR", Character.valueOf('i'), "ingotIron", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), "alloyBasic", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 1), new Object[] {
			"ECE", "BIB", "ECE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('C'), "circuitAdvanced", Character.valueOf('B'), Items.bucket, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 2), new Object[] {
			"RCR", "SIS", "RCR", Character.valueOf('S'), Blocks.cobblestone, Character.valueOf('C'), "circuitElite", Character.valueOf('R'), "alloyElite", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 3), new Object[] {
			"RCR", "LIL", "RCR", Character.valueOf('R'), "dustRedstone", Character.valueOf('L'), Items.lava_bucket, Character.valueOf('C'), "circuitBasic", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SpeedUpgrade), new Object[] {
			" G ", "APA", " G ", Character.valueOf('P'), "dustOsmium", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "blockGlass"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(EnergyUpgrade), new Object[] {
			" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(AtomicDisassembler.getUnchargedItem(), new Object[] {
			"AEA", "ACA", " O ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicAlloy, Character.valueOf('O'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEmptyGasTank(), new Object[] {
			"PPP", "PDP", "PPP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 8), new Object[] {
			"IFI", "ROR", "IFI", Character.valueOf('I'), "ingotIron", Character.valueOf('F'), Blocks.furnace, Character.valueOf('R'), "dustRedstone", Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(TeleportationCore), new Object[] {
			"LAL", "GDG", "LAL", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4), Character.valueOf('A'), AtomicAlloy, Character.valueOf('G'), "ingotGold", Character.valueOf('D'), Items.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PortableTeleporter), new Object[] {
			" E ", "CTC", " E ", Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), "circuitBasic", Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 11), new Object[] {
			"COC", "OTO", "COC", Character.valueOf('C'), "circuitBasic", Character.valueOf('O'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 9), new Object[] {
			"ECE", "ORO", "ECE", Character.valueOf('C'), "circuitAdvanced", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('O'), "ingotOsmium", Character.valueOf('R'), new ItemStack(MachineBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Configurator), new Object[] {
			" L ", "AEA", " S ", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4), Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('S'), Items.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 9, 7), new Object[] {
			"OOO", "OGO", "OOO", Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('G'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 8), new Object[] {
			" S ", "SPS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('P'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 10), new Object[] {
			"RCR", "GIG", "RCR", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), "alloyBasic", Character.valueOf('G'), "blockGlass", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 12), new Object[] {
			" B ", "ECE", "OOO", Character.valueOf('B'), Items.bucket, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 13), new Object[] {
			"SGS", "CcC", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), Blocks.chest, Character.valueOf('c'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 8, 9), new Object[] {
			" I ", "ISI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('S'), Blocks.cobblestone
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 8, 10), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('G'), "blockGlass"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 2, 11), new Object[] {
			" I ", "ICI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 14), new Object[] {
			"PPP", "SES", Character.valueOf('P'), Blocks.stone_pressure_plate, Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(Robit.getUnchargedItem(), new Object[] {
			" S ", "ECE", "OIO", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicAlloy, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('I'), new ItemStack(MachineBlock, 1, 13)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(NetworkReader), new Object[] {
			" G ", "AEA", " I ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('I'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(WalkieTalkie), new Object[] {
			"  O", "SCS", " S ", Character.valueOf('O'), "ingotOsmium", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 15), new Object[] {
			"IPI", "ICI", "III", Character.valueOf('I'), "ingotIron", Character.valueOf('P'), Blocks.piston, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 6), new Object[] {
			"SSS", "SCS", "SSS", Character.valueOf('S'), Blocks.cobblestone, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 4), new Object[] {
			"ACA", "SES", "TIT", Character.valueOf('A'), AtomicAlloy, Character.valueOf('C'), "circuitBasic", Character.valueOf('S'), new ItemStack(MachineBlock, 1, 15), Character.valueOf('E'), Robit.getUnchargedItem(),
			Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 0), new Object[] {
			"GCG", "tET", "GIG", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), "circuitBasic", Character.valueOf('t'), MekanismUtils.getEmptyGasTank(), Character.valueOf('E'), EnergyTablet.getUnchargedItem(),
			Character.valueOf('T'), new ItemStack(BasicBlock, 1, 9), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(Jetpack.getEmptyItem(), new Object[] {
			"SCS", "TGT", " T ", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic", Character.valueOf('T'), "ingotTin", Character.valueOf('G'), MekanismUtils.getEmptyGasTank()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Dictionary), new Object[] {
			"C", "B", Character.valueOf('C'), "circuitBasic", Character.valueOf('B'), Items.book
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GasMask), new Object[] {
			" S ", "GCG", "S S", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(ScubaTank.getEmptyItem(), new Object[] {
			" C ", "ATA", "SSS", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('S'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 1), new Object[] {
			"ACA", "ERG", "ACA", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), new ItemStack(BasicBlock, 1, 9), Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('E'), new ItemStack(MachineBlock, 1, 13), Character.valueOf('A'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 2), new Object[] {
			"ACA", "GRG", "ACA", Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), new ItemStack(BasicBlock, 1, 9), Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('A'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 3), new Object[] {
			"RCR", "GPG", "RCR", Character.valueOf('C'), "circuitElite", Character.valueOf('R'), "alloyElite", Character.valueOf('G'), "ingotGold", Character.valueOf('P'), new ItemStack(MachineBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 4), new Object[] {
			"IRI", "ECE", "IRI", Character.valueOf('I'), "ingotIron", Character.valueOf('R'), "dustRedstone", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('C'), ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ElectrolyticCore), new Object[] {
			"EPE", "IEG", "EPE", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('P'), "dustOsmium", Character.valueOf('I'), "dustIron", Character.valueOf('G'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(CardboardBox), new Object[] {
			"SS", "SS", Character.valueOf('S'), "pulpWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Items.paper, 6), new Object[] {
			"SSS", Character.valueOf('S'), Sawdust
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 5), new Object[] {
			"ICI", "ASA", "ICI", Character.valueOf('I'), "ingotIron", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('S'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 14), new Object[] {
			"CGC", "IBI", "CGC", Character.valueOf('C'), "circuitBasic", Character.valueOf('G'), "paneGlass", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('B'), "blockCopper"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 15), new Object[] {
			"ITI", "CBC", "ITI", Character.valueOf('I'), "ingotCopper", Character.valueOf('T'), new ItemStack(BasicBlock, 1, 11), Character.valueOf('C'), "circuitBasic", Character.valueOf('B'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock2, 1, 0), new Object[] {
			"CCC", "CTC", "CCC", Character.valueOf('C'), "ingotCopper", Character.valueOf('T'), new ItemStack(BasicBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 6), new Object[] {
			"CGC", "EAE", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), AtomicAlloy, Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 7), new Object[] {
			"CWC", "EIE", "CGC", Character.valueOf('W'), Items.bucket, Character.valueOf('C'), "circuitBasic", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 8), new Object[] {
			"CGC", "ASA", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), AtomicAlloy, Character.valueOf('S'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(FreeRunners), new Object[] {
			"C C", "A A", "T T", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('T'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(ArmoredJetpack.getEmptyItem(), new Object[] {
			"D D", "BSB", " J ", Character.valueOf('D'), "dustDiamond", Character.valueOf('B'), "ingotBronze", Character.valueOf('S'), "blockSteel", Character.valueOf('J'), Jetpack.getEmptyItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(FilterCard), new Object[] {
			" A ", "ACA", " A ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(SeismicReader.getUnchargedItem(), new Object[] {
			"SLS", "STS", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 9), new Object[] {
			"TLT", "CIC", "TTT", Character.valueOf('T'), "ingotTin", Character.valueOf('L'), new ItemStack(Items.dye, 1, 4), Character.valueOf('C'), "circuitBasic", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 10), new Object[] {
			"TET", "CIC", "GFG", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), "circuitBasic", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), 
			Character.valueOf('I'), new ItemStack(MachineBlock, 1, 0), Character.valueOf('F'), new ItemStack(BasicBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 11), new Object[] {
			"III", "GCG", "III", Character.valueOf('I'), "ingotIron", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 12), new Object[] {
			"TTT", "CPC", "TTT", Character.valueOf('P'), new ItemStack(MachineBlock, 1, 12), Character.valueOf('T'), "ingotTin", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Blocks.rail, 24), new Object[] {
			"O O", "OSO", "O O", Character.valueOf('O'), "ingotOsmium", Character.valueOf('S'), "stickWood"
		}));
		
		//Energy Cube recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), new Object[] {
			"RTR", "iIi", "RTR", Character.valueOf('R'), "alloyBasic", Character.valueOf('i'), "ingotIron", Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), new Object[] {
			"ETE", "oBo", "ETE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('o'), "ingotOsmium", Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), new Object[] {
			"RTR", "gAg", "RTR", Character.valueOf('R'), "alloyElite", Character.valueOf('g'), "ingotGold", Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), new Object[] {
			"ATA", "dEd", "ATA", Character.valueOf('A'), "alloyUltimate", Character.valueOf('d'), "gemDiamond", Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE)
		}));
		
		//Circuit recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ControlCircuit, 1, 1), new Object[] {
			"ECE", Character.valueOf('C'), new ItemStack(ControlCircuit, 1, 0), Character.valueOf('E'), "alloyAdvanced"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ControlCircuit, 1, 2), new Object[] {
			"RCR", Character.valueOf('C'), new ItemStack(ControlCircuit, 1, 1), Character.valueOf('R'), "alloyElite"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ControlCircuit, 1, 3), new Object[] {
			"ACA", Character.valueOf('C'), new ItemStack(ControlCircuit, 1, 2), Character.valueOf('A'), "alloyUltimate"
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
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 0), new Object[] {
			"SRS", Character.valueOf('S'), "ingotSteel", Character.valueOf('R'), "dustRedstone"
		}));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 1), new Object[] {
            "TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 0)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 2), new Object[] {
            "TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 1)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 3), new Object[] {
            "TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 2)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 4), new Object[] {
            "SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Items.bucket
        }));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 5), new Object[] {
			"TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 4)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 6), new Object[] {
			"TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 5)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 7), new Object[] {
			"TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 6)
		}));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 8), new Object[] {
            "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass"
        }));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 9), new Object[] {
			"SCS", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 2, 10), new Object[] {
			"SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Blocks.iron_bars
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 2, 11), new Object[] {
			"RRR", "SBS", "RRR", Character.valueOf('R'), "dustRedstone", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Blocks.iron_bars
		}));
		
		//Plastic stuff
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Polyethene, 1, 1), new Object[] {
			"PP", "PP", "PP", Character.valueOf('P'), new ItemStack(Polyethene, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Polyethene, 1, 2), new Object[] {
			"PPP", "P P", "PPP", Character.valueOf('P'), new ItemStack(Polyethene, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Polyethene, 1, 3), new Object[] {
			"R", "R", Character.valueOf('R'), new ItemStack(Polyethene, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockHDPE, 4, 15), new Object[] {
			"SSS", "S S", "SSS", Character.valueOf('S'), new ItemStack(Polyethene, 1, 2)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowPanel, 2, 15), new Object[] {
			"PSP", "S S", "GSG", Character.valueOf('P'), "paneGlass", Character.valueOf('S'), new ItemStack(Polyethene, 1, 2), Character.valueOf('G'), Items.glowstone_dust
		}));

		for(int i = 0; i < EnumColor.DYES.length-1; i++)
		{
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockHDPE, 4, i), new Object[] {
				"SSS", "SDS", "SSS", Character.valueOf('S'), new ItemStack(Polyethene, 1, 2), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowPanel, 2, i), new Object[] {
				"PSP", "SDS", "GSG", Character.valueOf('P'), "paneGlass", Character.valueOf('S'), new ItemStack(Polyethene, 1, 2), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName, Character.valueOf('G'), Items.glowstone_dust
			}));
		}

		for(int i = 0; i < EnumColor.DYES.length; i++)
        {
			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Balloon, 2, i), new Object[] {
				Items.leather, Items.string, "dye" + EnumColor.DYES[i].dyeName
			}));

			for(int j = 0; j < EnumColor.DYES.length; j++)
			{
				CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Balloon, 1, i), new Object[] {
					new ItemStack(Balloon, 1, j), "dye" + EnumColor.DYES[i].dyeName
				}));

				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockHDPE, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(BlockHDPE, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockSlickHDPE, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(BlockSlickHDPE, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockGlowHDPE, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(BlockGlowHDPE, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockReinforcedHDPE, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(BlockReinforcedHDPE, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
				CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GlowPanel, 4, i), new Object[] {
					" P ", "PDP", " P ", Character.valueOf('P'), new ItemStack(GlowPanel, 1, j), Character.valueOf('D'), "dye" + EnumColor.DYES[i].dyeName
				}));
			}

			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(BlockGlowHDPE, 3, i), new Object[] {
				new ItemStack(BlockHDPE, 1, i), new ItemStack(BlockHDPE, 1, i), new ItemStack(BlockHDPE, 1, i), new ItemStack(Items.glowstone_dust)
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockReinforcedHDPE, 4, i), new Object[] {
				" P ", "POP", " P ", Character.valueOf('P'), new ItemStack(BlockHDPE, 1, i), Character.valueOf('O'), new ItemStack(Dust, 1, 2)
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BlockRoadHDPE, 3, i), new Object[] {
				"SSS", "PPP", "SSS", Character.valueOf('S'), Blocks.sand, Character.valueOf('P'), new ItemStack(BlockSlickHDPE, 1, i)
			}));
        }
	
		//Furnace Recipes
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(OreBlock, 1, 0), new ItemStack(Ingot, 1, 1), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(OreBlock, 1, 1), new ItemStack(Ingot, 1, 5), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(OreBlock, 1, 2), new ItemStack(Ingot, 1, 6), 1.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(Dust, 1, 2), new ItemStack(Ingot, 1, 1), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(Dust, 1, 0), new ItemStack(Items.iron_ingot), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(Dust, 1, 1), new ItemStack(Items.gold_ingot), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(Dust, 1, 5), new ItemStack(Ingot, 1, 4), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(Dust, 1, 6), new ItemStack(Ingot, 1, 5), 0.0F);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(Dust, 1, 7), new ItemStack(Ingot, 1, 6), 0.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.redstone_ore), new ItemStack(Items.redstone, 12));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.obsidian), new ItemStack(DirtyDust, 2, 6));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.coal, 1, 0), new ItemStack(CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.coal, 1, 1), new ItemStack(CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.redstone), new ItemStack(CompressedRedstone));
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
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(SaltBlock), new ItemStack(Salt, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.diamond), new ItemStack(CompressedDiamond));
		
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(BlockHDPE, 1, i), new ItemStack(BlockSlickHDPE, 1, i));
		}
		
		//Combiner recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.redstone, 16), new ItemStack(Blocks.redstone_ore));
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.dye, 16, 4), new ItemStack(Blocks.lapis_ore));
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.flint), new ItemStack(Blocks.gravel));
		
		//Osmium Compressor Recipes
		RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Items.glowstone_dust), new ItemStack(Ingot, 1, 3));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.diamond), new ItemStack(Dust, 1, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.iron_ingot), new ItemStack(Dust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.gold_ingot), new ItemStack(Dust, 1, 1));
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
		RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.tallgrass), new ItemStack(BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.reeds), new ItemStack(BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.wheat_seeds), new ItemStack(BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.wheat), new ItemStack(BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.pumpkin_seeds), new ItemStack(BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.melon_seeds), new ItemStack(BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.apple), new ItemStack(BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.bread), new ItemStack(BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.potato), new ItemStack(BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.carrot), new ItemStack(BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.rotten_flesh), new ItemStack(BioFuel, 2));

		//Purification Chamber Recipes
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Blocks.obsidian), new ItemStack(Clump, 3, 6));
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Blocks.gravel), new ItemStack(Items.flint));
        
        //Chemical Injection Chamber Recipes
        RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(new ItemStack(Blocks.obsidian), GasRegistry.getGas("hydrogenChloride")), new ItemStack(Shard, 4, 6));
        RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(new ItemStack(Blocks.dirt), GasRegistry.getGas("water")), new ItemStack(Blocks.clay));
        RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(new ItemStack(Items.gunpowder), GasRegistry.getGas("hydrogenChloride")), new ItemStack(Mekanism.Dust, 1, 10));
		
		//Precision Sawmill Recipes
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ladder, 3), new ChanceOutput(new ItemStack(Items.stick, 7)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.torch, 4), new ChanceOutput(new ItemStack(Items.stick, 1), new ItemStack(Items.coal, 1), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.chest), new ChanceOutput(new ItemStack(Blocks.planks, 8)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.trapdoor), new ChanceOutput(new ItemStack(Blocks.planks, 3)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.boat), new ChanceOutput(new ItemStack(Blocks.planks, 5)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.bed), new ChanceOutput(new ItemStack(Blocks.planks, 3), new ItemStack(Blocks.wool, 3), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.wooden_door), new ChanceOutput(new ItemStack(Blocks.planks, 6)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.jukebox), new ChanceOutput(new ItemStack(Blocks.planks, 8), new ItemStack(Items.diamond), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.bookshelf), new ChanceOutput(new ItemStack(Blocks.planks, 6), new ItemStack(Items.book, 3), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.wooden_pressure_plate), new ChanceOutput(new ItemStack(Blocks.planks, 2)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.fence), new ChanceOutput(new ItemStack(Items.stick, 3)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.fence_gate), new ChanceOutput(new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.noteblock), new ChanceOutput(new ItemStack(Blocks.planks, 8), new ItemStack(Items.redstone, 1), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.redstone_torch), new ChanceOutput(new ItemStack(Items.stick, 1), new ItemStack(Items.redstone), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.crafting_table), new ChanceOutput(new ItemStack(Blocks.planks, 4)));
		
        //Metallurgic Infuser Recipes
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("CARBON"), 10, new ItemStack(Items.iron_ingot)), new ItemStack(EnrichedIron));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("CARBON"), 10, new ItemStack(EnrichedIron)), new ItemStack(Dust, 1, 5));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("REDSTONE"), 10, new ItemStack(Items.iron_ingot)), new ItemStack(EnrichedAlloy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("FUNGI"), 10, new ItemStack(Blocks.dirt)), new ItemStack(Blocks.mycelium));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.cobblestone)), new ItemStack(Blocks.mossy_cobblestone));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.stonebrick, 1, 0)), new ItemStack(Blocks.stonebrick, 1, 1));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.sand)), new ItemStack(Blocks.dirt));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("DIAMOND"), 10, new ItemStack(EnrichedAlloy)), new ItemStack(ReinforcedAlloy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("OBSIDIAN"), 10, new ItemStack(ReinforcedAlloy)), new ItemStack(AtomicAlloy));
        
        //Chemical Infuser Recipes
        RecipeHandler.addChemicalInfuserRecipe(new ChemicalPair(new GasStack(GasRegistry.getGas("oxygen"), 1), new GasStack(GasRegistry.getGas("sulfurDioxideGas"), 2)), new GasStack(GasRegistry.getGas("sulfurTrioxideGas"), 2));
		RecipeHandler.addChemicalInfuserRecipe(new ChemicalPair(new GasStack(GasRegistry.getGas("sulfurTrioxideGas"), 1), new GasStack(GasRegistry.getGas("water"), 1)), new GasStack(GasRegistry.getGas("sulfuricAcid"), 1));
		RecipeHandler.addChemicalInfuserRecipe(new ChemicalPair(new GasStack(GasRegistry.getGas("hydrogen"), 1), new GasStack(GasRegistry.getGas("chlorine"), 1)), new GasStack(GasRegistry.getGas("hydrogenChloride"), 1));

		//Electrolytic Separator Recipes
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("water", 2), new ChemicalPair(new GasStack(GasRegistry.getGas("hydrogen"), 2), new GasStack(GasRegistry.getGas("oxygen"), 1)));
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("brine", 10), new ChemicalPair(new GasStack(GasRegistry.getGas("sodium"), 1), new GasStack(GasRegistry.getGas("chlorine"), 1)));
		
		//T4 Processing Recipes
		for(Gas gas : GasRegistry.getRegisteredGasses())
		{
			if(gas instanceof OreGas && !((OreGas)gas).isClean())
			{
				OreGas oreGas = (OreGas)gas;
				
				RecipeHandler.addChemicalWasherRecipe(new GasStack(oreGas, 1), new GasStack(oreGas.getCleanGas(), 1));
				RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(oreGas.getCleanGas(), 200), new ItemStack(Crystal, 1, Resource.getFromName(oreGas.getName()).ordinal()));
			}
		}
		
		//Chemical Dissolution Chamber Recipes
		RecipeHandler.addChemicalDissolutionChamberRecipe(new ItemStack(Blocks.obsidian), new GasStack(GasRegistry.getGas("obsidian"), 1000));

		//Pressurized Reaction Chamber Recipes
		RecipeHandler.addPRCRecipe(
				new PressurizedReactants(new ItemStack(BioFuel, 2), new FluidStack(FluidRegistry.WATER, 10), new GasStack(GasRegistry.getGas("hydrogen"), 100)),
				new PressurizedProducts(new ItemStack(Substrate), new GasStack(GasRegistry.getGas("ethene"), 100)),
				0,
				100
		);

		RecipeHandler.addPRCRecipe(
				new PressurizedReactants(new ItemStack(Substrate), new FluidStack(FluidRegistry.getFluid("ethene"), 50), new GasStack(GasRegistry.getGas("oxygen"), 10)),
				new PressurizedProducts(new ItemStack(Polyethene), new GasStack(GasRegistry.getGas("oxygen"), 5)),
				1000,
				60
		);

        //Infuse objects
		InfuseRegistry.registerInfuseObject(new ItemStack(BioFuel), new InfuseObject(InfuseRegistry.get("BIO"), 5));
		InfuseRegistry.registerInfuseObject(new ItemStack(Items.coal, 1, 0), new InfuseObject(InfuseRegistry.get("CARBON"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Items.coal, 1, 1), new InfuseObject(InfuseRegistry.get("CARBON"), 20));
        InfuseRegistry.registerInfuseObject(new ItemStack(CompressedCarbon), new InfuseObject(InfuseRegistry.get("CARBON"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(Items.redstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.redstone_block), new InfuseObject(InfuseRegistry.get("REDSTONE"), 90));
        InfuseRegistry.registerInfuseObject(new ItemStack(CompressedRedstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.red_mushroom), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.brown_mushroom), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(CompressedDiamond), new InfuseObject(InfuseRegistry.get("DIAMOND"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(CompressedObsidian), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 100));
        
        //Fuels
        GameRegistry.registerFuelHandler(new IFuelHandler() {
			@Override
			public int getBurnTime(ItemStack fuel)
			{
				if(fuel.isItemEqual(new ItemStack(BasicBlock, 1, 3)))
				{
					return 200*8*9;
				}
				
				return 0;
			}
        });

		//Fuel Gases
		FuelHandler.addGas(GasRegistry.getGas("hydrogen"), 1, FROM_H2);
		
		//RecipeSorter registrations
		RecipeSorter.register("mekanism", MekanismRecipe.class, Category.SHAPED, "");
		RecipeSorter.register("bin", BinRecipe.class, Category.SHAPELESS, "");
	}
	
	/**
	 * Adds and registers all items.
	 */
	public void addItems()
	{	
		//Declarations
		PartTransmitter = new ItemPartTransmitter().setUnlocalizedName("MultipartTransmitter");
		EnrichedAlloy = new ItemMekanism().setUnlocalizedName("EnrichedAlloy");
		ReinforcedAlloy = new ItemMekanism().setUnlocalizedName("ReinforcedAlloy");
		AtomicAlloy = new ItemMekanism().setUnlocalizedName("AtomicAlloy");
		ControlCircuit = new ItemControlCircuit();
		EnrichedIron = new ItemMekanism().setUnlocalizedName("EnrichedIron");
		CompressedCarbon = new ItemMekanism().setUnlocalizedName("CompressedCarbon");
		CompressedRedstone = new ItemMekanism().setUnlocalizedName("CompressedRedstone");
		CompressedDiamond = new ItemMekanism().setUnlocalizedName("CompressedDiamond");
		CompressedObsidian = new ItemMekanism().setUnlocalizedName("CompressedObsidian");
		TeleportationCore = new ItemMekanism().setUnlocalizedName("TeleportationCore");
		ElectrolyticCore = new ItemMekanism().setUnlocalizedName("ElectrolyticCore");
		SpeedUpgrade = new ItemMachineUpgrade().setUnlocalizedName("SpeedUpgrade");
		EnergyUpgrade = new ItemMachineUpgrade().setUnlocalizedName("EnergyUpgrade");
		EnergyTablet = (ItemEnergized)new ItemEnergized(1000000).setUnlocalizedName("EnergyTablet");
		Dictionary = new ItemDictionary().setUnlocalizedName("Dictionary");
		FilterCard = new ItemFilterCard().setUnlocalizedName("FilterCard");
		ElectricBow = (ItemElectricBow)new ItemElectricBow().setUnlocalizedName("ElectricBow");
		PortableTeleporter = new ItemPortableTeleporter().setUnlocalizedName("PortableTeleporter");
		Configurator = new ItemConfigurator().setUnlocalizedName("Configurator");
		NetworkReader = new ItemNetworkReader().setUnlocalizedName("NetworkReader");
		WalkieTalkie = new ItemWalkieTalkie().setUnlocalizedName("WalkieTalkie");
		SeismicReader = (ItemSeismicReader)new ItemSeismicReader().setUnlocalizedName("SeismicReader");
		AtomicDisassembler = (ItemAtomicDisassembler)new ItemAtomicDisassembler().setUnlocalizedName("AtomicDisassembler");
		GasMask = (ItemGasMask)new ItemGasMask().setUnlocalizedName("GasMask");
		ScubaTank = (ItemScubaTank)new ItemScubaTank().setUnlocalizedName("ScubaTank");
		Jetpack = (ItemJetpack)new ItemJetpack().setUnlocalizedName("Jetpack");
		ArmoredJetpack = (ItemJetpack)new ItemJetpack().setUnlocalizedName("ArmoredJetpack");
		FreeRunners = new ItemFreeRunners().setUnlocalizedName("FreeRunners");
		BrineBucket = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("BrineBucket");
		Sawdust = new ItemMekanism().setUnlocalizedName("Sawdust");
		Salt = new ItemMekanism().setUnlocalizedName("Salt");
		Ingot = new ItemIngot();
		DirtyDust = new ItemDirtyDust();
		Clump = new ItemClump();
		Shard = new ItemShard();
		Crystal = new ItemCrystal();
		Dust = new ItemDust();
		Robit = (ItemRobit)new ItemRobit().setUnlocalizedName("Robit");
		Balloon = new ItemBalloon().setUnlocalizedName("Balloon");
		ItemProxy = new ItemProxy().setUnlocalizedName("ItemProxy");
		Substrate = new ItemMekanism().setUnlocalizedName("Substrate");
		Polyethene = new ItemHDPE().setUnlocalizedName("HDPE");
		BioFuel = new ItemMekanism().setUnlocalizedName("BioFuel");
		GlowPanel = new ItemGlowPanel().setUnlocalizedName("GlowPanel");

		//Fluid Container stuff
		FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("brine"), new ItemStack(BrineBucket), FluidContainerRegistry.EMPTY_BUCKET);
		
		//Registrations
		GameRegistry.registerItem(PartTransmitter, "PartTransmitter");
		GameRegistry.registerItem(ElectricBow, "ElectricBow");
		GameRegistry.registerItem(Dust, "Dust");
		GameRegistry.registerItem(Ingot, "Ingot");
		GameRegistry.registerItem(EnergyTablet, "EnergyTablet");
		GameRegistry.registerItem(SpeedUpgrade, "SpeedUpgrade");
		GameRegistry.registerItem(EnergyUpgrade, "EnergyUpgrade");
		GameRegistry.registerItem(Robit, "Robit");
		GameRegistry.registerItem(AtomicDisassembler, "AtomicDisassembler");
		GameRegistry.registerItem(EnrichedAlloy, "EnrichedAlloy");
		GameRegistry.registerItem(ReinforcedAlloy, "ReinforcedAlloy");
		GameRegistry.registerItem(AtomicAlloy, "AtomicAlloy");
		GameRegistry.registerItem(ItemProxy, "ItemProxy");
		GameRegistry.registerItem(ControlCircuit, "ControlCircuit");
		GameRegistry.registerItem(EnrichedIron, "EnrichedIron");
		GameRegistry.registerItem(CompressedCarbon, "CompressedCarbon");
		GameRegistry.registerItem(CompressedRedstone, "CompressedRedstone");
		GameRegistry.registerItem(CompressedDiamond, "CompressedDiamond");
		GameRegistry.registerItem(CompressedObsidian, "CompressedObsidian");
		GameRegistry.registerItem(PortableTeleporter, "PortableTeleporter");
		GameRegistry.registerItem(TeleportationCore, "TeleportationCore");
		GameRegistry.registerItem(Clump, "Clump");
		GameRegistry.registerItem(DirtyDust, "DirtyDust");
		GameRegistry.registerItem(Configurator, "Configurator");
		GameRegistry.registerItem(NetworkReader, "NetworkReader");
		GameRegistry.registerItem(WalkieTalkie, "WalkieTalkie");
		GameRegistry.registerItem(Jetpack, "Jetpack");
		GameRegistry.registerItem(Dictionary, "Dictionary");
		GameRegistry.registerItem(GasMask, "GasMask");
		GameRegistry.registerItem(ScubaTank, "ScubaTank");
		GameRegistry.registerItem(Balloon, "Balloon");
		GameRegistry.registerItem(Shard, "Shard");
		GameRegistry.registerItem(ElectrolyticCore, "ElectrolyticCore");
		GameRegistry.registerItem(Sawdust, "Sawdust");
		GameRegistry.registerItem(Salt, "Salt");
		GameRegistry.registerItem(BrineBucket, "BrineBucket");
		GameRegistry.registerItem(Crystal, "Crystal");
		GameRegistry.registerItem(FreeRunners, "FrictionBoots");
		GameRegistry.registerItem(ArmoredJetpack, "ArmoredJetpack");
		GameRegistry.registerItem(FilterCard, "FilterCard");
		GameRegistry.registerItem(SeismicReader, "SeismicReader");
		GameRegistry.registerItem(Substrate, "Substrate");
		GameRegistry.registerItem(Polyethene, "Polyethene");
		GameRegistry.registerItem(BioFuel, "BioFuel");
		GameRegistry.registerItem(GlowPanel, "GlowPanel");
	}
	
	/**
	 * Adds and registers all blocks.
	 */
	public void addBlocks()
	{
		//Declarations
		BasicBlock = new BlockBasic().setBlockName("BasicBlock");
		BasicBlock2 = new BlockBasic().setBlockName("BasicBlock2");
		MachineBlock = new BlockMachine().setBlockName("MachineBlock");
		MachineBlock2 = new BlockMachine().setBlockName("MachineBlock2");
		OreBlock = new BlockOre().setBlockName("OreBlock");
		EnergyCube = new BlockEnergyCube().setBlockName("EnergyCube");
		ObsidianTNT = new BlockObsidianTNT().setBlockName("ObsidianTNT").setCreativeTab(tabMekanism);
		BoundingBlock = (BlockBounding)new BlockBounding().setBlockName("BoundingBlock");
		GasTank = new BlockGasTank().setBlockName("GasTank");
		CardboardBox = new BlockCardboardBox().setBlockName("CardboardBox");
		BlockHDPE = new BlockPlastic().setBlockName("PlasticBlock");
		BlockSlickHDPE = new BlockPlastic().setBlockName("SlickPlasticBlock");
		BlockGlowHDPE = new BlockPlastic().setBlockName("GlowPlasticBlock");
		BlockReinforcedHDPE = new BlockPlastic().setBlockName("ReinforcedPlasticBlock");
		BlockRoadHDPE = new BlockPlastic().setBlockName("RoadPlasticBlock");
		BlockHDPEFence = new BlockPlasticFence().setBlockName("PlasticFence");
		SaltBlock = new BlockSalt().setBlockName("SaltBlock");

		//Registrations
		GameRegistry.registerBlock(BasicBlock, ItemBlockBasic.class, "BasicBlock");
		GameRegistry.registerBlock(BasicBlock2, ItemBlockBasic.class, "BasicBlock2");
		GameRegistry.registerBlock(MachineBlock, ItemBlockMachine.class, "MachineBlock");
		GameRegistry.registerBlock(MachineBlock2, ItemBlockMachine.class, "MachineBlock2");
		GameRegistry.registerBlock(OreBlock, ItemBlockOre.class, "OreBlock");
		GameRegistry.registerBlock(EnergyCube, ItemBlockEnergyCube.class, "EnergyCube");
		GameRegistry.registerBlock(ObsidianTNT, "ObsidianTNT");
		GameRegistry.registerBlock(BoundingBlock, "BoundingBlock");
		GameRegistry.registerBlock(GasTank, ItemBlockGasTank.class, "GasTank");
		GameRegistry.registerBlock(CardboardBox, ItemBlockCardboardBox.class, "CardboardBox");
		GameRegistry.registerBlock(BlockHDPE, ItemBlockPlastic.class, "PlasticBlock");
		GameRegistry.registerBlock(BlockSlickHDPE, ItemBlockPlastic.class, "SlickPlasticBlock");
		GameRegistry.registerBlock(BlockGlowHDPE, ItemBlockPlastic.class, "GlowPlasticBlock");
		GameRegistry.registerBlock(BlockReinforcedHDPE, ItemBlockPlastic.class, "ReinforcedPlasticBlock");
		GameRegistry.registerBlock(BlockRoadHDPE, ItemBlockPlastic.class, "RoadPlasticBlock");
		GameRegistry.registerBlock(BlockHDPEFence, "PlasticFence");
		GameRegistry.registerBlock(SaltBlock, "SaltBlock");
	}
	
	/**
	 * Registers specified items with the Ore Dictionary.
	 */
	public void registerOreDict()
	{
		//Add specific items to ore dictionary for recipe usage in other mods.
		OreDictionary.registerOre("universalCable", new ItemStack(PartTransmitter, 8, 0));
		OreDictionary.registerOre("battery", EnergyTablet.getUnchargedItem());
		OreDictionary.registerOre("pulpWood", Sawdust);
		OreDictionary.registerOre("dustWood", Sawdust);
		OreDictionary.registerOre("blockSalt", SaltBlock);
		
		//Alloys!
		OreDictionary.registerOre("alloyBasic", new ItemStack(Items.redstone));
		OreDictionary.registerOre("alloyAdvanced", new ItemStack(EnrichedAlloy));
		OreDictionary.registerOre("alloyElite", new ItemStack(ReinforcedAlloy));
		OreDictionary.registerOre("alloyUltimate", new ItemStack(AtomicAlloy));
		
		//for RailCraft/IC2.
		OreDictionary.registerOre("dustObsidian", new ItemStack(DirtyDust, 1, 6));
		
		//GregoriousT?
		OreDictionary.registerOre("itemSalt", Salt);
		OreDictionary.registerOre("dustSalt", Salt);
		
		OreDictionary.registerOre("dustIron", new ItemStack(Dust, 1, 0));
		OreDictionary.registerOre("dustGold", new ItemStack(Dust, 1, 1));
		OreDictionary.registerOre("dustOsmium", new ItemStack(Dust, 1, 2));
		OreDictionary.registerOre("dustRefinedObsidian", new ItemStack(Dust, 1, 3));
		OreDictionary.registerOre("dustDiamond", new ItemStack(Dust, 1, 4));
		OreDictionary.registerOre("dustSteel", new ItemStack(Dust, 1, 5));
		OreDictionary.registerOre("dustCopper", new ItemStack(Dust, 1, 6));
		OreDictionary.registerOre("dustTin", new ItemStack(Dust, 1, 7));
		OreDictionary.registerOre("dustSilver", new ItemStack(Dust, 1, 8));
		OreDictionary.registerOre("dustLead", new ItemStack(Dust, 1, 9));
		OreDictionary.registerOre("dustSulfur", new ItemStack(Dust, 1, 10));
		
		OreDictionary.registerOre("ingotRefinedObsidian", new ItemStack(Ingot, 1, 0));
		OreDictionary.registerOre("ingotOsmium", new ItemStack(Ingot, 1, 1));
		OreDictionary.registerOre("ingotBronze", new ItemStack(Ingot, 1, 2));
		OreDictionary.registerOre("ingotRefinedGlowstone", new ItemStack(Ingot, 1, 3));
		OreDictionary.registerOre("ingotSteel", new ItemStack(Ingot, 1, 4));
		OreDictionary.registerOre("ingotCopper", new ItemStack(Ingot, 1, 5));
		OreDictionary.registerOre("ingotTin", new ItemStack(Ingot, 1, 6));
		
		OreDictionary.registerOre("blockOsmium", new ItemStack(BasicBlock, 1, 0));
		OreDictionary.registerOre("blockBronze", new ItemStack(BasicBlock, 1, 1));
		OreDictionary.registerOre("blockRefinedObsidian", new ItemStack(BasicBlock, 1, 2));
		OreDictionary.registerOre("blockCharcoal", new ItemStack(BasicBlock, 1, 3));
		OreDictionary.registerOre("blockRefinedGlowstone", new ItemStack(BasicBlock, 1, 4));
		OreDictionary.registerOre("blockSteel", new ItemStack(BasicBlock, 1, 5));
		OreDictionary.registerOre("blockCopper", new ItemStack(BasicBlock, 1, 12));
		OreDictionary.registerOre("blockTin", new ItemStack(BasicBlock, 1, 13));
		
		for(Resource resource : Resource.values())
		{
			OreDictionary.registerOre("dustDirty" + resource.getName(), new ItemStack(DirtyDust, 1, resource.ordinal()));
			OreDictionary.registerOre("clump" + resource.getName(), new ItemStack(Clump, 1, resource.ordinal()));
			OreDictionary.registerOre("shard" + resource.getName(), new ItemStack(Shard, 1, resource.ordinal()));
			OreDictionary.registerOre("crystal" + resource.getName(), new ItemStack(Crystal, 1, resource.ordinal()));
		}
		
		OreDictionary.registerOre("oreOsmium", new ItemStack(OreBlock, 1, 0));
		OreDictionary.registerOre("oreCopper", new ItemStack(OreBlock, 1, 1));
		OreDictionary.registerOre("oreTin", new ItemStack(OreBlock, 1, 2));
		
		//MC stuff
		OreDictionary.registerOre("oreCoal", new ItemStack(Blocks.coal_ore));
		OreDictionary.registerOre("ingotIron", new ItemStack(Items.iron_ingot));
		OreDictionary.registerOre("ingotGold", new ItemStack(Items.gold_ingot));
		OreDictionary.registerOre("oreRedstone", new ItemStack(Blocks.redstone_ore));
		//OreDictionary.registerOre("oreRedstone", new ItemStack(Blocks.lit_redstone_ore));
		
		if(controlCircuitOreDict)
		{
			OreDictionary.registerOre("circuitBasic", new ItemStack(ControlCircuit, 1, 0));
			OreDictionary.registerOre("circuitAdvanced", new ItemStack(ControlCircuit, 1, 1));
			OreDictionary.registerOre("circuitElite", new ItemStack(ControlCircuit, 1, 2));
			OreDictionary.registerOre("circuitUltimate", new ItemStack(ControlCircuit, 1, 3));
		}
		
		OreDictionary.registerOre("itemCompressedCarbon", new ItemStack(CompressedCarbon));
		OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(EnrichedAlloy));
		OreDictionary.registerOre("itemBioFuel", new ItemStack(BioFuel));
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
		EntityRegistry.registerModEntity(EntityObsidianTNT.class, "ObsidianTNT", 0, this, 40, 5, true);
		EntityRegistry.registerModEntity(EntityRobit.class, "Robit", 1, this, 40, 2, true);
		EntityRegistry.registerModEntity(EntityBalloon.class, "Balloon", 2, this, 40, 1, true);
		
		//Tile entities
		GameRegistry.registerTileEntity(TileEntityBoundingBlock.class, "BoundingBlock");
		GameRegistry.registerTileEntity(TileEntityAdvancedBoundingBlock.class, "AdvancedBoundingBlock");
		GameRegistry.registerTileEntity(TileEntityCardboardBox.class, "CardboardBox");
		GameRegistry.registerTileEntity(TileEntitySalinationValve.class, "SalinationValve");
		GameRegistry.registerTileEntity(TileEntitySalinationTank.class, "SalinationTank");

		//Load tile entities that have special renderers.
		proxy.registerSpecialTileEntities();
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		if(voiceServerEnabled)
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
		if(voiceServerEnabled)
		{
			voiceManager.stop();
		}
		
		//Clear all cache data
		teleporters.clear();
		dynamicInventories.clear();
		jetpackOn.clear();
		gasmaskOn.clear();
		activeVibrators.clear();
		worldTickHandler.resetRegenChunks();
		
		TransporterManager.flowingStacks.clear();
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
		
		for(Resource resource : Resource.values())
		{
			String name = resource.getName();
			
			OreGas clean = (OreGas)GasRegistry.register(new OreGas("clean" + name, "oregas." + name.toLowerCase()).setVisible(false));
			GasRegistry.register(new OreGas(name.toLowerCase(), "oregas." + name.toLowerCase()).setCleanGas(clean).setVisible(false));
		}
		
		Mekanism.proxy.preInit();
		
		//Register infuses
        InfuseRegistry.registerInfuseType(new InfuseType("CARBON", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 0, 0).setUnlocalizedName("infuse.carbon"));
        InfuseRegistry.registerInfuseType(new InfuseType("TIN", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 4, 0).setUnlocalizedName("infuse.tin"));
        InfuseRegistry.registerInfuseType(new InfuseType("DIAMOND", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 8, 0).setUnlocalizedName("infuse.diamond"));
        InfuseRegistry.registerInfuseType(new InfuseType("REDSTONE", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 16, 0).setUnlocalizedName("infuse.redstone"));
        InfuseRegistry.registerInfuseType(new InfuseType("FUNGI", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 20, 0).setUnlocalizedName("infuse.fungi"));
		InfuseRegistry.registerInfuseType(new InfuseType("BIO", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 12, 0).setUnlocalizedName("infuse.bio"));
		InfuseRegistry.registerInfuseType(new InfuseType("OBSIDIAN", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 24, 0).setUnlocalizedName("infuse.obsidian"));

		//Load configuration
		proxy.loadConfiguration();

		//Register the mod's world generators
		GameRegistry.registerWorldGenerator(genHandler, 1);

		//Register player tracker
		FMLCommonHandler.instance().bus().register(new CommonPlayerTracker());
		FMLCommonHandler.instance().bus().register(new CommonPlayerTickHandler());

		//Register to receive subscribed events
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		//Register with TransmitterNetworkRegistry
		TransmitterNetworkRegistry.initiate();

		//Load this module
		addItems();
		addBlocks();
		addRecipes();
		addEntities();

		registerOreDict();

		new MultipartMekanism();

		//Load proxy
		proxy.registerRenderInformation();
		proxy.loadUtilities();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) 
	{
		//Register the mod's GUI handler
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CoreGuiHandler());
		
		//Initialization notification
		logger.info("Version " + versionNumber + " initializing...");
		
		//Get data from server.
		new ThreadGetData();
		
		//Set up VoiceServerManager
		if(voiceServerEnabled)
		{
			voiceManager = new VoiceServerManager();
		}

		//Packet registrations
		packetHandler.initialize();

		//Completion notification
		logger.info("Loading complete.");
		
		//Success message
		logger.info("Mod loaded.");
	}	
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{		
		proxy.loadSoundHandler();
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
		MekanismAPI.addBoxBlacklist(CardboardBox, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(BoundingBlock, OreDictionary.WILDCARD_VALUE);
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
			nbtTags.setInteger("MekanismUserWorldGen", userWorldGenVersion);
		}
	}
	
	@SubscribeEvent
	public synchronized void onChunkDataLoad(ChunkDataEvent.Load event)
	{
		if(!event.world.isRemote)
		{
			if(enableWorldRegeneration)
			{
				NBTTagCompound loadData = event.getData();
				
				if(loadData.getInteger("MekanismWorldGen") == baseWorldGenVersion && loadData.getInteger("MekanismUserWorldGen") == userWorldGenVersion)
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
