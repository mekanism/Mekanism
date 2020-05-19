package mekanism.generators.client;

import java.util.Map;
import java.util.function.Function;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.generators.client.gui.GuiBioGenerator;
import mekanism.generators.client.gui.GuiFissionReactor;
import mekanism.generators.client.gui.GuiFissionReactorLogicAdapter;
import mekanism.generators.client.gui.GuiFissionReactorStats;
import mekanism.generators.client.gui.GuiFusionReactorController;
import mekanism.generators.client.gui.GuiFusionReactorFuel;
import mekanism.generators.client.gui.GuiFusionReactorHeat;
import mekanism.generators.client.gui.GuiFusionReactorLogicAdapter;
import mekanism.generators.client.gui.GuiFusionReactorStats;
import mekanism.generators.client.gui.GuiGasGenerator;
import mekanism.generators.client.gui.GuiHeatGenerator;
import mekanism.generators.client.gui.GuiIndustrialTurbine;
import mekanism.generators.client.gui.GuiSolarGenerator;
import mekanism.generators.client.gui.GuiTurbineStats;
import mekanism.generators.client.gui.GuiWindGenerator;
import mekanism.generators.client.render.RenderAdvancedSolarGenerator;
import mekanism.generators.client.render.RenderBioGenerator;
import mekanism.generators.client.render.RenderFissionReactor;
import mekanism.generators.client.render.RenderFusionReactor;
import mekanism.generators.client.render.RenderGasGenerator;
import mekanism.generators.client.render.RenderHeatGenerator;
import mekanism.generators.client.render.RenderIndustrialTurbine;
import mekanism.generators.client.render.RenderTurbineRotor;
import mekanism.generators.client.render.RenderWindGenerator;
import mekanism.generators.client.render.item.RenderAdvancedSolarGeneratorItem;
import mekanism.generators.client.render.item.RenderBioGeneratorItem;
import mekanism.generators.client.render.item.RenderGasGeneratorItem;
import mekanism.generators.client.render.item.RenderHeatGeneratorItem;
import mekanism.generators.client.render.item.RenderWindGeneratorItem;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismGenerators.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneratorsClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR, RenderAdvancedSolarGenerator::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.BIO_GENERATOR, RenderBioGenerator::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR, RenderGasGenerator::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.HEAT_GENERATOR, RenderHeatGenerator::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.FUSION_REACTOR_CONTROLLER, RenderFusionReactor::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.FISSION_REACTOR_CASING, RenderFissionReactor::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.FISSION_REACTOR_PORT, RenderFissionReactor::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.FISSION_REACTOR_LOGIC_ADAPTER, RenderFissionReactor::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_CASING, RenderIndustrialTurbine::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_ROTOR, RenderTurbineRotor::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_VALVE, RenderIndustrialTurbine::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.TURBINE_VENT, RenderIndustrialTurbine::new);
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.WIND_GENERATOR, RenderWindGenerator::new);
        //Block render layers
        ClientRegistrationUtil.setRenderLayer(RenderType.getTranslucent(), GeneratorsBlocks.LASER_FOCUS_MATRIX, GeneratorsBlocks.REACTOR_GLASS);
        //Fluids (translucent)
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : GeneratorsFluids.FLUIDS.getAllFluids()) {
            ClientRegistrationUtil.setRenderLayer(RenderType.getTranslucent(), fluidRO);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("Convert2MethodRef")
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.BIO_GENERATOR, GuiBioGenerator::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.GAS_BURNING_GENERATOR, GuiGasGenerator::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.HEAT_GENERATOR, GuiHeatGenerator::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.INDUSTRIAL_TURBINE, GuiIndustrialTurbine::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FISSION_REACTOR, GuiFissionReactor::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FISSION_REACTOR_STATS, GuiFissionReactorStats::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FISSION_REACTOR_LOGIC_ADAPTER, GuiFissionReactorLogicAdapter::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FUSION_REACTOR_CONTROLLER, GuiFusionReactorController::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FUSION_REACTOR_FUEL, GuiFusionReactorFuel::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FUSION_REACTOR_HEAT, GuiFusionReactorHeat::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FUSION_REACTOR_LOGIC_ADAPTER, GuiFusionReactorLogicAdapter::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.FUSION_REACTOR_STATS, GuiFusionReactorStats::new);
        // for some reason java is unable to infer the types with this generics structure, so we include constructor signature ourselves
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.SOLAR_GENERATOR, (MekanismTileContainer<TileEntitySolarGenerator> container, PlayerInventory inv, ITextComponent title) -> new GuiSolarGenerator<>(container, inv, title));
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.ADVANCED_SOLAR_GENERATOR, (MekanismTileContainer<TileEntityAdvancedSolarGenerator> container, PlayerInventory inv, ITextComponent title) -> new GuiSolarGenerator<>(container, inv, title));
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.TURBINE_STATS, GuiTurbineStats::new);
        ClientRegistrationUtil.registerScreen(GeneratorsContainerTypes.WIND_GENERATOR, GuiWindGenerator::new);
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
        ModelResourceLocation resourceLocation = ClientRegistrationUtil.getInventoryMRL(MekanismGenerators::rl, type);
        modelRegistry.put(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.get(resourceLocation))));
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        RenderBioGenerator.resetCachedModels();
    }
}