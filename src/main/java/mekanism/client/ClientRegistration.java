package mekanism.client;

import java.util.Map;
import java.util.function.Function;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiDictionary;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDigitalMinerConfig;
import mekanism.client.gui.GuiDynamicTank;
import mekanism.client.gui.GuiElectricPump;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnergizedSmelter;
import mekanism.client.gui.GuiEnergyCube;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiFactory;
import mekanism.client.gui.GuiFluidTank;
import mekanism.client.gui.GuiFluidicPlenisher;
import mekanism.client.gui.GuiFormulaicAssemblicator;
import mekanism.client.gui.GuiFuelwoodHeater;
import mekanism.client.gui.GuiGasTank;
import mekanism.client.gui.GuiInductionMatrix;
import mekanism.client.gui.GuiLaserAmplifier;
import mekanism.client.gui.GuiLaserTractorBeam;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOredictionificator;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPersonalChestItem;
import mekanism.client.gui.GuiPersonalChestTile;
import mekanism.client.gui.GuiPortableTeleporter;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiQuantumEntangloporter;
import mekanism.client.gui.GuiResistiveHeater;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiSecurityDesk;
import mekanism.client.gui.GuiSeismicReader;
import mekanism.client.gui.GuiSeismicVibrator;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.GuiThermoelectricBoiler;
import mekanism.client.gui.chemical.GuiChemicalCrystallizer;
import mekanism.client.gui.chemical.GuiChemicalDissolutionChamber;
import mekanism.client.gui.chemical.GuiChemicalInfuser;
import mekanism.client.gui.chemical.GuiChemicalInjectionChamber;
import mekanism.client.gui.chemical.GuiChemicalOxidizer;
import mekanism.client.gui.chemical.GuiChemicalWasher;
import mekanism.client.gui.filter.GuiMFilterSelect;
import mekanism.client.gui.filter.GuiMItemStackFilter;
import mekanism.client.gui.filter.GuiMMaterialFilter;
import mekanism.client.gui.filter.GuiMModIDFilter;
import mekanism.client.gui.filter.GuiMOreDictFilter;
import mekanism.client.gui.filter.GuiOredictionificatorFilter;
import mekanism.client.gui.filter.GuiTFilterSelect;
import mekanism.client.gui.filter.GuiTItemStackFilter;
import mekanism.client.gui.filter.GuiTMaterialFilter;
import mekanism.client.gui.filter.GuiTModIDFilter;
import mekanism.client.gui.filter.GuiTOreDictFilter;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.gui.robit.GuiRobitInventory;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.gui.robit.GuiRobitSmelting;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.block.RenderChemicalCrystallizerItem;
import mekanism.client.render.item.block.RenderChemicalDissolutionChamberItem;
import mekanism.client.render.item.block.RenderDigitalMinerItem;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.block.RenderPersonalChestItem;
import mekanism.client.render.item.block.RenderQuantumEntangloporterItem;
import mekanism.client.render.item.block.RenderResistiveHeaterItem;
import mekanism.client.render.item.block.RenderSecurityDeskItem;
import mekanism.client.render.item.block.RenderSeismicVibratorItem;
import mekanism.client.render.item.block.RenderSolarNeutronActivatorItem;
import mekanism.client.render.item.gear.RenderArmoredJetpack;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderGasMask;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.client.render.obj.MekanismOBJLoader;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderChemicalCrystallizer;
import mekanism.client.render.tileentity.RenderChemicalDissolutionChamber;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderGasTank;
import mekanism.client.render.tileentity.RenderPersonalChest;
import mekanism.client.render.tileentity.RenderQuantumEntangloporter;
import mekanism.client.render.tileentity.RenderResistiveHeater;
import mekanism.client.render.tileentity.RenderSecurityDesk;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderSolarNeutronActivator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.tileentity.RenderThermalEvaporationController;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderPressurizedTube;
import mekanism.client.render.transmitter.RenderThermodynamicConductor;
import mekanism.client.render.transmitter.RenderUniversalCable;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        MekanismRenderer.init();

        OBJLoader.INSTANCE.addDomain(Mekanism.MODID);
        ModelLoaderRegistry.registerLoader(MekanismOBJLoader.INSTANCE);

        MinecraftForge.EVENT_BUS.register(MekanismOBJLoader.INSTANCE);

        //Register entity rendering handlers
        RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, RenderObsidianTNTPrimed::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, RenderRobit::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class, RenderBalloon::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBabySkeleton.class, SkeletonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, RenderFlame::new);
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        ScreenManager.registerFactory(MekanismContainerTypes.DICTIONARY, GuiDictionary::new);
        ScreenManager.registerFactory(MekanismContainerTypes.PORTABLE_TELEPORTER, GuiPortableTeleporter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.SEISMIC_READER, GuiSeismicReader::new);

        ScreenManager.registerFactory(MekanismContainerTypes.MAIN_ROBIT, GuiRobitMain::new);
        ScreenManager.registerFactory(MekanismContainerTypes.INVENTORY_ROBIT, GuiRobitInventory::new);
        ScreenManager.registerFactory(MekanismContainerTypes.SMELTING_ROBIT, GuiRobitSmelting::new);
        ScreenManager.registerFactory(MekanismContainerTypes.CRAFTING_ROBIT, GuiRobitCrafting::new);
        ScreenManager.registerFactory(MekanismContainerTypes.REPAIR_ROBIT, GuiRobitRepair::new);

        ScreenManager.registerFactory(MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer::new);
        ScreenManager.registerFactory(MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber::new);
        ScreenManager.registerFactory(MekanismContainerTypes.CHEMICAL_INFUSER, GuiChemicalInfuser::new);
        ScreenManager.registerFactory(MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER, GuiChemicalInjectionChamber::new);
        ScreenManager.registerFactory(MekanismContainerTypes.CHEMICAL_OXIDIZER, GuiChemicalOxidizer::new);
        ScreenManager.registerFactory(MekanismContainerTypes.CHEMICAL_WASHER, GuiChemicalWasher::new);
        ScreenManager.registerFactory(MekanismContainerTypes.COMBINER, GuiCombiner::new);
        ScreenManager.registerFactory(MekanismContainerTypes.CRUSHER, GuiCrusher::new);
        ScreenManager.registerFactory(MekanismContainerTypes.DIGITAL_MINER, GuiDigitalMiner::new);
        ScreenManager.registerFactory(MekanismContainerTypes.DYNAMIC_TANK, GuiDynamicTank::new);
        ScreenManager.registerFactory(MekanismContainerTypes.ELECTRIC_PUMP, GuiElectricPump::new);
        ScreenManager.registerFactory(MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator::new);
        ScreenManager.registerFactory(MekanismContainerTypes.ENERGIZED_SMELTER, GuiEnergizedSmelter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.ENRICHMENT_CHAMBER, GuiEnrichmentChamber::new);
        ScreenManager.registerFactory(MekanismContainerTypes.FLUIDIC_PLENISHER, GuiFluidicPlenisher::new);
        ScreenManager.registerFactory(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, GuiFormulaicAssemblicator::new);
        ScreenManager.registerFactory(MekanismContainerTypes.FUELWOOD_HEATER, GuiFuelwoodHeater::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LASER_AMPLIFIER, GuiLaserAmplifier::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LASER_TRACTOR_BEAM, GuiLaserTractorBeam::new);
        ScreenManager.registerFactory(MekanismContainerTypes.METALLURGIC_INFUSER, GuiMetallurgicInfuser::new);
        ScreenManager.registerFactory(MekanismContainerTypes.OREDICTIONIFICATOR, GuiOredictionificator::new);
        ScreenManager.registerFactory(MekanismContainerTypes.OSMIUM_COMPRESSOR, GuiOsmiumCompressor::new);
        ScreenManager.registerFactory(MekanismContainerTypes.PRECISION_SAWMILL, GuiPrecisionSawmill::new);
        ScreenManager.registerFactory(MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, GuiPRC::new);
        ScreenManager.registerFactory(MekanismContainerTypes.PURIFICATION_CHAMBER, GuiPurificationChamber::new);
        ScreenManager.registerFactory(MekanismContainerTypes.QUANTUM_ENTANGLOPORTER, GuiQuantumEntangloporter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.RESISTIVE_HEATER, GuiResistiveHeater::new);
        ScreenManager.registerFactory(MekanismContainerTypes.ROTARY_CONDENSENTRATOR, GuiRotaryCondensentrator::new);
        ScreenManager.registerFactory(MekanismContainerTypes.SECURITY_DESK, GuiSecurityDesk::new);
        ScreenManager.registerFactory(MekanismContainerTypes.SEISMIC_VIBRATOR, GuiSeismicVibrator::new);
        ScreenManager.registerFactory(MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator::new);
        ScreenManager.registerFactory(MekanismContainerTypes.TELEPORTER, GuiTeleporter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController::new);

        ScreenManager.registerFactory(MekanismContainerTypes.FACTORY, GuiFactory::new);
        ScreenManager.registerFactory(MekanismContainerTypes.GAS_TANK, GuiGasTank::new);
        ScreenManager.registerFactory(MekanismContainerTypes.FLUID_TANK, GuiFluidTank::new);
        ScreenManager.registerFactory(MekanismContainerTypes.ENERGY_CUBE, GuiEnergyCube::new);
        ScreenManager.registerFactory(MekanismContainerTypes.INDUCTION_MATRIX, GuiInductionMatrix::new);
        ScreenManager.registerFactory(MekanismContainerTypes.THERMOELECTRIC_BOILER, GuiThermoelectricBoiler::new);
        //TODO: Fix UpgradeManagement gui registration
        //ScreenManager.registerFactory(MekanismContainerTypes.UPGRADE_MANAGEMENT, GuiUpgradeManagement::new);
        ScreenManager.registerFactory(MekanismContainerTypes.PERSONAL_CHEST_ITEM, GuiPersonalChestItem::new);
        ScreenManager.registerFactory(MekanismContainerTypes.PERSONAL_CHEST_BLOCK, GuiPersonalChestTile::new);

        ScreenManager.registerFactory(MekanismContainerTypes.DIGITAL_MINER_CONFIG, GuiDigitalMinerConfig::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LOGISTICAL_SORTER, GuiLogisticalSorter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.DM_FILTER_SELECT, GuiMFilterSelect::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LS_FILTER_SELECT, GuiTFilterSelect::new);
        ScreenManager.registerFactory(MekanismContainerTypes.DM_TAG_FILTER, GuiMOreDictFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LS_TAG_FILTER, GuiTOreDictFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.DM_MOD_ID_FILTER, GuiMModIDFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LS_MOD_ID_FILTER, GuiTModIDFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.DM_MATERIAL_FILTER, GuiMMaterialFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LS_MATERIAL_FILTER, GuiTMaterialFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.DM_ITEMSTACK_FILTER, GuiMItemStackFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.LS_ITEMSTACK_FILTER, GuiTItemStackFilter::new);
        ScreenManager.registerFactory(MekanismContainerTypes.OREDICTIONIFICATOR_FILTER, GuiOredictionificatorFilter::new);
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBin.class, new RenderBin());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerCasing.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerValve.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalCrystallizer.class, new RenderChemicalCrystallizer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalDissolutionChamber.class, new RenderChemicalDissolutionChamber());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalInjectionChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCombiner.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrusher.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDigitalMiner.class, new RenderDigitalMiner());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDiversionTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicTank.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicValve.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergizedSmelter.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyCube.class, new RenderEnergyCube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnrichmentChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFactory.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidTank.class, RenderFluidTank.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFormulaicAssemblicator.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasTank.class, new RenderGasTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLogisticalTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechanicalPipe.class, new RenderMechanicalPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMetallurgicInfuser.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOsmiumCompressor.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPRC.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPersonalChest.class, new RenderPersonalChest());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPrecisionSawmill.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressurizedTube.class, new RenderPressurizedTube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPurificationChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityQuantumEntangloporter.class, new RenderQuantumEntangloporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityResistiveHeater.class, new RenderResistiveHeater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRestrictiveTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityDesk.class, new RenderSecurityDesk());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySeismicVibrator.class, new RenderSeismicVibrator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarNeutronActivator.class, new RenderSolarNeutronActivator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderTeleporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermalEvaporationController.class, new RenderThermalEvaporationController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermodynamicConductor.class, new RenderThermodynamicConductor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalCable.class, new RenderUniversalCable());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        registerItemStackModel(modelRegistry, "jetpack", model -> RenderJetpack.model = model);
        registerItemStackModel(modelRegistry, "jetpack_armored", model -> RenderArmoredJetpack.model = model);
        registerItemStackModel(modelRegistry, "gas_mask", model -> RenderGasMask.model = model);
        registerItemStackModel(modelRegistry, "scuba_tank", model -> RenderScubaTank.model = model);
        registerItemStackModel(modelRegistry, "free_runners", model -> RenderFreeRunners.model = model);
        registerItemStackModel(modelRegistry, "atomic_disassembler", model -> RenderAtomicDisassembler.model = model);
        registerItemStackModel(modelRegistry, "flamethrower", model -> RenderFlameThrower.model = model);
        registerItemStackModel(modelRegistry, "digital_miner", model -> RenderDigitalMinerItem.model = model);
        registerItemStackModel(modelRegistry, "solar_neutron_activator", model -> RenderSolarNeutronActivatorItem.model = model);
        registerItemStackModel(modelRegistry, "chemical_dissolution_chamber", model -> RenderChemicalDissolutionChamberItem.model = model);
        registerItemStackModel(modelRegistry, "chemical_crystallizer", model -> RenderChemicalCrystallizerItem.model = model);
        registerItemStackModel(modelRegistry, "seismic_vibrator", model -> RenderSeismicVibratorItem.model = model);
        registerItemStackModel(modelRegistry, "quantum_entangloporter", model -> RenderQuantumEntangloporterItem.model = model);
        registerItemStackModel(modelRegistry, "resistive_heater", model -> RenderResistiveHeaterItem.model = model);
        registerItemStackModel(modelRegistry, "personal_chest", model -> RenderPersonalChestItem.model = model);
        registerItemStackModel(modelRegistry, "security_desk", model -> RenderSecurityDeskItem.model = model);

        //TODO: Does tier matter
        registerItemStackModel(modelRegistry, "energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "fluid_tank", model -> RenderFluidTankItem.model = model);
    }

    private static ModelResourceLocation getInventoryMRL(String type) {
        return new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, type), "inventory");
    }

    private static void registerItemStackModel(Map<ResourceLocation, IBakedModel> modelRegistry, String type, Function<ItemLayerWrapper, IBakedModel> setModel) {
        ModelResourceLocation resourceLocation = getInventoryMRL(type);
        modelRegistry.put(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.get(resourceLocation))));
    }
}