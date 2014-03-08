package mekanism.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import mekanism.api.AdvancedInput;
import mekanism.api.ChanceOutput;
import mekanism.api.ChemicalPair;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
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
import mekanism.common.PacketHandler.Transmission;
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
import mekanism.common.item.ItemClump;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemCrystal;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemDirtyDust;
import mekanism.common.item.ItemDust;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemFilterCard;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemIngot;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemMachineUpgrade;
import mekanism.common.item.ItemMekanism;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemProxy;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemShard;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multipart.ItemPartTransmitter;
import mekanism.common.multipart.MultipartMekanism;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfiguratorState;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketDigitUpdate;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketElectricBowState;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketKey;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.network.PacketPortableTeleport;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.network.PacketRedstoneControl;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.network.PacketStatusUpdate;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.network.PacketWalkieTalkieState;
import mekanism.common.recipe.BinRecipe;
import mekanism.common.recipe.MekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tank.DynamicTankCache;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.voice.VoiceServerManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rebelkeithy.mods.metallurgy.api.IOreInfo;
import rebelkeithy.mods.metallurgy.api.MetallurgyAPI;
import codechicken.multipart.handler.MultipartProxy;
import cpw.mods.fml.common.FMLLog;
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
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Mekanism - the mod that doesn't have a category.
 * @author AidanBrady
 *
 */
@Mod(modid = "Mekanism", name = "Mekanism", version = "6.0.4")
@NetworkMod(channels = {"MEK"}, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Mekanism
{
	/** Mekanism logger instance */
	public static Logger logger = Logger.getLogger("Minecraft");
	
	/** Mekanism proxy instance */
	@SidedProxy(clientSide = "mekanism.client.ClientProxy", serverSide = "mekanism.common.CommonProxy")
	public static CommonProxy proxy;
	
	/** Mekanism debug mode */
	public static boolean debug = false;
	
    /** Mekanism mod instance */
	@Instance("Mekanism")
    public static Mekanism instance;
    
    /** Mekanism hooks instance */
    public static MekanismHooks hooks = new MekanismHooks();
    
    /** Mekanism configuration instance */
    public static Configuration configuration;
    
	/** Mekanism version number */
	public static Version versionNumber = new Version(6, 0, 4);
	
	/** Map of Teleporters */
	public static Map<Teleporter.Code, ArrayList<Coord4D>> teleporters = new HashMap<Teleporter.Code, ArrayList<Coord4D>>();
	
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
	
	public static KeySync keyMap = new KeySync();
	
	public static Set<String> jetpackOn = new HashSet<String>();
	public static Set<String> gasmaskOn = new HashSet<String>();
	
	public static Set<Coord4D> ic2Registered = new HashSet<Coord4D>();
    
	//Block IDs
    public static int basicBlockID;
    public static int basicBlock2ID;
    public static int machineBlockID;
    public static int machineBlock2ID;
    public static int oreBlockID;
	public static int obsidianTNTID;
	public static int energyCubeID;
	public static int boundingBlockID;
	public static int gasTankID;
	public static int cardboardBoxID;
	
	//Items
	public static ItemElectricBow ElectricBow;
	public static Item EnrichedAlloy;
	public static ItemEnergized EnergyTablet;
	public static Item SpeedUpgrade;
	public static Item EnergyUpgrade;
	public static ItemRobit Robit;
	public static ItemAtomicDisassembler AtomicDisassembler;
	public static Item AtomicCore;
	public static Item ControlCircuit;
	public static Item EnrichedIron;
	public static Item CompressedCarbon;
	public static Item PortableTeleporter;
	public static Item TeleportationCore;
	public static Item Configurator;
	public static Item NetworkReader;
	public static Item WalkieTalkie;
	public static Item ItemProxy;
	public static Item PartTransmitter;
	public static ItemJetpack Jetpack;
	public static ItemScubaTank ScubaTank;
	public static ItemGasMask GasMask;
	public static Item Dictionary;
	public static Item Balloon;
	public static Item ElectrolyticCore;
	public static Item CompressedRedstone;
	public static Item Sawdust;
	public static Item Salt;
	public static Item BrineBucket;
	public static Item FreeRunners;
	public static ItemJetpack ArmoredJetpack;
	public static Item FilterCard;

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

	//Multi-ID Items
	public static Item Dust;
	public static Item Ingot;
	public static Item Clump;
	public static Item DirtyDust;
	public static Item Shard;
	public static Item Crystal;
	
	public static int ITEM_ID = 11200;
	public static int BLOCK_ID = 3000;

	//General Configuration
	public static boolean osmiumGenerationEnabled = true;
	public static boolean copperGenerationEnabled = true;
	public static boolean tinGenerationEnabled = true;
	public static boolean disableBCBronzeCrafting = true;
	public static boolean disableBCSteelCrafting = true;
	public static boolean updateNotifications = true;
	public static boolean controlCircuitOreDict = true;
	public static boolean logPackets = false;
	public static boolean dynamicTankEasterEgg = false;
	public static boolean voiceServerEnabled = true;
	public static boolean forceBuildcraft = false;
	public static boolean cardboardSpawners = true;
	public static boolean machineEffects = true;
	public static int obsidianTNTBlastRadius = 12;
	public static int osmiumGenerationAmount = 12;
	public static int copperGenerationAmount = 16;
	public static int tinGenerationAmount = 14;
	public static int obsidianTNTDelay = 100;
	public static int UPDATE_DELAY = 10;
	public static int VOICE_PORT = 36123;
	public static int maxUpgradeMultiplier = 10;
	public static double ENERGY_PER_REDSTONE = 10000;
	public static EnergyType activeType = EnergyType.J;

	public static double TO_IC2;
	public static double TO_BC;
	public static double TO_TE;
	public static double TO_UE = .001;
	public static double FROM_IC2;
	public static double FROM_BC;
	public static double FROM_TE;
	public static double FROM_UE = 1/TO_UE;

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
	public static double rotaryCondensentratorUsage;
	public static double oxidationChamberUsage;
	public static double chemicalInfuserUsage;
	public static double chemicalInjectionChamberUsage;
	public static double electrolyticSeparatorUsage;
	public static double precisionSawmillUsage;
	public static double chemicalDissolutionChamberUsage;
	public static double chemicalWasherUsage;
	public static double chemicalCrystalizerUsage;

	/**
	 * Adds all in-game crafting and smelting recipes.
	 */
	public void addRecipes()
	{
		//Storage Recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 3), new Object[] {
			"***", "***", "***", Character.valueOf('*'), new ItemStack(Item.coal, 1, 1)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Item.coal, 9, 1), new Object[] {
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
		
		//Base Recipes
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ObsidianTNT, 1), new Object[] {
			"***", "XXX", "***", Character.valueOf('*'), Block.obsidian, Character.valueOf('X'), Block.tnt
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(ElectricBow.getUnchargedItem(), new Object[] {
			" AB", "E B", " AB", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('B'), Item.silk, Character.valueOf('E'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(EnergyTablet.getUnchargedItem(), new Object[] {
			"RCR", "ECE", "RCR", Character.valueOf('C'), Item.ingotGold, Character.valueOf('R'), Item.redstone, Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 0), new Object[] {
			"ARA", "CIC", "ARA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('R'), Item.redstone, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 1), new Object[] {
			"RCR", "GIG", "RCR", Character.valueOf('R'), Item.redstone, Character.valueOf('C'), "circuitBasic", Character.valueOf('G'), Block.glass, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 2), new Object[] {
			"SCS", "RIR", "SCS", Character.valueOf('S'), Block.cobblestone, Character.valueOf('C'), "circuitBasic", Character.valueOf('R'), Item.redstone, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 3), new Object[] {
			"RLR", "CIC", "RLR", Character.valueOf('R'), Item.redstone, Character.valueOf('L'), Item.bucketLava, Character.valueOf('C'), "circuitBasic", Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(SpeedUpgrade), new Object[] {
			" G ", "APA", " G ", Character.valueOf('P'), "dustOsmium", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), Block.glass
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(EnergyUpgrade), new Object[] {
			" G ", "ADA", " G ", Character.valueOf('G'), Block.glass, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(AtomicCore), new Object[] {
			"AOA", "PDP", "AOA", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('O'), "dustObsidian", Character.valueOf('P'), "dustOsmium", Character.valueOf('D'), Item.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(AtomicDisassembler.getUnchargedItem(), new Object[] {
			"AEA", "ACA", " O ", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEmptyGasTank(), new Object[] {
			"PPP", "PDP", "PPP", Character.valueOf('P'), "ingotOsmium", Character.valueOf('D'), "dustIron"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), new Object[] {
			"RLR", "TIT", "RLR", Character.valueOf('R'), Item.redstone, Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), new Object[] {
			"EGE", "TBT", "EGE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('G'), Item.ingotGold, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), new Object[] {
			"CDC", "TAT", "CDC", Character.valueOf('C'), "circuitBasic", Character.valueOf('D'), Item.diamond, Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), new Object[] {
			"COC", "TAT", "COC", Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('T'), EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ControlCircuit), new Object[] {
			"RER", Character.valueOf('R'), Item.redstone, Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 8), new Object[] {
			"IFI", "ROR", "IFI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('F'), Block.furnaceIdle, Character.valueOf('R'), Item.redstone, Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(TeleportationCore), new Object[] {
			"LAL", "GDG", "LAL", Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('A'), AtomicCore, Character.valueOf('G'), Item.ingotGold, Character.valueOf('D'), Item.diamond
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PortableTeleporter), new Object[] {
			" E ", "CTC", " E ", Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), "circuitBasic", Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 11), new Object[] {
			"COC", "OTO", "COC", Character.valueOf('C'), "circuitBasic", Character.valueOf('O'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 9), new Object[] {
			"CAC", "ERE", "CAC", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), AtomicCore, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('R'), new ItemStack(MachineBlock, 1, 0)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Configurator), new Object[] {
			" L ", "AEA", " S ", Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('S'), Item.stick
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 9, 7), new Object[] {
			"OOO", "OGO", "OOO", Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('G'), "ingotRefinedGlowstone"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 8), new Object[] {
			" S ", "SPS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('P'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 10), new Object[] {
			"SCS", "GIG", "SCS", Character.valueOf('S'), Block.cobblestone, Character.valueOf('C'), "circuitBasic", Character.valueOf('G'), Block.glass, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 12), new Object[] {
			" B ", "ECE", "OOO", Character.valueOf('B'), Item.bucketEmpty, Character.valueOf('E'), EnrichedAlloy, Character.valueOf('C'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('O'), "ingotOsmium"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 13), new Object[] {
			"SGS", "CcC", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), Block.glass, Character.valueOf('C'), Block.chest, Character.valueOf('c'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 8, 9), new Object[] {
			" I ", "ISI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('S'), Block.cobblestone
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 8, 10), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('G'), Block.glass
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 2, 11), new Object[] {
			" I ", "ICI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 14), new Object[] {
			"PPP", "SES", Character.valueOf('P'), Block.pressurePlateStone, Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), EnergyTablet.getUnchargedItem()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(Robit.getUnchargedItem(), new Object[] {
			" S ", "ECE", "OIO", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('C'), AtomicCore, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('I'), new ItemStack(MachineBlock, 1, 13)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(NetworkReader), new Object[] {
			" G ", "AEA", " I ", Character.valueOf('G'), Block.glass, Character.valueOf('A'), EnrichedAlloy, Character.valueOf('E'), EnergyTablet.getUnchargedItem(), Character.valueOf('I'), "ingotSteel"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(WalkieTalkie), new Object[] {
			"  O", "SCS", " S ", Character.valueOf('O'), "ingotOsmium", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 15), new Object[] {
			"IPI", "ICI", "III", Character.valueOf('I'), Item.ingotIron, Character.valueOf('P'), Block.pistonBase, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 6), new Object[] {
			"SSS", "SCS", "SSS", Character.valueOf('S'), Block.cobblestone, Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock, 1, 4), new Object[] {
			"ACA", "SES", "TIT", Character.valueOf('A'), AtomicCore, Character.valueOf('C'), "circuitBasic", Character.valueOf('S'), new ItemStack(MachineBlock, 1, 15), Character.valueOf('E'), Robit.getUnchargedItem(),
			Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('T'), TeleportationCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 0), new Object[] {
			"GCG", "tET", "GIG", Character.valueOf('G'), Block.glass, Character.valueOf('C'), "circuitBasic", Character.valueOf('t'), MekanismUtils.getEmptyGasTank(), Character.valueOf('E'), EnergyTablet.getUnchargedItem(), 
			Character.valueOf('T'), new ItemStack(BasicBlock, 1, 9), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(Jetpack.getEmptyItem(), new Object[] {
			"SCS", "TGT", " T ", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic", Character.valueOf('T'), "ingotTin", Character.valueOf('G'), MekanismUtils.getEmptyGasTank()
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Dictionary), new Object[] {
			"C", "B", Character.valueOf('C'), "circuitBasic", Character.valueOf('B'), Item.book
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(GasMask), new Object[] {
			" S ", "GCG", "S S", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), Block.glass, Character.valueOf('C'), "circuitBasic"
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
			"ACA", "ERE", "ACA", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), AtomicCore, Character.valueOf('E'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('R'), new ItemStack(MachineBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 4), new Object[] {
			"IRI", "ECE", "IRI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), Item.redstone, Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('C'), ElectrolyticCore
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(ElectrolyticCore), new Object[] {
			"EPE", "IEG", "EPE", Character.valueOf('E'), Mekanism.EnrichedAlloy, Character.valueOf('P'), "dustOsmium", Character.valueOf('I'), "dustIron", Character.valueOf('G'), "dustGold"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(CardboardBox), new Object[] {
			"SS", "SS", Character.valueOf('S'), "pulpWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(Item.paper, 6), new Object[] {
			"SSS", Character.valueOf('S'), Sawdust
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 5), new Object[] {
			"ICI", "ASA", "ICI", Character.valueOf('I'), "ingotIron", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('S'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 14), new Object[] {
			"CGC", "IBI", "CGC", Character.valueOf('C'), "circuitBasic", Character.valueOf('G'), Block.thinGlass, Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8), Character.valueOf('B'), "blockCopper"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock, 1, 15), new Object[] {
			"ITI", "CBC", "ITI", Character.valueOf('I'), "ingotCopper", Character.valueOf('T'), new ItemStack(BasicBlock, 1, 11), Character.valueOf('C'), "circuitBasic", Character.valueOf('B'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(BasicBlock2, 1, 0), new Object[] {
			"CCC", "CTC", "CCC", Character.valueOf('C'), "ingotCopper", Character.valueOf('T'), new ItemStack(BasicBlock, 1, 9)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 6), new Object[] {
			"CGC", "EAE", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), AtomicCore, Character.valueOf('E'), EnrichedAlloy
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 7), new Object[] {
			"CWC", "EIE", "CGC", Character.valueOf('W'), Item.bucketWater, Character.valueOf('C'), "circuitBasic", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('I'), new ItemStack(BasicBlock, 1, 8)
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(MachineBlock2, 1, 8), new Object[] {
			"CGC", "ASA", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(), Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), AtomicCore, Character.valueOf('S'), new ItemStack(BasicBlock, 1, 8)
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

		for(RecipeType type : RecipeType.values())
		{
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, type), new Object[] {
				"CAC", "GOG", "CAC", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('G'), "dustGold", Character.valueOf('O'), type.getStack()
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.ADVANCED, type), new Object[] {
				"CAC", "DOD", "CAC", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('D'), "dustDiamond", Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.BASIC, type)
			}));
			CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.ELITE, type), new Object[] {
				"CAC", "cOc", "CAC", Character.valueOf('C'), "circuitBasic", Character.valueOf('A'), EnrichedAlloy, Character.valueOf('c'), AtomicCore, Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.ADVANCED, type)
			}));
		}
		
		CraftingManager.getInstance().getRecipeList().add(new BinRecipe());
		
        //Transmitters
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 0), new Object[] {
            "SRS", Character.valueOf('S'), "ingotSteel", Character.valueOf('R'), Item.redstone
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 1, 1), new Object[] {
            "ETE", Character.valueOf('E'), EnrichedAlloy, Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 0)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 1, 2), new Object[] {
            "CTC", Character.valueOf('C'), "circuitBasic", Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 0)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 1, 3), new Object[] {
            "CTC", Character.valueOf('C'), AtomicCore, Character.valueOf('T'), new ItemStack(PartTransmitter, 1, 0)
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 4), new Object[] {
            "SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Item.bucketEmpty
        }));
        CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 5), new Object[] {
            "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), Block.glass
        }));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 8, 6), new Object[] {
			"SCS", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "circuitBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 2, 7), new Object[] {
			"SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Block.fenceIron
		}));
		CraftingManager.getInstance().getRecipeList().add(new MekanismRecipe(new ItemStack(PartTransmitter, 2, 8), new Object[] {
			"RRR", "SBS", "RRR", Character.valueOf('R'), Item.redstone, Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Block.fenceIron
		}));
        
        for(int i = 0; i < EnumColor.DYES.length; i++)
        {
        	EnumColor color = EnumColor.DYES[i];
        	
        	if(color != null)
        	{
        		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Balloon, 2, i), new Object[] {
        			Item.leather, Item.silk, new ItemStack(Item.dyePowder, 1, i)
        		}));
        		
        		for(int j = 0; j < EnumColor.DYES.length; j++)
        		{
        			EnumColor color1 = EnumColor.DYES[j];
        			
        			if(color1 != null)
        			{
        				CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Balloon, 1, i), new Object[] {
        					new ItemStack(Balloon, 1, j), new ItemStack(Item.dyePowder, 1, i)
        				}));
        			}
        		}
        	}
        }
	
		//Furnace Recipes
		FurnaceRecipes.smelting().addSmelting(oreBlockID, 0, new ItemStack(Ingot, 1, 1), 1.0F);
		FurnaceRecipes.smelting().addSmelting(oreBlockID, 1, new ItemStack(Ingot, 1, 5), 1.0F);
		FurnaceRecipes.smelting().addSmelting(oreBlockID, 2, new ItemStack(Ingot, 1, 6), 1.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 2, new ItemStack(Ingot, 1, 1), 0.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 0, new ItemStack(Item.ingotIron), 0.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 1, new ItemStack(Item.ingotGold), 0.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 5, new ItemStack(Ingot, 1, 4), 0.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 6, new ItemStack(Ingot, 1, 5), 0.0F);
		FurnaceRecipes.smelting().addSmelting(Dust.itemID, 7, new ItemStack(Ingot, 1, 6), 0.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreRedstone), new ItemStack(Item.redstone, 12));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.obsidian), new ItemStack(DirtyDust, 2, 6));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.coal, 1, 0), new ItemStack(CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.coal, 1, 1), new ItemStack(CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.redstone), new ItemStack(CompressedRedstone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreLapis), new ItemStack(Item.dyePowder, 12, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreCoal), new ItemStack(Item.coal, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreDiamond), new ItemStack(Item.diamond, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.cobblestoneMossy), new ItemStack(Block.cobblestone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stone), new ItemStack(Block.stoneBrick, 1, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.sand), new ItemStack(Block.gravel));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.gravel), new ItemStack(Block.cobblestone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Item.gunpowder), new ItemStack(Item.flint));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stoneBrick, 1, 2), new ItemStack(Block.stoneBrick, 1, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1, 3));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.stoneBrick, 1, 1), new ItemStack(Block.stoneBrick, 1, 0));
		
		//Combiner recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.redstone, 16), new ItemStack(Block.oreRedstone));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.dyePowder, 16, 4), new ItemStack(Block.oreLapis));
		RecipeHandler.addCombinerRecipe(new ItemStack(Item.flint), new ItemStack(Block.gravel));
		
		//Osmium Compressor Recipes
		RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Item.glowstone), new ItemStack(Ingot, 1, 3));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Item.diamond), new ItemStack(Dust, 1, 4));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotIron), new ItemStack(Dust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.ingotGold), new ItemStack(Dust, 1, 1));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.gravel), new ItemStack(Block.sand));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stone), new ItemStack(Block.cobblestone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.cobblestone), new ItemStack(Block.gravel));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stoneBrick, 1, 2), new ItemStack(Block.stone));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stoneBrick, 1, 0), new ItemStack(Block.stoneBrick, 1, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.stoneBrick, 1, 3), new ItemStack(Block.stoneBrick, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Item.flint, 4), new ItemStack(Item.gunpowder));
        RecipeHandler.addCrusherRecipe(new ItemStack(Block.sandStone), new ItemStack(Block.sand, 2));

        //Purification Chamber Recipes
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.obsidian), new ItemStack(Clump, 3, 6));
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.gravel), new ItemStack(Item.flint));
        
        //Chemical Injection Chamber Recipes
        RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(new ItemStack(Block.obsidian), GasRegistry.getGas("hydrogenChloride")), new ItemStack(Shard, 4, 6));
        RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(new ItemStack(Block.dirt), GasRegistry.getGas("water")), new ItemStack(Block.blockClay));
        RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(new ItemStack(Item.gunpowder), GasRegistry.getGas("hydrogenChloride")), new ItemStack(Mekanism.Dust, 1, 10));
		
		//Precision Sawmill Recipes
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.ladder, 3), new ChanceOutput(new ItemStack(Item.stick, 7)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.chest), new ChanceOutput(new ItemStack(Block.planks, 8)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.trapdoor), new ChanceOutput(new ItemStack(Block.planks, 3)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Item.boat), new ChanceOutput(new ItemStack(Block.planks, 5)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Item.bed), new ChanceOutput(new ItemStack(Block.planks, 3), new ItemStack(Block.cloth, 3), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.jukebox), new ChanceOutput(new ItemStack(Block.planks, 8), new ItemStack(Item.diamond), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.bookShelf), new ChanceOutput(new ItemStack(Block.planks, 6), new ItemStack(Item.book, 3), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.pressurePlatePlanks), new ChanceOutput(new ItemStack(Block.planks, 2)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.fence), new ChanceOutput(new ItemStack(Item.stick, 3)));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.fenceGate), new ChanceOutput(new ItemStack(Block.planks, 2), new ItemStack(Item.stick, 4), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.music), new ChanceOutput(new ItemStack(Block.planks, 8), new ItemStack(Item.redstone, 1), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.torchRedstoneIdle), new ChanceOutput(new ItemStack(Item.stick, 1), new ItemStack(Item.redstone), 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Block.workbench), new ChanceOutput(new ItemStack(Block.planks, 4)));
		
        //Metallurgic Infuser Recipes
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("CARBON"), 10, new ItemStack(Item.ingotIron)), new ItemStack(EnrichedIron));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("CARBON"), 10, new ItemStack(EnrichedIron)), new ItemStack(Dust, 1, 5));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("REDSTONE"), 10, new ItemStack(Item.ingotIron)), new ItemStack(EnrichedAlloy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("FUNGI"), 10, new ItemStack(Block.dirt)), new ItemStack(Block.mycelium));
        
        if(InfuseRegistry.contains("BIO"))
        {
	        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("BIO"), 10, new ItemStack(Block.cobblestone)), new ItemStack(Block.cobblestoneMossy));
	        RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("BIO"), 10, new ItemStack(Block.stoneBrick, 1, 0)), new ItemStack(Block.stoneBrick, 1, 1));
        }
        
        //Chemical Infuser Recipes
        RecipeHandler.addChemicalInfuserRecipe(new ChemicalPair(new GasStack(GasRegistry.getGas("oxygen"), 1), new GasStack(GasRegistry.getGas("sulfurDioxideGas"), 2)), new GasStack(GasRegistry.getGas("sulfurTrioxideGas"), 2));
		RecipeHandler.addChemicalInfuserRecipe(new ChemicalPair(new GasStack(GasRegistry.getGas("sulfurTrioxideGas"), 1), new GasStack(GasRegistry.getGas("water"), 1)), new GasStack(GasRegistry.getGas("sulfuricAcid"), 1));
		RecipeHandler.addChemicalInfuserRecipe(new ChemicalPair(new GasStack(GasRegistry.getGas("hydrogen"), 1), new GasStack(GasRegistry.getGas("chlorine"), 1)), new GasStack(GasRegistry.getGas("hydrogenChloride"), 1));

		//Electrolytic Separator Recipes
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("water", 2), new ChemicalPair(new GasStack(GasRegistry.getGas("hydrogen"), 2), new GasStack(GasRegistry.getGas("oxygen"), 1)));
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("brine", 10), new ChemicalPair(new GasStack(GasRegistry.getGas("hydrogen"), 1), new GasStack(GasRegistry.getGas("chlorine"), 1)));
		
		//Chemical Washer Recipes
		for(Gas gas : GasRegistry.getRegisteredGasses())
		{
			if(gas instanceof OreGas && !((OreGas)gas).isClean())
			{
				OreGas oreGas = (OreGas)gas;
				
				RecipeHandler.addChemicalWasherRecipe(new GasStack(oreGas, 1), new GasStack(oreGas.getCleanGas(), 1));
				RecipeHandler.addChemicalCrystalizerRecipe(new GasStack(oreGas.getCleanGas(), 200), new ItemStack(Crystal, 1, Resource.getFromName(oreGas.getName()).ordinal()));
			}
		}

        //Infuse objects
        InfuseRegistry.registerInfuseObject(new ItemStack(Item.coal, 1, 0), new InfuseObject(InfuseRegistry.get("CARBON"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Item.coal, 1, 1), new InfuseObject(InfuseRegistry.get("CARBON"), 20));
        InfuseRegistry.registerInfuseObject(new ItemStack(CompressedCarbon), new InfuseObject(InfuseRegistry.get("CARBON"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(Item.redstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Block.blockRedstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 90));
        InfuseRegistry.registerInfuseObject(new ItemStack(CompressedRedstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 100));
        InfuseRegistry.registerInfuseObject(new ItemStack(Block.mushroomRed), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Block.mushroomBrown), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        
        //Fuels
        GameRegistry.registerFuelHandler(new IFuelHandler() {
			@Override
			public int getBurnTime(ItemStack fuel)
			{
				if(fuel.isItemEqual(new ItemStack(BasicBlock, 1, 3)))
				{
					return 1800;
				}
				
				return 0;
			}
        });
	}
	
	/**
	 * Adds and registers all items.
	 */
	public void addItems()
	{	
		//Declarations
		configuration.load();
		PartTransmitter = new ItemPartTransmitter(configuration.getItem("MultipartTransmitter", ITEM_ID++).getInt()).setUnlocalizedName("MultipartTransmitter");
		EnrichedAlloy = new ItemMekanism(configuration.getItem("EnrichedAlloy", ITEM_ID++).getInt()).setUnlocalizedName("EnrichedAlloy");
		EnrichedIron = new ItemMekanism(configuration.getItem("EnrichedIron", ITEM_ID++).getInt()).setUnlocalizedName("EnrichedIron");
		ControlCircuit = new ItemMekanism(configuration.getItem("ControlCircuit", ITEM_ID++).getInt()).setUnlocalizedName("ControlCircuit");
		AtomicCore = new ItemMekanism(configuration.getItem("AtomicCore", ITEM_ID++).getInt()).setUnlocalizedName("AtomicCore");
		TeleportationCore = new ItemMekanism(configuration.getItem("TeleportationCore", ITEM_ID++).getInt()).setUnlocalizedName("TeleportationCore");
		ElectrolyticCore = new ItemMekanism(configuration.getItem("ElectrolyticCore", ITEM_ID++).getInt()).setUnlocalizedName("ElectrolyticCore");
		CompressedCarbon = new ItemMekanism(configuration.getItem("CompressedCarbon", ITEM_ID++).getInt()).setUnlocalizedName("CompressedCarbon");
		CompressedRedstone = new ItemMekanism(configuration.getItem("CompressedRedstone", ITEM_ID++).getInt()).setUnlocalizedName("CompressedRedstone");
		SpeedUpgrade = new ItemMachineUpgrade(configuration.getItem("SpeedUpgrade", ITEM_ID++).getInt()).setUnlocalizedName("SpeedUpgrade");
		EnergyUpgrade = new ItemMachineUpgrade(configuration.getItem("EnergyUpgrade", ITEM_ID++).getInt()).setUnlocalizedName("EnergyUpgrade");
		EnergyTablet = (ItemEnergized)new ItemEnergized(configuration.getItem("EnergyTablet", ITEM_ID++).getInt(), 1000000, 120).setUnlocalizedName("EnergyTablet");
		Dictionary = new ItemDictionary(configuration.getItem("Dictionary", ITEM_ID++).getInt()).setUnlocalizedName("Dictionary");
		FilterCard = new ItemFilterCard(configuration.getItem("FilterCard", ITEM_ID++).getInt()).setUnlocalizedName("FilterCard");
		ElectricBow = (ItemElectricBow)new ItemElectricBow(configuration.getItem("ElectricBow", ITEM_ID++).getInt()).setUnlocalizedName("ElectricBow");
		PortableTeleporter = new ItemPortableTeleporter(configuration.getItem("PortableTeleporter", ITEM_ID++).getInt()).setUnlocalizedName("PortableTeleporter");
		Configurator = new ItemConfigurator(configuration.getItem("Configurator", ITEM_ID++).getInt()).setUnlocalizedName("Configurator");
		NetworkReader = new ItemNetworkReader(configuration.getItem("NetworkReader", ITEM_ID++).getInt()).setUnlocalizedName("NetworkReader");
		WalkieTalkie = new ItemWalkieTalkie(configuration.getItem("WalkieTalkie", ITEM_ID++).getInt()).setUnlocalizedName("WalkieTalkie");
		AtomicDisassembler = (ItemAtomicDisassembler)new ItemAtomicDisassembler(configuration.getItem("AtomicDisassembler", ITEM_ID++).getInt()).setUnlocalizedName("AtomicDisassembler");
		GasMask = (ItemGasMask)new ItemGasMask(configuration.getItem("GasMask", ITEM_ID++).getInt()).setUnlocalizedName("GasMask");
		ScubaTank = (ItemScubaTank)new ItemScubaTank(configuration.getItem("ScubaTank", ITEM_ID++).getInt()).setUnlocalizedName("ScubaTank");
		Jetpack = (ItemJetpack)new ItemJetpack(configuration.getItem("Jetpack", ITEM_ID++).getInt()).setUnlocalizedName("Jetpack");
		ArmoredJetpack = (ItemJetpack)new ItemJetpack(configuration.getItem("ArmoredJetpack", ITEM_ID++).getInt()).setUnlocalizedName("ArmoredJetpack");
		FreeRunners = new ItemFreeRunners(configuration.getItem("FreeRunners", ITEM_ID++).getInt()).setUnlocalizedName("FreeRunners");
		BrineBucket = new ItemMekanism(configuration.getItem("BrineBucket", ITEM_ID++).getInt()).setMaxStackSize(1).setContainerItem(Item.bucketEmpty).setUnlocalizedName("BrineBucket");
		Sawdust = new ItemMekanism(configuration.getItem("Sawdust", ITEM_ID++).getInt()).setUnlocalizedName("Sawdust");
		Salt = new ItemMekanism(configuration.getItem("Salt", ITEM_ID++).getInt()).setUnlocalizedName("Salt");
		Ingot = new ItemIngot(configuration.getItem("Ingot", ITEM_ID++).getInt());
		DirtyDust = new ItemDirtyDust(configuration.getItem("DirtyDust", ITEM_ID++).getInt());
		Clump = new ItemClump(configuration.getItem("Clump", ITEM_ID++).getInt());
		Shard = new ItemShard(configuration.getItem("Shard", ITEM_ID++).getInt());
		Crystal = new ItemCrystal(configuration.getItem("Crystal", ITEM_ID++).getInt());
		Dust = new ItemDust(configuration.getItem("Dust", ITEM_ID++).getInt());
		Robit = (ItemRobit)new ItemRobit(configuration.getItem("Robit", ITEM_ID++).getInt()).setUnlocalizedName("Robit");
		Balloon = new ItemBalloon(configuration.getItem("Balloon", ITEM_ID++).getInt()).setUnlocalizedName("Balloon");
		ItemProxy = new ItemProxy(configuration.getItem("ItemProxy", ITEM_ID++).getInt()).setUnlocalizedName("ItemProxy");

		configuration.save();
		
		//Fluid Container stuff
		FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("brine"), new ItemStack(BrineBucket), FluidContainerRegistry.EMPTY_BUCKET);
		
		//Registrations
		GameRegistry.registerItem(ElectricBow, "ElectricBow");
		GameRegistry.registerItem(Dust, "Dust");
		GameRegistry.registerItem(Ingot, "Ingot");
		GameRegistry.registerItem(EnergyTablet, "EnergyTablet");
		GameRegistry.registerItem(SpeedUpgrade, "SpeedUpgrade");
		GameRegistry.registerItem(EnergyUpgrade, "EnergyUpgrade");
		GameRegistry.registerItem(Robit, "Robit");
		GameRegistry.registerItem(AtomicDisassembler, "AtomicDisassembler");
		GameRegistry.registerItem(AtomicCore, "AtomicCore");
		GameRegistry.registerItem(EnrichedAlloy, "EnrichedAlloy");
		GameRegistry.registerItem(ItemProxy, "ItemProxy");
		GameRegistry.registerItem(ControlCircuit, "ControlCircuit");
		GameRegistry.registerItem(EnrichedIron, "EnrichedIron");
		GameRegistry.registerItem(CompressedCarbon, "CompressedCarbon");
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
		GameRegistry.registerItem(CompressedRedstone, "CompressedRedstone");
		GameRegistry.registerItem(Sawdust, "Sawdust");
		GameRegistry.registerItem(Salt, "Salt");
		GameRegistry.registerItem(BrineBucket, "BrineBucket");
		GameRegistry.registerItem(Crystal, "Crystal");
		GameRegistry.registerItem(FreeRunners, "FrictionBoots");
		GameRegistry.registerItem(ArmoredJetpack, "ArmoredJetpack");
		GameRegistry.registerItem(FilterCard, "FilterCard");
	}
	
	/**
	 * Adds and registers all blocks.
	 */
	public void addBlocks()
	{
		//Declarations
		BasicBlock = new BlockBasic(basicBlockID).setUnlocalizedName("BasicBlock");
		BasicBlock2 = new BlockBasic(basicBlock2ID).setUnlocalizedName("BasicBlock2");
		MachineBlock = new BlockMachine(machineBlockID).setUnlocalizedName("MachineBlock");
		MachineBlock2 = new BlockMachine(machineBlock2ID).setUnlocalizedName("MachineBlock2");
		OreBlock = new BlockOre(oreBlockID).setUnlocalizedName("OreBlock");
		EnergyCube = new BlockEnergyCube(energyCubeID).setUnlocalizedName("EnergyCube");
		ObsidianTNT = new BlockObsidianTNT(obsidianTNTID).setUnlocalizedName("ObsidianTNT").setCreativeTab(tabMekanism);
		BoundingBlock = (BlockBounding) new BlockBounding(boundingBlockID).setUnlocalizedName("BoundingBlock");
		GasTank = new BlockGasTank(gasTankID).setUnlocalizedName("GasTank");
		CardboardBox = new BlockCardboardBox(cardboardBoxID).setUnlocalizedName("CardboardBox");
		
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
	}
	
	/**
	 * Registers specified items with the Ore Dictionary.
	 */
	public void registerOreDict()
	{
		//Add specific items to ore dictionary for recipe usage in other mods. @Calclavia
		OreDictionary.registerOre("universalCable", new ItemStack(PartTransmitter, 8, 0));
		OreDictionary.registerOre("battery", EnergyTablet.getUnchargedItem());
		OreDictionary.registerOre("pulpWood", Sawdust);
		
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
		OreDictionary.registerOre("oreCoal", new ItemStack(Block.oreCoal));
		OreDictionary.registerOre("ingotIron", new ItemStack(Item.ingotIron));
		OreDictionary.registerOre("ingotGold", new ItemStack(Item.ingotGold));
		OreDictionary.registerOre("oreRedstone", new ItemStack(Block.oreRedstone));
		OreDictionary.registerOre("oreRedstone", new ItemStack(Block.oreRedstoneGlowing));
		
		if(controlCircuitOreDict || !hooks.BasicComponentsLoaded)
		{
			OreDictionary.registerOre("circuitBasic", new ItemStack(ControlCircuit));
		}
		
		OreDictionary.registerOre("itemCompressedCarbon", new ItemStack(CompressedCarbon));
		OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(EnrichedAlloy));
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
		//Entity IDs
		EntityRegistry.registerGlobalEntityID(EntityObsidianTNT.class, "ObsidianTNT", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityRobit.class, "Robit", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityBalloon.class, "Balloon", EntityRegistry.findGlobalUniqueEntityId());
		
		//Registrations
		EntityRegistry.registerModEntity(EntityObsidianTNT.class, "ObsidianTNT", 0, this, 40, 5, true);
		EntityRegistry.registerModEntity(EntityRobit.class, "Robit", 1, this, 40, 2, true);
		EntityRegistry.registerModEntity(EntityBalloon.class, "Balloon", 2, this, 40, 1, true);
		
		//Tile entities
		GameRegistry.registerTileEntity(TileEntityBoundingBlock.class, "BoundingBlock");
		GameRegistry.registerTileEntity(TileEntityAdvancedBoundingBlock.class, "AdvancedBoundingBlock");
		GameRegistry.registerTileEntity(TileEntityCardboardBox.class, "CardboardBox");
		
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
		
		for(Map.Entry<List<Integer>, ItemStack> entry : FurnaceRecipes.smelting().getMetaSmeltingList().entrySet())
		{
			TileEntityEnergizedSmelter.furnaceRecipes.put(new ItemStack(entry.getKey().get(0), 1, entry.getKey().get(1)), entry.getValue());
		}
		
		for(Object obj : FurnaceRecipes.smelting().getSmeltingList().entrySet())
		{
			Map.Entry<Integer, ItemStack> entry = (Map.Entry<Integer, ItemStack>)obj;
			TileEntityEnergizedSmelter.furnaceRecipes.put(new ItemStack(entry.getKey(), 1, OreDictionary.WILDCARD_VALUE), entry.getValue());
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
		ic2Registered.clear();
		jetpackOn.clear();
		gasmaskOn.clear();
		
		TransporterManager.flowingStacks.clear();
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger.setParent(FMLLog.getLogger());
		File config = event.getSuggestedConfigurationFile();
		
		//Set the mod's configuration
		configuration = new Configuration(config);
		
		if(config.getAbsolutePath().contains("voltz"))
		{
			System.out.println("[Mekanism] Detected Voltz in root directory - hello, fellow user!");
		}
		else if(config.getAbsolutePath().contains("tekkit"))
		{
			System.out.println("[Mekanism] Detected Tekkit in root directory - hello, fellow user!");
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
		
		for(Resource resource : Resource.values())
		{
			String name = resource.getName();
			
			OreGas clean = (OreGas)GasRegistry.register(new OreGas("clean" + name, "oregas." + name.toLowerCase()).setVisible(false));
			GasRegistry.register(new OreGas(name.toLowerCase(), "oregas." + name.toLowerCase()).setCleanGas(clean).setVisible(false));
		}

		FluidRegistry.registerFluid(new Fluid("brine"));
		
		Mekanism.proxy.preInit();
		
		//Register infuses
        InfuseRegistry.registerInfuseType(new InfuseType("CARBON", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 0, 0).setUnlocalizedName("infuse.carbon"));
        InfuseRegistry.registerInfuseType(new InfuseType("TIN", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 4, 0).setUnlocalizedName("infuse.tin"));
        InfuseRegistry.registerInfuseType(new InfuseType("DIAMOND", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 8, 0).setUnlocalizedName("infuse.diamond"));
        InfuseRegistry.registerInfuseType(new InfuseType("REDSTONE", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 16, 0).setUnlocalizedName("infuse.redstone"));
        InfuseRegistry.registerInfuseType(new InfuseType("FUNGI", MekanismUtils.getResource(ResourceType.INFUSE, "Infusions.png"), 20, 0).setUnlocalizedName("infuse.fungi"));
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) 
	{
		//Register the mod's ore handler
		GameRegistry.registerWorldGenerator(new OreHandler());
		
		//Register player tracker
		GameRegistry.registerPlayerTracker(new CommonPlayerTracker());
		
		//Register the mod's GUI handler
		NetworkRegistry.instance().registerGuiHandler(this, new CoreGuiHandler());
		
		//Initialization notification
		System.out.println("[Mekanism] Version " + versionNumber + " initializing...");
		
		//Get data from server.
		new ThreadGetData();
		
		//Register to receive subscribed events
		MinecraftForge.EVENT_BUS.register(this);

		//Set up VoiceServerManager
		if(voiceServerEnabled)
		{
			voiceManager = new VoiceServerManager();
		}
		
		//Register with TransmitterNetworkRegistry
		TransmitterNetworkRegistry.initiate();
		
		//Load configuration
		proxy.loadConfiguration();

		//Load this module
		addItems();
		addBlocks();
		addRecipes();
		addEntities();
		
		registerOreDict();

		new MultipartMekanism();

		//Packet registrations
		PacketHandler.registerPacket(PacketRobit.class);
		PacketHandler.registerPacket(PacketTransmitterUpdate.class);
		PacketHandler.registerPacket(PacketElectricChest.class);
		PacketHandler.registerPacket(PacketElectricBowState.class);
		PacketHandler.registerPacket(PacketConfiguratorState.class);
		PacketHandler.registerPacket(PacketTileEntity.class);
		PacketHandler.registerPacket(PacketPortalFX.class);
		PacketHandler.registerPacket(PacketDataRequest.class);
		PacketHandler.registerPacket(PacketStatusUpdate.class);
		PacketHandler.registerPacket(PacketDigitUpdate.class);
		PacketHandler.registerPacket(PacketPortableTeleport.class);
		PacketHandler.registerPacket(PacketRemoveUpgrade.class);
		PacketHandler.registerPacket(PacketRedstoneControl.class);
		PacketHandler.registerPacket(PacketWalkieTalkieState.class);
		PacketHandler.registerPacket(PacketLogisticalSorterGui.class);
		PacketHandler.registerPacket(PacketNewFilter.class);
		PacketHandler.registerPacket(PacketEditFilter.class);
		PacketHandler.registerPacket(PacketConfigurationUpdate.class);
		PacketHandler.registerPacket(PacketSimpleGui.class);
		PacketHandler.registerPacket(PacketDigitalMinerGui.class);
		PacketHandler.registerPacket(PacketJetpackData.class);
		PacketHandler.registerPacket(PacketKey.class);
		PacketHandler.registerPacket(PacketScubaTankData.class);
		PacketHandler.registerPacket(PacketConfigSync.class);
		PacketHandler.registerPacket(PacketBoxBlacklist.class);
		
		//Load proxy
		proxy.registerRenderInformation();
		proxy.loadUtilities();
		
		//Completion notification
		System.out.println("[Mekanism] Loading complete.");
		
		//Success message
		logger.info("[Mekanism] Mod loaded.");
	}	
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{		
		proxy.loadSoundHandler();
		hooks.hook();
		
		MinecraftForge.EVENT_BUS.post(new BoxBlacklistEvent());
		
		addIntegratedItems();
		
		OreDictManager.init();
		
		System.out.println("[Mekanism] Hooking complete.");
	}
	
	@ForgeSubscribe
	public void onEnergyTransferred(EnergyTransferEvent event)
	{
		try {
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTransmitterUpdate().setParams(PacketType.ENERGY, event.energyNetwork.transmitters.iterator().next(), event.power));
		} catch(Exception e) {}
	}
	
	@ForgeSubscribe
	public void onGasTransferred(GasTransferEvent event)
	{
		try {
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTransmitterUpdate().setParams(PacketType.GAS, event.gasNetwork.transmitters.iterator().next(), event.transferType, event.didTransfer));
		} catch(Exception e) {}
	}
	
	@ForgeSubscribe
	public void onLiquidTransferred(FluidTransferEvent event)
	{
		try {
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTransmitterUpdate().setParams(PacketType.FLUID, event.fluidNetwork.transmitters.iterator().next(), event.fluidType, event.didTransfer));
		} catch(Exception e) {}
	}
	
	@ForgeSubscribe
	public void onNetworkClientRequest(NetworkClientRequest event)
	{
		try {
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Coord4D.get(event.tileEntity)));
		} catch(Exception e) {}
	}
	
	@ForgeSubscribe
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
	
	@ForgeSubscribe
	public void onBlacklistUpdate(BoxBlacklistEvent event)
	{
		MekanismAPI.addBoxBlacklist(CardboardBox.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(BoundingBlock.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Block.bedrock.blockID, 0);
		MekanismAPI.addBoxBlacklist(Block.portal.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Block.endPortal.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Block.endPortalFrame.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Block.bed.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Block.doorWood.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Block.doorIron.blockID, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(MultipartProxy.block().blockID, OreDictionary.WILDCARD_VALUE);
		
		BoxBlacklistParser.load();
	}
	
	@ForgeSubscribe
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
	
					if(tileEntity instanceof TileEntityElectricBlock)
					{
						((TileEntityElectricBlock)tileEntity).register();
					}
				}
			}
		}
	}
}
