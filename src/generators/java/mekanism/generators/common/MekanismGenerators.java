package mekanism.generators.common;

import mekanism.api.MekanismIMC;
import mekanism.api.chemical.gas.attribute.GasAttributes.Fuel;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.config.listener.ConfigBasedCachedFLSupplier;
import mekanism.common.lib.Version;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.recipe.ClearConfigurationRecipe;
import mekanism.common.registries.MekanismGases;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorCache;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator;
import mekanism.generators.common.content.fusion.FusionReactorCache;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.content.fusion.FusionReactorValidator;
import mekanism.generators.common.content.turbine.TurbineCache;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineValidator;
import mekanism.generators.common.network.GeneratorsPacketHandler;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsBuilders.FissionReactorBuilder;
import mekanism.generators.common.registries.GeneratorsBuilders.FusionReactorBuilder;
import mekanism.generators.common.registries.GeneratorsBuilders.TurbineBuilder;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.registries.GeneratorsCreativeTabs;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.registries.GeneratorsModules;
import mekanism.generators.common.registries.GeneratorsSounds;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@Mod(MekanismGenerators.MODID)
public class MekanismGenerators implements IModModule {

    public static final String MODID = "mekanismgenerators";
    private static final ConfigBasedCachedFLSupplier ETHENE_ENERGY_DENSITY = new ConfigBasedCachedFLSupplier(() -> {
        FloatingLong energy = MekanismGeneratorsConfig.generators.bioGeneration.get().multiply(2)
              .timesEqual(MekanismGeneratorsConfig.generators.etheneDensityMultiplier.get());
        return energy.plusEqual(MekanismConfig.general.FROM_H2.get());
    }, MekanismConfig.general.FROM_H2, MekanismGeneratorsConfig.generators.bioGeneration, MekanismGeneratorsConfig.generators.etheneDensityMultiplier);

    public static MekanismGenerators instance;

    /**
     * MekanismGenerators version number
     */
    public final Version versionNumber;
    /**
     * Mekanism Generators Packet Pipeline
     */
    private final GeneratorsPacketHandler packetHandler;

    public static final MultiblockManager<TurbineMultiblockData> turbineManager = new MultiblockManager<>("industrialTurbine", TurbineCache::new, TurbineValidator::new);
    public static final MultiblockManager<FissionReactorMultiblockData> fissionReactorManager = new MultiblockManager<>("fissionReactor", FissionReactorCache::new, FissionReactorValidator::new);
    public static final MultiblockManager<FusionReactorMultiblockData> fusionReactorManager = new MultiblockManager<>("fusionReactor", FusionReactorCache::new, FusionReactorValidator::new);

    public MekanismGenerators(ModContainer modContainer, IEventBus modEventBus) {
        Mekanism.addModule(instance = this);
        //Set our version number to match the neoforge.mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(modContainer);
        MekanismGeneratorsConfig.registerConfigs(modContainer);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::imcQueue);

        GeneratorsDataComponents.DATA_COMPONENTS.register(modEventBus);
        GeneratorsItems.ITEMS.register(modEventBus);
        GeneratorsBlocks.BLOCKS.register(modEventBus);
        GeneratorsFluids.FLUIDS.register(modEventBus);
        GeneratorsCreativeTabs.CREATIVE_TABS.register(modEventBus);
        GeneratorsSounds.SOUND_EVENTS.register(modEventBus);
        GeneratorsContainerTypes.CONTAINER_TYPES.register(modEventBus);
        GeneratorsTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        GeneratorsGases.GASES.register(modEventBus);
        GeneratorsModules.MODULES.register(modEventBus);
        packetHandler = new GeneratorsPacketHandler(modEventBus, versionNumber);
    }

    public static GeneratorsPacketHandler packetHandler() {
        return instance.packetHandler;
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismGenerators.MODID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //Add fuel attribute to ethene
            MekanismGases.ETHENE.get().addAttribute(new Fuel(MekanismGeneratorsConfig.generators.etheneBurnTicks, ETHENE_ENERGY_DENSITY));
            //Register dispenser behaviors
            GeneratorsFluids.FLUIDS.registerBucketDispenserBehavior();
            //Register extended build commands (in enqueue as it is not thread safe)
            BuildCommand.register("turbine", GeneratorsLang.TURBINE, new TurbineBuilder());
            BuildCommand.register("fission", GeneratorsLang.FISSION_REACTOR, new FissionReactorBuilder());
            BuildCommand.register("fusion", GeneratorsLang.FUSION_REACTOR, new FusionReactorBuilder());

            ClearConfigurationRecipe.addAttachments(GeneratorsDataComponents.FISSION_LOGIC_TYPE, GeneratorsDataComponents.FUSION_LOGIC_TYPE, GeneratorsDataComponents.ACTIVE_COOLED);
        });

        //Finalization
        Mekanism.logger.info("Loaded 'Mekanism: Generators' module.");
    }

    private void imcQueue(InterModEnqueueEvent event) {
        MekanismIMC.addMekaSuitHelmetModules(GeneratorsModules.SOLAR_RECHARGING_UNIT);
        MekanismIMC.addMekaSuitPantsModules(GeneratorsModules.GEOTHERMAL_GENERATOR_UNIT);
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Generators";
    }

    @Override
    public void resetClient() {
        TurbineMultiblockData.clientRotationMap.clear();
    }

    private void onConfigLoad(ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig mekConfig) {
            mekConfig.clearCache(configEvent);
        }
    }
}
