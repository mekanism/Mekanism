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
import mekanism.api.gas.Slurry;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
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
import mekanism.common.tags.MekanismTagManager;
import mekanism.common.tags.MekanismTags;
import mekanism.common.transmitters.grid.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.transmitters.grid.FluidNetwork.FluidTransferEvent;
import mekanism.common.transmitters.grid.GasNetwork.GasTransferEvent;
import mekanism.common.world.GenHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//TODO: Use data generators for things
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

    private MekanismTagManager mekanismTagManager;

    public Mekanism() {
        instance = this;
        MekanismConfig.registerConfigs(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //TODO: Figure out the proper event bus to register things on
        MinecraftForge.EVENT_BUS.addListener(this::onEnergyTransferred);
        MinecraftForge.EVENT_BUS.addListener(this::onGasTransferred);
        MinecraftForge.EVENT_BUS.addListener(this::onLiquidTransferred);
        MinecraftForge.EVENT_BUS.addListener(this::onTransmittersAddedEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onNetworkClientRequest);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTickUpdate);
        MinecraftForge.EVENT_BUS.addListener(this::onBlacklistUpdate);
        MinecraftForge.EVENT_BUS.addListener(this::chunkSave);
        MinecraftForge.EVENT_BUS.addListener(this::onChunkDataLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldUnload);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        modEventBus.addListener(this::handleIMC);
        //TODO: Register other listeners and various stuff that is needed

        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
        MekanismConfig.loadFromFiles();
    }

    public void setTagManager(MekanismTagManager manager) {
        if (mekanismTagManager == null) {
            mekanismTagManager = manager;
        }
        //TODO: Else throw error
    }

    public MekanismTagManager getTagManager() {
        return mekanismTagManager;
    }

    /**
     * Adds all in-game crafting, smelting and machine recipes.
     */
    public static void addRecipes() {
        //Enrichment Chamber Recipes
        if (MekanismBlock.ENRICHMENT_CHAMBER.isEnabled()) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.OBSIDIAN), MekanismItem.OBSIDIAN_DUST.getItemStack(4));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(ItemTags.COALS), MekanismItem.ENRICHED_CARBON.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE), MekanismItem.ENRICHED_REDSTONE.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Blocks.MOSSY_COBBLESTONE), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Blocks.STONE), new ItemStack(Blocks.CRACKED_STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.SAND), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.GRAVEL), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.GUNPOWDER), new ItemStack(Items.FLINT));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Blocks.CRACKED_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Blocks.STONE_BRICKS), new ItemStack(Blocks.CHISELED_STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Blocks.MOSSY_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            //TODO: Add a tag for glowstone blocks and clay blocks??
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Blocks.GLOWSTONE), new ItemStack(Items.GLOWSTONE_DUST, 4));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Blocks.CLAY), new ItemStack(Items.CLAY_BALL, 4));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismBlock.SALT_BLOCK), MekanismItem.SALT.getItemStack(4));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.GEMS_DIAMOND), MekanismItem.ENRICHED_DIAMOND.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismItem.HDPE_PELLET, 3), MekanismItem.HDPE_SHEET.getItemStack());
        }

        //Combiner recipes
        if (MekanismBlock.COMBINER.isEnabled()) {
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(Items.FLINT), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(Items.COAL, 3), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.COAL_ORE));
        }

        //Osmium Compressor Recipes
        if (MekanismBlock.OSMIUM_COMPRESSOR.isEnabled()) {
            RecipeHandler.addOsmiumCompressorRecipe(ItemStackIngredient.from(Tags.Items.DUSTS_GLOWSTONE), GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1),
                  MekanismItem.REFINED_GLOWSTONE_INGOT.getItemStack());
        }

        //Crusher Recipes
        if (MekanismBlock.CRUSHER.isEnabled()) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.INGOTS_IRON), MekanismItem.IRON_DUST.getItemStack());
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.INGOTS_GOLD), MekanismItem.GOLD_DUST.getItemStack());
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GRAVEL), new ItemStack(Blocks.SAND));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.STONE), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.CRACKED_STONE_BRICKS), new ItemStack(Blocks.STONE));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.STONE_BRICKS), new ItemStack(Blocks.CRACKED_STONE_BRICKS));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.CHISELED_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.FLINT), new ItemStack(Items.GUNPOWDER));
            //TODO: Multiple ingredients passed to it directly? Or just make tags for these. given the base forge sandstone tag has both normal and red
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.SANDSTONE), new ItemStack(Blocks.SAND, 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.CHISELED_SANDSTONE), new ItemStack(Blocks.SAND, 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.CUT_SANDSTONE), new ItemStack(Blocks.SAND, 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.SMOOTH_SANDSTONE), new ItemStack(Blocks.SAND, 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.RED_SANDSTONE), new ItemStack(Blocks.RED_SAND, 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.CHISELED_RED_SANDSTONE), new ItemStack(Blocks.RED_SAND, 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.CUT_RED_SANDSTONE), new ItemStack(Blocks.RED_SAND, 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.SMOOTH_RED_SANDSTONE), new ItemStack(Blocks.RED_SAND, 2));

            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(ItemTags.WOOL), new ItemStack(Items.STRING, 4));

            //BioFuel Crusher Recipes
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.GRASS), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.SUGAR_CANE), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            //TODO: This also includes nether wart which we did not have a recipe for in 1.12.
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.CROPS), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.APPLE), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.BREAD), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.ROTTEN_FLESH), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.MELON), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.PUMPKIN), MekanismItem.BIO_FUEL.getItemStack(6));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.BAKED_POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.POISONOUS_POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.CACTUS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(ItemTags.SAPLINGS), MekanismItem.BIO_FUEL.getItemStack(2));
            //TODO: Remove this TODO, it is mainly here to make it easier to see what new ones got added that the recipe rewrite branch does not have
            // Also evaluate these numbers as I believe they are probably wrong/unbalanced
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.MUSHROOMS), MekanismItem.BIO_FUEL.getItemStack(1));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(ItemTags.LEAVES, 10), MekanismItem.BIO_FUEL.getItemStack(1));
            //TODO: Should flowers instead produce their dye when crushed? (Or maybe use the enricher for that)
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(ItemTags.SMALL_FLOWERS), MekanismItem.BIO_FUEL.getItemStack(1));
            //TODO: Should this be bamboo sapling instead or can that not be gotten as an item
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Blocks.BAMBOO), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.SWEET_BERRIES), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.KELP), MekanismItem.BIO_FUEL.getItemStack(2));
            //TODO: Coral, and sweet berry bush?
        }

        //Purification Chamber Recipes
        if (MekanismBlock.PURIFICATION_CHAMBER.isEnabled()) {
            RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from(Tags.Items.GRAVEL), new ItemStack(Items.FLINT));
        }

        //Chemical Injection Chamber Recipes
        if (MekanismBlock.CHEMICAL_INJECTION_CHAMBER.isEnabled()) {
            RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(Blocks.DIRT), GasStackIngredient.from(MekanismTags.STEAM, 1), new ItemStack(Blocks.CLAY));
            RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(Blocks.TERRACOTTA), GasStackIngredient.from(MekanismTags.STEAM, 1), new ItemStack(Blocks.CLAY));
            RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(Tags.Items.INGOTS_BRICK), GasStackIngredient.from(MekanismTags.STEAM, 1), new ItemStack(Items.CLAY_BALL));
            RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(Tags.Items.GUNPOWDER), GasStackIngredient.from(MekanismTags.HYDROGEN_CHLORIDE, 1), MekanismItem.SULFUR_DUST.getItemStack());
        }

        //Precision Sawmill Recipes
        if (MekanismBlock.PRECISION_SAWMILL.isEnabled()) {
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.LADDER, 3), new ItemStack(Items.STICK, 7));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.TORCH, 4), new ItemStack(Items.STICK), new ItemStack(Items.COAL), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.JUKEBOX), new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.DIAMOND), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.BOOKSHELF), new ItemStack(Blocks.OAK_PLANKS, 6), new ItemStack(Items.BOOK, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.NOTE_BLOCK), new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.REDSTONE), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.REDSTONE_TORCH), new ItemStack(Items.STICK), new ItemStack(Items.REDSTONE), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.OAK_PLANKS, 4));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.CHEST), new ItemStack(Blocks.OAK_PLANKS, 8));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.TRAPPED_CHEST), new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Blocks.TRIPWIRE_HOOK), 0.75);
            //Trapdoors
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.ACACIA_TRAPDOOR), new ItemStack(Blocks.ACACIA_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.BIRCH_TRAPDOOR), new ItemStack(Blocks.BIRCH_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.DARK_OAK_TRAPDOOR), new ItemStack(Blocks.DARK_OAK_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.JUNGLE_TRAPDOOR), new ItemStack(Blocks.JUNGLE_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.OAK_TRAPDOOR), new ItemStack(Blocks.OAK_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.SPRUCE_TRAPDOOR), new ItemStack(Blocks.SPRUCE_PLANKS, 3));
            //Boats
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.ACACIA_BOAT), new ItemStack(Blocks.ACACIA_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.BIRCH_BOAT), new ItemStack(Blocks.BIRCH_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.DARK_OAK_BOAT), new ItemStack(Blocks.DARK_OAK_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.JUNGLE_BOAT), new ItemStack(Blocks.JUNGLE_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.OAK_BOAT), new ItemStack(Blocks.OAK_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.SPRUCE_BOAT), new ItemStack(Blocks.SPRUCE_PLANKS, 5));
            //Beds
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.WHITE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.WHITE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.ORANGE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.ORANGE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.MAGENTA_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.MAGENTA_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.LIGHT_BLUE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIGHT_BLUE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.YELLOW_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.YELLOW_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.LIME_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIME_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.PINK_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.PINK_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.GRAY_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.GRAY_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.LIGHT_GRAY_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIGHT_GRAY_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.CYAN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.CYAN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.PURPLE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.PURPLE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.BLUE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BLUE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.BROWN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BROWN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.GREEN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.GREEN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.RED_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.RED_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.BLACK_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BLACK_WOOL, 3), 1);
            //Doors
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.ACACIA_DOOR), new ItemStack(Blocks.ACACIA_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.BIRCH_DOOR), new ItemStack(Blocks.BIRCH_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.DARK_OAK_DOOR), new ItemStack(Blocks.DARK_OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.JUNGLE_DOOR), new ItemStack(Blocks.JUNGLE_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.OAK_DOOR), new ItemStack(Blocks.OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Items.SPRUCE_DOOR), new ItemStack(Blocks.SPRUCE_PLANKS, 2));
            //Pressure Plates
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.ACACIA_PRESSURE_PLATE), new ItemStack(Blocks.ACACIA_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.BIRCH_PRESSURE_PLATE), new ItemStack(Blocks.BIRCH_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.DARK_OAK_PRESSURE_PLATE), new ItemStack(Blocks.DARK_OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.JUNGLE_PRESSURE_PLATE), new ItemStack(Blocks.JUNGLE_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.OAK_PRESSURE_PLATE), new ItemStack(Blocks.OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.SPRUCE_PRESSURE_PLATE), new ItemStack(Blocks.SPRUCE_PLANKS, 2));
            //Fences
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Tags.Items.FENCES_WOODEN), new ItemStack(Items.STICK, 3));
            //Fence Gates
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.ACACIA_FENCE_GATE), new ItemStack(Blocks.ACACIA_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.BIRCH_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.DARK_OAK_FENCE_GATE), new ItemStack(Blocks.DARK_OAK_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.JUNGLE_FENCE_GATE), new ItemStack(Blocks.JUNGLE_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.OAK_FENCE_GATE), new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Blocks.SPRUCE_FENCE_GATE), new ItemStack(Blocks.SPRUCE_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
        }

        if (MekanismBlock.METALLURGIC_INFUSER.isEnabled()) {
            InfuseType carbon = MekanismInfuseTypes.CARBON.getInfuseType();
            InfuseType bio = MekanismInfuseTypes.BIO.getInfuseType();
            InfuseType redstone = MekanismInfuseTypes.REDSTONE.getInfuseType();
            InfuseType fungi = MekanismInfuseTypes.FUNGI.getInfuseType();
            InfuseType diamond = MekanismInfuseTypes.DIAMOND.getInfuseType();
            InfuseType obsidian = MekanismInfuseTypes.REFINED_OBSIDIAN.getInfuseType();

            //TODO: Make this be a proper recipe system
            //Infuse objects
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismItem.BIO_FUEL), new InfusionStack(bio, 5));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Items.COAL), new InfusionStack(carbon, 10));
            //TODO: Figure out why charcoal is twice as good as coal, if we make it be the same we can instead use the coals tag
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Items.CHARCOAL), new InfusionStack(carbon, 20));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Blocks.COAL_BLOCK), new InfusionStack(carbon, 90));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismBlock.CHARCOAL_BLOCK), new InfusionStack(carbon, 180));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_CARBON), new InfusionStack(carbon, 80));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE), new InfusionStack(redstone, 10));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Blocks.REDSTONE_BLOCK), new InfusionStack(redstone, 90));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_REDSTONE), new InfusionStack(redstone, 80));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Tags.Items.MUSHROOMS), new InfusionStack(fungi, 10));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_DIAMOND), new InfusionStack(diamond, 80));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_REFINED_OBSIDIAN), new InfusionStack(obsidian, 10));
            InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_OBSIDIAN), new InfusionStack(obsidian, 80));

            //Metallurgic Infuser Recipes
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(carbon, 10), ItemStackIngredient.from(Tags.Items.INGOTS_IRON), MekanismItem.ENRICHED_IRON.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(carbon, 10), ItemStackIngredient.from(MekanismItem.ENRICHED_IRON), MekanismItem.STEEL_DUST.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(redstone, 10), ItemStackIngredient.from(Tags.Items.INGOTS_IRON), MekanismItem.INFUSED_ALLOY.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(fungi, 10), ItemStackIngredient.from(Blocks.DIRT), new ItemStack(Blocks.MYCELIUM));
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(bio, 10), ItemStackIngredient.from(Blocks.COBBLESTONE), new ItemStack(Blocks.MOSSY_COBBLESTONE));
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(bio, 10), ItemStackIngredient.from(Blocks.STONE_BRICKS), new ItemStack(Blocks.MOSSY_STONE_BRICKS));
            //TODO: Should this be any sand?
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(bio, 10), ItemStackIngredient.from(Blocks.SAND), new ItemStack(Blocks.DIRT));
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(bio, 10), ItemStackIngredient.from(Blocks.DIRT), new ItemStack(Blocks.PODZOL));
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(diamond, 10), ItemStackIngredient.from(MekanismTags.ALLOYS_INFUSED), MekanismItem.REINFORCED_ALLOY.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(obsidian, 10), ItemStackIngredient.from(MekanismTags.ALLOYS_REINFORCED), MekanismItem.ATOMIC_ALLOY.getItemStack());
        }

        //Chemical Infuser Recipes
        if (MekanismBlock.CHEMICAL_INFUSER.isEnabled()) {
            RecipeHandler.addChemicalInfuserRecipe(GasStackIngredient.from(MekanismTags.OXYGEN, 1), GasStackIngredient.from(MekanismTags.SULFUR_DIOXIDE, 2),
                  MekanismGases.SULFUR_TRIOXIDE.getGasStack(2));
            RecipeHandler.addChemicalInfuserRecipe(GasStackIngredient.from(MekanismTags.SULFUR_TRIOXIDE, 1), GasStackIngredient.from(MekanismTags.STEAM, 1),
                  MekanismGases.SULFURIC_ACID.getGasStack(1));
            RecipeHandler.addChemicalInfuserRecipe(GasStackIngredient.from(MekanismTags.HYDROGEN, 1), GasStackIngredient.from(MekanismTags.CHLORINE, 1),
                  MekanismGases.HYDROGEN_CHLORIDE.getGasStack(1));
            RecipeHandler.addChemicalInfuserRecipe(GasStackIngredient.from(MekanismTags.DEUTERIUM, 1), GasStackIngredient.from(MekanismTags.TRITIUM, 1),
                  MekanismGases.FUSION_FUEL.getGasStack(2));
        }

        //Electrolytic Separator Recipes
        if (MekanismBlock.ELECTROLYTIC_SEPARATOR.isEnabled()) {
            RecipeHandler.addElectrolyticSeparatorRecipe(FluidStackIngredient.from(FluidTags.WATER, 2), 2 * MekanismConfig.general.FROM_H2.get(),
                  MekanismGases.HYDROGEN.getGasStack(2), MekanismGases.OXYGEN.getGasStack(1));
            RecipeHandler.addElectrolyticSeparatorRecipe(FluidStackIngredient.from(MekanismGases.BRINE.getFluid(), 10), 2 * MekanismConfig.general.FROM_H2.get(),
                  MekanismGases.SODIUM.getGasStack(1), MekanismGases.CHLORINE.getGasStack(1));
            RecipeHandler.addElectrolyticSeparatorRecipe(FluidStackIngredient.from(MekanismGases.HEAVY_WATER, 2), MekanismConfig.usage.heavyWaterElectrolysis.get(),
                  MekanismGases.DEUTERIUM.getGasStack(2), MekanismGases.OXYGEN.getGasStack(1));
        }

        //Thermal Evaporation Plant Recipes
        RecipeHandler.addThermalEvaporationRecipe(FluidStackIngredient.from(FluidTags.WATER, 10), new FluidStack(MekanismGases.BRINE.getFluid(), 1));
        RecipeHandler.addThermalEvaporationRecipe(FluidStackIngredient.from(MekanismGases.BRINE.getFluid(), 10), new FluidStack(MekanismGases.LITHIUM.getFluid(), 1));

        //Chemical Crystallizer Recipes
        if (MekanismBlock.CHEMICAL_CRYSTALLIZER.isEnabled()) {
            RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(MekanismTags.LITHIUM, 100), MekanismItem.SULFUR_DUST.getItemStack());
            RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(MekanismTags.BRINE, 15), MekanismItem.SALT.getItemStack());
        }

        //T4 Processing Recipes
        for (Gas gas : MekanismAPI.GAS_REGISTRY.getValues()) {
            if (gas instanceof Slurry && ((Slurry) gas).isDirty()) {
                Slurry slurry = (Slurry) gas;
                if (MekanismBlock.CHEMICAL_WASHER.isEnabled()) {
                    RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.from(FluidTags.WATER, 5), GasStackIngredient.from(slurry, 1),
                          new GasStack(slurry.getCleanSlurry(), 1));
                }

                if (MekanismBlock.CHEMICAL_CRYSTALLIZER.isEnabled()) {
                    //do the crystallizer only if it's one of our gases!
                    Resource gasResource = Resource.getFromName(slurry.getName());
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
                        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(slurry.getCleanSlurry(), 200), crystal.getItemStack());
                    }
                }
            }
        }

        //Pressurized Reaction Chamber Recipes
        if (MekanismBlock.PRESSURIZED_REACTION_CHAMBER.isEnabled()) {
            RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismItem.SUBSTRATE, 2), FluidStackIngredient.from(FluidTags.WATER, 10),
                  GasStackIngredient.from(MekanismTags.HYDROGEN, 100), MekanismItem.SUBSTRATE.getItemStack(), MekanismGases.ETHENE.getGasStack(100), 0, 100);
            RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismItem.SUBSTRATE), FluidStackIngredient.from(MekanismGases.ETHENE.getFluid(), 50),
                  GasStackIngredient.from(MekanismTags.OXYGEN, 10), MekanismItem.HDPE_PELLET.getItemStack(), MekanismGases.OXYGEN.getGasStack(5), 1000, 60);
            RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismItem.SUBSTRATE), FluidStackIngredient.from(FluidTags.WATER, 200),
                  GasStackIngredient.from(MekanismTags.ETHENE, 100), MekanismItem.SUBSTRATE.getItemStack(8), MekanismGases.OXYGEN.getGasStack(10), 200, 400);
            RecipeHandler.addPRCRecipe(ItemStackIngredient.from(ItemTags.COALS), FluidStackIngredient.from(FluidTags.WATER, 100),
                  GasStackIngredient.from(MekanismTags.OXYGEN, 100), MekanismItem.SULFUR_DUST.getItemStack(), MekanismGases.HYDROGEN.getGasStack(100), 0, 100);
        }

        //Solar Neutron Activator Recipes
        if (MekanismBlock.SOLAR_NEUTRON_ACTIVATOR.isEnabled()) {
            RecipeHandler.addSolarNeutronRecipe(GasStackIngredient.from(MekanismTags.LITHIUM, 1), MekanismGases.TRITIUM.getGasStack(1));
        }

        //Fuel Gases
        FuelHandler.addGas(MekanismTags.HYDROGEN, 1, MekanismConfig.general.FROM_H2.get());
    }

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        event.getServer().getResourceManager().addReloadListener(mekanismTagManager);
    }

    private void serverStarting(FMLServerStartingEvent event) {
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
            //The vanilla furnace does not support NBT so we use an ingredient that does not to ensure that things behave properly
            Recipe.ENERGIZED_SMELTER.put(new ItemStackToItemStackRecipe(ItemStackIngredient.from(Ingredient.fromStacks(entry.getKey())), entry.getValue()));
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
        if (event.getWorld() != null && !event.getWorld().isRemote()) {
            CompoundNBT nbtTags = event.getData();

            nbtTags.putInt("MekanismWorldGen", baseWorldGenVersion);
            nbtTags.putInt("MekanismUserWorldGen", MekanismConfig.general.userWorldGenVersion.get());
        }
    }

    private synchronized void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (event.getWorld() != null && !event.getWorld().isRemote()) {
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