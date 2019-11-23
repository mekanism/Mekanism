package mekanism.common;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfusionStack;
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
import mekanism.common.entity.MekanismEntityTypes;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.integration.IMCHandler;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.OreDictManager;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.particle.MekanismParticleType;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.RecipeCacheManager;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tags.MekanismTagManager;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.transmitters.grid.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.transmitters.grid.FluidNetwork.FluidTransferEvent;
import mekanism.common.transmitters.grid.GasNetwork.GasTransferEvent;
import mekanism.common.world.GenHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
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
    //TODO: Remove need for having a proxy as it is the legacy way of doing things
    //Note: Do not replace with method reference: https://gist.github.com/williewillus/353c872bcf1a6ace9921189f6100d09a#gistcomment-2876130
    public static CommonProxy proxy = DistExecutor.runForDist(() -> getClientProxy(), () -> () -> new CommonProxy());

    //TODO: Remove unneeded OnlyIn calls throughout everywhere
    @OnlyIn(Dist.CLIENT)
    private static Supplier<CommonProxy> getClientProxy() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return ClientProxy::new;
    }

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
    private RecipeCacheManager recipeCacheManager;

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

        MekanismItem.ITEMS.register(modEventBus);
        MekanismBlock.BLOCKS.register(modEventBus);
        MekanismFluids.FLUIDS.register(modEventBus);
        MekanismContainerTypes.CONTAINER_TYPES.register(modEventBus);
        MekanismEntityTypes.ENTITY_TYPES.register(modEventBus);
        MekanismTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MekanismSounds.SOUND_EVENTS.register(modEventBus);
        MekanismParticleType.PARTICLE_TYPES.register(modEventBus);
        MekanismRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        //Delay adding the deferred registers for infuse types and gases until after their registries are actually assigned
        modEventBus.addListener(EventPriority.LOW, this::addCustomRegistryDeferredRegisters);
    }

    private void addCustomRegistryDeferredRegisters(RegistryEvent.NewRegistry event) {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MekanismInfuseTypes.INFUSE_TYPES.register(modEventBus);
        MekanismGases.GASES.register(modEventBus);
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

    public void setRecipeCacheManager(RecipeCacheManager manager) {
        if (recipeCacheManager == null) {
            recipeCacheManager = manager;
        }
        //TODO: Else throw error
    }

    public RecipeCacheManager getRecipeCacheManager() {
        return recipeCacheManager;
    }

    /**
     * Adds all in-game crafting, smelting and machine recipes.
     */
    public static void addRecipes() {
        //TODO: Make this be a proper recipe system
        //Infuse objects
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Items.COAL), new InfusionStack(MekanismInfuseTypes.CARBON, 10));
        //TODO: Figure out why charcoal is twice as good as coal, if we make it be the same we can instead use the coals tag
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Items.CHARCOAL), new InfusionStack(MekanismInfuseTypes.CARBON, 20));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Blocks.COAL_BLOCK), new InfusionStack(MekanismInfuseTypes.CARBON, 90));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismBlock.CHARCOAL_BLOCK), new InfusionStack(MekanismInfuseTypes.CARBON, 180));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_CARBON), new InfusionStack(MekanismInfuseTypes.CARBON, 80));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE), new InfusionStack(MekanismInfuseTypes.REDSTONE, 10));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Blocks.REDSTONE_BLOCK), new InfusionStack(MekanismInfuseTypes.REDSTONE, 90));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_REDSTONE), new InfusionStack(MekanismInfuseTypes.REDSTONE, 80));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_DIAMOND), new InfusionStack(MekanismInfuseTypes.DIAMOND, 10));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_DIAMOND), new InfusionStack(MekanismInfuseTypes.DIAMOND, 80));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_REFINED_OBSIDIAN), new InfusionStack(MekanismInfuseTypes.REFINED_OBSIDIAN, 10));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.ENRICHED_OBSIDIAN), new InfusionStack(MekanismInfuseTypes.REFINED_OBSIDIAN, 80));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismItem.BIO_FUEL), new InfusionStack(MekanismInfuseTypes.BIO, 5));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(Tags.Items.MUSHROOMS), new InfusionStack(MekanismInfuseTypes.FUNGI, 10));
        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_TIN), new InfusionStack(MekanismInfuseTypes.TIN, 10));

        //Fuel Gases
        FuelHandler.addGas(MekanismTags.HYDROGEN, 1, MekanismConfig.general.FROM_H2.get());
    }

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        IReloadableResourceManager resourceManager = event.getServer().getResourceManager();
        resourceManager.addReloadListener(getTagManager());
        //TODO: Move this reload listener to LOWEST so that it happens after CrT?
        resourceManager.addReloadListener(getRecipeCacheManager());
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
        //proxy.loadConfiguration();
        proxy.onConfigSync(false);
        hooks.hookPreInit();

        //TODO: Multipart
        /*if (hooks.MCMPLoaded) {
            //Set up multiparts
            new MultipartMekanism();
        } else {
            logger.info("Didn't detect MCMP, ignoring compatibility package");
        }*/

        Capabilities.registerCapabilities();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //TODO: Figure out where preinit stuff should be, potentially also move it directly into this method
        preInit();

        //TODO: Make recipes be done from JSON
        //TODO: Bin recipe
        //event.getRegistry().register(new BinRecipe());
        addRecipes();
        OreDictManager.init();
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

        //Load this module
        hooks.hookCommonSetup();

        //Packet registrations
        packetHandler.initialize();

        //Load proxy
        proxy.init();

        //Fake player info
        logger.info("Fake player readout: UUID = " + gameProfile.getId().toString() + ", name = " + gameProfile.getName());

        //TODO
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

        //TODO
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