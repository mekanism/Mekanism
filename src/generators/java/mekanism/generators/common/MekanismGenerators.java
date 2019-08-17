package mekanism.generators.common;

import mekanism.api.MekanismAPI;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.FuelHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismTags;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.recipe.RecipeHandler;
import mekanism.generators.client.GeneratorsClientProxy;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(MekanismGenerators.MODID)
@Mod.EventBusSubscriber()
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
        instance = this;
        MekanismGeneratorsConfig.registerConfigs(ModLoadingContext.get());

        //TODO: Register other listeners and various stuff that is needed

        MekanismGeneratorsConfig.loadFromFiles();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        // Register blocks and tile entities
        GeneratorsBlock.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // Register items and itemBlocks
        GeneratorsItem.registerItems(event.getRegistry());
        GeneratorsBlock.registerItemBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // Register models
        proxy.registerBlockRenders();
        proxy.registerItemRenders();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
        proxy.loadConfiguration();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //Add this module to the core list
        Mekanism.modulesLoaded.add(this);

        //Set up the GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GeneratorsGuiHandler());
        MinecraftForge.EVENT_BUS.register(this);

        //Load the proxy
        proxy.registerTileEntities();
        proxy.registerTESRs();

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

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
        FuelHandler.addGas(MekanismFluids.Ethene, MekanismConfig.general.ETHENE_BURN_TIME.get(),
              MekanismConfig.general.FROM_H2.get() + MekanismGeneratorsConfig.generators.bioGeneration.get() * 2 * MekanismConfig.general.ETHENE_BURN_TIME.get());

        for (Item dust : MekanismTags.GOLD_DUST.getAllElements()) {
            RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, new ItemStack(dust, 4), GeneratorsItem.HOHLRAUM.getItemStack());
        }
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
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

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(MekanismGenerators.MODID) || event.getModID().equals(Mekanism.MODID)) {
            proxy.loadConfiguration();
        }
    }

    @SubscribeEvent
    public void onBlacklistUpdate(MekanismAPI.BoxBlacklistEvent event) {
        // Mekanism Generators multiblock structures
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.ADVANCED_SOLAR_GENERATOR.getBlock(), 0);
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.WIND_GENERATOR.getBlock(), 0);
    }
}