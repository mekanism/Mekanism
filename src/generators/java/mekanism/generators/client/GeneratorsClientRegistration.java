package mekanism.generators.client;

import java.util.Map;
import java.util.function.Function;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.generators.client.gui.GuiBioGenerator;
import mekanism.generators.client.gui.GuiGasGenerator;
import mekanism.generators.client.gui.GuiHeatGenerator;
import mekanism.generators.client.gui.GuiIndustrialTurbine;
import mekanism.generators.client.gui.GuiReactorController;
import mekanism.generators.client.gui.GuiReactorFuel;
import mekanism.generators.client.gui.GuiReactorHeat;
import mekanism.generators.client.gui.GuiReactorLogicAdapter;
import mekanism.generators.client.gui.GuiReactorStats;
import mekanism.generators.client.gui.GuiSolarGenerator;
import mekanism.generators.client.gui.GuiTurbineStats;
import mekanism.generators.client.gui.GuiWindGenerator;
import mekanism.generators.client.render.RenderAdvancedSolarGenerator;
import mekanism.generators.client.render.RenderBioGenerator;
import mekanism.generators.client.render.RenderGasGenerator;
import mekanism.generators.client.render.RenderHeatGenerator;
import mekanism.generators.client.render.RenderIndustrialTurbine;
import mekanism.generators.client.render.RenderReactor;
import mekanism.generators.client.render.RenderTurbineRotor;
import mekanism.generators.client.render.RenderWindGenerator;
import mekanism.generators.client.render.item.RenderAdvancedSolarGeneratorItem;
import mekanism.generators.client.render.item.RenderBioGeneratorItem;
import mekanism.generators.client.render.item.RenderGasGeneratorItem;
import mekanism.generators.client.render.item.RenderHeatGeneratorItem;
import mekanism.generators.client.render.item.RenderWindGeneratorItem;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismGenerators.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneratorsClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        bindTileEntityRenderer(GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR, new RenderAdvancedSolarGenerator());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.BIO_GENERATOR, new RenderBioGenerator());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR, new RenderGasGenerator());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.HEAT_GENERATOR, new RenderHeatGenerator());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.REACTOR_CONTROLLER, new RenderReactor());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_CASING, new RenderIndustrialTurbine());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_ROTOR, new RenderTurbineRotor());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_VALVE, new RenderIndustrialTurbine());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_VENT, new RenderIndustrialTurbine());
        bindTileEntityRenderer(GeneratorsTileEntityTypes.WIND_GENERATOR, new RenderWindGenerator());
    }

    private static <T extends TileEntity> void bindTileEntityRenderer(TileEntityTypeRegistryObject<T> tileTypeRO, TileEntityRenderer<? super T> specialRenderer) {
        ClientRegistry.bindTileEntityRenderer(tileTypeRO.getTileEntityType(), specialRenderer);
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        registerScreen(GeneratorsContainerTypes.BIO_GENERATOR, GuiBioGenerator::new);
        registerScreen(GeneratorsContainerTypes.GAS_BURNING_GENERATOR, GuiGasGenerator::new);
        registerScreen(GeneratorsContainerTypes.HEAT_GENERATOR, GuiHeatGenerator::new);
        registerScreen(GeneratorsContainerTypes.INDUSTRIAL_TURBINE, GuiIndustrialTurbine::new);
        registerScreen(GeneratorsContainerTypes.REACTOR_CONTROLLER, GuiReactorController::new);
        registerScreen(GeneratorsContainerTypes.REACTOR_FUEL, GuiReactorFuel::new);
        registerScreen(GeneratorsContainerTypes.REACTOR_HEAT, GuiReactorHeat::new);
        registerScreen(GeneratorsContainerTypes.REACTOR_LOGIC_ADAPTER, GuiReactorLogicAdapter::new);
        registerScreen(GeneratorsContainerTypes.REACTOR_STATS, GuiReactorStats::new);
        registerScreen(GeneratorsContainerTypes.SOLAR_GENERATOR, GuiSolarGenerator::new);
        registerScreen(GeneratorsContainerTypes.TURBINE_STATS, GuiTurbineStats::new);
        registerScreen(GeneratorsContainerTypes.WIND_GENERATOR, GuiWindGenerator::new);
    }

    private static <C extends Container, U extends Screen & IHasContainer<C>> void registerScreen(ContainerTypeRegistryObject<C> type, IScreenFactory<C, U> factory) {
        ScreenManager.registerFactory(type.getContainerType(), factory);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        registerItemStackModel(modelRegistry, "heat_generator", model -> RenderHeatGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "bio_generator", model -> RenderBioGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "wind_generator", model -> RenderWindGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "gas_burning_generator", model -> RenderGasGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "advanced_solar_generator", model -> RenderAdvancedSolarGeneratorItem.model = model);
    }

    private static void registerItemStackModel(Map<ResourceLocation, IBakedModel> modelRegistry, String type, Function<ItemLayerWrapper, IBakedModel> setModel) {
        ModelResourceLocation resourceLocation = getInventoryMRL(type);
        modelRegistry.put(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.get(resourceLocation))));
    }

    private static ModelResourceLocation getInventoryMRL(String type) {
        return new ModelResourceLocation(new ResourceLocation(MekanismGenerators.MODID, type), "inventory");
    }
}