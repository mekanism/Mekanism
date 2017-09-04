package mekanism.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.OreGas;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.transmitters.DynamicNetwork.ClientTickUpdate;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.DynamicNetwork.TransmittersAddedEvent;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.ClientTickHandler;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.BinTier;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.base.IChunkLoadHandler;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IModule;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.chunkloading.ChunkManager;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.config.TypeConfigManager;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.integration.IMCHandler;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.OreDictManager;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import mekanism.common.recipe.BinRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.recipe.ShapelessMekanismRecipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.TileEntityThermalEvaporationValve;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.transmitters.grid.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.transmitters.grid.FluidNetwork.FluidTransferEvent;
import mekanism.common.transmitters.grid.GasNetwork.GasTransferEvent;
import mekanism.common.util.MekanismUtils;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.GameProfile;

/**
 * Mekanism - a Minecraft mod
 * @author AidanBrady
 *
 */
@Mod(modid = "mekanism", name = "Mekanism", version = "9.3.5", guiFactory = "mekanism.client.gui.ConfigGuiFactory",
		dependencies = 	"after:mcmultipart;" +
						"after:jei;" +
						"after:buildcraft;" +
						"after:buildcraftapi;" +
						"after:ic2;" +
						"after:cofhcore;" +
						"after:computercraft;" +
						"after:galacticraft api;" +
						"after:metallurgycore")
public class Mekanism
{
	/** Mekanism Packet Pipeline */
	public static PacketHandler packetHandler = new PacketHandler();

	/** Mekanism logger instance */
	public static Logger logger = LogManager.getLogger("Mekanism");
	
	/** Mekanism proxy instance */
	@SidedProxy(clientSide = "mekanism.client.ClientProxy", serverSide = "mekanism.common.CommonProxy")
	public static CommonProxy proxy;
	
    /** Mekanism mod instance */
	@Instance("mekanism")
    public static Mekanism instance;
    
    /** Mekanism hooks instance */
    public static MekanismHooks hooks = new MekanismHooks();
    
    /** Mekanism configuration instance */
    public static Configuration configuration;
    
	/** Mekanism version number */
	public static Version versionNumber = new Version(9, 3, 5);
	
	/** MultiblockManagers for various structrures */
	public static MultiblockManager<SynchronizedTankData> tankManager = new MultiblockManager<SynchronizedTankData>("dynamicTank");
	public static MultiblockManager<SynchronizedMatrixData> matrixManager = new MultiblockManager<SynchronizedMatrixData>("inductionMatrix");
	public static MultiblockManager<SynchronizedBoilerData> boilerManager = new MultiblockManager<SynchronizedBoilerData>("thermoelectricBoiler");
	
	/** FrequencyManagers for various networks */
	public static FrequencyManager publicTeleporters = new FrequencyManager(Frequency.class, Frequency.TELEPORTER);
	public static Map<UUID, FrequencyManager> privateTeleporters = new HashMap<UUID, FrequencyManager>();
	
	public static FrequencyManager publicEntangloporters = new FrequencyManager(InventoryFrequency.class, InventoryFrequency.ENTANGLOPORTER);
	public static Map<UUID, FrequencyManager> privateEntangloporters = new HashMap<UUID, FrequencyManager>();
	
	public static FrequencyManager securityFrequencies = new FrequencyManager(SecurityFrequency.class, SecurityFrequency.SECURITY);
	
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
	
	/** The GameProfile used by the dummy Mekanism player */
	public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes()), "[Mekanism]");
	
	public static KeySync keyMap = new KeySync();
	
	public static Set<String> jetpackOn = new HashSet<>();
	public static Set<String> gasmaskOn = new HashSet<>();
	public static Set<String> freeRunnerOn = new HashSet<>();
	public static Set<String> flamethrowerActive = new HashSet<>();
	
	public static Set<Coord4D> activeVibrators = new HashSet<>();
	
	static {		
		MekanismFluids.register();
	}

	/**
	 * Adds all in-game crafting, smelting and machine recipes.
	 */
	public void addRecipes()
	{
		//Storage Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 3),
			"***", "***", "***", Character.valueOf('*'), new ItemStack(Items.COAL, 1, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(Items.COAL, 9, 1),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 3)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 2),
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedObsidian"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 0),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 2)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 4),
			"***", "***", "***", Character.valueOf('*'), "ingotRefinedGlowstone"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 3),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 4)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 0),
			"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotOsmium"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 1),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 0)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 1),
			"***", "***", "***", Character.valueOf('*'), "ingotBronze"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 2),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 5),
			"***", "***", "***", Character.valueOf('*'), "ingotSteel"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 4),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 5)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 12),
			"***", "***", "***", Character.valueOf('*'), "ingotCopper"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 5),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 12)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 13),
			"***", "***", "***", Character.valueOf('*'), "ingotTin"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Ingot, 9, 6),
			"*", Character.valueOf('*'), new ItemStack(MekanismBlocks.BasicBlock, 1, 13)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.SaltBlock),
			"**", "**", Character.valueOf('*'), MekanismItems.Salt
		));
		
		//Base Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.ObsidianTNT, 1),
			"***", "XXX", "***", Character.valueOf('*'), Blocks.OBSIDIAN, Character.valueOf('X'), Blocks.TNT
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.ElectricBow.getUnchargedItem(),
			" AB", "E B", " AB", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('B'), Items.STRING, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.EnergyTablet.getUnchargedItem(),
			"RCR", "ECE", "RCR", Character.valueOf('C'), "ingotGold", Character.valueOf('R'), "dustRedstone", Character.valueOf('E'), MekanismItems.EnrichedAlloy
		));
		MachineType.ENRICHMENT_CHAMBER.addRecipe(new ShapedMekanismRecipe(MachineType.ENRICHMENT_CHAMBER.getStack(),
			"RCR", "iIi", "RCR", Character.valueOf('i'), "ingotIron", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('R'), "alloyBasic", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		MachineType.OSMIUM_COMPRESSOR.addRecipe(new ShapedMekanismRecipe(MachineType.OSMIUM_COMPRESSOR.getStack(),
			"ECE", "BIB", "ECE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED), Character.valueOf('B'), Items.BUCKET, Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		MachineType.COMBINER.addRecipe(new ShapedMekanismRecipe(MachineType.COMBINER.getStack(),
			"RCR", "SIS", "RCR", Character.valueOf('S'), "cobblestone", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE), Character.valueOf('R'), "alloyElite", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		MachineType.CRUSHER.addRecipe(new ShapedMekanismRecipe(MachineType.CRUSHER.getStack(),
			"RCR", "LIL", "RCR", Character.valueOf('R'), "dustRedstone", Character.valueOf('L'), Items.LAVA_BUCKET, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.SpeedUpgrade),
			" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dustOsmium"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.EnergyUpgrade),
			" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dustGold"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.GasUpgrade),
			" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dustIron"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.FilterUpgrade),
			" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dustTin"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.MufflingUpgrade),
			" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dustSteel"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.AnchorUpgrade),
				" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dustDiamond"
			));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.AtomicDisassembler.getUnchargedItem(),
			"AEA", "ACA", " O ", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), MekanismItems.AtomicAlloy, Character.valueOf('O'), "ingotRefinedObsidian"
		));
		MachineType.METALLURGIC_INFUSER.addRecipe(new ShapedMekanismRecipe(MachineType.METALLURGIC_INFUSER.getStack(),
			"IFI", "ROR", "IFI", Character.valueOf('I'), "ingotIron", Character.valueOf('F'), Blocks.FURNACE, Character.valueOf('R'), "dustRedstone", Character.valueOf('O'), "ingotOsmium"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.TeleportationCore),
			"LAL", "GDG", "LAL", Character.valueOf('L'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('G'), "ingotGold", Character.valueOf('D'), Items.DIAMOND
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.PortableTeleporter.getUnchargedItem(),
			" E ", "CTC", " E ", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('T'), MekanismItems.TeleportationCore
		));
		MachineType.TELEPORTER.addRecipe(new ShapedMekanismRecipe(MachineType.TELEPORTER.getStack(),
			"COC", "OTO", "COC", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('O'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('T'), MekanismItems.TeleportationCore
		));
		MachineType.PURIFICATION_CHAMBER.addRecipe(new ShapedMekanismRecipe(MachineType.PURIFICATION_CHAMBER.getStack(),
			"ECE", "ORO", "ECE", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED), Character.valueOf('E'), "alloyAdvanced", Character.valueOf('O'), "ingotOsmium", Character.valueOf('R'), MachineType.ENRICHMENT_CHAMBER.getStack()
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.Configurator.getUnchargedItem(),
			" L ", "AEA", " S ", Character.valueOf('L'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('S'), Items.STICK
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 9, 7),
			"OOO", "OGO", "OOO", Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('G'), "ingotRefinedGlowstone"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 8), new Object[] {
			"SGS", "GPG", "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('P'), "ingotOsmium", Character.valueOf('G'), "blockGlass"
		}));
		MachineType.ENERGIZED_SMELTER.addRecipe(new ShapedMekanismRecipe(MachineType.ENERGIZED_SMELTER.getStack(),
			"RCR", "GIG", "RCR", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('R'), "alloyBasic", Character.valueOf('G'), "blockGlass", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		MachineType.ELECTRIC_PUMP.addRecipe(new ShapedMekanismRecipe(MachineType.ELECTRIC_PUMP.getStack(),
			" B ", "ECE", "OOO", Character.valueOf('B'), Items.BUCKET, Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('O'), "ingotOsmium"
		));
		MachineType.PERSONAL_CHEST.addRecipe(new ShapedMekanismRecipe(MachineType.PERSONAL_CHEST.getStack(),
			"SGS", "CcC", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), "chestWood", Character.valueOf('c'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 4, 9),
			" I ", "IBI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('B'), Items.BUCKET
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 4, 10),
			" I ", "IGI", " I ", Character.valueOf('I'), "ingotSteel", Character.valueOf('G'), "blockGlass"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 2, 11),
			" I ", "ICI", " I ", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		MachineType.CHARGEPAD.addRecipe(new ShapedMekanismRecipe(MachineType.CHARGEPAD.getStack(),
			"PPP", "SES", Character.valueOf('P'), Blocks.STONE_PRESSURE_PLATE, Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.Robit.getUnchargedItem(),
			" S ", "ECE", "OIO", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), MekanismItems.AtomicAlloy, Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('I'), MachineType.PERSONAL_CHEST.getStack()
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.NetworkReader.getUnchargedItem(),
			" G ", "AEA", " I ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('I'), "ingotSteel"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.WalkieTalkie),
			"  O", "SCS", " S ", Character.valueOf('O'), "ingotOsmium", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		MachineType.LOGISTICAL_SORTER.addRecipe(new ShapedMekanismRecipe(MachineType.LOGISTICAL_SORTER.getStack(),
			"IPI", "ICI", "III", Character.valueOf('I'), "ingotIron", Character.valueOf('P'), Blocks.PISTON, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		MachineType.DIGITAL_MINER.addRecipe(new ShapedMekanismRecipe(MachineType.DIGITAL_MINER.getStack(),
			"ACA", "SES", "TIT", Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('S'), MachineType.LOGISTICAL_SORTER.getStack(), Character.valueOf('E'), MekanismItems.Robit.getUnchargedItem(),
			Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('T'), MekanismItems.TeleportationCore
		));
		MachineType.ROTARY_CONDENSENTRATOR.addRecipe(new ShapedMekanismRecipe(MachineType.ROTARY_CONDENSENTRATOR.getStack(),
			"GCG", "tEI", "GCG", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('t'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(),
			Character.valueOf('T'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9), Character.valueOf('I'), MekanismUtils.getEmptyFluidTank(FluidTankTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.Jetpack.getEmptyItem(),
			"SCS", "TGT", " T ", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('T'), "ingotTin", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Dictionary),
			"C", "B", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('B'), Items.BOOK
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.GasMask),
			" S ", "GCG", "S S", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.ScubaTank.getEmptyItem(),
			" C ", "ATA", "SSS", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('S'), "ingotSteel"
		));
		MachineType.CHEMICAL_OXIDIZER.addRecipe(new ShapedMekanismRecipe(MachineType.CHEMICAL_OXIDIZER.getStack(),
			"ACA", "ERG", "ACA", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('R'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9), Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), Character.valueOf('E'), MachineType.PERSONAL_CHEST.getStack(), Character.valueOf('A'), MekanismItems.EnrichedAlloy
		));
		MachineType.CHEMICAL_INFUSER.addRecipe(new ShapedMekanismRecipe(MachineType.CHEMICAL_INFUSER.getStack(),
			"ACA", "GRG", "ACA", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('R'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9), Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), Character.valueOf('A'), MekanismItems.EnrichedAlloy
		));
		MachineType.CHEMICAL_INJECTION_CHAMBER.addRecipe(new ShapedMekanismRecipe(MachineType.CHEMICAL_INJECTION_CHAMBER.getStack(),
			"RCR", "GPG", "RCR", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE), Character.valueOf('R'), "alloyElite", Character.valueOf('G'), "ingotGold", Character.valueOf('P'), MachineType.PURIFICATION_CHAMBER.getStack()
		));
		MachineType.ELECTROLYTIC_SEPARATOR.addRecipe(new ShapedMekanismRecipe(MachineType.ELECTROLYTIC_SEPARATOR.getStack(),
			"IRI", "ECE", "IRI", Character.valueOf('I'), "ingotIron", Character.valueOf('R'), "dustRedstone", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), MekanismItems.ElectrolyticCore
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.ElectrolyticCore),
			"EPE", "IEG", "EPE", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('P'), "dustOsmium", Character.valueOf('I'), "dustIron", Character.valueOf('G'), "dustGold"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.CardboardBox),
			"SS", "SS", Character.valueOf('S'), "pulpWood"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(Items.PAPER, 6),
			"SSS", Character.valueOf('S'), MekanismItems.Sawdust
		));
		MachineType.PRECISION_SAWMILL.addRecipe(new ShapedMekanismRecipe(MachineType.PRECISION_SAWMILL.getStack(),
			"ICI", "ASA", "ICI", Character.valueOf('I'), "ingotIron", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('S'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 14),
			"CGC", "IBI", "III", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED), Character.valueOf('G'), "paneGlass", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock2, 1, 0), Character.valueOf('B'), Items.BUCKET
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 15),
			" I ", "ICI", " I ", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock2, 1, 0), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 4, 0),
			" S ", "SCS", " S ", Character.valueOf('C'), "ingotCopper", Character.valueOf('S'), "ingotSteel"
		));
		MachineType.CHEMICAL_DISSOLUTION_CHAMBER.addRecipe(new ShapedMekanismRecipe(MachineType.CHEMICAL_DISSOLUTION_CHAMBER.getStack(),
			"CGC", "EAE", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('E'), MekanismItems.EnrichedAlloy
		));
		MachineType.CHEMICAL_WASHER.addRecipe(new ShapedMekanismRecipe(MachineType.CHEMICAL_WASHER.getStack(),
			"CWC", "EIE", "CGC", Character.valueOf('W'), Items.BUCKET, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		MachineType.CHEMICAL_CRYSTALLIZER.addRecipe(new ShapedMekanismRecipe(MachineType.CHEMICAL_CRYSTALLIZER.getStack(),
			"CGC", "ASA", "CGC", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('A'), MekanismItems.AtomicAlloy, Character.valueOf('S'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.FreeRunners.getUnchargedItem(),
			"C C", "A A", "T T", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem()
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.ArmoredJetpack.getEmptyItem(),
			"D D", "BSB", " J ", Character.valueOf('D'), "dustDiamond", Character.valueOf('B'), "ingotBronze", Character.valueOf('S'), "blockSteel", Character.valueOf('J'), MekanismItems.Jetpack.getEmptyItem()
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.ConfigurationCard),
			" A ", "ACA", " A ", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.SeismicReader.getUnchargedItem(),
			"SLS", "STS", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('L'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem()
		));
		MachineType.SEISMIC_VIBRATOR.addRecipe(new ShapedMekanismRecipe(MachineType.SEISMIC_VIBRATOR.getStack(),
			"TLT", "CIC", "TTT", Character.valueOf('T'), "ingotTin", Character.valueOf('L'), new ItemStack(Items.DYE, 1, 4), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		MachineType.PRESSURIZED_REACTION_CHAMBER.addRecipe(new ShapedMekanismRecipe(MachineType.PRESSURIZED_REACTION_CHAMBER.getStack(),
			"TET", "CIC", "GFG", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC),
			Character.valueOf('I'), MachineType.ENRICHMENT_CHAMBER.getStack(), Character.valueOf('F'), new ItemStack(MekanismBlocks.BasicBlock, 1, 9)
		));
		MachineType.FLUIDIC_PLENISHER.addRecipe(new ShapedMekanismRecipe(MachineType.FLUIDIC_PLENISHER.getStack(),
			"TTT", "CPC", "TTT", Character.valueOf('P'), MachineType.ELECTRIC_PUMP.getStack(), Character.valueOf('T'), "ingotTin", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(Blocks.RAIL, 24),
			"O O", "OSO", "O O", Character.valueOf('O'), "ingotOsmium", Character.valueOf('S'), "stickWood"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.Flamethrower.getEmptyItem(),
			"TTT", "TGS", "BCB", Character.valueOf('T'), "ingotTin", Character.valueOf('G'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), Character.valueOf('S'), Items.FLINT_AND_STEEL, Character.valueOf('B'), "ingotBronze", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.GaugeDropper.getEmptyItem(),
			" O ", "G G", "GGG", Character.valueOf('O'), "ingotOsmium", Character.valueOf('G'), "paneGlass"
		));
		MachineType.SOLAR_NEUTRON_ACTIVATOR.addRecipe(new ShapedMekanismRecipe(MachineType.SOLAR_NEUTRON_ACTIVATOR.getStack(),
			"APA", "CSC", "BBB", Character.valueOf('A'), "alloyElite", Character.valueOf('S'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('P'), new ItemStack(MekanismItems.Polyethene, 1, 2), Character.valueOf('B'), "ingotBronze", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 4, 1),
			" S ", "SES", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 2, 2),
			" I ", "ICI", " I ", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock2, 1, 1), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 0), new Object[] {
			"RCR", "iWi", "RCR", Character.valueOf('R'), "alloyBasic", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('i'), "ingotIron", Character.valueOf('W'), "plankWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 1), new Object[] {
			"ECE", "oWo", "ECE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED), Character.valueOf('o'), "ingotOsmium", Character.valueOf('W'), "plankWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 2), new Object[] {
			"RCR", "gWg", "RCR", Character.valueOf('R'), "alloyElite", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE), Character.valueOf('g'), "ingotGold", Character.valueOf('W'), "plankWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 3), new Object[] {
			"RCR", "dWd", "RCR", Character.valueOf('R'), "alloyUltimate", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), Character.valueOf('d'), "gemDiamond", Character.valueOf('W'), "plankWood"
		}));
		MachineType.OREDICTIONIFICATOR.addRecipe(new ShapedMekanismRecipe(MachineType.OREDICTIONIFICATOR.getStack(),
			"SGS", "CBC", "SWS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "paneGlass", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('B'), MekanismItems.Dictionary, Character.valueOf('W'), "chestWood"
		));
		MachineType.LASER.addRecipe(new ShapedMekanismRecipe(MachineType.LASER.getStack(),
			"RE ", "RCD", "RE ", Character.valueOf('R'), "alloyElite", Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('C'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('D'), "gemDiamond"
		));
		MachineType.LASER_AMPLIFIER.addRecipe(new ShapedMekanismRecipe(MachineType.LASER_AMPLIFIER.getStack(),
			"SSS", "SED", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), Character.valueOf('D'), "gemDiamond"
		));
		MachineType.LASER_TRACTOR_BEAM.addRecipe(new ShapedMekanismRecipe(MachineType.LASER_TRACTOR_BEAM.getStack(),
			"C", "F", Character.valueOf('C'), MachineType.PERSONAL_CHEST.getStack(), Character.valueOf('F'), MachineType.LASER_AMPLIFIER.getStack()
		));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 1, 6),
			"SFS", "FAF", "SFS", Character.valueOf('S'), "ingotSteel", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('F'), Blocks.IRON_BARS
		));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 4, 7),
			" S ", "SIS", " S ", Character.valueOf('S'), "ingotSteel", Character.valueOf('I'), "ingotIron"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 2, 8),
			" I ", "ICI", " I ", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock2, 1, 7), Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 1, 5),
			"ACA", "CIC", "ACA", Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('C'), "ingotCopper", Character.valueOf('A'), "alloyBasic"
		));
		MachineType.RESISTIVE_HEATER.addRecipe(new ShapedMekanismRecipe(MachineType.RESISTIVE_HEATER.getStack(), new Object[] {
			"CRC", "RHR", "CEC", Character.valueOf('C'), "ingotTin", Character.valueOf('R'), "dustRedstone", Character.valueOf('H'), new ItemStack(MekanismBlocks.BasicBlock2, 1, 5), Character.valueOf('E'), MekanismItems.EnergyTablet.getUnchargedItem()
		}));
		MachineType.QUANTUM_ENTANGLOPORTER.addRecipe(new ShapedMekanismRecipe(MachineType.QUANTUM_ENTANGLOPORTER.getStack(), new Object[] {
			"OCO", "ATA", "OCO", Character.valueOf('O'), "ingotRefinedObsidian", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismItems.TeleportationCore
		}));
		MachineType.FORMULAIC_ASSEMBLICATOR.addRecipe(new ShapedMekanismRecipe(MachineType.FORMULAIC_ASSEMBLICATOR.getStack(), new Object[] {
			"STS", "BIB", "SCS", Character.valueOf('S'), "ingotSteel", Character.valueOf('T'), Blocks.CRAFTING_TABLE, Character.valueOf('B'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('C'), "chestWood"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapelessMekanismRecipe(new ItemStack(MekanismItems.CraftingFormula), new Object[] {
			Items.PAPER, MekanismUtils.getControlCircuit(BaseTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 1, 9), new Object[] {
			"SGS", "CIC", "STS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 
			Character.valueOf('T'), MekanismItems.TeleportationCore
		}));
		MachineType.FUELWOOD_HEATER.addRecipe(new ShapedMekanismRecipe(MachineType.FUELWOOD_HEATER.getStack(), new Object[] {
			"SCS", "FHF", "SSS", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('F'), Blocks.FURNACE, Character.valueOf('H'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		}));
		
		//Energy Cube recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC),
			"RTR", "iIi", "RTR", Character.valueOf('R'), "alloyBasic", Character.valueOf('i'), "ingotIron", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('I'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED),
			"ETE", "oBo", "ETE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('o'), "ingotOsmium", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE),
			"RTR", "gAg", "RTR", Character.valueOf('R'), "alloyElite", Character.valueOf('g'), "ingotGold", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('A'), MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE),
			"ATA", "dEd", "ATA", Character.valueOf('A'), "alloyUltimate", Character.valueOf('d'), "gemDiamond", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE)
		));
		
		//Gas Tank Recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), new Object[] {
			"APA", "P P", "APA", Character.valueOf('P'), "ingotOsmium", Character.valueOf('A'), "alloyBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.ADVANCED), new Object[] {
			"APA", "PTP", "APA", Character.valueOf('P'), "ingotOsmium", Character.valueOf('A'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getEmptyGasTank(GasTankTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.ELITE), new Object[] {
			"APA", "PTP", "APA", Character.valueOf('P'), "ingotOsmium", Character.valueOf('A'), "alloyElite", Character.valueOf('T'), MekanismUtils.getEmptyGasTank(GasTankTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.ULTIMATE), new Object[] {
			"APA", "PTP", "APA", Character.valueOf('P'), "ingotOsmium", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getEmptyGasTank(GasTankTier.ELITE)
		}));
		
		//Fluid Tank Recipes
		MachineType.FLUID_TANK.addRecipe(new ShapedMekanismRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.BASIC), new Object[] {
			"AIA", "I I", "AIA", Character.valueOf('I'), "ingotIron", Character.valueOf('A'), "alloyBasic"
		}));
		MachineType.FLUID_TANK.addRecipe(new ShapedMekanismRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.ADVANCED), new Object[] {
			"AIA", "ITI", "AIA", Character.valueOf('I'), "ingotIron", Character.valueOf('A'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getEmptyFluidTank(FluidTankTier.BASIC)
		}));
		MachineType.FLUID_TANK.addRecipe(new ShapedMekanismRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.ELITE), new Object[] {
			"AIA", "ITI", "AIA", Character.valueOf('I'), "ingotIron", Character.valueOf('A'), "alloyElite", Character.valueOf('T'), MekanismUtils.getEmptyFluidTank(FluidTankTier.ADVANCED)
		}));
		MachineType.FLUID_TANK.addRecipe(new ShapedMekanismRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.ULTIMATE), new Object[] {
			"AIA", "ITI", "AIA", Character.valueOf('I'), "ingotIron", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getEmptyFluidTank(FluidTankTier.ELITE)
		}));
		
		//Bin recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getBin(BinTier.BASIC), new Object[] {
			"SCS", "A A", "SSS", Character.valueOf('S'), "cobblestone", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('A'), "alloyBasic"
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getBin(BinTier.ADVANCED), new Object[] {
			"SCS", "ABA", "SSS", Character.valueOf('S'), "cobblestone", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED), Character.valueOf('A'), "alloyAdvanced", Character.valueOf('B'), MekanismUtils.getBin(BinTier.BASIC)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getBin(BinTier.ELITE), new Object[] {
			"SCS", "ABA", "SSS", Character.valueOf('S'), "cobblestone", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE), Character.valueOf('A'), "alloyElite", Character.valueOf('B'), MekanismUtils.getBin(BinTier.ADVANCED)
		}));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getBin(BinTier.ULTIMATE), new Object[] {
			"SCS", "ABA", "SSS", Character.valueOf('S'), "cobblestone", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), Character.valueOf('A'), "alloyUltimate", Character.valueOf('B'), MekanismUtils.getBin(BinTier.ELITE)
		}));
		
		//Induction Cell recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionCell(InductionCellTier.BASIC),
			"LTL", "TET", "LTL", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), Character.valueOf('L'), "dustLithium"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionCell(InductionCellTier.ADVANCED),
			"TCT", "CEC", "TCT", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), Character.valueOf('C'), MekanismUtils.getInductionCell(InductionCellTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionCell(InductionCellTier.ELITE),
			"TCT", "CEC", "TCT", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), Character.valueOf('C'), MekanismUtils.getInductionCell(InductionCellTier.ADVANCED)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionCell(InductionCellTier.ULTIMATE),
			"TCT", "CEC", "TCT", Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), Character.valueOf('C'), MekanismUtils.getInductionCell(InductionCellTier.ELITE)
		));
		
		//Induction Provider recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.BASIC),
			"LCL", "CEC", "LCL", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), Character.valueOf('L'), "dustLithium"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.ADVANCED),
			"CPC", "PEP", "CPC", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), Character.valueOf('P'), MekanismUtils.getInductionProvider(InductionProviderTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.ELITE),
			"CPC", "PEP", "CPC", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), Character.valueOf('P'), MekanismUtils.getInductionProvider(InductionProviderTier.ADVANCED)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.ULTIMATE),
			"CPC", "PEP", "CPC", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), Character.valueOf('E'), MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), Character.valueOf('P'), MekanismUtils.getInductionProvider(InductionProviderTier.ELITE)
		));
		
		//Circuit recipes
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 1),
			"ECE", Character.valueOf('C'), new ItemStack(MekanismItems.ControlCircuit, 1, 0), Character.valueOf('E'), "alloyAdvanced"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 2),
			"RCR", Character.valueOf('C'), new ItemStack(MekanismItems.ControlCircuit, 1, 1), Character.valueOf('R'), "alloyElite"
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 3),
			"ACA", Character.valueOf('C'), new ItemStack(MekanismItems.ControlCircuit, 1, 2), Character.valueOf('A'), "alloyUltimate"
		));

		//Factory recipes
		for(RecipeType type : RecipeType.values())
		{
			MachineType.BASIC_FACTORY.addRecipe(new ShapedMekanismRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, type),
				"RCR", "iOi", "RCR", Character.valueOf('R'), "alloyBasic", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC), Character.valueOf('i'), "ingotIron", Character.valueOf('O'), type.getStack()
			));
			MachineType.ADVANCED_FACTORY.addRecipe(new ShapedMekanismRecipe(MekanismUtils.getFactory(FactoryTier.ADVANCED, type),
				"ECE", "oOo", "ECE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ADVANCED), Character.valueOf('o'), "ingotOsmium", Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.BASIC, type)
			));
			MachineType.ELITE_FACTORY.addRecipe(new ShapedMekanismRecipe(MekanismUtils.getFactory(FactoryTier.ELITE, type),
				"RCR", "gOg", "RCR", Character.valueOf('R'), "alloyElite", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.ELITE), Character.valueOf('g'), "ingotGold", Character.valueOf('O'), MekanismUtils.getFactory(FactoryTier.ADVANCED, type)
			));
		}
		
		//Add the bin recipe system to the CraftingManager
		CraftingManager.getInstance().getRecipeList().add(new BinRecipe());
		
        //Transmitters
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.BASIC, 8),
			"SRS", Character.valueOf('S'), "ingotSteel", Character.valueOf('R'), "dustRedstone"
		));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ADVANCED, 8),
            "TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.BASIC, 1)
        ));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ELITE, 8),
            "TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ADVANCED, 1)
        ));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ULTIMATE, 8),
            "TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ELITE, 1)
        ));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.BASIC, 8),
            "SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Items.BUCKET
        ));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ADVANCED, 8),
			"TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.BASIC, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ELITE, 8),
			"TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ADVANCED, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ULTIMATE, 8),
			"TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ELITE, 1)
		));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.BASIC, 8),
            "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('G'), "blockGlass"
        ));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ADVANCED, 8),
			"TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.BASIC, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ELITE, 8),
			"TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ADVANCED, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ULTIMATE, 8),
			"TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ELITE, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.BASIC, 8),
			"SCS", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), MekanismUtils.getControlCircuit(BaseTier.BASIC)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ADVANCED, 8),
			"TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.BASIC, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ELITE, 8),
			"TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ADVANCED, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ULTIMATE, 8),
			"TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ELITE, 1)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.RESTRICTIVE_TRANSPORTER, BaseTier.BASIC, 2),
			"SBS", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Blocks.IRON_BARS
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.DIVERSION_TRANSPORTER, BaseTier.BASIC, 2),
			"RRR", "SBS", "RRR", Character.valueOf('R'), "dustRedstone", Character.valueOf('S'), "ingotSteel", Character.valueOf('B'), Blocks.IRON_BARS
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.BASIC, 8),
			"SCS", Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), "ingotCopper"
		));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ADVANCED, 8),
            "TTT", "TET", "TTT", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.BASIC, 1)
        ));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ELITE, 8),
            "TTT", "TRT", "TTT", Character.valueOf('R'), "alloyElite", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ADVANCED, 1)
        ));
        CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ULTIMATE, 8),
            "TTT", "TAT", "TTT", Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ELITE, 1)
        ));

		//Plastic stuff
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 1),
			"PP", "PP", Character.valueOf('P'), new ItemStack(MekanismItems.Polyethene, 1, 0)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 2),
			"PPP", "P P", "PPP", Character.valueOf('P'), new ItemStack(MekanismItems.Polyethene, 1, 0)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.Polyethene, 1, 3),
			"R", "R", Character.valueOf('R'), new ItemStack(MekanismItems.Polyethene, 1, 1)
		));

		//Creation of plastic block and glow panel with colors
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, 15),
				"SSS", "S S", "SSS", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2)
		));
		CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.GlowPanel, 2, 15),
				"PSP", "S S", "GSG", 'P', "paneGlass", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'G', "dustGlowstone"
		));
		for (int i = 0; i < EnumColor.DYES.length - 1; i++) {
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i),
					"SSS", "SDS", "SSS", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.GlowPanel, 2, i),
					"PSP", "SDS", "GSG", 'P', "paneGlass", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'D', "dye" + EnumColor.DYES[i].dyeName, 'G', "dustGlowstone"
			));
		}

		for (int i = 0; i < EnumColor.DYES.length; i++) {
            /*
             * Balloon
             * Plastic block
             * Slick
             * Glow
             * Reinforced
             * Road
             * Panel
             * Fence
             */

			//Creation
			CraftingManager.getInstance().getRecipeList().add(new ShapelessMekanismRecipe(new ItemStack(MekanismItems.Balloon, 2, i),
					Items.LEATHER, Items.STRING, "dye" + EnumColor.DYES[i].dyeName
			));
			//Plastic block creation is separate
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.SlickPlasticBlock, 4, i),
					" P ", "PSP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, i), 'S', "slimeball"
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapelessMekanismRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 3, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(Items.GLOWSTONE_DUST)));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i),
					" P ", "POP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, i), 'O', "dustOsmium"
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.RoadPlasticBlock, 3, i),
					"SSS", "PPP", "SSS", 'S', Blocks.SAND, 'P', new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, i)
			));
			//Panel creation is separate
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.PlasticFence, 3, i),
					"BSB", "BSB", 'B', new ItemStack(MekanismBlocks.PlasticBlock, 1, i), 'S', new ItemStack(MekanismItems.Polyethene, 1, 3)
			));

			//Recolor
			CraftingManager.getInstance().getRecipeList().add(new ShapelessMekanismRecipe(new ItemStack(MekanismItems.Balloon, 1, i),
					new ItemStack(MekanismItems.Balloon, 1, OreDictionary.WILDCARD_VALUE), "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.SlickPlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.GlowPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.RoadPlasticBlock, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.RoadPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.GlowPanel, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.GlowPanel, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
			CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.PlasticFence, 4, i),
					" P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticFence, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName
			));
		}
	
		//Furnace Recipes
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismBlocks.OreBlock, 1, 0), new ItemStack(MekanismItems.Ingot, 1, 1), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismBlocks.OreBlock, 1, 1), new ItemStack(MekanismItems.Ingot, 1, 5), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismBlocks.OreBlock, 1, 2), new ItemStack(MekanismItems.Ingot, 1, 6), 1.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.OSMIUM.ordinal()), new ItemStack(MekanismItems.Ingot, 1, 1), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.IRON.ordinal()), new ItemStack(Items.IRON_INGOT), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.GOLD.ordinal()), new ItemStack(Items.GOLD_INGOT), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.OtherDust, 1, 1), new ItemStack(MekanismItems.Ingot, 1, 4), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.COPPER.ordinal()), new ItemStack(MekanismItems.Ingot, 1, 5), 0.0F);
		FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.TIN.ordinal()), new ItemStack(MekanismItems.Ingot, 1, 6), 0.0F);
		
		//Enrichment Chamber Recipes
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.REDSTONE_ORE), new ItemStack(Items.REDSTONE, 12));
        RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.OBSIDIAN), new ItemStack(MekanismItems.OtherDust, 2, 6));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.COAL, 1, 0), new ItemStack(MekanismItems.CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.COAL, 1, 1), new ItemStack(MekanismItems.CompressedCarbon));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.REDSTONE), new ItemStack(MekanismItems.CompressedRedstone));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.LAPIS_ORE), new ItemStack(Items.DYE, 12, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.COAL_ORE), new ItemStack(Items.COAL, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.DIAMOND_ORE), new ItemStack(Items.DIAMOND, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.EMERALD_ORE), new ItemStack(Items.EMERALD, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE), new ItemStack(Blocks.COBBLESTONE));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONE), new ItemStack(Blocks.STONEBRICK, 1, 2));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.SAND), new ItemStack(Blocks.GRAVEL));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.COBBLESTONE));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.GUNPOWDER), new ItemStack(Items.FLINT));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONEBRICK, 1, 2), new ItemStack(Blocks.STONEBRICK, 1, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONEBRICK, 1, 0), new ItemStack(Blocks.STONEBRICK, 1, 3));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONEBRICK, 1, 1), new ItemStack(Blocks.STONEBRICK, 1, 0));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.GLOWSTONE), new ItemStack(Items.GLOWSTONE_DUST, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.CLAY), new ItemStack(Items.CLAY_BALL, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(MekanismBlocks.SaltBlock), new ItemStack(MekanismItems.Salt, 4));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.DIAMOND), new ItemStack(MekanismItems.CompressedDiamond));
		RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(MekanismItems.Polyethene, 3, 0), new ItemStack(MekanismItems.Polyethene, 1, 2));
		
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, i));
		}
		
		//Combiner recipes
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.REDSTONE, 16), new ItemStack(Blocks.REDSTONE_ORE));
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.DYE, 16, 4), new ItemStack(Blocks.LAPIS_ORE));
		RecipeHandler.addCombinerRecipe(new ItemStack(Items.FLINT), new ItemStack(Blocks.GRAVEL));
		
		//Osmium Compressor Recipes
		RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(MekanismItems.Ingot, 1, 3));
		
		//Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.DIAMOND), new ItemStack(MekanismItems.OtherDust, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.IRON_INGOT), new ItemStack(MekanismItems.Dust, 1, Resource.IRON.ordinal()));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.GOLD_INGOT), new ItemStack(MekanismItems.Dust, 1, Resource.GOLD.ordinal()));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.SAND));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONE), new ItemStack(Blocks.COBBLESTONE));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONEBRICK, 1, 2), new ItemStack(Blocks.STONE));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONEBRICK, 1, 0), new ItemStack(Blocks.STONEBRICK, 1, 2));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONEBRICK, 1, 3), new ItemStack(Blocks.STONEBRICK, 1, 0));
        RecipeHandler.addCrusherRecipe(new ItemStack(Items.FLINT), new ItemStack(Items.GUNPOWDER));
        RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.SANDSTONE), new ItemStack(Blocks.SAND, 2));
        
        for(int i = 0; i < 16; i++)
        {
        	RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.WOOL, 1, i), new ItemStack(Items.STRING, 4));
        }
        
		//BioFuel Crusher Recipes
		RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.TALLGRASS), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.REEDS), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.WHEAT_SEEDS), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.WHEAT), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.MELON_SEEDS), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.APPLE), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.BREAD), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.POTATO), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.CARROT), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.ROTTEN_FLESH), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.MELON), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.PUMPKIN), new ItemStack(MekanismItems.BioFuel, 6));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.BAKED_POTATO), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.POISONOUS_POTATO), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.BEETROOT), new ItemStack(MekanismItems.BioFuel, 4));
		RecipeHandler.addCrusherRecipe(new ItemStack(Items.BEETROOT_SEEDS), new ItemStack(MekanismItems.BioFuel, 2));
		RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CACTUS), new ItemStack(MekanismItems.BioFuel, 2));

		//Purification Chamber Recipes
        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Items.FLINT));
        
        //Chemical Injection Chamber Recipes
        RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.DIRT), MekanismFluids.Water, new ItemStack(Blocks.CLAY));
        RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.HARDENED_CLAY), MekanismFluids.Water, new ItemStack(Blocks.CLAY));
        RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Items.BRICK), MekanismFluids.Water, new ItemStack(Items.CLAY_BALL));
        RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Items.GUNPOWDER), MekanismFluids.HydrogenChloride, new ItemStack(MekanismItems.OtherDust, 1, 3));
		
		//Precision Sawmill Recipes
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.LADDER, 3), new ItemStack(Items.STICK, 7));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.TORCH, 4), new ItemStack(Items.STICK), new ItemStack(Items.COAL), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.CHEST), new ItemStack(Blocks.PLANKS, 8));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.TRAPDOOR), new ItemStack(Blocks.PLANKS, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BOAT), new ItemStack(Blocks.PLANKS, 5));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BED), new ItemStack(Blocks.PLANKS, 3), new ItemStack(Blocks.WOOL, 3), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.OAK_DOOR), new ItemStack(Blocks.PLANKS, 2, 0));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.SPRUCE_DOOR), new ItemStack(Blocks.PLANKS, 2, 1));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BIRCH_DOOR), new ItemStack(Blocks.PLANKS, 2, 2));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.JUNGLE_DOOR), new ItemStack(Blocks.PLANKS, 2, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.ACACIA_DOOR), new ItemStack(Blocks.PLANKS, 2, 4));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.DARK_OAK_DOOR), new ItemStack(Blocks.PLANKS, 2, 5));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUKEBOX), new ItemStack(Blocks.PLANKS, 8), new ItemStack(Items.DIAMOND), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BOOKSHELF), new ItemStack(Blocks.PLANKS, 6), new ItemStack(Items.BOOK, 3), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE), new ItemStack(Blocks.PLANKS, 2));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_FENCE), new ItemStack(Items.STICK, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_FENCE), new ItemStack(Items.STICK, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_FENCE), new ItemStack(Items.STICK, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_FENCE), new ItemStack(Items.STICK, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_FENCE), new ItemStack(Items.STICK, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_FENCE), new ItemStack(Items.STICK, 3));
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack(Blocks.PLANKS, 2, 0), new ItemStack(Items.STICK, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_FENCE_GATE), new ItemStack(Blocks.PLANKS, 2, 1), new ItemStack(Items.STICK, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.PLANKS, 2, 2), new ItemStack(Items.STICK, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_FENCE_GATE), new ItemStack(Blocks.PLANKS, 2, 3), new ItemStack(Items.STICK, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_FENCE_GATE), new ItemStack(Blocks.PLANKS, 2, 4), new ItemStack(Items.STICK, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_FENCE_GATE), new ItemStack(Blocks.PLANKS, 2, 5), new ItemStack(Items.STICK, 4), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.NOTEBLOCK), new ItemStack(Blocks.PLANKS, 8), new ItemStack(Items.REDSTONE), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.REDSTONE_TORCH), new ItemStack(Items.STICK), new ItemStack(Items.REDSTONE), 1);
		RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.PLANKS, 4));
		
        //Metallurgic Infuser Recipes
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, new ItemStack(Items.IRON_INGOT), new ItemStack(MekanismItems.EnrichedIron));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, new ItemStack(MekanismItems.EnrichedIron), new ItemStack(MekanismItems.OtherDust, 1, 1));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("REDSTONE"), 10, new ItemStack(Items.IRON_INGOT), new ItemStack(MekanismItems.EnrichedAlloy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("FUNGI"), 10, new ItemStack(Blocks.DIRT), new ItemStack(Blocks.MYCELIUM));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.MOSSY_COBBLESTONE));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.STONEBRICK, 1, 0), new ItemStack(Blocks.STONEBRICK, 1, 1));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.SAND), new ItemStack(Blocks.DIRT));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("BIO"), 10, new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT, 1, 2));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("DIAMOND"), 10, new ItemStack(MekanismItems.EnrichedAlloy), new ItemStack(MekanismItems.ReinforcedAlloy));
        RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("OBSIDIAN"), 10, new ItemStack(MekanismItems.ReinforcedAlloy), new ItemStack(MekanismItems.AtomicAlloy));
        
        //Chemical Infuser Recipes
        RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.Oxygen, 1), new GasStack(MekanismFluids.SulfurDioxide, 2), new GasStack(MekanismFluids.SulfurTrioxide, 2));
		RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.SulfurTrioxide, 1), new GasStack(MekanismFluids.Water, 1), new GasStack(MekanismFluids.SulfuricAcid, 1));
		RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.Hydrogen, 1), new GasStack(MekanismFluids.Chlorine, 1), new GasStack(MekanismFluids.HydrogenChloride, 1));
		RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.Deuterium, 1), new GasStack(MekanismFluids.Tritium, 1), new GasStack(MekanismFluids.FusionFuel, 2));

		//Electrolytic Separator Recipes
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("water", 2), 2 * general.FROM_H2, new GasStack(MekanismFluids.Hydrogen, 2), new GasStack(MekanismFluids.Oxygen, 1));
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("brine", 10), 2 * general.FROM_H2, new GasStack(MekanismFluids.Sodium, 1), new GasStack(MekanismFluids.Chlorine, 1));
		RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("heavywater", 2), usage.heavyWaterElectrolysisUsage, new GasStack(MekanismFluids.Deuterium, 2), new GasStack(MekanismFluids.Oxygen, 1));
		
		//Thermal Evaporation Plant Recipes
		RecipeHandler.addThermalEvaporationRecipe(FluidRegistry.getFluidStack("water", 10), FluidRegistry.getFluidStack("brine", 1));
		RecipeHandler.addThermalEvaporationRecipe(FluidRegistry.getFluidStack("brine", 10), FluidRegistry.getFluidStack("liquidlithium", 1));
		
		//Chemical Crystallizer Recipes
		RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(MekanismFluids.Lithium, 100), new ItemStack(MekanismItems.OtherDust, 1, 4));
		RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(MekanismFluids.Brine, 15), new ItemStack(MekanismItems.Salt));
		
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

		//Pressurized Reaction Chamber Recipes
		RecipeHandler.addPRCRecipe(
				new ItemStack(MekanismItems.BioFuel, 2), new FluidStack(FluidRegistry.WATER, 10), new GasStack(MekanismFluids.Hydrogen, 100),
				new ItemStack(MekanismItems.Substrate), new GasStack(MekanismFluids.Ethene, 100),
				0,
				100
		);
		RecipeHandler.addPRCRecipe(
				new ItemStack(MekanismItems.Substrate), new FluidStack(MekanismFluids.Ethene.getFluid(), 50), new GasStack(MekanismFluids.Oxygen, 10),
				new ItemStack(MekanismItems.Polyethene), new GasStack(MekanismFluids.Oxygen, 5),
				1000,
				60
		);
		RecipeHandler.addPRCRecipe(
				new ItemStack(MekanismItems.Substrate), new FluidStack(FluidRegistry.WATER, 200), new GasStack(MekanismFluids.Ethene, 100),
				new ItemStack(MekanismItems.Substrate, 8), new GasStack(MekanismFluids.Oxygen, 10),
				200,
				400
		);
		
		//Solar Neutron Activator Recipes
		RecipeHandler.addSolarNeutronRecipe(new GasStack(MekanismFluids.Lithium, 1), new GasStack(MekanismFluids.Tritium, 1));

        //Infuse objects
		InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.BioFuel), new InfuseObject(InfuseRegistry.get("BIO"), 5));
		InfuseRegistry.registerInfuseObject(new ItemStack(Items.COAL, 1, 0), new InfuseObject(InfuseRegistry.get("CARBON"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Items.COAL, 1, 1), new InfuseObject(InfuseRegistry.get("CARBON"), 20));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedCarbon), new InfuseObject(InfuseRegistry.get("CARBON"), 80));
        InfuseRegistry.registerInfuseObject(new ItemStack(Items.REDSTONE), new InfuseObject(InfuseRegistry.get("REDSTONE"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.REDSTONE_BLOCK), new InfuseObject(InfuseRegistry.get("REDSTONE"), 90));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedRedstone), new InfuseObject(InfuseRegistry.get("REDSTONE"), 80));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.RED_MUSHROOM), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.BROWN_MUSHROOM), new InfuseObject(InfuseRegistry.get("FUNGI"), 10));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedDiamond), new InfuseObject(InfuseRegistry.get("DIAMOND"), 80));
        InfuseRegistry.registerInfuseObject(new ItemStack(MekanismItems.CompressedObsidian), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 80));
        
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
		FuelHandler.addGas(MekanismFluids.Hydrogen, 1, general.FROM_H2);
		
		//RecipeSorter registrations
		RecipeSorter.register("mekanism_shaped", ShapedMekanismRecipe.class, Category.SHAPED, "");
		RecipeSorter.register("mekanism_shapeless", ShapelessMekanismRecipe.class, Category.SHAPELESS, "");
		RecipeSorter.register("bin", BinRecipe.class, Category.SHAPELESS, "");
	}

	/**
	 * Registers specified items with the Ore Dictionary.
	 */
	public void registerOreDict()
	{
		//Add specific items to ore dictionary for recipe usage in other mods.
		OreDictionary.registerOre("universalCable", MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.BASIC, 1));
		OreDictionary.registerOre("battery", MekanismItems.EnergyTablet.getUnchargedItem());
		OreDictionary.registerOre("pulpWood", MekanismItems.Sawdust);
		OreDictionary.registerOre("dustWood", MekanismItems.Sawdust);
		OreDictionary.registerOre("blockSalt", MekanismBlocks.SaltBlock);
		
		//Alloys!
		OreDictionary.registerOre("alloyBasic", new ItemStack(Items.REDSTONE));
		OreDictionary.registerOre("alloyAdvanced", new ItemStack(MekanismItems.EnrichedAlloy));
		OreDictionary.registerOre("alloyElite", new ItemStack(MekanismItems.ReinforcedAlloy));
		OreDictionary.registerOre("alloyUltimate", new ItemStack(MekanismItems.AtomicAlloy));
		
		//GregoriousT?
		OreDictionary.registerOre("itemSalt", MekanismItems.Salt);
		OreDictionary.registerOre("dustSalt", MekanismItems.Salt);
		
		OreDictionary.registerOre("dustDiamond", new ItemStack(MekanismItems.OtherDust, 1, 0));
		OreDictionary.registerOre("dustSteel", new ItemStack(MekanismItems.OtherDust, 1, 1));
		//Lead was once here
		OreDictionary.registerOre("dustSulfur", new ItemStack(MekanismItems.OtherDust, 1, 3));
		OreDictionary.registerOre("dustLithium", new ItemStack(MekanismItems.OtherDust, 1, 4));
		OreDictionary.registerOre("dustRefinedObsidian", new ItemStack(MekanismItems.OtherDust, 1, 5));
		OreDictionary.registerOre("dustObsidian", new ItemStack(MekanismItems.OtherDust, 1, 6));
		
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
			OreDictionary.registerOre("dust" + resource.getName(), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
			OreDictionary.registerOre("dustDirty" + resource.getName(), new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
			OreDictionary.registerOre("clump" + resource.getName(), new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
			OreDictionary.registerOre("shard" + resource.getName(), new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
			OreDictionary.registerOre("crystal" + resource.getName(), new ItemStack(MekanismItems.Crystal, 1, resource.ordinal()));
		}
		
		OreDictionary.registerOre("oreOsmium", new ItemStack(MekanismBlocks.OreBlock, 1, 0));
		OreDictionary.registerOre("oreCopper", new ItemStack(MekanismBlocks.OreBlock, 1, 1));
		OreDictionary.registerOre("oreTin", new ItemStack(MekanismBlocks.OreBlock, 1, 2));
		
		if(general.controlCircuitOreDict)
		{
			OreDictionary.registerOre("circuitBasic", new ItemStack(MekanismItems.ControlCircuit, 1, 0));
			OreDictionary.registerOre("circuitAdvanced", new ItemStack(MekanismItems.ControlCircuit, 1, 1));
			OreDictionary.registerOre("circuitElite", new ItemStack(MekanismItems.ControlCircuit, 1, 2));
			OreDictionary.registerOre("circuitUltimate", new ItemStack(MekanismItems.ControlCircuit, 1, 3));
		}

		OreDictionary.registerOre("itemCompressedCarbon", new ItemStack(MekanismItems.CompressedCarbon));
		OreDictionary.registerOre("itemCompressedRedstone", new ItemStack(MekanismItems.CompressedRedstone));
		OreDictionary.registerOre("itemCompressedDiamond", new ItemStack(MekanismItems.CompressedDiamond));
		OreDictionary.registerOre("itemCompressedObsidian", new ItemStack(MekanismItems.CompressedObsidian));

		OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(MekanismItems.EnrichedAlloy));
		OreDictionary.registerOre("itemBioFuel", new ItemStack(MekanismItems.BioFuel));
	}
	
	/**
	 * Adds and registers all entities and tile entities.
	 */
	public void addEntities()
	{
		//Registrations
		EntityRegistry.registerModEntity(new ResourceLocation("mekanism", "ObsidianTNT"), EntityObsidianTNT.class, "ObsidianTNT", 0, this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation("mekanism", "Robit"), EntityRobit.class, "Robit", 1, this, 64, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation("mekanism", "Balloon"), EntityBalloon.class, "Balloon", 2, this, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation("mekanism", "BabySkeleton"), EntityBabySkeleton.class, "BabySkeleton", 3, this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation("mekanism", "Flame"), EntityFlame.class, "Flame", 4, this, 64, 5, true);
		
		//Tile entities
		GameRegistry.registerTileEntity(TileEntityBoundingBlock.class, "BoundingBlock");
		GameRegistry.registerTileEntity(TileEntityAdvancedBoundingBlock.class, "AdvancedBoundingBlock");
		GameRegistry.registerTileEntity(TileEntityCardboardBox.class, "CardboardBox");
		GameRegistry.registerTileEntity(TileEntityThermalEvaporationValve.class, "ThermalEvaporationValve");
		GameRegistry.registerTileEntity(TileEntityThermalEvaporationBlock.class, "ThermalEvaporationBlock");
		GameRegistry.registerTileEntity(TileEntityPressureDisperser.class, "PressureDisperser");
		GameRegistry.registerTileEntity(TileEntitySuperheatingElement.class, "SuperheatingElement");
		GameRegistry.registerTileEntity(TileEntityLaser.class, "Laser");
		GameRegistry.registerTileEntity(TileEntityAmbientAccumulator.class, "AmbientAccumulator");
		GameRegistry.registerTileEntity(TileEntityInductionCasing.class, "InductionCasing");
		GameRegistry.registerTileEntity(TileEntityInductionPort.class, "InductionPort");
		GameRegistry.registerTileEntity(TileEntityInductionCell.class, "InductionCell");
		GameRegistry.registerTileEntity(TileEntityInductionProvider.class, "InductionProvider");
		GameRegistry.registerTileEntity(TileEntityOredictionificator.class, "Oredictionificator");
		GameRegistry.registerTileEntity(TileEntityStructuralGlass.class, "StructuralGlass");
		GameRegistry.registerTileEntity(TileEntityFuelwoodHeater.class, "FuelwoodHeater");
		GameRegistry.registerTileEntity(TileEntityLaserAmplifier.class, "LaserAmplifier");
		GameRegistry.registerTileEntity(TileEntityLaserTractorBeam.class, "LaserTractorBeam");
		GameRegistry.registerTileEntity(TileEntityChemicalWasher.class, "ChemicalWasher");
		GameRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator");
		GameRegistry.registerTileEntity(TileEntityChemicalOxidizer.class, "ChemicalOxidizer");
		GameRegistry.registerTileEntity(TileEntityChemicalInfuser.class, "ChemicalInfuser");
		GameRegistry.registerTileEntity(TileEntityRotaryCondensentrator.class, "RotaryCondensentrator");
		GameRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump");
		GameRegistry.registerTileEntity(TileEntityFluidicPlenisher.class, "FluidicPlenisher");
		GameRegistry.registerTileEntity(TileEntityGlowPanel.class, "GlowPanel");

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
		jetpackOn.clear();
		gasmaskOn.clear();
		activeVibrators.clear();
		worldTickHandler.resetRegenChunks();
		privateTeleporters.clear();
		privateEntangloporters.clear();
		freeRunnerOn.clear();
		
		//Reset consistent managers
		MultiblockManager.reset();
		FrequencyManager.reset();
		TransporterManager.reset();
		PathfinderCache.reset();
		TransmitterNetworkRegistry.reset();
	}
	
	@EventHandler
	public void loadComplete(FMLInterModComms.IMCEvent event)
	{
		new IMCHandler().onIMCEvent(event.getMessages());
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		File config = event.getSuggestedConfigurationFile();
		
		//Set the mod's configuration
		configuration = new Configuration(config);

        //Register tier information
        Tier.init();

		if(config.getAbsolutePath().contains("voltz"))
		{
			logger.info("Detected Voltz in root directory - hello, fellow user!");
		}
		else if(config.getAbsolutePath().contains("tekkit"))
		{
			logger.info("Detected Tekkit in root directory - hello, fellow user!");
		}
		
		//Register blocks and items
		MekanismItems.register();
		MekanismBlocks.register();

		//Integrate certain OreDictionary recipes
		registerOreDict();

		if(Loader.isModLoaded("mcmultipart")) 
		{
			//Set up multiparts
			new MultipartMekanism();
		} 
		else {
			logger.info("Didn't detect MCMP, ignoring compatibility package");
		}

		Mekanism.proxy.preInit();

		//Register infuses
        InfuseRegistry.registerInfuseType(new InfuseType("CARBON", new ResourceLocation("mekanism:blocks/infuse/Carbon")).setUnlocalizedName("carbon"));
        InfuseRegistry.registerInfuseType(new InfuseType("TIN", new ResourceLocation("mekanism:blocks/infuse/Tin")).setUnlocalizedName("tin"));
        InfuseRegistry.registerInfuseType(new InfuseType("DIAMOND", new ResourceLocation("mekanism:blocks/infuse/Diamond")).setUnlocalizedName("diamond"));
        InfuseRegistry.registerInfuseType(new InfuseType("REDSTONE", new ResourceLocation("mekanism:blocks/infuse/Redstone")).setUnlocalizedName("redstone"));
        InfuseRegistry.registerInfuseType(new InfuseType("FUNGI", new ResourceLocation("mekanism:blocks/infuse/Fungi")).setUnlocalizedName("fungi"));
		InfuseRegistry.registerInfuseType(new InfuseType("BIO", new ResourceLocation("mekanism:blocks/infuse/Bio")).setUnlocalizedName("bio"));
		InfuseRegistry.registerInfuseType(new InfuseType("OBSIDIAN", new ResourceLocation("mekanism:blocks/infuse/Obsidian")).setUnlocalizedName("obsidian"));

		Capabilities.registerCapabilities();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) 
	{
		//Register the mod's world generators
		GameRegistry.registerWorldGenerator(genHandler, 1);
		
		//Register the mod's GUI handler
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CoreGuiHandler());
		
		//Register player tracker
		MinecraftForge.EVENT_BUS.register(new CommonPlayerTracker());
		MinecraftForge.EVENT_BUS.register(new CommonPlayerTickHandler());
		
		//Initialization notification
		logger.info("Version " + versionNumber + " initializing...");
		
		//Get data from server
		new ThreadGetData();
		
		//Register with ForgeChunkManager
		ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkManager());
		
		//Register to receive subscribed events
		MinecraftForge.EVENT_BUS.register(this);
		
		//Register this module's GUI handler in the simple packet protocol
		PacketSimpleGui.handlers.add(0, proxy);

		//Set up VoiceServerManager
		if(general.voiceServerEnabled)
		{
			voiceManager = new VoiceServerManager();
		}
		
		//Register with TransmitterNetworkRegistry
		TransmitterNetworkRegistry.initiate();
		
		//Load configuration
		proxy.loadConfiguration();
		proxy.onConfigSync(false);
		
		//Add baby skeleton spawner
		if(general.spawnBabySkeletons)
		{
			for(Biome biome : BiomeProvider.allowedBiomes) 
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
		
		//Integrate with Waila
		FMLInterModComms.sendMessage(MekanismHooks.WAILA_MOD_ID, "register", "mekanism.common.integration.WailaDataProvider.register");

		//Integrate with OpenComputers
		if(Loader.isModLoaded(MekanismHooks.OPENCOMPUTERS_MOD_ID))
		{
			hooks.loadOCDrivers();
		}

		if(Loader.isModLoaded(MekanismHooks.APPLIED_ENERGISTICS_2_MOD_ID))
		{
			hooks.registerAE2P2P();
		}

		//Packet registrations
		packetHandler.initialize();

		//Load proxy
		proxy.init();
		
		//Completion notification
		logger.info("Loading complete.");
		
		//Success message
		logger.info("Mod loaded.");
	}	
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		logger.info("Fake player readout: UUID = " + gameProfile.getId().toString() + ", name = " + gameProfile.getName());

		hooks.hook();
		
		MinecraftForge.EVENT_BUS.post(new BoxBlacklistEvent());
		
		OreDictManager.init();
		
		//Update the config-dependent recipes after the recipes have actually been added in the first place
		TypeConfigManager.updateConfigRecipes(MachineType.getValidMachines(), general.machinesManager);
		
		logger.info("Hooking complete.");
	}
	
	@SubscribeEvent
	public void onEnergyTransferred(EnergyTransferEvent event)
	{
		try {
			packetHandler.sendToReceivers(new TransmitterUpdateMessage(PacketType.ENERGY, event.energyNetwork.transmitters.iterator().next().coord(), event.power), event.energyNetwork.getPacketRange());
		} catch(Exception e) {}
	}
	
	@SubscribeEvent
	public void onGasTransferred(GasTransferEvent event)
	{
		try {
			packetHandler.sendToReceivers(new TransmitterUpdateMessage(PacketType.GAS, event.gasNetwork.transmitters.iterator().next().coord(), event.transferType, event.didTransfer), event.gasNetwork.getPacketRange());
		} catch(Exception e) {}
	}
	
	@SubscribeEvent
	public void onLiquidTransferred(FluidTransferEvent event)
	{
		try {
			packetHandler.sendToReceivers(new TransmitterUpdateMessage(PacketType.FLUID, event.fluidNetwork.transmitters.iterator().next().coord(), event.fluidType, event.didTransfer), event.fluidNetwork.getPacketRange());
		} catch(Exception e) {}
	}

	@SubscribeEvent
	public void onTransmittersAddedEvent(TransmittersAddedEvent event)
	{
		try {
			packetHandler.sendToReceivers(new TransmitterUpdateMessage(PacketType.UPDATE, event.network.transmitters.iterator().next().coord(), event.newNetwork, event.newTransmitters), event.network.getPacketRange());
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

		// Mekanism multiblock structures
		MekanismAPI.addBoxBlacklist(MekanismBlocks.BoundingBlock, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(MekanismBlocks.BasicBlock2, 9);   // Security Desk
		MekanismAPI.addBoxBlacklist(MekanismBlocks.MachineBlock, 4);  // Digital Miner
		MekanismAPI.addBoxBlacklist(MekanismBlocks.MachineBlock2, 9); // Seismic Vibrator
		MekanismAPI.addBoxBlacklist(MekanismBlocks.MachineBlock3, 1); // Solar Neutron Activator

		// Minecraft unobtainable
		MekanismAPI.addBoxBlacklist(Blocks.BEDROCK, 0);
		MekanismAPI.addBoxBlacklist(Blocks.PORTAL, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.END_PORTAL, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.END_PORTAL_FRAME, OreDictionary.WILDCARD_VALUE);

		// Minecraft multiblock structures
		MekanismAPI.addBoxBlacklist(Blocks.BED, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.OAK_DOOR, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.SPRUCE_DOOR, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.BIRCH_DOOR, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.JUNGLE_DOOR, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.ACACIA_DOOR, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.DARK_OAK_DOOR, OreDictionary.WILDCARD_VALUE);
		MekanismAPI.addBoxBlacklist(Blocks.IRON_DOOR, OreDictionary.WILDCARD_VALUE);
		
		BoxBlacklistParser.load();
	}
	
	@SubscribeEvent
	public synchronized void onChunkLoad(ChunkEvent.Load event)
	{
		if(event.getChunk() != null && !event.getWorld().isRemote)
		{
			Map copy = (Map)((HashMap)event.getChunk().getTileEntityMap()).clone();
			 
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
					else if(tileEntity instanceof IChunkLoadHandler)
					{
						((IChunkLoadHandler)tileEntity).onChunkLoad();
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void chunkSave(ChunkDataEvent.Save event) 
	{
		if(!event.getWorld().isRemote)
		{
			NBTTagCompound nbtTags = event.getData();

			nbtTags.setInteger("MekanismWorldGen", baseWorldGenVersion);
			nbtTags.setInteger("MekanismUserWorldGen", general.userWorldGenVersion);
		}
	}
	
	@SubscribeEvent
	public synchronized void onChunkDataLoad(ChunkDataEvent.Load event)
	{
		if(!event.getWorld().isRemote)
		{
			if(general.enableWorldRegeneration)
			{
				NBTTagCompound loadData = event.getData();
				
				if(loadData.getInteger("MekanismWorldGen") == baseWorldGenVersion && loadData.getInteger("MekanismUserWorldGen") == general.userWorldGenVersion)
				{
					return;
				}
	
				ChunkPos coordPair = event.getChunk().getPos();
				worldTickHandler.addRegenChunk(event.getWorld().provider.getDimension(), coordPair);
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.getModID().equals("mekanism"))
		{
			proxy.loadConfiguration();
			proxy.onConfigSync(false);
		}
	}
}
