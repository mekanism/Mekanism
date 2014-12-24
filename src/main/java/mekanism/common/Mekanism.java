package mekanism.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
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
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import mekanism.common.recipe.BinRecipe;
import mekanism.common.recipe.MekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityElectricBlock;
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
import net.minecraft.item.crafting.IRecipe;
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
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rebelkeithy.mods.metallurgy.api.IOreInfo;
import rebelkeithy.mods.metallurgy.api.MetallurgyAPI;

import static mekanism.common.block.states.BlockStateBasic.BasicBlockType.*;
import static mekanism.common.block.states.BlockStateMachine.MachineBlockType.*;

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
	
	public static Set<Coord4D> activeVibrators = new HashSet<Coord4D>();

	/**
	 * Adds all in-game crafting, smelting and machine recipes.
	 */
	public void addRecipes()
	{
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		//Storage Recipes
		recipes.add(new MekanismRecipe(COAL_BLOCK.getStack(1),
			"***", "***", "***", '*', new ItemStack(Items.coal, 1, 1)
		));
		recipes.add(new MekanismRecipe(new ItemStack(Items.coal, 9, 1),
			"*", '*', BasicBlockType.COAL_BLOCK.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.REFINED_OBSIDIAN.getStack(1),
			"***", "***", "***", '*', "ingotRefinedObsidian"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 0),
			"*", '*', BasicBlockType.REFINED_OBSIDIAN.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.REFINED_GLOWSTONE.getStack(1),
			"***", "***", "***", '*', "ingotRefinedGlowstone"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 3),
			"*", '*', BasicBlockType.REFINED_GLOWSTONE.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.OSMIUM_BLOCK.getStack(1),
			"XXX", "XXX", "XXX", 'X', "ingotOsmium"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 1),
			"*", '*', BasicBlockType.OSMIUM_BLOCK.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.BRONZE_BLOCK.getStack(1),
			"***", "***", "***", '*', "ingotBronze"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 2),
			"*", '*', BasicBlockType.BRONZE_BLOCK.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.STEEL_BLOCK.getStack(1),
			"***", "***", "***", '*', "ingotSteel"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 4),
			"*", '*', BasicBlockType.STEEL_BLOCK.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.COPPER_BLOCK.getStack(1),
			"***", "***", "***", '*', "ingotCopper"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 5),
			"*", '*', BasicBlockType.COPPER_BLOCK.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.TIN_BLOCK.getStack(1),
			"***", "***", "***", '*', "ingotTin"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 6),
			"*", '*', BasicBlockType.TIN_BLOCK.getStack(1)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.SaltBlock),
			"**", "**", '*', MekanismItems.Salt
		));
		
		//Base Recipes
		recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.ObsidianTNT, 1),
			"***", "XXX", "***", '*', Blocks.obsidian, 'X', Blocks.tnt
		));
		recipes.add(new MekanismRecipe(MekanismItems.ElectricBow.getUnchargedItem(),
			" AB", "E B", " AB", 'A', MekanismItems.EnrichedAlloy, 'B', Items.string, 'E', MekanismItems.EnergyTablet.getUnchargedItem()
		));
		recipes.add(new MekanismRecipe(MekanismItems.EnergyTablet.getUnchargedItem(),
			"RCR", "ECE", "RCR", 'C', "ingotGold", 'R', Items.redstone, 'E', MekanismItems.EnrichedAlloy
		));
		recipes.add(new MekanismRecipe(ENRICHMENT_CHAMBER.getStack(1),
			"RCR", "iIi", "RCR", 'i', "ingotIron", 'C', "circuitBasic", 'R', "alloyBasic", 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(OSMIUM_COMPRESSOR.getStack(1),
			"ECE", "BIB", "ECE", 'E', "alloyAdvanced", 'C', "circuitAdvanced", 'B', Items.bucket, 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(COMBINER.getStack(1),
			"RCR", "SIS", "RCR", 'S', Blocks.cobblestone, 'C', "circuitElite", 'R', "alloyElite", 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(CRUSHER.getStack(1),
			"RCR", "LIL", "RCR", 'R', Items.redstone, 'L', Items.lava_bucket, 'C', "circuitBasic", 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.SpeedUpgrade),
			" G ", "APA", " G ", 'P', "dustOsmium", 'A', MekanismItems.EnrichedAlloy, 'G', "blockGlass"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.EnergyUpgrade),
			" G ", "ADA", " G ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'D', "dustGold"
		));
		recipes.add(new MekanismRecipe(MekanismItems.AtomicDisassembler.getUnchargedItem(),
			"AEA", "ACA", " O ", 'A', MekanismItems.EnrichedAlloy, 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', MekanismItems.AtomicAlloy, 'O', "ingotRefinedObsidian"
		));
		recipes.add(new MekanismRecipe(MekanismUtils.getEmptyGasTank(),
			"PPP", "PDP", "PPP", 'P', "ingotOsmium", 'D', "dustIron"
		));
		recipes.add(new MekanismRecipe(METALLURGIC_INFUSER.getStack(1),
			"IFI", "ROR", "IFI", 'I', Items.iron_ingot, 'F', Blocks.furnace, 'R', Items.redstone, 'O', "ingotOsmium"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.TeleportationCore),
			"LAL", "GDG", "LAL", 'L', new ItemStack(Items.dye, 1, 4), 'A', MekanismItems.AtomicAlloy, 'G', Items.gold_ingot, 'D', Items.diamond
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PortableTeleporter),
			" E ", "CTC", " E ", 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', "circuitBasic", 'T', MekanismItems.TeleportationCore
		));
		recipes.add(new MekanismRecipe(TELEPORTER.getStack(1),
			"COC", "OTO", "COC", 'C', "circuitBasic", 'O', BasicBlockType.STEEL_CASING.getStack(1), 'T', MekanismItems.TeleportationCore
		));
		recipes.add(new MekanismRecipe(PURIFICATION_CHAMBER.getStack(1),
			"ECE", "ORO", "ECE", 'C', "circuitAdvanced", 'E', "alloyAdvanced", 'O', "ingotOsmium", 'R', ENRICHMENT_CHAMBER.getStack(1)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Configurator),
			" L ", "AEA", " S ", 'L', new ItemStack(Items.dye, 1, 4), 'A', MekanismItems.EnrichedAlloy, 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'S', Items.stick
		));
		recipes.add(new MekanismRecipe(BasicBlockType.TELEPORTER_FRAME.getStack(9),
			"OOO", "OGO", "OOO", 'O', "ingotRefinedObsidian", 'G', "ingotRefinedGlowstone"
		));
		recipes.add(new MekanismRecipe(BasicBlockType.STEEL_CASING.getStack(1),
			" S ", "SPS", " S ", 'S', "ingotSteel", 'P', "ingotOsmium"
		));
		recipes.add(new MekanismRecipe(ENERGIZED_SMELTER.getStack(1),
			"RCR", "GIG", "RCR", 'C', "circuitBasic", 'R', "alloyBasic", 'G', "blockGlass", 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(ELECTRIC_PUMP.getStack(1),
			" B ", "ECE", "OOO", 'B', Items.bucket, 'E', MekanismItems.EnrichedAlloy, 'C', BasicBlockType.STEEL_CASING.getStack(1), 'O', "ingotOsmium"
		));
		recipes.add(new MekanismRecipe(ELECTRIC_CHEST.getStack(1),
			"SGS", "CcC", "SSS", 'S', "ingotSteel", 'G', "blockGlass", 'C', Blocks.chest, 'c', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(BasicBlockType.DYNAMIC_TANK.getStack(8),
			" I ", "ISI", " I ", 'I', "ingotSteel", 'S', Blocks.cobblestone
		));
		recipes.add(new MekanismRecipe(BasicBlockType.DYNAMIC_GLASS.getStack(8),
			" I ", "IGI", " I ", 'I', "ingotSteel", 'G', "blockGlass"
		));
		recipes.add(new MekanismRecipe(BasicBlockType.DYNAMIC_VALVE.getStack(2),
			" I ", "ICI", " I ", 'I', "ingotSteel", 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(CHARGEPAD.getStack(1),
			"PPP", "SES", 'P', Blocks.stone_pressure_plate, 'S', "ingotSteel", 'E', MekanismItems.EnergyTablet.getUnchargedItem()
		));
		recipes.add(new MekanismRecipe(MekanismItems.Robit.getUnchargedItem(),
			" S ", "ECE", "OIO", 'S', "ingotSteel", 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', MekanismItems.AtomicAlloy, 'O', "ingotRefinedObsidian", 'I', ELECTRIC_CHEST.getStack(1)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.NetworkReader),
			" G ", "AEA", " I ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'I', "ingotSteel"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.WalkieTalkie),
			"  O", "SCS", " S ", 'O', "ingotOsmium", 'S', "ingotSteel", 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(LOGISTICAL_SORTER.getStack(1),
			"IPI", "ICI", "III", 'I', "ingotIron", 'P', Blocks.piston, 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(BasicBlockType.BIN.getStack(1),
			"SSS", "SCS", "SSS", 'S', Blocks.cobblestone, 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(DIGITAL_MINER.getStack(1),
			"ACA", "SES", "TIT", 'A', MekanismItems.AtomicAlloy, 'C', "circuitBasic", 'S', LOGISTICAL_SORTER.getStack(1), 'E', MekanismItems.Robit.getUnchargedItem(),
				'I', BasicBlockType.STEEL_CASING.getStack(1), 'T', MekanismItems.TeleportationCore
		));
		recipes.add(new MekanismRecipe(ROTARY_CONDENSENTRATOR.getStack(1),
			"GCG", "tET", "GIG", 'G', "blockGlass", 'C', "circuitBasic", 't', MekanismUtils.getEmptyGasTank(), 'E', MekanismItems.EnergyTablet.getUnchargedItem(),
				'T', BasicBlockType.DYNAMIC_TANK.getStack(1), 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(MekanismItems.Jetpack.getEmptyItem(),
			"SCS", "TGT", " T ", 'S', "ingotSteel", 'C', "circuitBasic", 'T', "ingotTin", 'G', MekanismUtils.getEmptyGasTank()
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Dictionary),
			"C", "B", 'C', "circuitBasic", 'B', Items.book
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.GasMask),
			" S ", "GCG", "S S", 'S', "ingotSteel", 'G', "blockGlass", 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(MekanismItems.ScubaTank.getEmptyItem(),
			" C ", "ATA", "SSS", 'C', "circuitBasic", 'A', MekanismItems.EnrichedAlloy, 'S', "ingotSteel"
		));
		recipes.add(new MekanismRecipe(CHEMICAL_OXIDIZER.getStack(1),
			"ACA", "ERG", "ACA", 'C', "circuitBasic", 'R', BasicBlockType.DYNAMIC_TANK.getStack(1), 'G', MekanismUtils.getEmptyGasTank(), 'E', ELECTRIC_CHEST.getStack(1), 'A', MekanismItems.EnrichedAlloy
		));
		recipes.add(new MekanismRecipe(CHEMICAL_INFUSER.getStack(1),
			"ACA", "GRG", "ACA", 'C', "circuitBasic", 'R', BasicBlockType.DYNAMIC_TANK.getStack(1), 'G', MekanismUtils.getEmptyGasTank(), 'A', MekanismItems.EnrichedAlloy
		));
		recipes.add(new MekanismRecipe(CHEMICAL_INJECTION_CHAMBER.getStack(1),
			"RCR", "GPG", "RCR", 'C', "circuitElite", 'R', "alloyElite", 'G', "ingotGold", 'P', PURIFICATION_CHAMBER.getStack(1)
		));
		recipes.add(new MekanismRecipe(ELECTROLYTIC_SEPARATOR.getStack(1),
			"IRI", "ECE", "IRI", 'I', "ingotIron", 'R', Items.redstone, 'E', MekanismItems.EnrichedAlloy, 'C', MekanismItems.ElectrolyticCore
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.ElectrolyticCore),
			"EPE", "IEG", "EPE", 'E', MekanismItems.EnrichedAlloy, 'P', "dustOsmium", 'I', "dustIron", 'G', "dustGold"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.CardboardBox),
			"SS", "SS", 'S', "pulpWood"
		));
		recipes.add(new MekanismRecipe(new ItemStack(Items.paper, 6),
			"SSS", 'S', MekanismItems.Sawdust
		));
		recipes.add(new MekanismRecipe(PRECISION_SAWMILL.getStack(1),
			"ICI", "ASA", "ICI", 'I', "ingotIron", 'C', "circuitBasic", 'A', MekanismItems.EnrichedAlloy, 'S', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.SALINATION_CONTROLLER.getStack(1),
			"CGC", "IBI", "CGC", 'C', "circuitBasic", 'G', "paneGlass", 'I', BasicBlockType.STEEL_CASING.getStack(1), 'B', "blockCopper"
		));
		recipes.add(new MekanismRecipe(BasicBlockType.SALINATION_VALVE.getStack(1),
			"ITI", "CBC", "ITI", 'I', "ingotCopper", 'T', BasicBlockType.DYNAMIC_VALVE.getStack(1), 'C', "circuitBasic", 'B', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(BasicBlockType.SALINATION_BLOCK.getStack(1),
			"CCC", "CTC", "CCC", 'C', "ingotCopper", 'T', BasicBlockType.DYNAMIC_TANK.getStack(1)
		));
		recipes.add(new MekanismRecipe(CHEMICAL_DISSOLUTION_CHAMBER.getStack(1),
			"CGC", "EAE", "CGC", 'G', MekanismUtils.getEmptyGasTank(), 'C', "circuitBasic", 'A', MekanismItems.AtomicAlloy, 'E', MekanismItems.EnrichedAlloy
		));
		recipes.add(new MekanismRecipe(CHEMICAL_WASHER.getStack(1),
			"CWC", "EIE", "CGC", 'W', Items.bucket, 'C', "circuitBasic", 'E', MekanismItems.EnrichedAlloy, 'G', MekanismUtils.getEmptyGasTank(), 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(CHEMICAL_CRYSTALLIZER.getStack(1),
			"CGC", "ASA", "CGC", 'G', MekanismUtils.getEmptyGasTank(), 'C', "circuitBasic", 'A', MekanismItems.AtomicAlloy, 'S', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.FrictionBoots),
			"C C", "A A", "T T", 'C', "circuitBasic", 'A', MekanismItems.EnrichedAlloy, 'T', MekanismItems.EnergyTablet.getUnchargedItem()
		));
		recipes.add(new MekanismRecipe(MekanismItems.ArmoredJetpack.getEmptyItem(),
			"D D", "BSB", " J ", 'D', "dustDiamond", 'B', "ingotBronze", 'S', "blockSteel", 'J', MekanismItems.Jetpack.getEmptyItem()
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.FilterCard),
			" A ", "ACA", " A ", 'A', MekanismItems.EnrichedAlloy, 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(MekanismItems.SeismicReader.getUnchargedItem(),
			"SLS", "STS", "SSS", 'S', "ingotSteel", 'L', new ItemStack(Items.dye, 1, 4)
		));
		recipes.add(new MekanismRecipe(SEISMIC_VIBRATOR.getStack(1),
			"TLT", "CIC", "TTT", 'T', "ingotTin", 'L', new ItemStack(Items.dye, 1, 4), 'C', "circuitBasic", 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(PRESSURIZED_REACTION_CHAMBER.getStack(1),
			"TET", "CIC", "GFG", 'S', "ingotSteel", 'E', MekanismItems.EnrichedAlloy, 'C', "circuitBasic", 'G', MekanismUtils.getEmptyGasTank(),
				'I', ENRICHMENT_CHAMBER.getStack(1), 'F', BasicBlockType.DYNAMIC_TANK.getStack(1)
		));
		recipes.add(new MekanismRecipe(PORTABLE_TANK.getStack(1),
			"III", "GCG", "III", 'I', "ingotIron", 'G', "blockGlass", 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(FLUIDIC_PLENISHER.getStack(1),
			"TTT", "CPC", "TTT", 'P', ELECTRIC_PUMP.getStack(1), 'T', "ingotTin", 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(new ItemStack(Blocks.rail, 24),
			"O O", "OSO", "O O", 'O', "ingotOsmium", 'S', "stickWood"
		));
		recipes.add(new MekanismRecipe(MekanismItems.Flamethrower.getEmptyItem(),
			"TTT", "TGS", "BCB", 'T', "ingotTin", 'G', MekanismUtils.getEmptyGasTank(), 'S', Items.flint_and_steel, 'B', "ingotBronze", 'C', "circuitAdvanced"
		));
		
		//Energy Cube recipes
		recipes.add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC),
			"RTR", "iIi", "RTR", 'R', "alloyBasic", 'i', "ingotIron", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'I', BasicBlockType.STEEL_CASING.getStack(1)
		));
		recipes.add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED),
			"ETE", "oBo", "ETE", 'E', "alloyAdvanced", 'o', "ingotOsmium", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'B', MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC)
		));
		recipes.add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE),
			"RTR", "gAg", "RTR", 'R', "alloyElite", 'g', "ingotGold", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'A', MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED)
		));
		recipes.add(new MekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE),
			"ATA", "dEd", "ATA", 'A', "alloyUltimate", 'd', "gemDiamond", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE)
		));
		
		//Circuit recipes
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 1),
			"ECE", 'C', new ItemStack(MekanismItems.ControlCircuit, 1, 0), 'E', "alloyAdvanced"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 2),
			"RCR", 'C', new ItemStack(MekanismItems.ControlCircuit, 1, 1), 'R', "alloyElite"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 3),
			"ACA", 'C', new ItemStack(MekanismItems.ControlCircuit, 1, 2), 'A', "alloyUltimate"
		));

		//Factory recipes
		for(RecipeType type : RecipeType.values())
		{
			recipes.add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, type),
				"RCR", "iOi", "RCR", 'R', "alloyBasic", 'C', "circuitBasic", 'i', "ingotIron", 'O', type.getStack()
			));
			recipes.add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.ADVANCED, type),
				"ECE", "oOo", "ECE", 'E', "alloyAdvanced", 'C', "circuitAdvanced", 'o', "ingotOsmium", 'O', MekanismUtils.getFactory(FactoryTier.BASIC, type)
			));
			recipes.add(new MekanismRecipe(MekanismUtils.getFactory(FactoryTier.ELITE, type),
				"RCR", "gOg", "RCR", 'R', "alloyElite", 'C', "circuitElite", 'g', "ingotGold", 'O', MekanismUtils.getFactory(FactoryTier.ADVANCED, type)
			));
		}
		
		//Add the bin recipe system to the CraftingManager
		recipes.add(new BinRecipe());
		
/*
        //Transmitters
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 0), new Object[] {
			"SRS", 'S', "ingotSteel", 'R', Items.redstone
		));
        recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 1),
            "TTT", "TET", "TTT", 'E', "alloyAdvanced", 'T', new ItemStack(MekanismItems.PartTransmitter, 1, 0)
        ));
        recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 2),
            "TTT", "TRT", "TTT", 'R', "alloyElite", 'T', new ItemStack(MekanismItems.PartTransmitter, 1, 1)
        ));
        recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 3),
            "TTT", "TAT", "TTT", 'A', "alloyUltimate", 'T', new ItemStack(MekanismItems.PartTransmitter, 1, 2)
        ));
        recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 4),
            "SBS", 'S', "ingotSteel", 'B', Items.bucket
        ));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 5),
			"TTT", "TET", "TTT", 'E', "alloyAdvanced", 'T', new ItemStack(MekanismItems.PartTransmitter, 1, 4)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 6),
			"TTT", "TRT", "TTT", 'R', "alloyElite", 'T', new ItemStack(MekanismItems.PartTransmitter, 1, 5)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 7),
			"TTT", "TAT", "TTT", 'A', "alloyUltimate", 'T', new ItemStack(MekanismItems.PartTransmitter, 1, 6)
		));
        recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 8),
            "SGS", 'S', "ingotSteel", 'G', "blockGlass"
        ));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 8, 9),
			"SCS", 'S', "ingotSteel", 'C', "circuitBasic"
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 2, 10),
			"SBS", 'S', "ingotSteel", 'B', Blocks.iron_bars
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.PartTransmitter, 2, 11),
			"RRR", "SBS", "RRR", 'R', Items.redstone, 'S', "ingotSteel", 'B', Blocks.iron_bars
		));
*/

		//Plastic stuff
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 1),
			"PP", "PP", "PP", 'P', new ItemStack(MekanismItems.Polyethene, 1, 0)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 2),
			"PPP", "P P", "PPP", 'P', new ItemStack(MekanismItems.Polyethene, 1, 0)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 3),
			"R", "R", 'R', new ItemStack(MekanismItems.Polyethene, 1, 1)
		));
		recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, 15),
			"SSS", "S S", "SSS", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2)
		));
/*
		recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.GlowPanel, 2, 15),
			"PSP", "S S", "GSG", 'P', "paneGlass", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'G', Items.glowstone_dust
		));
*/
		recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticFence, 3, 15),
			"BSB", "BSB", 'B', new ItemStack(MekanismBlocks.PlasticBlock, 1, 15), 'S', new ItemStack(MekanismItems.Polyethene, 1, 3)
		));

		for(int i = 0; i < EnumColor.DYES.length-1; i++)
		{
			recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i),
				"SSS", "SDS", "SSS", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
/*
			recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.GlowPanel, 2, i),
				"PSP", "SDS", "GSG", 'P', "paneGlass", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'D', "dye" + EnumColor.DYES[i].dyeName, 'G', Items.glowstone_dust
			));
*/
		}

		for(int i = 0; i < EnumColor.DYES.length; i++)
        {
			recipes.add(new ShapelessOreRecipe(new ItemStack(MekanismItems.Balloon, 2, i),
				Items.leather, Items.string, "dye" + EnumColor.DYES[i].dyeName
			));

			for(int j = 0; j < EnumColor.DYES.length; j++)
			{
				recipes.add(new ShapelessOreRecipe(new ItemStack(MekanismItems.Balloon, 1, i),
					new ItemStack(MekanismItems.Balloon, 1, j), "dye" + EnumColor.DYES[i].dyeName
				));

				recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, j), 'D', "dye" + EnumColor.DYES[i].dyeName
				));
				recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.SlickPlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, 16 + j), 'D', "dye" + EnumColor.DYES[i].dyeName
				));
				recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.GlowPlasticBlock, 1, j), 'D', "dye" + EnumColor.DYES[i].dyeName
				));
				recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 1, j), 'D', "dye" + EnumColor.DYES[i].dyeName
				));
/*
				recipes.add(new MekanismRecipe(new ItemStack(MekanismItems.GlowPanel, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismItems.GlowPanel, 1, j), 'D', "dye" + EnumColor.DYES[i].dyeName
				));
*/
			}

			recipes.add(new ShapelessOreRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 3, i),
				new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), "dustGlowstone"
			));
			recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i),
				" P ", "POP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, i), 'O', new ItemStack(MekanismItems.Dust, 1, 2)
			));
			recipes.add(new MekanismRecipe(new ItemStack(MekanismBlocks.RoadPlasticBlock, 3, i),
				"SSS", "PPP", "SSS", 'S', Blocks.sand, 'P', new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, i)
			));
        }
	
		//Furnace Recipes
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismBlocks.OreBlock, 1, 0), new ItemStack(MekanismItems.Ingot, 1, 1), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismBlocks.OreBlock, 1, 1), new ItemStack(MekanismItems.Ingot, 1, 5), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismBlocks.OreBlock, 1, 2), new ItemStack(MekanismItems.Ingot, 1, 6), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 2), new ItemStack(MekanismItems.Ingot, 1, 1), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 0), new ItemStack(Items.iron_ingot), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 1), new ItemStack(Items.gold_ingot), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 5), new ItemStack(MekanismItems.Ingot, 1, 4), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 6), new ItemStack(MekanismItems.Ingot, 1, 5), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 7), new ItemStack(MekanismItems.Ingot, 1, 6), 0.0F);
		
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
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.oak_fence), new ItemStack(Items.stick, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.oak_fence_gate), new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.birch_fence), new ItemStack(Items.stick, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.birch_fence_gate), new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.jungle_fence), new ItemStack(Items.stick, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.jungle_fence_gate), new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.spruce_fence), new ItemStack(Items.stick, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.spruce_fence_gate), new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.dark_oak_fence), new ItemStack(Items.stick, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.dark_oak_fence_gate), new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.acacia_fence), new ItemStack(Items.stick, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.acacia_fence_gate), new ItemStack(Blocks.planks, 2), new ItemStack(Items.stick, 4), 1);
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
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("water", 2), 2 * general.FROM_H2, new GasStack(GasRegistry.getGas("hydrogen"), 2), new GasStack(GasRegistry.getGas("oxygen"), 1));
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("brine", 10), 2 * general.FROM_H2, new GasStack(GasRegistry.getGas("sodium"), 1), new GasStack(GasRegistry.getGas("chlorine"), 1));
		
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

		RecipeHandler.addCentrifugeRecipe(new GasStack(GasRegistry.getGas("hydrogen"), 10), new GasStack(GasRegistry.getGas("deuterium"), 1));
		RecipeHandler.addCentrifugeRecipe(new GasStack(GasRegistry.getGas("deuterium"), 10), new GasStack(GasRegistry.getGas("tritium"), 1));

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
				if(fuel.isItemEqual(COAL_BLOCK.getStack(1)))
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
//		OreDictionary.registerOre("universalCable", new ItemStack(MekanismItems.PartTransmitter, 8, 0));
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
		
		OreDictionary.registerOre("blockOsmium", OSMIUM_BLOCK.getStack(1));
		OreDictionary.registerOre("blockBronze", BRONZE_BLOCK.getStack(1));
		OreDictionary.registerOre("blockRefinedObsidian", REFINED_OBSIDIAN.getStack(1));
		OreDictionary.registerOre("blockCharcoal", COAL_BLOCK.getStack(1));
		OreDictionary.registerOre("blockRefinedGlowstone", REFINED_GLOWSTONE.getStack(1));
		OreDictionary.registerOre("blockSteel", STEEL_BLOCK.getStack(1));
		OreDictionary.registerOre("blockCopper", COPPER_BLOCK.getStack(1));
		OreDictionary.registerOre("blockTin", TIN_BLOCK.getStack(1));
		
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
		Recipe.ENERGIZED_SMELTER.get().clear();
		
		for(Object obj : FurnaceRecipes.instance().getSmeltingList().entrySet())
		{
			Map.Entry<ItemStack, ItemStack> entry = (Map.Entry<ItemStack, ItemStack>)obj;
			SmeltingRecipe recipe = new SmeltingRecipe(new ItemStackInput(entry.getKey()), new ItemStackOutput(entry.getValue()));
			Recipe.ENERGIZED_SMELTER.put(recipe);
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
				if(biome.getSpawnableList(EnumCreatureType.MONSTER) != null && biome.getSpawnableList(EnumCreatureType.MONSTER).size() > 0)
				{
					EntityRegistry.addSpawn(EntityBabySkeleton.class, 40, 1, 3, EnumCreatureType.MONSTER, biome);
				}
			}
		}

		//Load this module
		addRecipes();
		addEntities();
		
		registerOreDict();

//		new MultipartMekanism();

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
		MekanismAPI.addBoxBlacklist(Blocks.oak_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.birch_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.jungle_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.spruce_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.dark_oak_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.acacia_door, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.iron_door, OreDictionary.WILDCARD_VALUE);
//		MekanismAPI.addBoxBlacklist(MultipartProxy.block(), OreDictionary.WILDCARD_VALUE);
		
		BoxBlacklistParser.load();
	}
	
	@SubscribeEvent
	public synchronized void onChunkLoad(ChunkEvent.Load event)
	{
		if(event.getChunk() != null && !event.world.isRemote)
		{
			Map copy = (Map)((HashMap)event.getChunk().getTileEntityMap()).clone();

			for(Object obj : copy.values())
			{
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
				worldTickHandler.addRegenChunk(event.world.provider.getDimensionId(), coordPair);
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
