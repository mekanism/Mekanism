package mekanism.generators.common;

import mekanism.api.MekanismAPI;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismTags;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.recipe.RecipeHandler;
import mekanism.generators.client.GeneratorsClientProxy;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismGenerators.MODID)
public class MekanismGenerators implements IModule {

    public static final String MODID = "mekanismgenerators";

    //Note: Do not replace with method reference: https://gist.github.com/williewillus/353c872bcf1a6ace9921189f6100d09a#gistcomment-2876130
    public static GeneratorsCommonProxy proxy = DistExecutor.runForDist(() -> () -> new GeneratorsClientProxy(), () -> () -> new GeneratorsCommonProxy());

    public static MekanismGenerators instance;

    /**
     * MekanismGenerators version number
     */
    public static Version versionNumber = new Version(999, 999, 999);

    public static MultiblockManager<SynchronizedTurbineData> turbineManager = new MultiblockManager<>("industrialTurbine");

    public MekanismGenerators() {
        Mekanism.modulesLoaded.add(instance = this);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerBlocks);
        modEventBus.addListener(this::registerItems);
        modEventBus.addListener(this::registerTileEntities);
        modEventBus.addListener(this::registerModels);
        modEventBus.addListener(this::registerContainers);
        modEventBus.addListener(this::onConfigChanged);
        modEventBus.addListener(this::onBlacklistUpdate);
        modEventBus.addListener(this::commonSetup);
        //TODO: Register other listeners and various stuff that is needed
    }

    private void registerBlocks(RegistryEvent.Register<Block> event) {
        // Register blocks and tile entities
        GeneratorsBlock.registerBlocks(event.getRegistry());
    }

    private void registerItems(RegistryEvent.Register<Item> event) {
        // Register items and itemBlocks
        GeneratorsItem.registerItems(event.getRegistry());
        GeneratorsBlock.registerItemBlocks(event.getRegistry());
    }

    private void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        GeneratorsTileEntityTypes.registerTileEntities(event.getRegistry());

        //Register the TESRs
        proxy.registerTESRs();
    }

    private void registerModels(ModelRegistryEvent event) {
        // Register models
        proxy.registerBlockRenders();
        proxy.registerItemRenders();
    }

    public void preInit() {
        //TODO: Figure out where this goes
        MekanismGeneratorsConfig.registerConfigs(ModLoadingContext.get());
        MekanismGeneratorsConfig.loadFromFiles();

        proxy.preInit();
        proxy.loadConfiguration();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        //TODO: Figure out where preinit stuff should be, potentially also move it directly into this method
        preInit();

        //TODO: Move recipes to JSON
        //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
        /*FuelHandler.addGas(MekanismFluids.Ethene, MekanismConfig.general.ETHENE_BURN_TIME.get(),
              MekanismConfig.general.FROM_H2.get() + MekanismGeneratorsConfig.generators.bioGeneration.get() * 2 * MekanismConfig.general.ETHENE_BURN_TIME.get());*/

        for (Item dust : MekanismTags.GOLD_DUST.getAllElements()) {
            RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, new ItemStack(dust, 4), GeneratorsItem.HOHLRAUM.getItemStack());
        }

        MinecraftForge.EVENT_BUS.register(this);

        //Finalization
        Mekanism.logger.info("Loaded MekanismGenerators module.");
    }

    //TODO: BuildCraft
    /*@EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (FuelHandler.BCPresent() && BuildcraftFuelRegistry.fuel != null) {
            for (IFuel s : BuildcraftFuelRegistry.fuel.getFuels()) {
                if (s.getFluid() != null && !GasRegistry.containsGas(s.getFluid().getFluid().getName())) {
                    GasRegistry.register(new Gas(s.getFluid().getFluid()));
                }
            }

            BuildcraftFuelRegistry.fuel.addFuel(MekanismFluids.Ethene.getFluid(), (ForgeEnergyIntegration.toForgeAsLong(12 * MjAPI.MJ)), 40 * Fluid.BUCKET_VOLUME);
        }
    }*/

    private void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        GeneratorsContainerTypes.registerContainers(event.getRegistry());
        proxy.registerScreenHandlers();
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Generators";
    }

    /*@Override
    public void writeConfig(PacketBuffer dataStream, MekanismConfig config) {
        config.generators.write(dataStream);
    }

    @Override
    public void readConfig(PacketBuffer dataStream, MekanismConfig destConfig) {
        destConfig.generators.read(dataStream);
    }*/

    @Override
    public void resetClient() {
        SynchronizedTurbineData.clientRotationMap.clear();
    }

    private void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(MekanismGenerators.MODID) || event.getModID().equals(Mekanism.MODID)) {
            proxy.loadConfiguration();
        }
    }

    private void onBlacklistUpdate(MekanismAPI.BoxBlacklistEvent event) {
        // Mekanism Generators multiblock structures
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.ADVANCED_SOLAR_GENERATOR);
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.WIND_GENERATOR);
    }
}