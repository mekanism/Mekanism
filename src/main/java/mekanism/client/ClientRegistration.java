package mekanism.client;

import java.util.Map;
import java.util.function.Function;
import mekanism.api.block.IColoredBlock;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.gui.GuiBoilerStats;
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
import mekanism.client.gui.GuiMatrixStats;
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
import mekanism.client.gui.GuiSideConfiguration;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.GuiThermoelectricBoiler;
import mekanism.client.gui.GuiTransporterConfig;
import mekanism.client.gui.GuiUpgradeManagement;
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
import mekanism.client.particle.ParticleLaser;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.entity.RenderFlame;
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
import mekanism.common.MekanismBlock;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.particle.MekanismParticleType;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
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
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
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
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
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
        //Note: The JavaDocs of the below methods specifies to register this here rather than during the EntityType registration

        //Register entity rendering handlers
        RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, RenderRobit::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, RenderFlame::new);

        //Register TileEntityRenderers
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBin.class, new RenderBin());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerCasing.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerValve.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalCrystallizer.class, new RenderChemicalCrystallizer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalDissolutionChamber.class, new RenderChemicalDissolutionChamber());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalInjectionChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCombiner.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrusher.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDigitalMiner.class, new RenderDigitalMiner());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicTank.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicValve.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergizedSmelter.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyCube.class, new RenderEnergyCube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnrichmentChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFactory.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidTank.class, RenderFluidTank.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFormulaicAssemblicator.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasTank.class, new RenderGasTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMetallurgicInfuser.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOsmiumCompressor.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressurizedReactionChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPersonalChest.class, new RenderPersonalChest());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPrecisionSawmill.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPurificationChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityQuantumEntangloporter.class, new RenderQuantumEntangloporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityResistiveHeater.class, new RenderResistiveHeater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityDesk.class, new RenderSecurityDesk());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySeismicVibrator.class, new RenderSeismicVibrator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarNeutronActivator.class, new RenderSolarNeutronActivator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderTeleporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermalEvaporationController.class, new RenderThermalEvaporationController());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLogisticalTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechanicalPipe.class, new RenderMechanicalPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressurizedTube.class, new RenderPressurizedTube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRestrictiveTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermodynamicConductor.class, new RenderThermodynamicConductor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalCable.class, new RenderUniversalCable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDiversionTransporter.class, new RenderLogisticalTransporter());
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        registerScreen(MekanismContainerTypes.DICTIONARY, GuiDictionary::new);
        registerScreen(MekanismContainerTypes.PORTABLE_TELEPORTER, GuiPortableTeleporter::new);
        registerScreen(MekanismContainerTypes.SEISMIC_READER, GuiSeismicReader::new);

        registerScreen(MekanismContainerTypes.MAIN_ROBIT, GuiRobitMain::new);
        registerScreen(MekanismContainerTypes.INVENTORY_ROBIT, GuiRobitInventory::new);
        registerScreen(MekanismContainerTypes.SMELTING_ROBIT, GuiRobitSmelting::new);
        registerScreen(MekanismContainerTypes.CRAFTING_ROBIT, GuiRobitCrafting::new);
        registerScreen(MekanismContainerTypes.REPAIR_ROBIT, GuiRobitRepair::new);

        registerScreen(MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer::new);
        registerScreen(MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber::new);
        registerScreen(MekanismContainerTypes.CHEMICAL_INFUSER, GuiChemicalInfuser::new);
        registerScreen(MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER, GuiChemicalInjectionChamber::new);
        registerScreen(MekanismContainerTypes.CHEMICAL_OXIDIZER, GuiChemicalOxidizer::new);
        registerScreen(MekanismContainerTypes.CHEMICAL_WASHER, GuiChemicalWasher::new);
        registerScreen(MekanismContainerTypes.COMBINER, GuiCombiner::new);
        registerScreen(MekanismContainerTypes.CRUSHER, GuiCrusher::new);
        registerScreen(MekanismContainerTypes.DIGITAL_MINER, GuiDigitalMiner::new);
        registerScreen(MekanismContainerTypes.DYNAMIC_TANK, GuiDynamicTank::new);
        registerScreen(MekanismContainerTypes.ELECTRIC_PUMP, GuiElectricPump::new);
        registerScreen(MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator::new);
        registerScreen(MekanismContainerTypes.ENERGIZED_SMELTER, GuiEnergizedSmelter::new);
        registerScreen(MekanismContainerTypes.ENRICHMENT_CHAMBER, GuiEnrichmentChamber::new);
        registerScreen(MekanismContainerTypes.FLUIDIC_PLENISHER, GuiFluidicPlenisher::new);
        registerScreen(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, GuiFormulaicAssemblicator::new);
        registerScreen(MekanismContainerTypes.FUELWOOD_HEATER, GuiFuelwoodHeater::new);
        registerScreen(MekanismContainerTypes.LASER_AMPLIFIER, GuiLaserAmplifier::new);
        registerScreen(MekanismContainerTypes.LASER_TRACTOR_BEAM, GuiLaserTractorBeam::new);
        registerScreen(MekanismContainerTypes.METALLURGIC_INFUSER, GuiMetallurgicInfuser::new);
        registerScreen(MekanismContainerTypes.OREDICTIONIFICATOR, GuiOredictionificator::new);
        registerScreen(MekanismContainerTypes.OSMIUM_COMPRESSOR, GuiOsmiumCompressor::new);
        registerScreen(MekanismContainerTypes.PRECISION_SAWMILL, GuiPrecisionSawmill::new);
        registerScreen(MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, GuiPRC::new);
        registerScreen(MekanismContainerTypes.PURIFICATION_CHAMBER, GuiPurificationChamber::new);
        registerScreen(MekanismContainerTypes.QUANTUM_ENTANGLOPORTER, GuiQuantumEntangloporter::new);
        registerScreen(MekanismContainerTypes.RESISTIVE_HEATER, GuiResistiveHeater::new);
        registerScreen(MekanismContainerTypes.ROTARY_CONDENSENTRATOR, GuiRotaryCondensentrator::new);
        registerScreen(MekanismContainerTypes.SECURITY_DESK, GuiSecurityDesk::new);
        registerScreen(MekanismContainerTypes.SEISMIC_VIBRATOR, GuiSeismicVibrator::new);
        registerScreen(MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator::new);
        registerScreen(MekanismContainerTypes.TELEPORTER, GuiTeleporter::new);
        registerScreen(MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController::new);

        registerScreen(MekanismContainerTypes.FACTORY, GuiFactory::new);
        registerScreen(MekanismContainerTypes.GAS_TANK, GuiGasTank::new);
        registerScreen(MekanismContainerTypes.FLUID_TANK, GuiFluidTank::new);
        registerScreen(MekanismContainerTypes.ENERGY_CUBE, GuiEnergyCube::new);
        registerScreen(MekanismContainerTypes.INDUCTION_MATRIX, GuiInductionMatrix::new);
        registerScreen(MekanismContainerTypes.THERMOELECTRIC_BOILER, GuiThermoelectricBoiler::new);
        registerScreen(MekanismContainerTypes.PERSONAL_CHEST_ITEM, GuiPersonalChestItem::new);
        registerScreen(MekanismContainerTypes.PERSONAL_CHEST_BLOCK, GuiPersonalChestTile::new);

        registerScreen(MekanismContainerTypes.DIGITAL_MINER_CONFIG, GuiDigitalMinerConfig::new);
        registerScreen(MekanismContainerTypes.LOGISTICAL_SORTER, GuiLogisticalSorter::new);
        registerScreen(MekanismContainerTypes.DM_FILTER_SELECT, GuiMFilterSelect::new);
        registerScreen(MekanismContainerTypes.LS_FILTER_SELECT, GuiTFilterSelect::new);
        registerScreen(MekanismContainerTypes.DM_TAG_FILTER, GuiMOreDictFilter::new);
        registerScreen(MekanismContainerTypes.LS_TAG_FILTER, GuiTOreDictFilter::new);
        registerScreen(MekanismContainerTypes.DM_MOD_ID_FILTER, GuiMModIDFilter::new);
        registerScreen(MekanismContainerTypes.LS_MOD_ID_FILTER, GuiTModIDFilter::new);
        registerScreen(MekanismContainerTypes.DM_MATERIAL_FILTER, GuiMMaterialFilter::new);
        registerScreen(MekanismContainerTypes.LS_MATERIAL_FILTER, GuiTMaterialFilter::new);
        registerScreen(MekanismContainerTypes.DM_ITEMSTACK_FILTER, GuiMItemStackFilter::new);
        registerScreen(MekanismContainerTypes.LS_ITEMSTACK_FILTER, GuiTItemStackFilter::new);
        registerScreen(MekanismContainerTypes.OREDICTIONIFICATOR_FILTER, GuiOredictionificatorFilter::new);

        //TODO: Add any missing ones like side configuration
        registerScreen(MekanismContainerTypes.UPGRADE_MANAGEMENT, GuiUpgradeManagement::new);
        registerScreen(MekanismContainerTypes.SIDE_CONFIGURATION, GuiSideConfiguration::new);
        registerScreen(MekanismContainerTypes.TRANSPORTER_CONFIGURATION, GuiTransporterConfig::new);

        registerScreen(MekanismContainerTypes.BOILER_STATS, GuiBoilerStats::new);
        registerScreen(MekanismContainerTypes.MATRIX_STATS, GuiMatrixStats::new);
    }

    private static <C extends Container, U extends Screen & IHasContainer<C>> void registerScreen(ContainerTypeRegistryObject<C> type, IScreenFactory<C, U> factory) {
        ScreenManager.registerFactory(type.getContainerType(), factory);
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

        registerItemStackModel(modelRegistry, "basic_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "advanced_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "elite_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "ultimate_energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "creative_energy_cube", model -> RenderEnergyCubeItem.model = model);

        registerItemStackModel(modelRegistry, "basic_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "advanced_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "elite_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "ultimate_fluid_tank", model -> RenderFluidTankItem.model = model);
        registerItemStackModel(modelRegistry, "creative_fluid_tank", model -> RenderFluidTankItem.model = model);
    }

    private static ModelResourceLocation getInventoryMRL(String type) {
        return new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, type), "inventory");
    }

    private static void registerItemStackModel(Map<ResourceLocation, IBakedModel> modelRegistry, String type, Function<ItemLayerWrapper, IBakedModel> setModel) {
        ModelResourceLocation resourceLocation = getInventoryMRL(type);
        modelRegistry.put(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.get(resourceLocation))));
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(MekanismParticleType.LASER, ParticleLaser.Factory::new);
    }

    //TODO: Move this to a utils class
    private static void registerBlockColorHandler(BlockColors blockColors, ItemColors itemColors, IBlockColor blockColor, IItemColor itemColor, IBlockProvider... blocks) {
        for (IBlockProvider mekanismBlock : blocks) {
            blockColors.register(blockColor, mekanismBlock.getBlock());
            itemColors.register(itemColor, mekanismBlock.getItem());
        }
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        registerBlockColorHandler(event.getBlockColors(), event.getItemColors(), (state, worldIn, pos, tintIndex) -> {
                  Block block = state.getBlock();
                  if (block instanceof IColoredBlock) {
                      return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                  }
                  return -1;
              }, (stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (item instanceof BlockItem) {
                      Block block = ((BlockItem) item).getBlock();
                      if (block instanceof IColoredBlock) {
                          return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                      }
                  }
                  return -1;
              },
              //Fluid Tank
              MekanismBlock.BASIC_FLUID_TANK, MekanismBlock.ADVANCED_FLUID_TANK, MekanismBlock.ELITE_FLUID_TANK, MekanismBlock.ULTIMATE_FLUID_TANK,
              MekanismBlock.CREATIVE_FLUID_TANK);
    }
}