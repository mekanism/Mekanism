package mekanism.generators.common;

import mekanism.api.chemical.gas.attribute.GasAttributes.Fuel;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.common.lib.multiblock.MultiblockManager;
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
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.registries.GeneratorsSounds;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismGenerators.MODID)
public class MekanismGenerators implements IModule {

    public static final String MODID = "mekanismgenerators";

    public static MekanismGenerators instance;

    /**
     * MekanismGenerators version number
     */
    public final Version versionNumber;
    /**
     * Mekanism Generators Packet Pipeline
     */
    public static final GeneratorsPacketHandler packetHandler = new GeneratorsPacketHandler();

    public static final MultiblockManager<TurbineMultiblockData> turbineManager = new MultiblockManager<>("industrialTurbine", TurbineCache::new, TurbineValidator::new);
    public static final MultiblockManager<FissionReactorMultiblockData> fissionReactorManager = new MultiblockManager<>("fissionReactor", FissionReactorCache::new, FissionReactorValidator::new);
    public static final MultiblockManager<FusionReactorMultiblockData> fusionReactorManager = new MultiblockManager<>("fusionReactor", FusionReactorCache::new, FusionReactorValidator::new);

    public MekanismGenerators() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismGeneratorsConfig.registerConfigs(ModLoadingContext.get());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);

        GeneratorsItems.ITEMS.register(modEventBus);
        GeneratorsBlocks.BLOCKS.register(modEventBus);
        GeneratorsFluids.FLUIDS.register(modEventBus);
        GeneratorsSounds.SOUND_EVENTS.register(modEventBus);
        GeneratorsContainerTypes.CONTAINER_TYPES.register(modEventBus);
        GeneratorsTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        GeneratorsGases.GASES.register(modEventBus);
        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismGenerators.MODID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
        MekanismGases.ETHENE.get().addAttribute(new Fuel(MekanismConfig.general.ETHENE_BURN_TIME,
              () -> MekanismConfig.general.FROM_H2.get().add(MekanismGeneratorsConfig.generators.bioGeneration.get()
                    .multiply(2L * MekanismConfig.general.ETHENE_BURN_TIME.get()))));

        //Register dispenser behaviors
        event.enqueueWork(GeneratorsFluids.FLUIDS::registerBucketDispenserBehavior);

        BuildCommand.register("turbine", GeneratorsLang.TURBINE, new TurbineBuilder());
        BuildCommand.register("fission", GeneratorsLang.FISSION_REACTOR, new FissionReactorBuilder());
        BuildCommand.register("fusion", GeneratorsLang.FUSION_REACTOR, new FusionReactorBuilder());

        packetHandler.initialize();

        //Finalization
        Mekanism.logger.info("Loaded 'Mekanism Generators' module.");
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

    private void onConfigLoad(ModConfig.ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, so as to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig) {
            ((MekanismModConfig) config).clearCache();
        }
    }
}