package mekanism.common;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.gear.ModuleData;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.providers.IItemProvider;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.common.base.IModModule;
import mekanism.common.base.KeySync;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.base.PlayerState;
import mekanism.common.base.TagCache;
import mekanism.common.command.CommandMek;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.command.builders.Builders.BoilerBuilder;
import mekanism.common.command.builders.Builders.EvaporationBuilder;
import mekanism.common.command.builders.Builders.MatrixBuilder;
import mekanism.common.command.builders.Builders.SPSBuilder;
import mekanism.common.command.builders.Builders.TankBuilder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerValidator;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.evaporation.EvaporationValidator;
import mekanism.common.content.gear.MekaSuitDispenseBehavior;
import mekanism.common.content.gear.ModuleDispenseBehavior;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.matrix.MatrixValidator;
import mekanism.common.content.network.BoxedChemicalNetwork.ChemicalTransferEvent;
import mekanism.common.content.network.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.content.network.FluidNetwork.FluidTransferEvent;
import mekanism.common.content.sps.SPSCache;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSValidator;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankValidator;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.item.block.machine.ItemBlockFluidTank.BasicCauldronInteraction;
import mekanism.common.item.block.machine.ItemBlockFluidTank.BasicDrainCauldronInteraction;
import mekanism.common.item.block.machine.ItemBlockFluidTank.FluidTankItemDispenseBehavior;
import mekanism.common.lib.MekAnnotationScanner;
import mekanism.common.lib.Version;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.PacketHandler;
import mekanism.common.network.to_client.PacketTransmitterUpdate;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.common.recipe.condition.ModVersionLoadedCondition;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismHeightProviderTypes;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismIntProviderTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.registries.MekanismPlacementModifiers;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismSlurries;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.TileComponentChunkLoader.ChunkValidationCallback;
import mekanism.common.tile.machine.TileEntityOredictionificator.ODConfigValueInvalidationListener;
import mekanism.common.world.GenHandler;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Mekanism.MODID)
public class Mekanism {

    public static final String MODID = MekanismAPI.MEKANISM_MODID;
    public static final String MOD_NAME = "Mekanism";
    public static final String LOG_TAG = '[' + MOD_NAME + ']';
    public static final PlayerState playerState = new PlayerState();
    /**
     * Mekanism Packet Pipeline
     */
    private final PacketHandler packetHandler;
    /**
     * Mekanism logger instance
     */
    public static final Logger logger = LogManager.getLogger(MOD_NAME);

    /**
     * Mekanism mod instance
     */
    public static Mekanism instance;
    /**
     * Mekanism hooks instance
     */
    public static final MekanismHooks hooks = new MekanismHooks();
    /**
     * Mekanism version number
     */
    public final Version versionNumber;
    /**
     * MultiblockManagers for various structures
     */
    public static final MultiblockManager<TankMultiblockData> tankManager = new MultiblockManager<>("dynamicTank", TankCache::new, TankValidator::new);
    public static final MultiblockManager<MatrixMultiblockData> matrixManager = new MultiblockManager<>("inductionMatrix", MultiblockCache::new, MatrixValidator::new);
    public static final MultiblockManager<BoilerMultiblockData> boilerManager = new MultiblockManager<>("thermoelectricBoiler", MultiblockCache::new, BoilerValidator::new);
    public static final MultiblockManager<EvaporationMultiblockData> evaporationManager = new MultiblockManager<>("evaporation", MultiblockCache::new, EvaporationValidator::new);
    public static final MultiblockManager<SPSMultiblockData> spsManager = new MultiblockManager<>("sps", SPSCache::new, SPSValidator::new);
    /**
     * Mekanism creative tab
     */
    public static final CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
    /**
     * List of Mekanism modules loaded
     */
    public static final List<IModModule> modulesLoaded = new ArrayList<>();
    /**
     * The server's world tick handler.
     */
    public static final CommonWorldTickHandler worldTickHandler = new CommonWorldTickHandler();
    /**
     * The GameProfile used by the dummy Mekanism player
     */
    public static final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes(StandardCharsets.UTF_8)), Mekanism.LOG_TAG);
    public static final KeySync keyMap = new KeySync();
    public static final Set<Coord4D> activeVibrators = new ObjectOpenHashSet<>();

    private ReloadListener recipeCacheManager;

    public Mekanism() {
        instance = this;
        MekanismConfig.registerConfigs(ModLoadingContext.get());

        MinecraftForge.EVENT_BUS.addListener(this::onEnergyTransferred);
        MinecraftForge.EVENT_BUS.addListener(this::onChemicalTransferred);
        MinecraftForge.EVENT_BUS.addListener(this::onLiquidTransferred);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldUnload);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::addReloadListenersLowest);
        MinecraftForge.EVENT_BUS.addListener(BinInsertRecipe::onCrafting);
        MinecraftForge.EVENT_BUS.addListener(this::onTagsReload);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, GenHandler::onBiomeLoad);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::imcQueue);
        modEventBus.addListener(this::imcHandle);
        MekanismItems.ITEMS.register(modEventBus);
        MekanismBlocks.BLOCKS.register(modEventBus);
        MekanismFluids.FLUIDS.register(modEventBus);
        MekanismContainerTypes.CONTAINER_TYPES.register(modEventBus);
        MekanismEntityTypes.ENTITY_TYPES.register(modEventBus);
        MekanismTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MekanismSounds.SOUND_EVENTS.register(modEventBus);
        MekanismParticleTypes.PARTICLE_TYPES.register(modEventBus);
        MekanismHeightProviderTypes.HEIGHT_PROVIDER_TYPES.register(modEventBus);
        MekanismIntProviderTypes.INT_PROVIDER_TYPES.register(modEventBus);
        MekanismPlacementModifiers.PLACEMENT_MODIFIERS.register(modEventBus);
        MekanismFeatures.FEATURES.register(modEventBus);
        MekanismFeatures.SETUP_FEATURES.register(modEventBus);
        MekanismRecipeType.RECIPE_TYPES.register(modEventBus);
        MekanismRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        MekanismDataSerializers.DATA_SERIALIZERS.register(modEventBus);
        MekanismGases.GASES.createAndRegister(modEventBus, Gas.class, builder -> builder.hasTags().setDefaultKey(rl("empty")));
        MekanismInfuseTypes.INFUSE_TYPES.createAndRegister(modEventBus, InfuseType.class, builder -> builder.hasTags().setDefaultKey(rl("empty")));
        MekanismPigments.PIGMENTS.createAndRegister(modEventBus, Pigment.class, builder -> builder.hasTags().setDefaultKey(rl("empty")));
        MekanismSlurries.SLURRIES.createAndRegister(modEventBus, Slurry.class, builder -> builder.hasTags().setDefaultKey(rl("empty")));
        MekanismRobitSkins.ROBIT_SKINS.createAndRegister(modEventBus, RobitSkin.class, builder -> builder.setDefaultKey(rl("robit")));
        //noinspection rawtypes,unchecked
        MekanismModules.MODULES.createAndRegister(modEventBus, (Class) ModuleData.class);
        modEventBus.addGenericListener(Gas.class, this::registerGases);
        modEventBus.addGenericListener(InfuseType.class, this::registerInfuseTypes);
        modEventBus.addGenericListener(Pigment.class, this::registerPigments);
        modEventBus.addGenericListener(Slurry.class, this::registerSlurries);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
        packetHandler = new PacketHandler();
        //Super early hooks, only reliable thing is for checking dependencies that we declare we are after
        hooks.hookConstructor(modEventBus);
        if (hooks.CraftTweakerLoaded && !DatagenModLoader.isRunningDataGen()) {
            //Attempt to grab the mod event bus for CraftTweaker so that we can register our custom content in their namespace
            // to make it clearer which chemicals were added by CraftTweaker, and which are added by actual mods.
            // Gracefully fallback to our event bus if something goes wrong with getting CrT's and just then have the log have
            // warnings about us registering things in their namespace.
            IEventBus crtModEventBus = modEventBus;
            Optional<? extends ModContainer> crtModContainer = ModList.get().getModContainerById(MekanismHooks.CRAFTTWEAKER_MOD_ID);
            if (crtModContainer.isPresent()) {
                ModContainer container = crtModContainer.get();
                if (container instanceof FMLModContainer modContainer) {
                    crtModEventBus = modContainer.getEventBus();
                }
            }
            //Register these at lowest priority to try and ensure they get later ids in the chemical registries
            crtModEventBus.addGenericListener(Gas.class, EventPriority.LOWEST, CrTContentUtils::registerCrTGases);
            crtModEventBus.addGenericListener(InfuseType.class, EventPriority.LOWEST, CrTContentUtils::registerCrTInfuseTypes);
            crtModEventBus.addGenericListener(Pigment.class, EventPriority.LOWEST, CrTContentUtils::registerCrTPigments);
            crtModEventBus.addGenericListener(Slurry.class, EventPriority.LOWEST, CrTContentUtils::registerCrTSlurries);
            crtModEventBus.addGenericListener(RobitSkin.class, EventPriority.LOWEST, CrTContentUtils::registerCrTRobitSkins);
        }
    }

    public static PacketHandler packetHandler() {
        return instance.packetHandler;
    }

    //Register the empty chemicals
    private void registerGases(RegistryEvent.Register<Gas> event) {
        MekanismAPI.EMPTY_GAS.setRegistryName(rl("empty"));
        event.getRegistry().register(MekanismAPI.EMPTY_GAS);
    }

    private void registerInfuseTypes(RegistryEvent.Register<InfuseType> event) {
        MekanismAPI.EMPTY_INFUSE_TYPE.setRegistryName(rl("empty"));
        event.getRegistry().register(MekanismAPI.EMPTY_INFUSE_TYPE);
    }

    private void registerPigments(RegistryEvent.Register<Pigment> event) {
        MekanismAPI.EMPTY_PIGMENT.setRegistryName(rl("empty"));
        event.getRegistry().register(MekanismAPI.EMPTY_PIGMENT);
    }

    private void registerSlurries(RegistryEvent.Register<Slurry> event) {
        MekanismAPI.EMPTY_SLURRY.setRegistryName(rl("empty"));
        event.getRegistry().register(MekanismAPI.EMPTY_SLURRY);
    }

    private void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        CraftingHelper.register(ModVersionLoadedCondition.Serializer.INSTANCE);
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(Mekanism.MODID, path);
    }

    private void setRecipeCacheManager(ReloadListener manager) {
        if (recipeCacheManager == null) {
            recipeCacheManager = manager;
        } else {
            logger.warn("Recipe cache manager has already been set.");
        }
    }

    public ReloadListener getRecipeCacheManager() {
        return recipeCacheManager;
    }

    private void onTagsReload(TagsUpdatedEvent event) {
        TagCache.resetTagCaches();
    }

    private void addReloadListenersLowest(AddReloadListenerEvent event) {
        //Note: We register reload listeners here which we want to make sure run after CraftTweaker or any other mods that may modify recipes or loot tables
        event.addListener(getRecipeCacheManager());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        BuildCommand.register("boiler", MekanismLang.BOILER, new BoilerBuilder());
        BuildCommand.register("matrix", MekanismLang.MATRIX, new MatrixBuilder());
        BuildCommand.register("tank", MekanismLang.DYNAMIC_TANK, new TankBuilder());
        BuildCommand.register("evaporation", MekanismLang.EVAPORATION_PLANT, new EvaporationBuilder());
        BuildCommand.register("sps", MekanismLang.SPS, new SPSBuilder());
        event.getDispatcher().register(CommandMek.register());
    }

    private void serverStopped(ServerStoppedEvent event) {
        //Clear all cache data, wait until server stopper though so that we make sure saving can use any data it needs
        playerState.clear(false);
        activeVibrators.clear();
        worldTickHandler.resetChunkData();
        FrequencyType.clear();
        BoilerMultiblockData.hotMap.clear();

        //Reset consistent managers
        RadiationManager.INSTANCE.reset();
        MultiblockManager.reset();
        FrequencyManager.reset();
        TransporterManager.reset();
        PathfinderCache.reset();
        TransmitterNetworkRegistry.reset();
    }

    private void imcQueue(InterModEnqueueEvent event) {
        //IMC messages we send to other mods
        hooks.sendIMCMessages(event);
        //IMC messages that we are sending to ourselves
        MekanismIMC.addModulesToAll(MekanismModules.ENERGY_UNIT);
        MekanismIMC.addMekaSuitModules(MekanismModules.LASER_DISSIPATION_UNIT, MekanismModules.RADIATION_SHIELDING_UNIT);
        MekanismIMC.addMekaToolModules(MekanismModules.ATTACK_AMPLIFICATION_UNIT, MekanismModules.SILK_TOUCH_UNIT, MekanismModules.VEIN_MINING_UNIT,
              MekanismModules.FARMING_UNIT, MekanismModules.SHEARING_UNIT, MekanismModules.TELEPORTATION_UNIT, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        MekanismIMC.addMekaSuitHelmetModules(MekanismModules.ELECTROLYTIC_BREATHING_UNIT, MekanismModules.INHALATION_PURIFICATION_UNIT,
              MekanismModules.VISION_ENHANCEMENT_UNIT, MekanismModules.NUTRITIONAL_INJECTION_UNIT);
        MekanismIMC.addMekaSuitBodyarmorModules(MekanismModules.JETPACK_UNIT, MekanismModules.GRAVITATIONAL_MODULATING_UNIT, MekanismModules.CHARGE_DISTRIBUTION_UNIT,
              MekanismModules.DOSIMETER_UNIT, MekanismModules.GEIGER_UNIT, MekanismModules.ELYTRA_UNIT);
        MekanismIMC.addMekaSuitPantsModules(MekanismModules.LOCOMOTIVE_BOOSTING_UNIT, MekanismModules.GYROSCOPIC_STABILIZATION_UNIT);
        MekanismIMC.addMekaSuitBootsModules(MekanismModules.HYDRAULIC_PROPULSION_UNIT, MekanismModules.MAGNETIC_ATTRACTION_UNIT, MekanismModules.FROST_WALKER_UNIT);
    }

    private void imcHandle(InterModProcessEvent event) {
        ModuleHelper.INSTANCE.processIMC();
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IGasHandler.class);
        event.register(IInfusionHandler.class);
        event.register(IPigmentHandler.class);
        event.register(ISlurryHandler.class);
        event.register(IHeatHandler.class);
        event.register(IStrictEnergyHandler.class);

        event.register(IConfigurable.class);
        event.register(IAlloyInteraction.class);
        event.register(IConfigCardAccess.class);
        event.register(IEvaporationSolar.class);
        event.register(ILaserReceptor.class);
        event.register(ILaserDissipation.class);

        event.register(IRadiationShielding.class);
        event.register(IRadiationEntity.class);

        event.register(IOwnerObject.class);
        event.register(ISecurityObject.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //Initialization notification
        logger.info("Version {} initializing...", versionNumber);
        hooks.hookCommonSetup();
        setRecipeCacheManager(new ReloadListener());

        event.enqueueWork(() -> {
            //Ensure our tags are all initialized
            MekanismTags.init();
            //Collect annotation scan data
            MekAnnotationScanner.collectScanData();
            //Add chunk loading callbacks
            ForgeChunkManager.setForcedChunkLoadingCallback(Mekanism.MODID, ChunkValidationCallback.INSTANCE);
            //Register dispenser behaviors
            MekanismFluids.FLUIDS.registerBucketDispenserBehavior();
            registerFluidTankBehaviors(MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK,
                  MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.CREATIVE_FLUID_TANK);
            registerDispenseBehavior(new ModuleDispenseBehavior(), MekanismItems.MEKA_TOOL);
            registerDispenseBehavior(new MekaSuitDispenseBehavior(), MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS,
                  MekanismItems.MEKASUIT_BOOTS);
        });

        //Register player tracker
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTickHandler());
        MinecraftForge.EVENT_BUS.register(Mekanism.worldTickHandler);

        MinecraftForge.EVENT_BUS.register(RadiationManager.INSTANCE);

        //Register with TransmitterNetworkRegistry
        TransmitterNetworkRegistry.initiate();

        //Packet registrations
        packetHandler.initialize();

        //Fake player info
        logger.info("Fake player readout: UUID = {}, name = {}", gameProfile.getId(), gameProfile.getName());
        logger.info("Mod loaded.");
    }

    private static void registerDispenseBehavior(DispenseItemBehavior behavior, IItemProvider... itemProviders) {
        for (IItemProvider itemProvider : itemProviders) {
            DispenserBlock.registerBehavior(itemProvider.asItem(), behavior);
        }
    }

    private static void registerFluidTankBehaviors(IItemProvider... itemProviders) {
        registerDispenseBehavior(FluidTankItemDispenseBehavior.INSTANCE);
        for (IItemProvider itemProvider : itemProviders) {
            Item item = itemProvider.asItem();
            CauldronInteraction.EMPTY.put(item, BasicCauldronInteraction.EMPTY);
            CauldronInteraction.WATER.put(item, BasicDrainCauldronInteraction.WATER);
            CauldronInteraction.LAVA.put(item, BasicDrainCauldronInteraction.LAVA);
        }
    }

    private void onEnergyTransferred(EnergyTransferEvent event) {
        packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network), event.network);
    }

    private void onChemicalTransferred(ChemicalTransferEvent event) {
        packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network, event.transferType), event.network);
    }

    private void onLiquidTransferred(FluidTransferEvent event) {
        packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network, event.fluidType), event.network);
    }

    private void onConfigLoad(ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig mekConfig) {
            mekConfig.clearCache();
        }
    }

    private void onWorldLoad(WorldEvent.Load event) {
        playerState.init(event.getWorld());
    }

    private void onWorldUnload(WorldEvent.Unload event) {
        // Make sure the global fake player drops its reference to the World
        // when the server shuts down
        if (event.getWorld() instanceof ServerLevel) {
            MekFakePlayer.releaseInstance(event.getWorld());
        }
        if (event.getWorld() instanceof Level level && MekanismConfig.general.validOredictionificatorFilters.hasInvalidationListeners()) {
            //Remove any invalidation listeners that loaded oredictionificators might have added if the OD was in the given level
            MekanismConfig.general.validOredictionificatorFilters.removeInvalidationListenersMatching(listener ->
                  listener instanceof ODConfigValueInvalidationListener odListener && odListener.isIn(level));
        }
    }
}