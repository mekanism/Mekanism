package mekanism.generators.common;

import mekanism.api.MekanismAPI;
import mekanism.common.FuelHandler;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.network.PacketGeneratorsGuiButtonPress;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import net.minecraftforge.common.MinecraftForge;
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

    public static MultiblockManager<SynchronizedTurbineData> turbineManager = new MultiblockManager<>("industrialTurbine");

    public MekanismGenerators() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismGeneratorsConfig.registerConfigs(ModLoadingContext.get());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(this::onBlacklistUpdate);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigReload);

        GeneratorsItem.ITEMS.register(modEventBus);
        GeneratorsBlock.BLOCKS.register(modEventBus);
        GeneratorsContainerTypes.CONTAINER_TYPES.register(modEventBus);
        GeneratorsTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        //TODO: Move recipes to JSON
        //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
        FuelHandler.addGas(MekanismTags.ETHENE, MekanismConfig.general.ETHENE_BURN_TIME.get(),
              MekanismConfig.general.FROM_H2.get() + MekanismGeneratorsConfig.generators.bioGeneration.get() * 2 * MekanismConfig.general.ETHENE_BURN_TIME.get());

        MinecraftForge.EVENT_BUS.register(this);

        registerPackets();

        //Finalization
        Mekanism.logger.info("Loaded MekanismGenerators module.");
    }

    private void registerPackets() {
        Mekanism.packetHandler.registerMessage(PacketGeneratorsGuiButtonPress.class, PacketGeneratorsGuiButtonPress::encode, PacketGeneratorsGuiButtonPress::decode,
              PacketGeneratorsGuiButtonPress::handle);
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
        SynchronizedTurbineData.clientRotationMap.clear();
    }

    private void onConfigReload(ModConfig.ConfigReloading configEvent) {
        //TODO: Handle reloading
        /*if (event.getModID().equals(MekanismGenerators.MODID) || event.getModID().equals(Mekanism.MODID)) {
            proxy.loadConfiguration();
        }*/
    }

    private void onBlacklistUpdate(MekanismAPI.BoxBlacklistEvent event) {
        // Mekanism Generators multiblock structures
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.ADVANCED_SOLAR_GENERATOR);
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.WIND_GENERATOR);
    }
}