package mekanism.generators.common;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.mj.MjAPI;
import io.netty.buffer.ByteBuf;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.FuelHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.StackUtils;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.fixers.GeneratorTEFixer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid = MekanismGenerators.MODID, useMetadata = true, guiFactory = "mekanism.generators.client.gui.GeneratorsGuiFactory")
@Mod.EventBusSubscriber()
public class MekanismGenerators implements IModule {

    public static final String MODID = "mekanismgenerators";

    @SidedProxy(clientSide = "mekanism.generators.client.GeneratorsClientProxy", serverSide = "mekanism.generators.common.GeneratorsCommonProxy")
    public static GeneratorsCommonProxy proxy;

    @Instance(MekanismGenerators.MODID)
    public static MekanismGenerators instance;

    /**
     * MekanismGenerators version number
     */
    public static Version versionNumber = new Version(9, 4, 13);
    public static final int DATA_VERSION = 1;

    public static MultiblockManager<SynchronizedTurbineData> turbineManager = new MultiblockManager<>(
          "industrialTurbine");

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        // Register blocks and tile entities
        GeneratorsBlocks.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // Register items and itemBlocks
        GeneratorsItems.registerItems(event.getRegistry());
        GeneratorsBlocks.registerItemBlocks(event.getRegistry());
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
        fixes.registerFix(FixTypes.BLOCK_ENTITY, new GeneratorTEFixer());

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

            BuildcraftFuelRegistry.fuel
                  .addFuel(MekanismFluids.Ethene.getFluid(), (long) (240 * general.TO_RF / 20 * MjAPI.MJ),
                        40 * Fluid.BUCKET_VOLUME);
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        FuelHandler.addGas(MekanismFluids.Ethene, general.ETHENE_BURN_TIME, general.FROM_H2
              + generators.bioGeneration * 2
              * general.ETHENE_BURN_TIME); //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus

        for (ItemStack ore : OreDictionary.getOres("dustGold")) {
            RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("CARBON"), 10, StackUtils.size(ore, 4),
                  GeneratorsItems.Hohlraum.getEmptyItem());
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
    public void writeConfig(ByteBuf dataStream) {
        dataStream.writeDouble(generators.advancedSolarGeneration);
        dataStream.writeDouble(generators.bioGeneration);
        dataStream.writeDouble(generators.heatGeneration);
        dataStream.writeDouble(generators.heatGenerationLava);
        dataStream.writeDouble(generators.heatGenerationNether);
        dataStream.writeDouble(generators.solarGeneration);

        dataStream.writeDouble(generators.windGenerationMin);
        dataStream.writeDouble(generators.windGenerationMax);

        dataStream.writeInt(generators.windGenerationMinY);
        dataStream.writeInt(generators.windGenerationMaxY);

        dataStream.writeInt(generators.turbineBladesPerCoil);
        dataStream.writeDouble(generators.turbineVentGasFlow);
        dataStream.writeDouble(generators.turbineDisperserGasFlow);
        dataStream.writeInt(generators.condenserRate);

        dataStream.writeDouble(generators.energyPerFusionFuel);

        for (GeneratorType type : GeneratorType.getGeneratorsForConfig()) {
            dataStream.writeBoolean(generators.generatorsManager.isEnabled(type.blockName));
        }
    }

    @Override
    public void readConfig(ByteBuf dataStream) {
        generators.advancedSolarGeneration = dataStream.readDouble();
        generators.bioGeneration = dataStream.readDouble();
        generators.heatGeneration = dataStream.readDouble();
        generators.heatGenerationLava = dataStream.readDouble();
        generators.heatGenerationNether = dataStream.readDouble();
        generators.solarGeneration = dataStream.readDouble();

        generators.windGenerationMin = dataStream.readDouble();
        generators.windGenerationMax = dataStream.readDouble();

        generators.windGenerationMinY = dataStream.readInt();
        generators.windGenerationMaxY = dataStream.readInt();

        generators.turbineBladesPerCoil = dataStream.readInt();
        generators.turbineVentGasFlow = dataStream.readDouble();
        generators.turbineDisperserGasFlow = dataStream.readDouble();
        generators.condenserRate = dataStream.readInt();

        generators.energyPerFusionFuel = dataStream.readDouble();

        for (GeneratorType type : GeneratorType.getGeneratorsForConfig()) {
            generators.generatorsManager.setEntry(type.blockName, dataStream.readBoolean());
        }
    }

    @Override
    public void resetClient() {
        SynchronizedTurbineData.clientRotationMap.clear();
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals("mekanismgenerators")) {
            proxy.loadConfiguration();
        }
    }

    @SubscribeEvent
    public void onBlacklistUpdate(MekanismAPI.BoxBlacklistEvent event) {
        // Mekanism Generators multiblock structures
        MekanismAPI.addBoxBlacklist(GeneratorsBlocks.Generator, 5); // Advanced Solar Generator
        MekanismAPI.addBoxBlacklist(GeneratorsBlocks.Generator, 6); // Wind Generator
    }
}
