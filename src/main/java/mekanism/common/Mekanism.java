package mekanism.common;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.OreGas;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.transmitters.DynamicNetwork.ClientTickUpdate;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.DynamicNetwork.TransmittersAddedEvent;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.ClientProxy;
import mekanism.client.ClientTickHandler;
import mekanism.common.base.IModule;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.command.CommandMek;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.integration.IMCHandler;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.transmitters.grid.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.transmitters.grid.FluidNetwork.FluidTransferEvent;
import mekanism.common.transmitters.grid.GasNetwork.GasTransferEvent;
import mekanism.common.world.GenHandler;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Mekanism.MODID)
public class Mekanism {

    public static final String MODID = MekanismAPI.MEKANISM_MODID;
    public static final String MOD_NAME = "Mekanism";
    public static final String LOG_TAG = '[' + MOD_NAME + ']';
    public static final PlayerState playerState = new PlayerState();
    public static final Set<UUID> freeRunnerOn = new HashSet<>();
    /**
     * Mekanism Packet Pipeline
     */
    public static PacketHandler packetHandler = new PacketHandler();
    /**
     * Mekanism logger instance
     */
    public static Logger logger = LogManager.getLogger(MOD_NAME);
    /**
     * Mekanism proxy instance
     */
    //Note: Do not replace with method reference: https://gist.github.com/williewillus/353c872bcf1a6ace9921189f6100d09a#gistcomment-2876130
    public static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new CommonProxy());
    /**
     * Mekanism mod instance
     */
    public static Mekanism instance;
    /**
     * Mekanism hooks instance
     */
    public static MekanismHooks hooks = new MekanismHooks();
    /**
     * Mekanism version number
     */
    public static Version versionNumber = new Version(999, 999, 999);
    /**
     * MultiblockManagers for various structrures
     */
    public static MultiblockManager<SynchronizedTankData> tankManager = new MultiblockManager<>("dynamicTank");
    public static MultiblockManager<SynchronizedMatrixData> matrixManager = new MultiblockManager<>("inductionMatrix");
    public static MultiblockManager<SynchronizedBoilerData> boilerManager = new MultiblockManager<>("thermoelectricBoiler");
    /**
     * FrequencyManagers for various networks
     */
    public static FrequencyManager publicTeleporters = new FrequencyManager(Frequency.class, Frequency.TELEPORTER);
    public static Map<UUID, FrequencyManager> privateTeleporters = new HashMap<>();
    public static FrequencyManager publicEntangloporters = new FrequencyManager(InventoryFrequency.class, InventoryFrequency.ENTANGLOPORTER);
    public static Map<UUID, FrequencyManager> privateEntangloporters = new HashMap<>();
    public static FrequencyManager securityFrequencies = new FrequencyManager(SecurityFrequency.class, SecurityFrequency.SECURITY);
    /**
     * Mekanism creative tab
     */
    public static CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
    /**
     * List of Mekanism modules loaded
     */
    public static List<IModule> modulesLoaded = new ArrayList<>();
    /**
     * The server's world tick handler.
     */
    public static CommonWorldTickHandler worldTickHandler = new CommonWorldTickHandler();
    /**
     * The version of ore generation in this version of Mekanism. Increment this every time the default ore generation changes.
     */
    public static int baseWorldGenVersion = 0;
    /**
     * The GameProfile used by the dummy Mekanism player
     */
    public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes()), Mekanism.LOG_TAG);
    public static KeySync keyMap = new KeySync();
    public static Set<Coord4D> activeVibrators = new HashSet<>();

    public Mekanism() {
        instance = this;
        MekanismConfig.registerConfigs(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //TODO: Is this the proper way to register these listeners
        modEventBus.addListener(this::onEnergyTransferred);
        modEventBus.addListener(this::onGasTransferred);
        modEventBus.addListener(this::onLiquidTransferred);
        modEventBus.addListener(this::onTransmittersAddedEvent);
        modEventBus.addListener(this::onNetworkClientRequest);
        modEventBus.addListener(this::onClientTickUpdate);
        modEventBus.addListener(this::onBlacklistUpdate);
        modEventBus.addListener(this::chunkSave);
        modEventBus.addListener(this::onChunkDataLoad);
        modEventBus.addListener(this::onWorldLoad);
        modEventBus.addListener(this::onWorldUnload);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::serverStarting);
        modEventBus.addListener(this::serverStopping);
        modEventBus.addListener(this::handleIMC);
        //TODO: Register other listeners and various stuff that is needed

        MekanismConfig.loadFromFiles();
    }

    /**
     * Adds all in-game crafting, smelting and machine recipes.
     */
    public static void addRecipes() {
        //Enrichment Chamber Recipes
        if (MekanismBlock.ENRICHMENT_CHAMBER.isEnabled()) {
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.OBSIDIAN), MekanismItem.OBSIDIAN_DUST.getItemStack(4));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.COAL), MekanismItem.ENRICHED_CARBON.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.CHARCOAL), MekanismItem.ENRICHED_CARBON.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.REDSTONE), MekanismItem.ENRICHED_REDSTONE.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONE), new ItemStack(Blocks.CRACKED_STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.SAND), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.GUNPOWDER), new ItemStack(Items.FLINT));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.CRACKED_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.CHISELED_STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.MOSSY_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.GLOWSTONE), new ItemStack(Items.GLOWSTONE_DUST, 4));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.CLAY), new ItemStack(Items.CLAY_BALL, 4));
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.SALT_BLOCK.getItemStack(), MekanismItem.SALT.getItemStack(4));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.DIAMOND), MekanismItem.ENRICHED_DIAMOND.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismItem.HDPE_PELLET.getItemStack(3), MekanismItem.HDPE_SHEET.getItemStack());
        }

        //Combiner recipes
        if (MekanismBlock.COMBINER.isEnabled()) {
            RecipeHandler.addCombinerRecipe(new ItemStack(Items.FLINT), new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addCombinerRecipe(new ItemStack(Items.COAL, 3), new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.COAL_ORE));
        }

        //Osmium Compressor Recipes
        if (MekanismBlock.OSMIUM_COMPRESSOR.isEnabled()) {
            RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Items.GLOWSTONE_DUST), MekanismItem.REFINED_GLOWSTONE_INGOT.getItemStack());
        }

        //Crusher Recipes
        if (MekanismBlock.CRUSHER.isEnabled()) {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.IRON_INGOT), MekanismItem.IRON_DUST.getItemStack());
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.GOLD_INGOT), MekanismItem.GOLD_DUST.getItemStack());
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.SAND));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONE), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CRACKED_STONE_BRICKS), new ItemStack(Blocks.STONE));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.CRACKED_STONE_BRICKS));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CHISELED_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.FLINT), new ItemStack(Items.GUNPOWDER));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.SANDSTONE), new ItemStack(Blocks.SAND, 2));

            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.WHITE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.ORANGE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.MAGENTA_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.LIGHT_BLUE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.YELLOW_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.LIME_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.PINK_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GRAY_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.LIGHT_GRAY_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CYAN_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.PURPLE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.BLUE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.BROWN_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GREEN_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.RED_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.BLACK_WOOL), new ItemStack(Items.STRING, 4));

            //BioFuel Crusher Recipes
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GRASS), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.SUGAR_CANE), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.WHEAT_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.WHEAT), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.PUMPKIN_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.MELON_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.APPLE), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BREAD), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.CARROT), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.ROTTEN_FLESH), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.MELON), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.PUMPKIN), MekanismItem.BIO_FUEL.getItemStack(6));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BAKED_POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.POISONOUS_POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BEETROOT), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BEETROOT_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CACTUS), MekanismItem.BIO_FUEL.getItemStack(2));
        }

        //Purification Chamber Recipes
        if (MekanismBlock.PURIFICATION_CHAMBER.isEnabled()) {
            RecipeHandler.addPurificationChamberRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Items.FLINT));
        }

        //Chemical Injection Chamber Recipes
        if (MekanismBlock.CHEMICAL_INJECTION_CHAMBER.isEnabled()) {
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.DIRT), MekanismGases.STEAM, new ItemStack(Blocks.CLAY));
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.TERRACOTTA), MekanismGases.STEAM, new ItemStack(Blocks.CLAY));
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Items.BRICK), MekanismGases.STEAM, new ItemStack(Items.CLAY_BALL));
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Items.GUNPOWDER), MekanismGases.HYDROGEN_CHLORIDE, MekanismItem.SULFUR_DUST.getItemStack());
        }

        //Precision Sawmill Recipes
        if (MekanismBlock.PRECISION_SAWMILL.isEnabled()) {
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.LADDER, 3), new ItemStack(Items.STICK, 7));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.TORCH, 4), new ItemStack(Items.STICK), new ItemStack(Items.COAL), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.CHEST), new ItemStack(Blocks.OAK_PLANKS, 8));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.NOTE_BLOCK), new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.REDSTONE), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.REDSTONE_TORCH), new ItemStack(Items.STICK), new ItemStack(Items.REDSTONE), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.OAK_PLANKS, 4));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUKEBOX), new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.DIAMOND), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BOOKSHELF), new ItemStack(Blocks.OAK_PLANKS, 6), new ItemStack(Items.BOOK, 3), 1);
            //Trapdoos
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_TRAPDOOR), new ItemStack(Blocks.ACACIA_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_TRAPDOOR), new ItemStack(Blocks.BIRCH_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_TRAPDOOR), new ItemStack(Blocks.DARK_OAK_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_TRAPDOOR), new ItemStack(Blocks.JUNGLE_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_TRAPDOOR), new ItemStack(Blocks.OAK_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_TRAPDOOR), new ItemStack(Blocks.SPRUCE_PLANKS, 3));
            //Boats
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.ACACIA_BOAT), new ItemStack(Blocks.ACACIA_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BIRCH_BOAT), new ItemStack(Blocks.BIRCH_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.DARK_OAK_BOAT), new ItemStack(Blocks.DARK_OAK_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.JUNGLE_BOAT), new ItemStack(Blocks.JUNGLE_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.OAK_BOAT), new ItemStack(Blocks.OAK_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.SPRUCE_BOAT), new ItemStack(Blocks.SPRUCE_PLANKS, 5));
            //Beds
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.WHITE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.WHITE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.ORANGE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.ORANGE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.MAGENTA_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.MAGENTA_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.LIGHT_BLUE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIGHT_BLUE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.YELLOW_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.YELLOW_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.LIME_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIME_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.PINK_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.PINK_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.GRAY_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.GRAY_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.LIGHT_GRAY_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIGHT_GRAY_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.CYAN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.CYAN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.PURPLE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.PURPLE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BLUE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BLUE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BROWN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BROWN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.GREEN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.GREEN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.RED_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.RED_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BLACK_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BLACK_WOOL, 3), 1);
            //Doors
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.ACACIA_DOOR), new ItemStack(Blocks.ACACIA_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BIRCH_DOOR), new ItemStack(Blocks.BIRCH_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.DARK_OAK_DOOR), new ItemStack(Blocks.DARK_OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.JUNGLE_DOOR), new ItemStack(Blocks.JUNGLE_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.OAK_DOOR), new ItemStack(Blocks.OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.SPRUCE_DOOR), new ItemStack(Blocks.SPRUCE_PLANKS, 2));
            //Pressure Plates
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_PRESSURE_PLATE), new ItemStack(Blocks.ACACIA_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_PRESSURE_PLATE), new ItemStack(Blocks.BIRCH_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE), new ItemStack(Blocks.DARK_OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE), new ItemStack(Blocks.JUNGLE_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_PRESSURE_PLATE), new ItemStack(Blocks.OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE), new ItemStack(Blocks.SPRUCE_PLANKS, 2));
            //Fences
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_FENCE), new ItemStack(Items.STICK, 3));
            //Fence Gates
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_FENCE_GATE), new ItemStack(Blocks.ACACIA_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.BIRCH_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_FENCE_GATE), new ItemStack(Blocks.DARK_OAK_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_FENCE_GATE), new ItemStack(Blocks.JUNGLE_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_FENCE_GATE), new ItemStack(Blocks.SPRUCE_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
        }

        if (MekanismBlock.METALLURGIC_INFUSER.isEnabled()) {
            InfuseType carbon = InfuseRegistry.get("CARBON");
            InfuseType bio = InfuseRegistry.get("BIO");
            InfuseType redstone = InfuseRegistry.get("REDSTONE");
            InfuseType fungi = InfuseRegistry.get("FUNGI");
            InfuseType diamond = InfuseRegistry.get("DIAMOND");
            InfuseType obsidian = InfuseRegistry.get("OBSIDIAN");

            //Infuse objects
            InfuseRegistry.registerInfuseObject(MekanismItem.BIO_FUEL.getItemStack(), new InfuseObject(bio, 5));
            InfuseRegistry.registerInfuseObject(new ItemStack(Items.CHARCOAL), new InfuseObject(carbon, 10));
            InfuseRegistry.registerInfuseObject(new ItemStack(Items.CHARCOAL), new InfuseObject(carbon, 20));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.COAL_BLOCK), new InfuseObject(carbon, 90));
            InfuseRegistry.registerInfuseObject(MekanismBlock.CHARCOAL_BLOCK.getItemStack(), new InfuseObject(carbon, 180));
            InfuseRegistry.registerInfuseObject(MekanismItem.ENRICHED_CARBON.getItemStack(), new InfuseObject(carbon, 80));
            InfuseRegistry.registerInfuseObject(new ItemStack(Items.REDSTONE), new InfuseObject(redstone, 10));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.REDSTONE_BLOCK), new InfuseObject(redstone, 90));
            InfuseRegistry.registerInfuseObject(MekanismItem.ENRICHED_REDSTONE.getItemStack(), new InfuseObject(redstone, 80));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.RED_MUSHROOM), new InfuseObject(fungi, 10));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.BROWN_MUSHROOM), new InfuseObject(fungi, 10));
            InfuseRegistry.registerInfuseObject(MekanismItem.ENRICHED_DIAMOND.getItemStack(), new InfuseObject(diamond, 80));
            InfuseRegistry.registerInfuseObject(MekanismItem.ENRICHED_OBSIDIAN.getItemStack(), new InfuseObject(obsidian, 80));

            //Metallurgic Infuser Recipes
            RecipeHandler.addMetallurgicInfuserRecipe(carbon, 10, new ItemStack(Items.IRON_INGOT), MekanismItem.ENRICHED_IRON.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(carbon, 10, MekanismItem.ENRICHED_IRON.getItemStack(), MekanismItem.STEEL_DUST.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(redstone, 10, new ItemStack(Items.IRON_INGOT), MekanismItem.INFUSED_ALLOY.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(fungi, 10, new ItemStack(Blocks.DIRT), new ItemStack(Blocks.MYCELIUM));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.MOSSY_COBBLESTONE));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.MOSSY_STONE_BRICKS));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.SAND), new ItemStack(Blocks.DIRT));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.DIRT), new ItemStack(Blocks.PODZOL));
            RecipeHandler.addMetallurgicInfuserRecipe(diamond, 10, MekanismItem.INFUSED_ALLOY.getItemStack(), MekanismItem.REINFORCED_ALLOY.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(obsidian, 10, MekanismItem.REINFORCED_ALLOY.getItemStack(), MekanismItem.ATOMIC_ALLOY.getItemStack());
        }

        //Chemical Infuser Recipes
        if (MekanismBlock.CHEMICAL_INFUSER.isEnabled()) {
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismGases.OXYGEN, 1), new GasStack(MekanismGases.SULFUR_DIOXIDE, 2), new GasStack(MekanismGases.SULFUR_TRIOXIDE, 2));
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismGases.SULFUR_TRIOXIDE, 1), new GasStack(MekanismGases.STEAM, 1), new GasStack(MekanismGases.SULFURIC_ACID, 1));
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismGases.HYDROGEN, 1), new GasStack(MekanismGases.CHLORINE, 1), new GasStack(MekanismGases.HYDROGEN_CHLORIDE, 1));
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismGases.DEUTERIUM, 1), new GasStack(MekanismGases.TRITIUM, 1), new GasStack(MekanismGases.FUSION_FUEL, 2));
        }

        //Electrolytic Separator Recipes
        if (MekanismBlock.ELECTROLYTIC_SEPARATOR.isEnabled()) {
            RecipeHandler.addElectrolyticSeparatorRecipe(new FluidStack(Fluids.WATER, 2), 2 * MekanismConfig.general.FROM_H2.get(),
                  new GasStack(MekanismGases.HYDROGEN, 2), new GasStack(MekanismGases.OXYGEN, 1));
            RecipeHandler.addElectrolyticSeparatorRecipe(new FluidStack(MekanismGases.BRINE.getFluid(), 10), 2 * MekanismConfig.general.FROM_H2.get(),
                  new GasStack(MekanismGases.SODIUM, 1), new GasStack(MekanismGases.CHLORINE, 1));
            RecipeHandler.addElectrolyticSeparatorRecipe(new FluidStack(MekanismGases.HEAVY_WATER, 2), MekanismConfig.usage.heavyWaterElectrolysis.get(),
                  new GasStack(MekanismGases.DEUTERIUM, 2), new GasStack(MekanismGases.OXYGEN, 1));
        }

        //Thermal Evaporation Plant Recipes
        RecipeHandler.addThermalEvaporationRecipe(new FluidStack(Fluids.WATER, 10), new FluidStack(MekanismGases.BRINE.getFluid(), 1));
        RecipeHandler.addThermalEvaporationRecipe(new FluidStack(MekanismGases.BRINE.getFluid(), 10), new FluidStack(MekanismGases.LITHIUM.getFluid(), 1));

        //Chemical Crystallizer Recipes
        if (MekanismBlock.CHEMICAL_CRYSTALLIZER.isEnabled()) {
            RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(MekanismGases.LITHIUM, 100), MekanismItem.SULFUR_DUST.getItemStack());
            RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(MekanismGases.BRINE, 15), MekanismItem.SALT.getItemStack());
        }

        //T4 Processing Recipes
        for (Gas gas : MekanismAPI.GAS_REGISTRY.getValues()) {
            if (gas instanceof OreGas && !((OreGas) gas).isClean()) {
                OreGas oreGas = (OreGas) gas;
                if (MekanismBlock.CHEMICAL_WASHER.isEnabled()) {
                    RecipeHandler.addChemicalWasherRecipe(new GasStack(oreGas, 1), new GasStack(oreGas.getCleanGas(), 1));
                }

                if (MekanismBlock.CHEMICAL_CRYSTALLIZER.isEnabled()) {
                    //do the crystallizer only if it's one of our gases!
                    Resource gasResource = Resource.getFromName(oreGas.getName());
                    if (gasResource != null) {
                        //TODO: Better way to do this
                        MekanismItem crystal = null;
                        switch (gasResource) {
                            case IRON:
                                crystal = MekanismItem.IRON_CRYSTAL;
                                break;
                            case GOLD:
                                crystal = MekanismItem.GOLD_CRYSTAL;
                                break;
                            case OSMIUM:
                                crystal = MekanismItem.OSMIUM_CRYSTAL;
                                break;
                            case COPPER:
                                crystal = MekanismItem.COPPER_CRYSTAL;
                                break;
                            case TIN:
                                crystal = MekanismItem.TIN_CRYSTAL;
                                break;
                            case SILVER:
                                crystal = MekanismItem.SILVER_CRYSTAL;
                                break;
                            case LEAD:
                                crystal = MekanismItem.LEAD_CRYSTAL;
                                break;
                        }
                        RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(oreGas.getCleanGas(), 200), crystal.getItemStack());
                    }
                }
            }
        }

        //Pressurized Reaction Chamber Recipes
        if (MekanismBlock.PRESSURIZED_REACTION_CHAMBER.isEnabled()) {
            RecipeHandler.addPRCRecipe(MekanismItem.SUBSTRATE.getItemStack(2), new FluidStack(Fluids.WATER, 10), new GasStack(MekanismGases.HYDROGEN, 100),
                  MekanismItem.SUBSTRATE.getItemStack(), new GasStack(MekanismGases.ETHENE, 100), 0, 100);
            RecipeHandler.addPRCRecipe(MekanismItem.SUBSTRATE.getItemStack(), new FluidStack(MekanismGases.ETHENE.getFluid(), 50),
                  new GasStack(MekanismGases.OXYGEN, 10), MekanismItem.HDPE_PELLET.getItemStack(), new GasStack(MekanismGases.OXYGEN, 5), 1000, 60);
            RecipeHandler.addPRCRecipe(MekanismItem.SUBSTRATE.getItemStack(), new FluidStack(Fluids.WATER, 200), new GasStack(MekanismGases.ETHENE, 100),
                  MekanismItem.SUBSTRATE.getItemStack(8), new GasStack(MekanismGases.OXYGEN, 10), 200, 400);
            RecipeHandler.addPRCRecipe(new ItemStack(Items.COAL), new FluidStack(Fluids.WATER, 100), new GasStack(MekanismGases.OXYGEN, 100),
                  MekanismItem.SULFUR_DUST.getItemStack(), new GasStack(MekanismGases.HYDROGEN, 100), 0, 100);
            RecipeHandler.addPRCRecipe(new ItemStack(Items.CHARCOAL), new FluidStack(Fluids.WATER, 100), new GasStack(MekanismGases.OXYGEN, 100),
                  MekanismItem.SULFUR_DUST.getItemStack(), new GasStack(MekanismGases.HYDROGEN, 100), 0, 100);
        }

        //Solar Neutron Activator Recipes
        if (MekanismBlock.SOLAR_NEUTRON_ACTIVATOR.isEnabled()) {
            RecipeHandler.addSolarNeutronRecipe(new GasStack(MekanismGases.LITHIUM, 1), new GasStack(MekanismGases.TRITIUM, 1));
        }

        //Fuel Gases
        FuelHandler.addGas(MekanismGases.HYDROGEN, 1, MekanismConfig.general.FROM_H2.get());
    }

    private void serverStarting(FMLServerStartingEvent event) {
        //TODO: Check this stuff
        //CommandMek.register(event);
        CommandMek.register();
        //TODO: Do we care about the alternates of mtp, and mtpop
    }

    private void serverStopping(FMLServerStoppingEvent event) {
        //Clear all cache data
        playerState.clear();
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

    public void handleIMC(InterModProcessEvent event) {
        new IMCHandler().onIMCEvent(event.getIMCStream());
    }

    public void preInit() {
        //TODO: Find proper stage for this
        //MekanismConfig.registerConfigs(ModLoadingContext.get());
        //MekanismConfig.loadFromFiles();
        //sanity check the api location if not deobf
        //TODO: Check API
        /*if (!((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))) {
            String apiLocation = MekanismAPI.class.getProtectionDomain().getCodeSource().getLocation().toString();
            if (apiLocation.toLowerCase(Locale.ROOT).contains("-api.jar")) {
                proxy.throwApiPresentException();
            }
        }*/

        //Load configuration
        proxy.loadConfiguration();
        proxy.onConfigSync(false);
        hooks.hookPreInit();

        MinecraftForge.EVENT_BUS.register(MekanismItem.GAS_MASK.getItem());
        MinecraftForge.EVENT_BUS.register(MekanismItem.FREE_RUNNERS.getItem());

        //TODO: Multipart
        /*if (hooks.MCMPLoaded) {
            //Set up multiparts
            new MultipartMekanism();
        } else {
            logger.info("Didn't detect MCMP, ignoring compatibility package");
        }*/

        Mekanism.proxy.preInit();

        //Register infuses
        //TODO: Let infuse types be registered via JSON and data packs
        InfuseRegistry.registerInfuseType(new InfuseType("CARBON", new ResourceLocation(Mekanism.MODID, "block/infuse/carbon")).setTranslationKey("carbon"));
        InfuseRegistry.registerInfuseType(new InfuseType("TIN", new ResourceLocation(Mekanism.MODID, "block/infuse/tin")).setTranslationKey("tin"));
        InfuseRegistry.registerInfuseType(new InfuseType("DIAMOND", new ResourceLocation(Mekanism.MODID, "block/infuse/diamond")).setTranslationKey("diamond"));
        InfuseRegistry.registerInfuseType(new InfuseType("REDSTONE", new ResourceLocation(Mekanism.MODID, "block/infuse/redstone")).setTranslationKey("redstone"));
        InfuseRegistry.registerInfuseType(new InfuseType("FUNGI", new ResourceLocation(Mekanism.MODID, "block/infuse/fungi")).setTranslationKey("fungi"));
        InfuseRegistry.registerInfuseType(new InfuseType("BIO", new ResourceLocation(Mekanism.MODID, "block/infuse/bio")).setTranslationKey("bio"));
        InfuseRegistry.registerInfuseType(new InfuseType("OBSIDIAN", new ResourceLocation(Mekanism.MODID, "block/infuse/obsidian")).setTranslationKey("obsidian"));

        Capabilities.registerCapabilities();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //TODO: Figure out where preinit stuff should be, potentially also move it directly into this method
        preInit();

        //TODO: Make recipes be done from JSON
        //TODO: Bin recipe
        //event.getRegistry().register(new BinRecipe());
        addRecipes();
        GasConversionHandler.addDefaultGasMappings();

        //Register the mod's world generators
        GenHandler.setupWorldGeneration();

        //Register player tracker
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTickHandler());

        //Initialization notification
        logger.info("Version " + versionNumber + " initializing...");

        //TODO: Chunk Loading
        //Register with ForgeChunkManager
        //ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkManager());

        //Register to receive subscribed events
        MinecraftForge.EVENT_BUS.register(this);

        //Register with TransmitterNetworkRegistry
        TransmitterNetworkRegistry.initiate();

        //Add baby skeleton spawner
        //TODO: Spawn baby skeletons
        /*if (MekanismConfig.general.spawnBabySkeletons.get()) {
            for (Biome biome : BiomeProvider.BIOMES_TO_SPAWN_IN) {
                if (biome.getSpawns(EntityClassification.MONSTER).size() > 0) {
                    EntityRegistry.addSpawn(EntityBabySkeleton.class, 40, 1, 3, EntityClassification.MONSTER, biome);
                }
            }
        }*/

        //Load this module
        hooks.hookCommonSetup();

        //Packet registrations
        packetHandler.initialize();

        //Load proxy
        proxy.init();

        //Fake player info
        logger.info("Fake player readout: UUID = " + gameProfile.getId().toString() + ", name = " + gameProfile.getName());

        // Add all furnace recipes to the energized smelter
        // Must happen after CraftTweaker for vanilla stuff has run.
        //TODO: Needs to be handled after/whenever reload is done
        /*for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            SmeltingRecipe recipe = new SmeltingRecipe(new ItemStackInput(entry.getKey()), new ItemStackOutput(entry.getValue()));
            Recipe.ENERGIZED_SMELTER.put(recipe);
        }*/

        MinecraftForge.EVENT_BUS.post(new BoxBlacklistEvent());

        //Completion notification
        logger.info("Loading complete.");

        //Success message
        logger.info("Mod loaded.");

        //TODO: Use FMLDedicatedServerSetupEvent and FMLClientSetupEvent
    }

    private void onEnergyTransferred(EnergyTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.ENERGY, event.energyNetwork.firstTransmitter().coord(), event.power),
                  event.energyNetwork.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    private void onGasTransferred(GasTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.GAS, event.gasNetwork.firstTransmitter().coord(), event.transferType, event.didTransfer),
                  event.gasNetwork.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    private void onLiquidTransferred(FluidTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.FLUID, event.fluidNetwork.firstTransmitter().coord(), event.fluidType, event.didTransfer),
                  event.fluidNetwork.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    private void onTransmittersAddedEvent(TransmittersAddedEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.UPDATE, event.network.firstTransmitter().coord(), event.newNetwork, event.newTransmitters),
                  event.network.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    private void onNetworkClientRequest(NetworkClientRequest event) {
        try {
            packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(event.tileEntity)));
        } catch (Exception ignored) {
        }
    }

    private void onClientTickUpdate(ClientTickUpdate event) {
        try {
            if (event.operation == 0) {
                ClientTickHandler.tickingSet.remove(event.network);
            } else {
                ClientTickHandler.tickingSet.add(event.network);
            }
        } catch (Exception ignored) {
        }
    }

    private void onBlacklistUpdate(BoxBlacklistEvent event) {
        event.blacklist(MekanismBlock.CARDBOARD_BOX);

        // Mekanism multiblock structures
        event.blacklist(MekanismBlock.BOUNDING_BLOCK);
        event.blacklist(MekanismBlock.ADVANCED_BOUNDING_BLOCK);
        event.blacklist(MekanismBlock.SECURITY_DESK);
        event.blacklist(MekanismBlock.DIGITAL_MINER);
        event.blacklist(MekanismBlock.SEISMIC_VIBRATOR);
        event.blacklist(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR);

        // Minecraft unobtainable
        event.blacklist(Blocks.BEDROCK);
        event.blacklist(Blocks.NETHER_PORTAL);
        event.blacklist(Blocks.END_PORTAL);
        event.blacklist(Blocks.END_PORTAL_FRAME);

        // Minecraft multiblock structures
        event.blacklist(Blocks.WHITE_BED);
        event.blacklist(Blocks.ORANGE_BED);
        event.blacklist(Blocks.MAGENTA_BED);
        event.blacklist(Blocks.LIGHT_BLUE_BED);
        event.blacklist(Blocks.YELLOW_BED);
        event.blacklist(Blocks.LIME_BED);
        event.blacklist(Blocks.PINK_BED);
        event.blacklist(Blocks.GRAY_BED);
        event.blacklist(Blocks.LIGHT_GRAY_BED);
        event.blacklist(Blocks.CYAN_BED);
        event.blacklist(Blocks.PURPLE_BED);
        event.blacklist(Blocks.BLUE_BED);
        event.blacklist(Blocks.BROWN_BED);
        event.blacklist(Blocks.GREEN_BED);
        event.blacklist(Blocks.RED_BED);
        event.blacklist(Blocks.BLACK_BED);
        event.blacklist(Blocks.OAK_DOOR);
        event.blacklist(Blocks.SPRUCE_DOOR);
        event.blacklist(Blocks.BIRCH_DOOR);
        event.blacklist(Blocks.JUNGLE_DOOR);
        event.blacklist(Blocks.ACACIA_DOOR);
        event.blacklist(Blocks.DARK_OAK_DOOR);
        event.blacklist(Blocks.IRON_DOOR);

        //Extra Utils 2
        event.blacklist(new ResourceLocation("extrautils2", "machine"));

        //ImmEng multiblocks
        event.blacklist(new ResourceLocation("immersiveengineering", "metal_device0"));
        event.blacklist(new ResourceLocation("immersiveengineering", "metal_device1"));
        event.blacklist(new ResourceLocation("immersiveengineering", "wooden_device0"));
        event.blacklist(new ResourceLocation("immersiveengineering", "wooden_device1"));
        event.blacklist(new ResourceLocation("immersiveengineering", "connector"));
        event.blacklist(new ResourceLocation("immersiveengineering", "metal_multiblock"));

        //IC2
        event.blacklist(new ResourceLocation("ic2", "te"));

        event.blacklistMod("storagedrawers");//without packing tape, you're gonna have a bad time
        event.blacklistMod("colossalchests");

        BoxBlacklistParser.load();
    }

    private void chunkSave(ChunkDataEvent.Save event) {
        if (!event.getWorld().isRemote()) {
            CompoundNBT nbtTags = event.getData();

            nbtTags.putInt("MekanismWorldGen", baseWorldGenVersion);
            nbtTags.putInt("MekanismUserWorldGen", MekanismConfig.general.userWorldGenVersion.get());
        }
    }

    private synchronized void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (!event.getWorld().isRemote()) {
            if (MekanismConfig.general.enableWorldRegeneration.get()) {
                CompoundNBT loadData = event.getData();
                if (loadData.getInt("MekanismWorldGen") == baseWorldGenVersion &&
                    loadData.getInt("MekanismUserWorldGen") == MekanismConfig.general.userWorldGenVersion.get()) {
                    return;
                }
                ChunkPos coordPair = event.getChunk().getPos();
                //TODO: Is this correct
                worldTickHandler.addRegenChunk(event.getWorld().getDimension().getType().getId(), coordPair);
            }
        }
    }

    //TODO
    /*private void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Mekanism.MODID)) {
            proxy.loadConfiguration();
            proxy.onConfigSync(false);
        }
    }*/

    private void onWorldLoad(WorldEvent.Load event) {
        playerState.init(event.getWorld());
    }

    private void onWorldUnload(WorldEvent.Unload event) {
        // Make sure the global fake player drops its reference to the World
        // when the server shuts down
        if (event.getWorld() instanceof ServerWorld) {
            MekFakePlayer.releaseInstance(event.getWorld());
        }
    }
}