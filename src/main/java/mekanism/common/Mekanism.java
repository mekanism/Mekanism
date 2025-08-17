package mekanism.common;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.base.IModModule;
import mekanism.common.base.KeySync;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.base.PlayerState;
import mekanism.common.base.TagCache;
import mekanism.common.base.holiday.HolidayManager;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.command.CommandMek;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.command.builders.Builders.BoilerBuilder;
import mekanism.common.command.builders.Builders.EvaporationBuilder;
import mekanism.common.command.builders.Builders.MatrixBuilder;
import mekanism.common.command.builders.Builders.SPSBuilder;
import mekanism.common.command.builders.Builders.TankBuilder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerValidator;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.evaporation.EvaporationValidator;
import mekanism.common.content.gear.MekaSuitDispenseBehavior;
import mekanism.common.content.gear.ModuleDispenseBehavior;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.matrix.MatrixValidator;
import mekanism.common.content.network.ChemicalNetwork.ChemicalTransferEvent;
import mekanism.common.content.network.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.content.network.FluidNetwork.FluidTransferEvent;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.content.sps.SPSCache;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSValidator;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankValidator;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.block.machine.ItemBlockFluidTank.BasicCauldronInteraction;
import mekanism.common.item.block.machine.ItemBlockFluidTank.BasicDrainCauldronInteraction;
import mekanism.common.item.block.machine.ItemBlockFluidTank.FluidTankItemDispenseBehavior;
import mekanism.common.item.interfaces.IHasConditionalAttributes;
import mekanism.common.item.loot.MekanismLootFunctions;
import mekanism.common.item.predicate.MekanismItemPredicates;
import mekanism.common.lib.MekAnnotationScanner;
import mekanism.common.lib.Version;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.PlayerExposure;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.PacketHandler;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.transmitter.PacketChemicalNetworkContents;
import mekanism.common.network.to_client.transmitter.PacketFluidNetworkContents;
import mekanism.common.network.to_client.transmitter.PacketNetworkScale;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.condition.MekanismRecipeConditions;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicalIngredientTypes;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismDataMapTypes;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismHeightProviderTypes;
import mekanism.common.registries.MekanismIntProviderTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismPlacementModifiers;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.machine.TileEntityOredictionificator.ODConfigValueInvalidationListener;
import mekanism.common.world.GenHandler;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import org.slf4j.Logger;

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
    public static final Logger logger = LogUtils.getLogger();

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
    public static final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes(StandardCharsets.UTF_8)), LOG_TAG);
    public static final KeySync keyMap = new KeySync();
    public static final Set<GlobalPos> activeVibrators = new ObjectOpenHashSet<>();

    private ReloadListener recipeCacheManager;

    public Mekanism(ModContainer modContainer, IEventBus modEventBus) {
        instance = this;
        //Set our version number to match the neoforge.mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(modContainer);
        MekanismConfig.registerConfigs(modContainer);

        NeoForgeMod.enableMilkFluid();
        NeoForge.EVENT_BUS.addListener(this::onEnergyTransferred);
        NeoForge.EVENT_BUS.addListener(this::onChemicalTransferred);
        NeoForge.EVENT_BUS.addListener(this::onLiquidTransferred);
        NeoForge.EVENT_BUS.addListener(this::onModifyItemAttributes);
        NeoForge.EVENT_BUS.addListener(this::onWorldLoad);
        NeoForge.EVENT_BUS.addListener(this::onWorldUnload);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::serverStopped);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::addReloadListenersLowest);
        NeoForge.EVENT_BUS.addListener(this::onTagsReload);
        NeoForge.EVENT_BUS.addListener(this::onDataMapsUpdated);
        NeoForge.EVENT_BUS.addListener(MekanismPermissions::registerPermissionNodes);
        NeoForge.EVENT_BUS.register(IncompleteRecipeScanner.class);
        modEventBus.addListener(EventPriority.HIGH, Capabilities::registerProxyableCapabilities);
        modEventBus.addListener(Capabilities::registerCapabilities);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerChunkTicketControllers);
        modEventBus.addListener(MekanismConfig::onConfigLoad);
        modEventBus.addListener(this::imcQueue);
        modEventBus.addListener(this::imcHandle);
        addRegistrationListeners(modEventBus);
        packetHandler = new PacketHandler(modEventBus, versionNumber);
        //Super early hooks, only reliable thing is for checking dependencies that we declare we are after
        hooks.hookConstructor(modEventBus);
    }

    public static synchronized void addModule(IModModule modModule) {
        modulesLoaded.add(modModule);
    }

    public static PacketHandler packetHandler() {
        return instance.packetHandler;
    }

    private void addRegistrationListeners(IEventBus modEventBus) {
        modEventBus.addListener(this::registerEventListener);
        modEventBus.addListener(this::registerRegistries);

        MekanismItems.ITEMS.register(modEventBus);
        MekanismBlocks.BLOCKS.register(modEventBus);
        MekanismFluids.FLUIDS.register(modEventBus);
        MekanismArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        MekanismAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        MekanismContainerTypes.CONTAINER_TYPES.register(modEventBus);
        MekanismCreativeTabs.CREATIVE_TABS.register(modEventBus);
        MekanismCriteriaTriggers.CRITERIA_TRIGGERS.register(modEventBus);
        MekanismDataComponents.DATA_COMPONENTS.register(modEventBus);
        MekanismEntityTypes.ENTITY_TYPES.register(modEventBus);
        MekanismTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MekanismGameEvents.GAME_EVENTS.register(modEventBus);
        MekanismSounds.SOUND_EVENTS.register(modEventBus);
        MekanismParticleTypes.PARTICLE_TYPES.register(modEventBus);
        MekanismHeightProviderTypes.HEIGHT_PROVIDER_TYPES.register(modEventBus);
        MekanismIntProviderTypes.INT_PROVIDER_TYPES.register(modEventBus);
        MekanismPlacementModifiers.PLACEMENT_MODIFIERS.register(modEventBus);
        MekanismFeatures.FEATURES.register(modEventBus);
        MekanismRecipeType.RECIPE_TYPES.register(modEventBus);
        MekanismRecipeSerializersInternal.RECIPE_SERIALIZERS.register(modEventBus);
        MekanismDataSerializers.DATA_SERIALIZERS.register(modEventBus);
        MekanismLootFunctions.REGISTER.register(modEventBus);
        MekanismChemicals.CHEMICALS.register(modEventBus);
        MekanismChemicalIngredientTypes.INGREDIENT_TYPES.register(modEventBus);
        MekanismRobitSkins.createAndRegisterDatapack(modEventBus);
        MekanismModules.MODULES.register(modEventBus);
        MekanismRecipeConditions.CONDITION_CODECS.register(modEventBus);
        MekanismItemPredicates.PREDICATES.register(modEventBus);
        MekanismDataMapTypes.REGISTER.register(modEventBus);
    }

    private void registerRegistries(NewRegistryEvent event) {
        event.register(MekanismAPI.CHEMICAL_REGISTRY);
        event.register(MekanismAPI.CHEMICAL_INGREDIENT_TYPES);
        event.register(MekanismAPI.MODULE_REGISTRY);
        event.register(MekanismAPI.ROBIT_SKIN_SERIALIZER_REGISTRY);
    }

    @SuppressWarnings("removal")
    private void registerEventListener(RegisterEvent event) {
        //Register the empty chemical
        event.register(MekanismAPI.CHEMICAL_REGISTRY_NAME, MekanismAPI.EMPTY_CHEMICAL_KEY.location(), () -> MekanismAPI.EMPTY_CHEMICAL);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
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

    private void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(MekanismAPI.CHEMICAL_REGISTRY_NAME, registry -> registry.holders().forEach(
              holder -> holder.value().updateFromDataMap(holder)
        ));
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
        QIOGlobalItemLookup.INSTANCE.reset();
        PlayerExposure.clear();
        MultiblockManager.reset();
        FrequencyManager.reset();
        TransporterManager.reset();
        PathfinderCache.reset();
        TransmitterNetworkRegistry.reset();
        GenHandler.reset();
        PersonalStorageManager.reset();
    }

    private void imcQueue(InterModEnqueueEvent event) {
        //IMC messages we send to other mods
        hooks.sendIMCMessages(event);
        //IMC messages that we are sending to ourselves
        MekanismIMC.addModuleContainer((Holder<Item>) MekanismItems.MEKA_TOOL, MekanismIMC.ADD_MEKA_TOOL_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>) MekanismItems.MEKASUIT_HELMET, MekanismIMC.ADD_MEKA_SUIT_HELMET_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>) MekanismItems.MEKASUIT_BODYARMOR, MekanismIMC.ADD_MEKA_SUIT_BODYARMOR_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>) MekanismItems.MEKASUIT_PANTS, MekanismIMC.ADD_MEKA_SUIT_PANTS_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>) MekanismItems.MEKASUIT_BOOTS, MekanismIMC.ADD_MEKA_SUIT_BOOTS_MODULES);
        MekanismIMC.addModulesToAll(MekanismModules.ENERGY_UNIT);
        MekanismIMC.addMekaSuitModules(MekanismModules.COLOR_MODULATION_UNIT, MekanismModules.LASER_DISSIPATION_UNIT, MekanismModules.RADIATION_SHIELDING_UNIT);
        MekanismIMC.addMekaToolModules(MekanismModules.ATTACK_AMPLIFICATION_UNIT, MekanismModules.SILK_TOUCH_UNIT, MekanismModules.FORTUNE_UNIT, MekanismModules.BLASTING_UNIT, MekanismModules.VEIN_MINING_UNIT,
              MekanismModules.FARMING_UNIT, MekanismModules.SHEARING_UNIT, MekanismModules.TELEPORTATION_UNIT, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        MekanismIMC.addMekaSuitHelmetModules(MekanismModules.ELECTROLYTIC_BREATHING_UNIT, MekanismModules.INHALATION_PURIFICATION_UNIT,
              MekanismModules.VISION_ENHANCEMENT_UNIT, MekanismModules.NUTRITIONAL_INJECTION_UNIT);
        MekanismIMC.addMekaSuitBodyarmorModules(MekanismModules.JETPACK_UNIT, MekanismModules.GRAVITATIONAL_MODULATING_UNIT, MekanismModules.CHARGE_DISTRIBUTION_UNIT,
              MekanismModules.DOSIMETER_UNIT, MekanismModules.GEIGER_UNIT, MekanismModules.ELYTRA_UNIT);
        MekanismIMC.addMekaSuitPantsModules(MekanismModules.LOCOMOTIVE_BOOSTING_UNIT, MekanismModules.GYROSCOPIC_STABILIZATION_UNIT,
              MekanismModules.HYDROSTATIC_REPULSOR_UNIT, MekanismModules.MOTORIZED_SERVO_UNIT);
        MekanismIMC.addMekaSuitBootsModules(MekanismModules.HYDRAULIC_PROPULSION_UNIT, MekanismModules.MAGNETIC_ATTRACTION_UNIT, MekanismModules.FROST_WALKER_UNIT,
              MekanismModules.SOUL_SURFER_UNIT);
    }

    private void imcHandle(InterModProcessEvent event) {
        ModuleHelper.get().processIMC(event);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //Initialization notification
        logger.info("Version {} initializing...", versionNumber);
        hooks.hookCommonSetup();
        setRecipeCacheManager(new ReloadListener());
        HolidayManager.init();

        event.enqueueWork(() -> {
            //Collect annotation scan data
            MekAnnotationScanner.collectScanData();
            //Register dispenser behaviors
            MekanismFluids.FLUIDS.registerBucketDispenserBehavior();
            registerFluidTankBehaviors(MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK,
                  MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.CREATIVE_FLUID_TANK);
            registerDispenseBehavior(new ModuleDispenseBehavior(), MekanismItems.MEKA_TOOL);
            registerDispenseBehavior(new MekaSuitDispenseBehavior(), MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS,
                  MekanismItems.MEKASUIT_BOOTS);
        });

        //Register player tracker
        NeoForge.EVENT_BUS.register(new CommonPlayerTracker());
        NeoForge.EVENT_BUS.register(new CommonPlayerTickHandler());
        NeoForge.EVENT_BUS.register(worldTickHandler);

        NeoForge.EVENT_BUS.register(RadiationManager.get());

        //Fake player info
        logger.info("Fake player readout: UUID = {}, name = {}", gameProfile.getId(), gameProfile.getName());
        logger.info("Mod loaded.");
    }

    @SafeVarargs
    private static void registerDispenseBehavior(DispenseItemBehavior behavior, Holder<Item>... items) {
        for (Holder<Item> item : items) {
            DispenserBlock.registerBehavior(item.value(), behavior);
        }
    }

    @SafeVarargs
    private static void registerFluidTankBehaviors(BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank>... tanks) {
        for (BlockRegistryObject<?, ?> tank : tanks) {
            Item item = tank.getItemHolder().value();
            DispenserBlock.registerBehavior(item, FluidTankItemDispenseBehavior.INSTANCE);
            CauldronInteraction.EMPTY.map().put(item, BasicCauldronInteraction.EMPTY);
            CauldronInteraction.WATER.map().put(item, BasicDrainCauldronInteraction.WATER);
            CauldronInteraction.LAVA.map().put(item, BasicDrainCauldronInteraction.LAVA);
        }
    }

    private void registerChunkTicketControllers(RegisterTicketControllersEvent event) {
        event.register(TileComponentChunkLoader.TICKET_CONTROLLER);
    }

    private void onEnergyTransferred(EnergyTransferEvent event) {
        PacketUtils.sendToAllTracking(event.network, new PacketNetworkScale(event.network));
    }

    private void onChemicalTransferred(ChemicalTransferEvent event) {
        UUID networkID = event.network.getUUID();
        PacketUtils.log("Sending type '{}' update message for chemical network with id {}", event.transferType.getRegisteredName(), networkID);
        PacketUtils.sendToAllTracking(event.network, new PacketNetworkScale(event.network), new PacketChemicalNetworkContents(networkID, event.transferType));
    }

    private void onLiquidTransferred(FluidTransferEvent event) {
        UUID networkID = event.network.getUUID();
        PacketUtils.log("Sending type '{}' update message for fluid network with id {}", event.fluidType.getFluidHolder().getRegisteredName(), networkID);
        PacketUtils.sendToAllTracking(event.network, new PacketNetworkScale(event.network), new PacketFluidNetworkContents(networkID, event.fluidType));
    }

    private void onModifyItemAttributes(ItemAttributeModifierEvent event) {
        if (event.getItemStack().getItem() instanceof IHasConditionalAttributes item) {
            item.adjustAttributes(event);
        }
    }

    private void onWorldLoad(LevelEvent.Load event) {
        playerState.init(event.getLevel());
    }

    private void onWorldUnload(LevelEvent.Unload event) {
        // Make sure the global fake player drops its reference to the World
        // when the server shuts down
        if (event.getLevel() instanceof ServerLevel level) {
            MekFakePlayer.releaseInstance(level);
        }
        if (event.getLevel() instanceof Level level && MekanismConfig.general.validOredictionificatorFilters.hasInvalidationListeners()) {
            //Remove any invalidation listeners that loaded oredictionificators might have added if the OD was in the given level
            MekanismConfig.general.validOredictionificatorFilters.removeInvalidationListenersMatching(level, (listener, world) ->
                  listener instanceof ODConfigValueInvalidationListener odListener && odListener.isIn(world));
        }
    }
}
