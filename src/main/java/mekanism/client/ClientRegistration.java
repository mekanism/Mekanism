package mekanism.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table.Cell;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.gui.GuiBoilerStats;
import mekanism.client.gui.GuiChemicalTank;
import mekanism.client.gui.GuiDimensionalStabilizer;
import mekanism.client.gui.GuiDynamicTank;
import mekanism.client.gui.GuiEnergyCube;
import mekanism.client.gui.GuiFluidTank;
import mekanism.client.gui.GuiInductionMatrix;
import mekanism.client.gui.GuiLaserAmplifier;
import mekanism.client.gui.GuiLaserTractorBeam;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.GuiMatrixStats;
import mekanism.client.gui.GuiModificationStation;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.GuiPersonalStorageTile;
import mekanism.client.gui.GuiQuantumEntangloporter;
import mekanism.client.gui.GuiSPS;
import mekanism.client.gui.GuiSecurityDesk;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.GuiThermoelectricBoiler;
import mekanism.client.gui.item.GuiDictionary;
import mekanism.client.gui.item.GuiPersonalStorageItem;
import mekanism.client.gui.item.GuiPortableTeleporter;
import mekanism.client.gui.item.GuiSeismicReader;
import mekanism.client.gui.machine.GuiAntiprotonicNucleosynthesizer;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.gui.machine.GuiChemicalDissolutionChamber;
import mekanism.client.gui.machine.GuiChemicalInfuser;
import mekanism.client.gui.machine.GuiChemicalOxidizer;
import mekanism.client.gui.machine.GuiChemicalWasher;
import mekanism.client.gui.machine.GuiCombiner;
import mekanism.client.gui.machine.GuiDigitalMiner;
import mekanism.client.gui.machine.GuiDigitalMinerConfig;
import mekanism.client.gui.machine.GuiElectricPump;
import mekanism.client.gui.machine.GuiElectrolyticSeparator;
import mekanism.client.gui.machine.GuiFactory;
import mekanism.client.gui.machine.GuiFluidicPlenisher;
import mekanism.client.gui.machine.GuiFormulaicAssemblicator;
import mekanism.client.gui.machine.GuiFuelwoodHeater;
import mekanism.client.gui.machine.GuiIsotopicCentrifuge;
import mekanism.client.gui.machine.GuiMetallurgicInfuser;
import mekanism.client.gui.machine.GuiNutritionalLiquifier;
import mekanism.client.gui.machine.GuiOredictionificator;
import mekanism.client.gui.machine.GuiPRC;
import mekanism.client.gui.machine.GuiPaintingMachine;
import mekanism.client.gui.machine.GuiPigmentExtractor;
import mekanism.client.gui.machine.GuiPigmentMixer;
import mekanism.client.gui.machine.GuiPrecisionSawmill;
import mekanism.client.gui.machine.GuiResistiveHeater;
import mekanism.client.gui.machine.GuiRotaryCondensentrator;
import mekanism.client.gui.machine.GuiSeismicVibrator;
import mekanism.client.gui.machine.GuiSolarNeutronActivator;
import mekanism.client.gui.qio.GuiPortableQIODashboard;
import mekanism.client.gui.qio.GuiQIODashboard;
import mekanism.client.gui.qio.GuiQIODriveArray;
import mekanism.client.gui.qio.GuiQIOExporter;
import mekanism.client.gui.qio.GuiQIOImporter;
import mekanism.client.gui.qio.GuiQIOItemFrequencySelect;
import mekanism.client.gui.qio.GuiQIORedstoneAdapter;
import mekanism.client.gui.qio.GuiQIOTileFrequencySelect;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.gui.robit.GuiRobitInventory;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.gui.robit.GuiRobitSmelting;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaMask;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.model.baked.DigitalMinerBakedModel;
import mekanism.client.model.baked.DriveArrayBakedModel;
import mekanism.client.model.baked.ExtensionBakedModel.LightedBakedModel;
import mekanism.client.model.data.DataBasedModelLoader;
import mekanism.client.model.energycube.EnergyCubeModelLoader;
import mekanism.client.model.robit.RobitModel;
import mekanism.client.particle.JetpackFlameParticle;
import mekanism.client.particle.JetpackSmokeParticle;
import mekanism.client.particle.LaserParticle;
import mekanism.client.particle.RadiationParticle;
import mekanism.client.particle.ScubaBubbleParticle;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.client.render.RenderPropertiesProvider.MekCustomArmorRenderProperties;
import mekanism.client.render.RenderPropertiesProvider.MekRenderProperties;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.armor.FreeRunnerArmor;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.armor.ScubaMaskArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.hud.MekaSuitEnergyLevel;
import mekanism.client.render.hud.MekanismHUD;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.client.render.hud.RadiationOverlay;
import mekanism.client.render.item.MekaSuitBarDecorator;
import mekanism.client.render.item.TransmitterTypeDecorator;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaMask;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.client.render.layer.MekanismArmorLayer;
import mekanism.client.render.layer.MekanismElytraLayer;
import mekanism.client.render.obj.TransmitterLoader;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDimensionalStabilizer;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderIndustrialAlarm;
import mekanism.client.render.tileentity.RenderNutritionalLiquifier;
import mekanism.client.render.tileentity.RenderPersonalChest;
import mekanism.client.render.tileentity.RenderPigmentMixer;
import mekanism.client.render.tileentity.RenderSPS;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.tileentity.RenderThermalEvaporationPlant;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderPressurizedTube;
import mekanism.client.render.transmitter.RenderThermodynamicConductor;
import mekanism.client.render.transmitter.RenderUniversalCable;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.lib.FieldReflectionHelper;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.registration.INamedEntry;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.model.SeparateTransformsModel;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class ClientRegistration {

    private static final FieldReflectionHelper<SeparateTransformsModel.Baked, BakedModel> SEPARATE_PERSPECTIVE_BASE_MODEL =
          new FieldReflectionHelper<>(SeparateTransformsModel.Baked.class, "baseModel", () -> null);
    private static final FieldReflectionHelper<SeparateTransformsModel.Baked, ImmutableMap<ItemDisplayContext, BakedModel>> SEPARATE_PERSPECTIVE_PERSPECTIVES =
          new FieldReflectionHelper<>(SeparateTransformsModel.Baked.class, "perspectives", ImmutableMap::of);
    private static final Map<ResourceLocation, CustomModelRegistryObject> customModels = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientTickHandler());
        NeoForge.EVENT_BUS.register(new RenderTickHandler());
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SoundHandler::onTilePlaySound);
        if (Mekanism.hooks.recipeViewerCompatEnabled()) {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RenderTickHandler::guiOpening);
        }
        IModuleHelper moduleHelper = IModuleHelper.INSTANCE;
        moduleHelper.addMekaSuitModuleModels(Mekanism.rl("models/entity/mekasuit_modules.obj"));
        moduleHelper.addMekaSuitModuleModelSpec("jetpack", MekanismModules.JETPACK_UNIT, EquipmentSlot.CHEST);
        moduleHelper.addMekaSuitModuleModelSpec("modulator", MekanismModules.GRAVITATIONAL_MODULATING_UNIT, EquipmentSlot.CHEST);
        moduleHelper.addMekaSuitModuleModelSpec("elytra", MekanismModules.ELYTRA_UNIT, EquipmentSlot.CHEST, LivingEntity::isFallFlying);

        event.enqueueWork(() -> {
            //Set fluids to a translucent render layer
            for (Holder<Fluid> fluid : MekanismFluids.FLUIDS.getFluidEntries()) {
                ItemBlockRenderTypes.setRenderLayer(fluid.value(), RenderType.translucent());
            }
            ClientRegistrationUtil.setPropertyOverride(MekanismBlocks.CARDBOARD_BOX.getItemHolder(), Mekanism.rl("storage"),
                  (stack, world, entity, seed) -> stack.has(MekanismDataComponents.BLOCK_DATA) ? 1 : 0);

            ClientRegistrationUtil.setPropertyOverride(MekanismItems.CRAFTING_FORMULA, Mekanism.rl("invalid"), (stack, world, entity, seed) -> {
                FormulaAttachment attachment = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
                return attachment.hasItems() && attachment.invalid() ? 1 : 0;
            });
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.CRAFTING_FORMULA, Mekanism.rl("encoded"), (stack, world, entity, seed) -> {
                FormulaAttachment attachment = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
                return attachment.hasItems() && !attachment.invalid() ? 1 : 0;
            });
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.CONFIGURATION_CARD, Mekanism.rl("encoded"),
                  (stack, world, entity, seed) -> ((ItemConfigurationCard) stack.getItem()).hasData(stack) ? 1 : 0);
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.CONFIGURATOR, Mekanism.rl("mode"), (stack, world, entity, seed) -> {
                ConfiguratorMode mode = ((ItemConfigurator) stack.getItem()).getMode(stack);
                return switch (mode) {
                    default -> 0;
                    case EMPTY -> 1;
                    case ROTATE -> 2;
                    case WRENCH -> 3;
                };
            });

            ClientRegistrationUtil.setPropertyOverride(MekanismItems.ELECTRIC_BOW, Mekanism.rl("pull"),
                  (stack, world, entity, seed) -> entity != null && entity.getUseItem() == stack ? (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) SharedConstants.TICKS_PER_SECOND : 0);
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.ELECTRIC_BOW, Mekanism.rl("pulling"),
                  (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

            ClientRegistrationUtil.setPropertyOverride(MekanismItems.GEIGER_COUNTER, Mekanism.rl("radiation"), (stack, world, entity, seed) -> {
                if (entity instanceof Player) {
                    return ClientRadiation.getClientScale().ordinal();
                }
                return 0;
            });
            //Note: Our implementation allows for a null entity so don't worry about it and pass it
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.HDPE_REINFORCED_ELYTRA, Mekanism.rl("broken"), (stack, world, entity, seed) -> {
                boolean canFly;
                if (entity == null) {
                    //Fallback to the vanilla check in case any mods like quark are making vanilla actually make use of the entity
                    canFly = stack.getDamageValue() < stack.getMaxDamage() - 1;
                } else {
                    canFly = MekanismItems.HDPE_REINFORCED_ELYTRA.get().canElytraFly(stack, entity);
                }
                return canFly ? 0.0F : 1.0F;
            });
        });

        addCustomModel(MekanismBlocks.QIO_DRIVE_ARRAY, (orig, evt) -> new DriveArrayBakedModel(orig));
        addCustomModel(MekanismBlocks.DIGITAL_MINER, (orig, evt) -> new DigitalMinerBakedModel(orig));

        addLitModel(MekanismItems.MEKA_TOOL);
    }

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        MekanismKeyHandler.registerKeybindings(event);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        //Note: We don't need to include our modid in the id as the active context is grabbed for making an RL inside the event
        event.registerBelowAll(Mekanism.rl("radiation_overlay"), RadiationOverlay.INSTANCE);
        event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, Mekanism.rl("energy_level"), MekaSuitEnergyLevel.INSTANCE);
        //Render status overlay after item name rather than action bar (record_overlay) so that things like the sleep fade will render in front of our overlay
        event.registerAbove(VanillaGuiLayers.SELECTED_ITEM_NAME, Mekanism.rl("status_overlay"), MekanismStatusOverlay.INSTANCE);
        event.registerAbove(VanillaGuiLayers.SUBTITLE_OVERLAY, Mekanism.rl("hud"), MekanismHUD.INSTANCE);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //Register entity rendering handlers
        event.registerEntityRenderer(MekanismEntityTypes.ROBIT.get(), RenderRobit::new);
        event.registerEntityRenderer(MekanismEntityTypes.FLAME.get(), RenderFlame::new);

        //Register TileEntityRenderers
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderThermoelectricBoiler::new, MekanismTileEntityTypes.BOILER_CASING, MekanismTileEntityTypes.BOILER_VALVE);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderDynamicTank::new, MekanismTileEntityTypes.DYNAMIC_TANK, MekanismTileEntityTypes.DYNAMIC_VALVE);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.DIGITAL_MINER.get(), RenderDigitalMiner::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.DIMENSIONAL_STABILIZER.get(), RenderDimensionalStabilizer::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.PERSONAL_CHEST.get(), RenderPersonalChest::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER.get(), RenderNutritionalLiquifier::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.PIGMENT_MIXER.get(), RenderPigmentMixer::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.SEISMIC_VIBRATOR.get(), RenderSeismicVibrator::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.TELEPORTER.get(), RenderTeleporter::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER.get(), RenderThermalEvaporationPlant::new);
        event.registerBlockEntityRenderer(MekanismTileEntityTypes.INDUSTRIAL_ALARM.get(), RenderIndustrialAlarm::new);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderSPS::new, MekanismTileEntityTypes.SPS_CASING, MekanismTileEntityTypes.SPS_PORT);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderBin::new, MekanismTileEntityTypes.BASIC_BIN, MekanismTileEntityTypes.ADVANCED_BIN, MekanismTileEntityTypes.ELITE_BIN,
              MekanismTileEntityTypes.ULTIMATE_BIN, MekanismTileEntityTypes.CREATIVE_BIN);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderEnergyCube::new, MekanismTileEntityTypes.BASIC_ENERGY_CUBE, MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE,
              MekanismTileEntityTypes.ELITE_ENERGY_CUBE, MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE, MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderFluidTank::new, MekanismTileEntityTypes.BASIC_FLUID_TANK, MekanismTileEntityTypes.ADVANCED_FLUID_TANK,
              MekanismTileEntityTypes.ELITE_FLUID_TANK, MekanismTileEntityTypes.ULTIMATE_FLUID_TANK, MekanismTileEntityTypes.CREATIVE_FLUID_TANK);
        //Transmitters
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderLogisticalTransporter::new, MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER,
              MekanismTileEntityTypes.DIVERSION_TRANSPORTER, MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER, MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER,
              MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER, MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderMechanicalPipe::new, MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE,
              MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE, MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE, MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderPressurizedTube::new, MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE,
              MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE, MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE, MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderUniversalCable::new, MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE,
              MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE, MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE, MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderThermodynamicConductor::new, MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR,
              MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelJetpack.JETPACK_LAYER, ModelJetpack::createLayerDefinition);
        event.registerLayerDefinition(ModelArmoredJetpack.ARMORED_JETPACK_LAYER, ModelArmoredJetpack::createLayerDefinition);
        event.registerLayerDefinition(ModelAtomicDisassembler.DISASSEMBLER_LAYER, ModelAtomicDisassembler::createLayerDefinition);
        event.registerLayerDefinition(ModelEnergyCore.CORE_LAYER, ModelEnergyCore::createLayerDefinition);
        event.registerLayerDefinition(ModelFlamethrower.FLAMETHROWER_LAYER, ModelFlamethrower::createLayerDefinition);
        event.registerLayerDefinition(ModelArmoredFreeRunners.ARMORED_FREE_RUNNER_LAYER, ModelArmoredFreeRunners::createLayerDefinition);
        event.registerLayerDefinition(ModelFreeRunners.FREE_RUNNER_LAYER, ModelFreeRunners::createLayerDefinition);
        event.registerLayerDefinition(ModelIndustrialAlarm.ALARM_LAYER, ModelIndustrialAlarm::createLayerDefinition);
        event.registerLayerDefinition(ModelScubaMask.MASK_LAYER, ModelScubaMask::createLayerDefinition);
        event.registerLayerDefinition(ModelScubaTank.TANK_LAYER, ModelScubaTank::createLayerDefinition);
        event.registerLayerDefinition(ModelTransporterBox.BOX_LAYER, ModelTransporterBox::createLayerDefinition);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        //Robit Texture Atlas
        event.registerReloadListener(new RobitSpriteUploader(Minecraft.getInstance().getTextureManager()));
        ClientRegistrationUtil.registerClientReloadListeners(event,
              //ISTERs
              RenderEnergyCubeItem.RENDERER, RenderJetpack.ARMORED_RENDERER, RenderAtomicDisassembler.RENDERER, RenderFlameThrower.RENDERER, RenderFreeRunners.RENDERER,
              RenderFreeRunners.ARMORED_RENDERER, RenderJetpack.RENDERER, RenderScubaMask.RENDERER, RenderScubaTank.RENDERER,
              //Custom Armor
              JetpackArmor.ARMORED_JETPACK, JetpackArmor.JETPACK, FreeRunnerArmor.ARMORED_FREE_RUNNERS, FreeRunnerArmor.FREE_RUNNERS, ScubaMaskArmor.SCUBA_MASK,
              ScubaTankArmor.SCUBA_TANK
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.MODULE_TWEAKER, GuiModuleTweaker::new);

        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.DICTIONARY, GuiDictionary::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PORTABLE_TELEPORTER, GuiPortableTeleporter::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.SEISMIC_READER, GuiSeismicReader::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, GuiQIOItemFrequencySelect::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, GuiPortableQIODashboard::new);

        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.MAIN_ROBIT, GuiRobitMain::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.INVENTORY_ROBIT, GuiRobitInventory::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.SMELTING_ROBIT, GuiRobitSmelting::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.CRAFTING_ROBIT, GuiRobitCrafting::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.REPAIR_ROBIT, GuiRobitRepair::new);

        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.CHEMICAL_INFUSER, GuiChemicalInfuser::new);
        ClientRegistrationUtil.registerAdvancedElectricScreen(event, MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.CHEMICAL_OXIDIZER, GuiChemicalOxidizer::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.CHEMICAL_WASHER, GuiChemicalWasher::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.COMBINER, GuiCombiner::new);
        ClientRegistrationUtil.registerElectricScreen(event, MekanismContainerTypes.CRUSHER);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.DIGITAL_MINER, GuiDigitalMiner::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.DYNAMIC_TANK, GuiDynamicTank::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.ELECTRIC_PUMP, GuiElectricPump::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator::new);
        ClientRegistrationUtil.registerElectricScreen(event, MekanismContainerTypes.ENERGIZED_SMELTER);
        ClientRegistrationUtil.registerElectricScreen(event, MekanismContainerTypes.ENRICHMENT_CHAMBER);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.FLUIDIC_PLENISHER, GuiFluidicPlenisher::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, GuiFormulaicAssemblicator::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.FUELWOOD_HEATER, GuiFuelwoodHeater::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.LASER_AMPLIFIER, GuiLaserAmplifier::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.LASER_TRACTOR_BEAM, GuiLaserTractorBeam::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.METALLURGIC_INFUSER, GuiMetallurgicInfuser::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.OREDICTIONIFICATOR, GuiOredictionificator::new);
        ClientRegistrationUtil.registerAdvancedElectricScreen(event, MekanismContainerTypes.OSMIUM_COMPRESSOR);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PRECISION_SAWMILL, GuiPrecisionSawmill::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, GuiPRC::new);
        ClientRegistrationUtil.registerAdvancedElectricScreen(event, MekanismContainerTypes.PURIFICATION_CHAMBER);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QUANTUM_ENTANGLOPORTER, GuiQuantumEntangloporter::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.RESISTIVE_HEATER, GuiResistiveHeater::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.ROTARY_CONDENSENTRATOR, GuiRotaryCondensentrator::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.SECURITY_DESK, GuiSecurityDesk::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.MODIFICATION_STATION, GuiModificationStation::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.ISOTOPIC_CENTRIFUGE, GuiIsotopicCentrifuge::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.NUTRITIONAL_LIQUIFIER, GuiNutritionalLiquifier::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, GuiAntiprotonicNucleosynthesizer::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PIGMENT_EXTRACTOR, GuiPigmentExtractor::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PIGMENT_MIXER, GuiPigmentMixer::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PAINTING_MACHINE, GuiPaintingMachine::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.SEISMIC_VIBRATOR, GuiSeismicVibrator::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.TELEPORTER, GuiTeleporter::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QIO_DRIVE_ARRAY, GuiQIODriveArray::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QIO_DASHBOARD, GuiQIODashboard::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QIO_IMPORTER, GuiQIOImporter::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QIO_EXPORTER, GuiQIOExporter::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QIO_REDSTONE_ADAPTER, GuiQIORedstoneAdapter::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.SPS, GuiSPS::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.DIMENSIONAL_STABILIZER, GuiDimensionalStabilizer::new);

        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.FACTORY, GuiFactory::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.CHEMICAL_TANK, GuiChemicalTank::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.FLUID_TANK, GuiFluidTank::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.ENERGY_CUBE, GuiEnergyCube::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.INDUCTION_MATRIX, GuiInductionMatrix::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.THERMOELECTRIC_BOILER, GuiThermoelectricBoiler::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PERSONAL_STORAGE_ITEM, GuiPersonalStorageItem::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.PERSONAL_STORAGE_BLOCK, GuiPersonalStorageTile::new);

        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.DIGITAL_MINER_CONFIG, GuiDigitalMinerConfig::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.LOGISTICAL_SORTER, GuiLogisticalSorter::new);

        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.QIO_FREQUENCY_SELECT_TILE, GuiQIOTileFrequencySelect::new);

        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.BOILER_STATS, GuiBoilerStats::new);
        ClientRegistrationUtil.registerScreen(event, MekanismContainerTypes.MATRIX_STATS, GuiMatrixStats::new);
    }

    @SubscribeEvent
    public static void registerModelLoaders(RegisterGeometryLoaders event) {
        event.register(Mekanism.rl("data_based"), DataBasedModelLoader.INSTANCE);
        event.register(Mekanism.rl("energy_cube"), EnergyCubeModelLoader.INSTANCE);
        event.register(Mekanism.rl("robit"), RobitModel.Loader.INSTANCE);
        event.register(Mekanism.rl("transmitter"), TransmitterLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(RegisterAdditional event) {
        MekanismModelCache.INSTANCE.setup(event);
    }

    @SubscribeEvent
    public static void onModelBake(ModifyBakingResult event) {
        event.getModels().replaceAll((rl, model) -> {
            CustomModelRegistryObject obj = customModels.get(rl.id());
            return obj == null ? model : obj.createModel(model, event);
        });
    }

    @SubscribeEvent
    public static void onModelBake(BakingCompleted event) {
        MekanismModelCache.INSTANCE.onBake(event);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MekanismParticleTypes.LASER.get(), LaserParticle.Factory::new);
        event.registerSpriteSet(MekanismParticleTypes.JETPACK_FLAME.get(), JetpackFlameParticle.Factory::new);
        event.registerSpriteSet(MekanismParticleTypes.JETPACK_SMOKE.get(), JetpackSmokeParticle.Factory::new);
        event.registerSpriteSet(MekanismParticleTypes.SCUBA_BUBBLE.get(), ScubaBubbleParticle.Factory::new);
        event.registerSpriteSet(MekanismParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        ClientRegistrationUtil.registerBlockColorHandler(event, (state, world, pos, tintIndex) -> {
                  if (tintIndex == 1) {
                      BaseTier tier = Attribute.getBaseTier(state.getBlockHolder());
                      if (tier != null) {
                          return tier.getPackedColor();
                      }
                  }
                  return -1;
              }, MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK);
        ClientRegistrationUtil.registerBlockColorHandler(event, (state, world, pos, tintIndex) -> {
                  if (pos != null) {
                      TileEntityQIOComponent tile = WorldUtils.getTileEntity(TileEntityQIOComponent.class, world, pos);
                      if (tile != null) {
                          EnumColor color = tile.getColor();
                          return color == null ? -1 : color.getPackedColor();
                      }
                  }
                  return -1;
              }, MekanismBlocks.QIO_DRIVE_ARRAY, MekanismBlocks.QIO_DASHBOARD, MekanismBlocks.QIO_IMPORTER, MekanismBlocks.QIO_EXPORTER,
              MekanismBlocks.QIO_REDSTONE_ADAPTER);
        ClientRegistrationUtil.registerBlockColorHandler(event, (state, world, pos, tintIndex) -> {
                  if (tintIndex == 1 && pos != null) {
                      TileEntityLogisticalTransporter transporter = WorldUtils.getTileEntity(TileEntityLogisticalTransporter.class, world, pos);
                      if (transporter != null) {
                          EnumColor renderColor = transporter.getTransmitter().getColor();
                          if (renderColor != null) {
                              return renderColor.getPackedColor();
                          }
                      }
                  }
                  return -1;
              }, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER,
              MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER);
        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            if (entry.getKey() instanceof PrimaryResource primaryResource) {
                int tint = primaryResource.getTint();
                ClientRegistrationUtil.registerBlockColorHandler(event, (state, world, pos, index) -> index == 1 ? tint : -1, entry.getValue());
            }
        }
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        ClientRegistrationUtil.registerItemColorHandler(event, (stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (tintIndex == 1 && item instanceof ItemBlockFluidTank tank) {
                      return tank.getTier().getBaseTier().getPackedColor();
                  }
                  return -1;
              }, MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK);
        ClientRegistrationUtil.registerBucketColorHandler(event, MekanismFluids.FLUIDS);
        for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            int tint = item.getColumnKey().getTint();
            ClientRegistrationUtil.registerItemColorHandler(event, (stack, index) -> index == 1 ? tint : -1, item.getValue());
        }
        ClientRegistrationUtil.registerIColoredItemHandler(event, MekanismItems.PORTABLE_QIO_DASHBOARD, MekanismBlocks.QIO_DRIVE_ARRAY, MekanismBlocks.QIO_DASHBOARD,
              MekanismBlocks.QIO_IMPORTER, MekanismBlocks.QIO_EXPORTER, MekanismBlocks.QIO_REDSTONE_ADAPTER);

        ClientRegistrationUtil.registerItemColorHandler(event, (stack, index) -> {
            if (index == 1) {
                IModule<ModuleColorModulationUnit> colorModulationUnit = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.COLOR_MODULATION_UNIT);
                if (colorModulationUnit == null) {
                    return -1;
                }
                return colorModulationUnit.getCustomInstance().tintARGB();
            }
            return -1;
        }, MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS, MekanismItems.MEKASUIT_BOOTS);

        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            if (entry.getKey() instanceof PrimaryResource primaryResource) {
                int tint = primaryResource.getTint();
                ClientRegistrationUtil.registerItemColorHandler(event, (stack, index) -> index == 1 ? tint : -1, entry.getValue());
            }
        }
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(MekanismItems.MEKASUIT_HELMET, MekaSuitBarDecorator.INSTANCE);
        event.register(MekanismItems.MEKASUIT_BODYARMOR, MekaSuitBarDecorator.INSTANCE);
        TransmitterTypeDecorator.registerDecorators(event, MekanismBlocks.BASIC_PRESSURIZED_TUBE, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE,
              MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR,
              MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR,
              MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new MekCustomArmorRenderProperties(RenderJetpack.ARMORED_RENDERER, JetpackArmor.ARMORED_JETPACK), MekanismItems.ARMORED_JETPACK);
        event.registerItem(new MekCustomArmorRenderProperties(RenderJetpack.RENDERER, JetpackArmor.JETPACK), MekanismItems.JETPACK);
        event.registerItem(new MekCustomArmorRenderProperties(RenderFreeRunners.ARMORED_RENDERER, FreeRunnerArmor.ARMORED_FREE_RUNNERS), MekanismItems.ARMORED_FREE_RUNNERS);
        event.registerItem(new MekCustomArmorRenderProperties(RenderFreeRunners.RENDERER, FreeRunnerArmor.FREE_RUNNERS), MekanismItems.FREE_RUNNERS);
        event.registerItem(new MekCustomArmorRenderProperties(RenderScubaMask.RENDERER, ScubaMaskArmor.SCUBA_MASK), MekanismItems.SCUBA_MASK);
        event.registerItem(new MekCustomArmorRenderProperties(RenderScubaTank.RENDERER, ScubaTankArmor.SCUBA_TANK), MekanismItems.SCUBA_TANK);
        event.registerItem(new MekRenderProperties(RenderAtomicDisassembler.RENDERER), MekanismItems.ATOMIC_DISASSEMBLER);
        event.registerItem(new MekRenderProperties(RenderFlameThrower.RENDERER), MekanismItems.FLAMETHROWER);

        event.registerItem(MekaSuitArmor.HELMET, MekanismItems.MEKASUIT_HELMET);
        event.registerItem(MekaSuitArmor.BODYARMOR, MekanismItems.MEKASUIT_BODYARMOR);
        event.registerItem(MekaSuitArmor.PANTS, MekanismItems.MEKASUIT_PANTS);
        event.registerItem(MekaSuitArmor.BOOTS, MekanismItems.MEKASUIT_BOOTS);

        ClientRegistrationUtil.registerItemExtensions(event, new MekRenderProperties(RenderEnergyCubeItem.RENDERER), MekanismBlocks.BASIC_ENERGY_CUBE,
              MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismBlocks.CREATIVE_ENERGY_CUBE);
        ClientRegistrationUtil.registerItemExtensions(event, new MekRenderProperties(RenderFluidTankItem.RENDERER), MekanismBlocks.BASIC_FLUID_TANK,
              MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.CREATIVE_FLUID_TANK);

        event.registerBlock(RenderPropertiesProvider.boundingParticles(), MekanismBlocks.BOUNDING_BLOCK);
        ClientRegistrationUtil.registerBlockExtensions(event, MekanismBlocks.BLOCKS);
        ClientRegistrationUtil.registerFluidExtensions(event, MekanismFluids.FLUIDS);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        //Add our own custom armor and elytra layer to the various player renderers
        for (PlayerSkin.Model skin : event.getSkins()) {
            //Note: We expect this to always be an instanceof PlayerRenderer, but we just bother checking if it is a LivingEntityRenderer
            if (event.getSkin(skin) instanceof LivingEntityRenderer<?, ?> renderer) {
                addCustomLayers(EntityType.PLAYER, renderer, event.getContext());
            }
        }
        //Add our own custom armor and elytra layer to everything that has an armor layer
        //Note: This includes any modded mobs that have vanilla's HumanoidArmorLayer or ElytraLayer added to them
        for (EntityType<?> entityType : event.getEntityTypes()) {
            if (event.getRenderer(entityType) instanceof LivingEntityRenderer<?, ?> renderer) {
                addCustomLayers(entityType, renderer, event.getContext());
            }
        }
    }

    private static <LIVING extends LivingEntity, MODEL extends EntityModel<LIVING>> void addCustomLayers(@NotNull EntityType<?> type,
          @NotNull LivingEntityRenderer<LIVING, MODEL> renderer, @NotNull EntityRendererProvider.Context context) {
        int layerTypes = 2;
        Map<String, RenderLayer<LIVING, MODEL>> layersToAdd = new HashMap<>(layerTypes);
        for (RenderLayer<LIVING, MODEL> layerRenderer : renderer.layers) {
            //Validate against the layer render being null, as it seems like some mods do stupid things and add in null layers
            if (layerRenderer != null) {
                //Only allow an exact class match, so we don't add to modded entities that only have a modded extended armor or elytra layer
                Class<?> layerClass = layerRenderer.getClass();
                if (layerClass == HumanoidArmorLayer.class) {
                    //Note: We know that the MODEL is actually an instance of HumanoidModel, or there wouldn't be a
                    //noinspection unchecked,rawtypes
                    layersToAdd.put("Armor", new MekanismArmorLayer(renderer, (HumanoidArmorLayer<LIVING, ?, ?>) layerRenderer, context.getModelManager()));
                    if (layersToAdd.size() == layerTypes) {
                        break;
                    }
                } else if (layerClass == ElytraLayer.class) {
                    layersToAdd.put("Elytra", new MekanismElytraLayer<>(renderer, context.getModelSet()));
                    if (layersToAdd.size() == layerTypes) {
                        break;
                    }
                }
            }
        }
        if (!layersToAdd.isEmpty()) {
            String entityName = Util.getRegisteredName(BuiltInRegistries.ENTITY_TYPE, type);
            for (Map.Entry<String, RenderLayer<LIVING, MODEL>> entry : layersToAdd.entrySet()) {
                renderer.addLayer(entry.getValue());
                Mekanism.logger.debug("Added Mekanism {} Layer to entity of type: {}", entry.getKey(), entityName);
            }
        }
    }

    public static void addCustomModel(INamedEntry provider, CustomModelRegistryObject object) {
        customModels.put(provider.getId(), object);
    }

    public static void addLitModel(INamedEntry... entries) {
        for (INamedEntry namedEntry : entries) {
            addCustomModel(namedEntry, (orig, evt) -> lightBakedModel(orig));
        }
    }

    private static BakedModel lightBakedModel(BakedModel orig) {
        if (orig instanceof SeparateTransformsModel.Baked separatePerspectiveModel) {
            //Transform inner components of the separate perspective model and then return the original model
            SEPARATE_PERSPECTIVE_BASE_MODEL.transformValue(separatePerspectiveModel, Objects::nonNull, ClientRegistration::lightBakedModel);
            SEPARATE_PERSPECTIVE_PERSPECTIVES.transformValue(separatePerspectiveModel, v -> !v.isEmpty(), org -> ImmutableMap.copyOf(Maps.transformValues(org,
                  ClientRegistration::lightBakedModel)));
            return orig;
        }
        return new LightedBakedModel(orig);
    }

    @FunctionalInterface
    public interface CustomModelRegistryObject {

        BakedModel createModel(BakedModel original, ModifyBakingResult event);
    }
}