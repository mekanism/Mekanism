package mekanism.generators.client;

import mekanism.client.ClientRegistration;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.model.baked.ExtensionBakedModel.TransformedBakedModel;
import mekanism.client.render.lib.QuadTransformation;
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
import mekanism.generators.client.render.RenderBioGenerator;
import mekanism.generators.client.render.RenderFissionReactor;
import mekanism.generators.client.render.RenderFusionReactor;
import mekanism.generators.client.render.RenderIndustrialTurbine;
import mekanism.generators.client.render.RenderTurbineRotor;
import mekanism.generators.client.render.RenderWindGenerator;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismGenerators.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneratorsClientRegistration {

    private GeneratorsClientRegistration() {
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        ClientRegistrationUtil.bindTileEntityRenderer(GeneratorsTileEntityTypes.BIO_GENERATOR, RenderBioGenerator::new);
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
        ClientRegistrationUtil.setRenderLayer(renderType -> renderType == RenderType.getSolid() || renderType == RenderType.getTranslucent(),
              GeneratorsBlocks.BIO_GENERATOR, GeneratorsBlocks.HEAT_GENERATOR);
        //Fluids (translucent)
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : GeneratorsFluids.FLUIDS.getAllFluids()) {
            ClientRegistrationUtil.setRenderLayer(RenderType.getTranslucent(), fluidRO);
        }

        // adv solar gen requires to be translated up 1 block, so handle the model separately
        ClientRegistration.addCustomModel(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, (orig, evt) -> new TransformedBakedModel<Void>(orig,
              QuadTransformation.translate(new Vector3d(0, 1, 0))));
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
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            return;
        }
        RenderBioGenerator.resetCachedModels();
        GeneratorsSpecialColors.GUI_OBJECTS.parse(MekanismGenerators.rl("textures/colormap/gui_objects.png"));
    }
}