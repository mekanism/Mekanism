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
import mekanism.common.entity.MekanismEntityTypes;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.particle.MekanismParticleType;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.MekanismTileEntityTypes;
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
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        //Note: The JavaDocs of the below methods specifies to register this here rather than during the EntityType registration

        //Register entity rendering handlers
        registerEntityRenderingHandler(MekanismEntityTypes.ROBIT, RenderRobit::new);
        registerEntityRenderingHandler(MekanismEntityTypes.FLAME, RenderFlame::new);

        //Register TileEntityRenderers
        bindTileEntityRenderer(MekanismTileEntityTypes.BOILER_CASING, new RenderThermoelectricBoiler());
        bindTileEntityRenderer(MekanismTileEntityTypes.BOILER_VALVE, new RenderThermoelectricBoiler());
        bindTileEntityRenderer(MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, new RenderChemicalCrystallizer());
        bindTileEntityRenderer(MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER, new RenderChemicalDissolutionChamber());
        bindTileEntityRenderer(MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.COMBINER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.CRUSHER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.DIGITAL_MINER, new RenderDigitalMiner());
        bindTileEntityRenderer(MekanismTileEntityTypes.DYNAMIC_TANK, new RenderDynamicTank());
        bindTileEntityRenderer(MekanismTileEntityTypes.DYNAMIC_VALVE, new RenderDynamicTank());
        bindTileEntityRenderer(MekanismTileEntityTypes.ENERGIZED_SMELTER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ENRICHMENT_CHAMBER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.METALLURGIC_INFUSER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.OSMIUM_COMPRESSOR, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.PERSONAL_CHEST, new RenderPersonalChest());
        bindTileEntityRenderer(MekanismTileEntityTypes.PRECISION_SAWMILL, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.PURIFICATION_CHAMBER, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER, new RenderQuantumEntangloporter());
        bindTileEntityRenderer(MekanismTileEntityTypes.RESISTIVE_HEATER, new RenderResistiveHeater());
        bindTileEntityRenderer(MekanismTileEntityTypes.SECURITY_DESK, new RenderSecurityDesk());
        bindTileEntityRenderer(MekanismTileEntityTypes.SEISMIC_VIBRATOR, new RenderSeismicVibrator());
        bindTileEntityRenderer(MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR, new RenderSolarNeutronActivator());
        bindTileEntityRenderer(MekanismTileEntityTypes.TELEPORTER, new RenderTeleporter());
        bindTileEntityRenderer(MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER, new RenderThermalEvaporationController());
        //Bins
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_BIN, new RenderBin());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_BIN, new RenderBin());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_BIN, new RenderBin());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_BIN, new RenderBin());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_BIN, new RenderBin());
        //Gas tanks
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_GAS_TANK, new RenderGasTank());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_GAS_TANK, new RenderGasTank());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_GAS_TANK, new RenderGasTank());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_GAS_TANK, new RenderGasTank());
        bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_GAS_TANK, new RenderGasTank());
        //Energy Cubes
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_ENERGY_CUBE, new RenderEnergyCube());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE, new RenderEnergyCube());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_ENERGY_CUBE, new RenderEnergyCube());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE, new RenderEnergyCube());
        bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE, new RenderEnergyCube());
        //Fluid Tanks
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_FLUID_TANK, RenderFluidTank.INSTANCE);
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_FLUID_TANK, RenderFluidTank.INSTANCE);
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_FLUID_TANK, RenderFluidTank.INSTANCE);
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_FLUID_TANK, RenderFluidTank.INSTANCE);
        bindTileEntityRenderer(MekanismTileEntityTypes.CREATIVE_FLUID_TANK, RenderFluidTank.INSTANCE);
        //Factories
        //Combining
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_COMBINING_FACTORY, new RenderConfigurableMachine<>());
        //Compressing
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_COMPRESSING_FACTORY, new RenderConfigurableMachine<>());
        //Crushing
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_CRUSHING_FACTORY, new RenderConfigurableMachine<>());
        //Enriching
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_ENRICHING_FACTORY, new RenderConfigurableMachine<>());
        //Infusing
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_INFUSING_FACTORY, new RenderConfigurableMachine<>());
        //Injecting
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_INJECTING_FACTORY, new RenderConfigurableMachine<>());
        //Purifying
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_PURIFYING_FACTORY, new RenderConfigurableMachine<>());
        //Sawing
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_SAWING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_SAWING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_SAWING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_SAWING_FACTORY, new RenderConfigurableMachine<>());
        //Smelting
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_SMELTING_FACTORY, new RenderConfigurableMachine<>());
        //Transmitters
        //Logistical transporters
        bindTileEntityRenderer(MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER, new RenderLogisticalTransporter());
        bindTileEntityRenderer(MekanismTileEntityTypes.DIVERSION_TRANSPORTER, new RenderLogisticalTransporter());
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER, new RenderLogisticalTransporter());
        //Mechanical Pipes
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE, new RenderMechanicalPipe());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE, new RenderMechanicalPipe());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE, new RenderMechanicalPipe());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE, new RenderMechanicalPipe());
        //Pressurized Tubes
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE, new RenderPressurizedTube());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE, new RenderPressurizedTube());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE, new RenderPressurizedTube());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE, new RenderPressurizedTube());
        //Universal Cables
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE, new RenderUniversalCable());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE, new RenderUniversalCable());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE, new RenderUniversalCable());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE, new RenderUniversalCable());
        //Thermodynamic Conductors
        bindTileEntityRenderer(MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());
        bindTileEntityRenderer(MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());
        bindTileEntityRenderer(MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());
        bindTileEntityRenderer(MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR, new RenderThermodynamicConductor());

        //TODO: Set this for the various blocks
        //RenderTypeLookup.setRenderLayer()
    }

    private static <T extends Entity> void registerEntityRenderingHandler(EntityTypeRegistryObject<T> entityTypeRO, IRenderFactory<? super T> renderFactory) {
        RenderingRegistry.registerEntityRenderingHandler(entityTypeRO.getEntityType(), renderFactory);
    }

    private static <T extends TileEntity> void bindTileEntityRenderer(TileEntityTypeRegistryObject<T> tileTypeRO, TileEntityRenderer<? super T> specialRenderer) {
        ClientRegistry.bindTileEntityRenderer(tileTypeRO.getTileEntityType(), specialRenderer);
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
        Minecraft.getInstance().particles.registerFactory(MekanismParticleType.LASER.getParticleType(), ParticleLaser.Factory::new);
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