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
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismGenerators.MODID)
public class MekanismGenerators implements IModule {

    public static final String MODID = "mekanismgenerators";

    public static MekanismGenerators instance;

    /**
     * MekanismGenerators version number
     */
    public static Version versionNumber = new Version(999, 999, 999);

    public static MultiblockManager<SynchronizedTurbineData> turbineManager = new MultiblockManager<>("industrialTurbine");

    public MekanismGenerators() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismGeneratorsConfig.registerConfigs(ModLoadingContext.get());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onBlacklistUpdate);
        modEventBus.addListener(this::commonSetup);
        MekanismGeneratorsConfig.loadFromFiles();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        //TODO: Move recipes to JSON
        //1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus
        FuelHandler.addGas(MekanismFluids.ETHENE, MekanismConfig.general.ETHENE_BURN_TIME.get(),
              MekanismConfig.general.FROM_H2.get() + MekanismGeneratorsConfig.generators.bioGeneration.get() * 2 * MekanismConfig.general.ETHENE_BURN_TIME.get());

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
                if (!s.getFluid().isEmpty() && !GasRegistry.containsGas(s.getFluid().getFluid().getName())) {
                    GasRegistry.register(new Gas(s.getFluid().getFluid()));
                }
            }

            BuildcraftFuelRegistry.fuel.addFuel(MekanismFluids.Ethene.getFluid(), (ForgeEnergyIntegration.toForgeAsLong(12 * MjAPI.MJ)), 40 * FluidAttributes.BUCKET_VOLUME);
        }
    }*/

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

    //TODO
    /*private void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(MekanismGenerators.MODID) || event.getModID().equals(Mekanism.MODID)) {
            proxy.loadConfiguration();
        }
    }*/

    private void onBlacklistUpdate(MekanismAPI.BoxBlacklistEvent event) {
        // Mekanism Generators multiblock structures
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.ADVANCED_SOLAR_GENERATOR);
        MekanismAPI.addBoxBlacklist(GeneratorsBlock.WIND_GENERATOR);
    }
}