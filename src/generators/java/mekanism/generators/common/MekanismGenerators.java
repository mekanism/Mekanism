package mekanism.generators.common;

import mekanism.api.MekanismIMC;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.lib.Version;
import mekanism.common.lib.multiblock.MultiblockManager;
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
import mekanism.generators.common.registries.GeneratorsChemicals;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.registries.GeneratorsCreativeTabs;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.registries.GeneratorsModules;
import mekanism.generators.common.registries.GeneratorsSounds;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@Mod(MekanismGenerators.MODID)
public class MekanismGenerators implements IModModule {

    public static final String MODID = "mekanismgenerators";

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
        modEventBus.addListener(MekanismGeneratorsConfig::onConfigLoad);
        modEventBus.addListener(this::imcQueue);

        GeneratorsDataComponents.DATA_COMPONENTS.register(modEventBus);
        GeneratorsItems.ITEMS.register(modEventBus);
        GeneratorsBlocks.BLOCKS.register(modEventBus);
        GeneratorsFluids.FLUIDS.register(modEventBus);
        GeneratorsCreativeTabs.CREATIVE_TABS.register(modEventBus);
        GeneratorsSounds.SOUND_EVENTS.register(modEventBus);
        GeneratorsContainerTypes.CONTAINER_TYPES.register(modEventBus);
        GeneratorsTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        GeneratorsChemicals.CHEMICALS.register(modEventBus);
        GeneratorsModules.MODULES.register(modEventBus);
        packetHandler = new GeneratorsPacketHandler(modEventBus, versionNumber);
    }

    public static GeneratorsPacketHandler packetHandler() {
        return instance.packetHandler;
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //Register dispenser behaviors
            GeneratorsFluids.FLUIDS.registerBucketDispenserBehavior();
            //Register extended build commands (in enqueue as it is not thread safe)
            BuildCommand.register("turbine", GeneratorsLang.TURBINE, new TurbineBuilder());
            BuildCommand.register("fission", GeneratorsLang.FISSION_REACTOR, new FissionReactorBuilder());
            BuildCommand.register("fusion", GeneratorsLang.FUSION_REACTOR, new FusionReactorBuilder());
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
    public void resetClientDimensionChanged() {
        TurbineMultiblockData.clientRotationMap.clear();
    }
}
