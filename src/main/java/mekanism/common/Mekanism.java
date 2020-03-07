package mekanism.common;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
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
import mekanism.common.config.MekanismModConfig;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.recipe.RecipeCacheManager;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismPlacements;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tags.MekanismTagManager;
import mekanism.common.transmitters.grid.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.transmitters.grid.FluidNetwork.FluidTransferEvent;
import mekanism.common.transmitters.grid.GasNetwork.GasTransferEvent;
import mekanism.common.world.GenHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Mekanism.MODID)
public class Mekanism {

    public static final String MODID = MekanismAPI.MEKANISM_MODID;
    public static final String MOD_NAME = "Mekanism";
    public static final String LOG_TAG = '[' + MOD_NAME + ']';
    public static final PlayerState playerState = new PlayerState();
    public static final Set<UUID> freeRunnerOn = new ObjectOpenHashSet<>();
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
    public final Version versionNumber;
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
    public static Map<UUID, FrequencyManager> privateTeleporters = new Object2ObjectOpenHashMap<>();
    public static FrequencyManager publicEntangloporters = new FrequencyManager(InventoryFrequency.class, InventoryFrequency.ENTANGLOPORTER);
    public static Map<UUID, FrequencyManager> privateEntangloporters = new Object2ObjectOpenHashMap<>();
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
    public static int baseWorldGenVersion = 0;//TODO: Just remove this?
    /**
     * The GameProfile used by the dummy Mekanism player
     */
    public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes()), Mekanism.LOG_TAG);
    public static KeySync keyMap = new KeySync();
    public static Set<Coord4D> activeVibrators = new ObjectOpenHashSet<>();

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
        MinecraftForge.EVENT_BUS.addListener(this::chunkSave);
        MinecraftForge.EVENT_BUS.addListener(this::onChunkDataLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldUnload);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::imcQueue);
        //TODO: Register other listeners and various stuff that is needed

        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::serverAboutToStartLowest);
        MinecraftForge.EVENT_BUS.addListener(BinInsertRecipe::onCrafting);

        MekanismItems.ITEMS.register(modEventBus);
        MekanismBlocks.BLOCKS.register(modEventBus);
        MekanismFluids.FLUIDS.register(modEventBus);
        MekanismContainerTypes.CONTAINER_TYPES.register(modEventBus);
        MekanismEntityTypes.ENTITY_TYPES.register(modEventBus);
        MekanismTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MekanismSounds.SOUND_EVENTS.register(modEventBus);
        MekanismParticleTypes.PARTICLE_TYPES.register(modEventBus);
        MekanismPlacements.PLACEMENTS.register(modEventBus);
        MekanismFeatures.FEATURES.register(modEventBus);
        MekanismRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        MekanismInfuseTypes.INFUSE_TYPES.register(modEventBus);
        MekanismGases.GASES.register(modEventBus);
        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(Mekanism.MODID, path);
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

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        IReloadableResourceManager resourceManager = event.getServer().getResourceManager();
        boolean added = false;
        if (resourceManager instanceof SimpleReloadableResourceManager) {
            //Note: We "hack" it so that our tag manager gets registered directly after the normal tag manager
            // to ensure that it is before the recipe manager and that the custom tags can be properly resolved
            //TODO: It would make sense to eventually make a PR to forge to make custom tags easier to do
            SimpleReloadableResourceManager manager = (SimpleReloadableResourceManager) resourceManager;
            for (int i = 0; i < manager.reloadListeners.size(); i++) {
                IFutureReloadListener listener = manager.reloadListeners.get(i);
                if (listener instanceof NetworkTagManager) {
                    manager.reloadListeners.add(i + 1, getTagManager());
                    added = true;
                    break;
                }
            }
        }
        if (!added) {
            //Fallback to trying to just add it even though this is probably too late to do so properly
            resourceManager.addReloadListener(getTagManager());
        }
    }

    private void serverAboutToStartLowest(FMLServerAboutToStartEvent event) {
        //Note: We register reload listeners here which we want to make sure run after CraftTweaker or any other mods that may modify recipes
        event.getServer().getResourceManager().addReloadListener(getRecipeCacheManager());
    }

    private void serverStarting(FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(CommandMek.register());
        //TODO: Do we care about the alternates of mtp, and mtpop
    }

    private void serverStopped(FMLServerStoppedEvent event) {
        //Clear all cache data, wait until server stopper though so that we make sure saving can use any data it needs
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

    private void imcQueue(InterModEnqueueEvent event) {
        hooks.sendIMCMessages(event);
    }

    public void preInit() {
        //TODO: Find proper stage for this
        //sanity check the api location if not deobf
        //TODO: Check API
        /*if (!FMLLoader.getNameFunction("srg").isPresent()) {
            String apiLocation = MekanismAPI.class.getProtectionDomain().getCodeSource().getLocation().toString();
            if (apiLocation.toLowerCase(Locale.ROOT).contains("-api.jar")) {
                proxy.throwApiPresentException();
            }
        }*/

        hooks.hookPreInit();

        Capabilities.registerCapabilities();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //TODO: Figure out where preinit stuff should be, potentially also move it directly into this method
        preInit();

        //TODO: Make recipes be done from JSON
        //TODO: Bin recipe
        //event.getRegistry().register(new BinRecipe());
        //Fuel Gases
        FuelHandler.addGas(MekanismGases.HYDROGEN, 1, MekanismConfig.general.FROM_H2.get());

        //Register the mod's world generators
        GenHandler.setupWorldGeneration();

        //Register player tracker
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTickHandler());

        //Initialization notification
        logger.info("Version " + versionNumber + " initializing...");

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

        //Completion notification
        logger.info("Loading complete.");

        //Success message
        logger.info("Mod loaded.");

        //TODO: Use FMLDedicatedServerSetupEvent and FMLClientSetupEvent
    }

    private void onEnergyTransferred(EnergyTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.ENERGY, event.energyNetwork.firstTransmitter().coord(), event.power), event.energyNetwork);
        } catch (Exception ignored) {
        }
    }

    private void onGasTransferred(GasTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.GAS, event.gasNetwork.firstTransmitter().coord(), event.transferType, event.didTransfer),
                  event.gasNetwork);
        } catch (Exception ignored) {
        }
    }

    private void onLiquidTransferred(FluidTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.FLUID, event.fluidNetwork.firstTransmitter().coord(), event.fluidType, event.didTransfer),
                  event.fluidNetwork);
        } catch (Exception ignored) {
        }
    }

    private void onTransmittersAddedEvent(TransmittersAddedEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.UPDATE, event.network.firstTransmitter().coord(), event.newNetwork, event.newTransmitters),
                  event.network);
        } catch (Exception ignored) {
        }
    }

    private void onNetworkClientRequest(NetworkClientRequest event) {
        try {
            packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(event.tile)));
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

    private void chunkSave(ChunkDataEvent.Save event) {
        if (event.getWorld() != null && !event.getWorld().isRemote()) {
            CompoundNBT nbtTags = event.getData();
            nbtTags.putInt(NBTConstants.WORLD_GEN, baseWorldGenVersion);
            nbtTags.putInt(NBTConstants.WORLD_GEN_VERSION, MekanismConfig.world.userGenVersion.get());
        }
    }

    private synchronized void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (event.getWorld() != null && !event.getWorld().isRemote()) {
            if (MekanismConfig.world.enableRegeneration.get()) {
                CompoundNBT loadData = event.getData();
                if (loadData.getInt(NBTConstants.WORLD_GEN) != baseWorldGenVersion || loadData.getInt(NBTConstants.WORLD_GEN_VERSION) != MekanismConfig.world.userGenVersion.get()) {
                    worldTickHandler.addRegenChunk(event.getWorld().getDimension().getType(), event.getChunk().getPos());
                }
            }
        }
    }

    private void onConfigLoad(ModConfig.ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, so as to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig) {
            ((MekanismModConfig) config).clearCache();
        }
    }

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