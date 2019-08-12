package mekanism.generators.common;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.mj.MjAPI;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.FuelHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.fixers.MekanismDataFixers.MekFixers;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.StackUtils;
import mekanism.generators.client.GeneratorsClientProxy;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.fixers.GeneratorTEFixer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.OreDictionary;

@Mod(MekanismGenerators.MODID)
//@Mod(modid = MekanismGenerators.MODID, useMetadata = true, guiFactory = "mekanism.generators.client.gui.GeneratorsGuiFactory")
@Mod.EventBusSubscriber()
public class MekanismGenerators implements IModule {

    public static final String MODID = "mekanismgenerators";

    public static GeneratorsCommonProxy proxy = DistExecutor.runForDist(() -> GeneratorsClientProxy::new, () -> GeneratorsCommonProxy::new);

    public static MekanismGenerators instance;

    /**
     * MekanismGenerators version number
     */
    public static Version versionNumber = new Version(999, 999, 999);
    public static final int DATA_VERSION = 1;

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

        //Register this module's GUI handler in the simple packet protocol
        PacketSimpleGui.handlers.add(1, proxy);

        //Set up the GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GeneratorsGuiHandler());
        MinecraftForge.EVENT_BUS.register(this);

        //Load the proxy
        proxy.registerTileEntities();
        proxy.registerTESRs();

        CompoundDataFixer fixer = FMLCommonHandler.instance().getDataFixer();
        ModFixs fixes = fixer.init(MODID, DATA_VERSION);
        //Fix old tile entity names
        fixes.registerFix(FixTypes.BLOCK_ENTITY, new GeneratorTEFixer(MekFixers.TILE_ENTITIES));

        //Finalization
        Mekanism.logger.info("Loaded MekanismGenerators module.");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (FuelHandler.BCPresent() && BuildcraftFuelRegistry.fuel != null) {
            for (IFuel s : BuildcraftFuelRegistry.fuel.getFuels()) {
                if (s.getFluid() != null && !GasRegistry.containsGas(s.getFluid().getFluid().getName())) {
                    GasRegistry.register(new Gas(s.getFluid().getFluid()));
                }
            }

            BuildcraftFuelRegistry.fuel.addFuel(MekanismFluids.Ethene.getFluid(), (ForgeEnergyIntegration.toForgeAsLong(12 * MjAPI.MJ)), 40 * Fluid.BUCKET_VOLUME);
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
        FuelHandler.addGas(MekanismFluids.Ethene, MekanismConfigOld.current().general.ETHENE_BURN_TIME.get(),
              MekanismConfigOld.current().general.FROM_H2.get() + MekanismConfigOld.current().generators.bioGeneration.get() * 2 * MekanismConfigOld.current().general.ETHENE_BURN_TIME.get());

        for (ItemStack ore : OreDictionary.getOres("dustGold", false)) {
            RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, StackUtils.size(ore, 4), GeneratorsItem.HOHLRAUM.getItemStack());
        }
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
    public void writeConfig(PacketBuffer dataStream, MekanismConfig config) {
        config.generators.write(dataStream);
    }

    @Override
    public void readConfig(PacketBuffer dataStream, MekanismConfig destConfig) {
        destConfig.generators.read(dataStream);
    }

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