package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPersonalBarrel;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityCombiningFactory;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import mekanism.common.tile.laser.TileEntityLaser;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import mekanism.common.tile.machine.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import mekanism.common.tile.machine.TileEntityCombiner;
import mekanism.common.tile.machine.TileEntityCrusher;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.tile.machine.TileEntityElectricPump;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import mekanism.common.tile.machine.TileEntityEnergizedSmelter;
import mekanism.common.tile.machine.TileEntityEnrichmentChamber;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.machine.TileEntityFuelwoodHeater;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOsmiumCompressor;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.machine.TileEntityPurificationChamber;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityBoilerValve;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.tile.multiblock.TileEntityDynamicValve;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.tile.multiblock.TileEntityStructuralGlass;
import mekanism.common.tile.multiblock.TileEntitySuperchargedCoil;
import mekanism.common.tile.multiblock.TileEntitySuperheatingElement;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationValve;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.common.util.EnumUtils;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

public class MekanismTileEntityTypes {

    private MekanismTileEntityTypes() {
    }

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(Mekanism.MODID);

    private static final Table<FactoryTier, FactoryType, TileEntityTypeRegistryObject<? extends TileEntityFactory<?>>> FACTORIES = HashBasedTable.create();

    static {
        for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
            FACTORIES.put(tier, FactoryType.COMBINING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.COMBINING), (pos, state) -> new TileEntityCombiningFactory(MekanismBlocks.getFactory(tier, FactoryType.COMBINING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.COMPRESSING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.COMPRESSING), (pos, state) -> new TileEntityItemStackGasToItemStackFactory(MekanismBlocks.getFactory(tier, FactoryType.COMPRESSING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.CRUSHING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.CRUSHING), (pos, state) -> new TileEntityItemStackToItemStackFactory(MekanismBlocks.getFactory(tier, FactoryType.CRUSHING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.ENRICHING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.ENRICHING), (pos, state) -> new TileEntityItemStackToItemStackFactory(MekanismBlocks.getFactory(tier, FactoryType.ENRICHING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.INFUSING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.INFUSING), (pos, state) -> new TileEntityMetallurgicInfuserFactory(MekanismBlocks.getFactory(tier, FactoryType.INFUSING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.INJECTING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.INJECTING), (pos, state) -> new TileEntityItemStackGasToItemStackFactory(MekanismBlocks.getFactory(tier, FactoryType.INJECTING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.PURIFYING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.PURIFYING), (pos, state) -> new TileEntityItemStackGasToItemStackFactory(MekanismBlocks.getFactory(tier, FactoryType.PURIFYING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.SAWING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.SAWING), (pos, state) -> new TileEntitySawingFactory(MekanismBlocks.getFactory(tier, FactoryType.SAWING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
            FACTORIES.put(tier, FactoryType.SMELTING, TILE_ENTITY_TYPES.register(MekanismBlocks.getFactory(tier, FactoryType.SMELTING), (pos, state) -> new TileEntityItemStackToItemStackFactory(MekanismBlocks.getFactory(tier, FactoryType.SMELTING), pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient));
        }
    }

    public static final TileEntityTypeRegistryObject<TileEntityBoundingBlock> BOUNDING_BLOCK = TILE_ENTITY_TYPES.builder(MekanismBlocks.BOUNDING_BLOCK, TileEntityBoundingBlock::new).build();

    //Regular Tiles
    public static final TileEntityTypeRegistryObject<TileEntityBoilerCasing> BOILER_CASING = TILE_ENTITY_TYPES.register(MekanismBlocks.BOILER_CASING, TileEntityBoilerCasing::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityBoilerValve> BOILER_VALVE = TILE_ENTITY_TYPES.register(MekanismBlocks.BOILER_VALVE, TileEntityBoilerValve::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityCardboardBox> CARDBOARD_BOX = TILE_ENTITY_TYPES.builder(MekanismBlocks.CARDBOARD_BOX, TileEntityCardboardBox::new).build();
    public static final TileEntityTypeRegistryObject<TileEntityChargepad> CHARGEPAD = TILE_ENTITY_TYPES.register(MekanismBlocks.CHARGEPAD, TileEntityChargepad::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = TILE_ENTITY_TYPES.register(MekanismBlocks.CHEMICAL_CRYSTALLIZER, TileEntityChemicalCrystallizer::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, TileEntityChemicalDissolutionChamber::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalInfuser> CHEMICAL_INFUSER = TILE_ENTITY_TYPES.register(MekanismBlocks.CHEMICAL_INFUSER, TileEntityChemicalInfuser::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, TileEntityChemicalInjectionChamber::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = TILE_ENTITY_TYPES.register(MekanismBlocks.CHEMICAL_OXIDIZER, TileEntityChemicalOxidizer::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalWasher> CHEMICAL_WASHER = TILE_ENTITY_TYPES.register(MekanismBlocks.CHEMICAL_WASHER, TileEntityChemicalWasher::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityCombiner> COMBINER = TILE_ENTITY_TYPES.register(MekanismBlocks.COMBINER, TileEntityCombiner::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityCrusher> CRUSHER = TILE_ENTITY_TYPES.register(MekanismBlocks.CRUSHER, TileEntityCrusher::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityDigitalMiner> DIGITAL_MINER = TILE_ENTITY_TYPES.register(MekanismBlocks.DIGITAL_MINER, TileEntityDigitalMiner::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityDynamicTank> DYNAMIC_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.DYNAMIC_TANK, TileEntityDynamicTank::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityDynamicValve> DYNAMIC_VALVE = TILE_ENTITY_TYPES.register(MekanismBlocks.DYNAMIC_VALVE, TileEntityDynamicValve::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityElectricPump> ELECTRIC_PUMP = TILE_ENTITY_TYPES.register(MekanismBlocks.ELECTRIC_PUMP, TileEntityElectricPump::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = TILE_ENTITY_TYPES.register(MekanismBlocks.ELECTROLYTIC_SEPARATOR, TileEntityElectrolyticSeparator::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = TILE_ENTITY_TYPES.register(MekanismBlocks.ENERGIZED_SMELTER, TileEntityEnergizedSmelter::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlocks.ENRICHMENT_CHAMBER, TileEntityEnrichmentChamber::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = TILE_ENTITY_TYPES.register(MekanismBlocks.FLUIDIC_PLENISHER, TileEntityFluidicPlenisher::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = TILE_ENTITY_TYPES.register(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, TileEntityFormulaicAssemblicator::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityFuelwoodHeater> FUELWOOD_HEATER = TILE_ENTITY_TYPES.register(MekanismBlocks.FUELWOOD_HEATER, TileEntityFuelwoodHeater::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionCasing> INDUCTION_CASING = TILE_ENTITY_TYPES.register(MekanismBlocks.INDUCTION_CASING, TileEntityInductionCasing::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionPort> INDUCTION_PORT = TILE_ENTITY_TYPES.register(MekanismBlocks.INDUCTION_PORT, TileEntityInductionPort::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityLaser> LASER = TILE_ENTITY_TYPES.register(MekanismBlocks.LASER, TileEntityLaser::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityLaserAmplifier> LASER_AMPLIFIER = TILE_ENTITY_TYPES.register(MekanismBlocks.LASER_AMPLIFIER, TileEntityLaserAmplifier::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = TILE_ENTITY_TYPES.register(MekanismBlocks.LASER_TRACTOR_BEAM, TileEntityLaserTractorBeam::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalSorter> LOGISTICAL_SORTER = TILE_ENTITY_TYPES.register(MekanismBlocks.LOGISTICAL_SORTER, TileEntityLogisticalSorter::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = TILE_ENTITY_TYPES.register(MekanismBlocks.METALLURGIC_INFUSER, TileEntityMetallurgicInfuser::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityOredictionificator> OREDICTIONIFICATOR = TILE_ENTITY_TYPES.register(MekanismBlocks.OREDICTIONIFICATOR, TileEntityOredictionificator::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = TILE_ENTITY_TYPES.register(MekanismBlocks.OSMIUM_COMPRESSOR, TileEntityOsmiumCompressor::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPersonalBarrel> PERSONAL_BARREL = TILE_ENTITY_TYPES.register(MekanismBlocks.PERSONAL_BARREL, TileEntityPersonalBarrel::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPersonalChest> PERSONAL_CHEST = TILE_ENTITY_TYPES.register(MekanismBlocks.PERSONAL_CHEST, TileEntityPersonalChest::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPrecisionSawmill> PRECISION_SAWMILL = TILE_ENTITY_TYPES.register(MekanismBlocks.PRECISION_SAWMILL, TileEntityPrecisionSawmill::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPressureDisperser> PRESSURE_DISPERSER = TILE_ENTITY_TYPES.register(MekanismBlocks.PRESSURE_DISPERSER, TileEntityPressureDisperser::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, TileEntityPressurizedReactionChamber::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPurificationChamber> PURIFICATION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlocks.PURIFICATION_CHAMBER, TileEntityPurificationChamber::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = TILE_ENTITY_TYPES.register(MekanismBlocks.QUANTUM_ENTANGLOPORTER, TileEntityQuantumEntangloporter::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityResistiveHeater> RESISTIVE_HEATER = TILE_ENTITY_TYPES.register(MekanismBlocks.RESISTIVE_HEATER, TileEntityResistiveHeater::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityModificationStation> MODIFICATION_STATION = TILE_ENTITY_TYPES.register(MekanismBlocks.MODIFICATION_STATION, TileEntityModificationStation::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityIsotopicCentrifuge> ISOTOPIC_CENTRIFUGE = TILE_ENTITY_TYPES.register(MekanismBlocks.ISOTOPIC_CENTRIFUGE, TileEntityIsotopicCentrifuge::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityNutritionalLiquifier> NUTRITIONAL_LIQUIFIER = TILE_ENTITY_TYPES.register(MekanismBlocks.NUTRITIONAL_LIQUIFIER, TileEntityNutritionalLiquifier::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = TILE_ENTITY_TYPES.register(MekanismBlocks.ROTARY_CONDENSENTRATOR, TileEntityRotaryCondensentrator::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntitySecurityDesk> SECURITY_DESK = TILE_ENTITY_TYPES.register(MekanismBlocks.SECURITY_DESK, TileEntitySecurityDesk::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntitySeismicVibrator> SEISMIC_VIBRATOR = TILE_ENTITY_TYPES.register(MekanismBlocks.SEISMIC_VIBRATOR, TileEntitySeismicVibrator::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntitySolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = TILE_ENTITY_TYPES.register(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, TileEntitySolarNeutronActivator::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityStructuralGlass> STRUCTURAL_GLASS = TILE_ENTITY_TYPES.register(MekanismBlocks.STRUCTURAL_GLASS, TileEntityStructuralGlass::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT = TILE_ENTITY_TYPES.register(MekanismBlocks.SUPERHEATING_ELEMENT, TileEntitySuperheatingElement::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityTeleporter> TELEPORTER = TILE_ENTITY_TYPES.register(MekanismBlocks.TELEPORTER, TileEntityTeleporter::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = TILE_ENTITY_TYPES.register(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, TileEntityThermalEvaporationBlock::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = TILE_ENTITY_TYPES.register(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, TileEntityThermalEvaporationController::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = TILE_ENTITY_TYPES.register(MekanismBlocks.THERMAL_EVAPORATION_VALVE, TileEntityThermalEvaporationValve::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityRadioactiveWasteBarrel> RADIOACTIVE_WASTE_BARREL = TILE_ENTITY_TYPES.register(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, TileEntityRadioactiveWasteBarrel::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityIndustrialAlarm> INDUSTRIAL_ALARM = TILE_ENTITY_TYPES.register(MekanismBlocks.INDUSTRIAL_ALARM, TileEntityIndustrialAlarm::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityAntiprotonicNucleosynthesizer> ANTIPROTONIC_NUCLEOSYNTHESIZER = TILE_ENTITY_TYPES.register(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, TileEntityAntiprotonicNucleosynthesizer::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPigmentExtractor> PIGMENT_EXTRACTOR = TILE_ENTITY_TYPES.register(MekanismBlocks.PIGMENT_EXTRACTOR, TileEntityPigmentExtractor::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPigmentMixer> PIGMENT_MIXER = TILE_ENTITY_TYPES.register(MekanismBlocks.PIGMENT_MIXER, TileEntityPigmentMixer::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityPaintingMachine> PAINTING_MACHINE = TILE_ENTITY_TYPES.register(MekanismBlocks.PAINTING_MACHINE, TileEntityPaintingMachine::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntitySPSCasing> SPS_CASING = TILE_ENTITY_TYPES.register(MekanismBlocks.SPS_CASING, TileEntitySPSCasing::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntitySPSPort> SPS_PORT = TILE_ENTITY_TYPES.register(MekanismBlocks.SPS_PORT, TileEntitySPSPort::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntitySuperchargedCoil> SUPERCHARGED_COIL = TILE_ENTITY_TYPES.register(MekanismBlocks.SUPERCHARGED_COIL, TileEntitySuperchargedCoil::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityDimensionalStabilizer> DIMENSIONAL_STABILIZER = TILE_ENTITY_TYPES.register(MekanismBlocks.DIMENSIONAL_STABILIZER, TileEntityDimensionalStabilizer::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);

    public static final TileEntityTypeRegistryObject<TileEntityQIODriveArray> QIO_DRIVE_ARRAY = TILE_ENTITY_TYPES.register(MekanismBlocks.QIO_DRIVE_ARRAY, TileEntityQIODriveArray::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityQIODashboard> QIO_DASHBOARD = TILE_ENTITY_TYPES.register(MekanismBlocks.QIO_DASHBOARD, TileEntityQIODashboard::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityQIOImporter> QIO_IMPORTER = TILE_ENTITY_TYPES.register(MekanismBlocks.QIO_IMPORTER, TileEntityQIOImporter::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityQIOExporter> QIO_EXPORTER = TILE_ENTITY_TYPES.register(MekanismBlocks.QIO_EXPORTER, TileEntityQIOExporter::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityQIORedstoneAdapter> QIO_REDSTONE_ADAPTER = TILE_ENTITY_TYPES.register(MekanismBlocks.QIO_REDSTONE_ADAPTER, TileEntityQIORedstoneAdapter::new, TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);

    //Transmitters
    public static final TileEntityTypeRegistryObject<TileEntityDiversionTransporter> DIVERSION_TRANSPORTER = TILE_ENTITY_TYPES.builder(MekanismBlocks.DIVERSION_TRANSPORTER, TileEntityDiversionTransporter::new).clientTicker(TileEntityLogisticalTransporterBase::tickClient).serverTicker(TileEntityTransmitter::tickServer).build();
    public static final TileEntityTypeRegistryObject<TileEntityRestrictiveTransporter> RESTRICTIVE_TRANSPORTER = TILE_ENTITY_TYPES.builder(MekanismBlocks.RESTRICTIVE_TRANSPORTER, TileEntityRestrictiveTransporter::new).clientTicker(TileEntityLogisticalTransporterBase::tickClient).serverTicker(TileEntityTransmitter::tickServer).build();
    //Logistic Transporters
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.builder(MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, (pos, state) -> new TileEntityLogisticalTransporter(MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, pos, state)).clientTicker(TileEntityLogisticalTransporterBase::tickClient).serverTicker(TileEntityTransmitter::tickServer).build();
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.builder(MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, (pos, state) -> new TileEntityLogisticalTransporter(MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, pos, state)).clientTicker(TileEntityLogisticalTransporterBase::tickClient).serverTicker(TileEntityTransmitter::tickServer).build();
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.builder(MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, (pos, state) -> new TileEntityLogisticalTransporter(MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, pos, state)).clientTicker(TileEntityLogisticalTransporterBase::tickClient).serverTicker(TileEntityTransmitter::tickServer).build();
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.builder(MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, (pos, state) -> new TileEntityLogisticalTransporter(MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, pos, state)).clientTicker(TileEntityLogisticalTransporterBase::tickClient).serverTicker(TileEntityTransmitter::tickServer).build();
    //Mechanical Pipes
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> BASIC_MECHANICAL_PIPE = registerTransmitter(MekanismBlocks.BASIC_MECHANICAL_PIPE, (pos, state) -> new TileEntityMechanicalPipe(MekanismBlocks.BASIC_MECHANICAL_PIPE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ADVANCED_MECHANICAL_PIPE = registerTransmitter(MekanismBlocks.ADVANCED_MECHANICAL_PIPE, (pos, state) -> new TileEntityMechanicalPipe(MekanismBlocks.ADVANCED_MECHANICAL_PIPE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ELITE_MECHANICAL_PIPE = registerTransmitter(MekanismBlocks.ELITE_MECHANICAL_PIPE, (pos, state) -> new TileEntityMechanicalPipe(MekanismBlocks.ELITE_MECHANICAL_PIPE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ULTIMATE_MECHANICAL_PIPE = registerTransmitter(MekanismBlocks.ULTIMATE_MECHANICAL_PIPE, (pos, state) -> new TileEntityMechanicalPipe(MekanismBlocks.ULTIMATE_MECHANICAL_PIPE, pos, state), TileEntityTransmitter::tickServer);
    //Pressurized Tubes
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> BASIC_PRESSURIZED_TUBE = registerTransmitter(MekanismBlocks.BASIC_PRESSURIZED_TUBE, (pos, state) -> new TileEntityPressurizedTube(MekanismBlocks.BASIC_PRESSURIZED_TUBE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ADVANCED_PRESSURIZED_TUBE = registerTransmitter(MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, (pos, state) -> new TileEntityPressurizedTube(MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ELITE_PRESSURIZED_TUBE = registerTransmitter(MekanismBlocks.ELITE_PRESSURIZED_TUBE, (pos, state) -> new TileEntityPressurizedTube(MekanismBlocks.ELITE_PRESSURIZED_TUBE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ULTIMATE_PRESSURIZED_TUBE = registerTransmitter(MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, (pos, state) -> new TileEntityPressurizedTube(MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, pos, state), TileEntityTransmitter::tickServer);
    //Thermodynamic Conductors
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR = registerTransmitter(MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, (pos, state) -> new TileEntityThermodynamicConductor(MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR = registerTransmitter(MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, (pos, state) -> new TileEntityThermodynamicConductor(MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR = registerTransmitter(MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, (pos, state) -> new TileEntityThermodynamicConductor(MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR = registerTransmitter(MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR, (pos, state) -> new TileEntityThermodynamicConductor(MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR, pos, state), TileEntityTransmitter::tickServer);
    //Universal Cables
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> BASIC_UNIVERSAL_CABLE = registerTransmitter(MekanismBlocks.BASIC_UNIVERSAL_CABLE, (pos, state) -> new TileEntityUniversalCable(MekanismBlocks.BASIC_UNIVERSAL_CABLE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ADVANCED_UNIVERSAL_CABLE = registerTransmitter(MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, (pos, state) -> new TileEntityUniversalCable(MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ELITE_UNIVERSAL_CABLE = registerTransmitter(MekanismBlocks.ELITE_UNIVERSAL_CABLE, (pos, state) -> new TileEntityUniversalCable(MekanismBlocks.ELITE_UNIVERSAL_CABLE, pos, state), TileEntityTransmitter::tickServer);
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ULTIMATE_UNIVERSAL_CABLE = registerTransmitter(MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, (pos, state) -> new TileEntityUniversalCable(MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, pos, state), TileEntityTransmitter::tickServer);

    //Tiered Tiles
    //Energy Cubes
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> BASIC_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlocks.BASIC_ENERGY_CUBE, (pos, state) -> new TileEntityEnergyCube(MekanismBlocks.BASIC_ENERGY_CUBE, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ADVANCED_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlocks.ADVANCED_ENERGY_CUBE, (pos, state) -> new TileEntityEnergyCube(MekanismBlocks.ADVANCED_ENERGY_CUBE, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ELITE_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlocks.ELITE_ENERGY_CUBE, (pos, state) -> new TileEntityEnergyCube(MekanismBlocks.ELITE_ENERGY_CUBE, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ULTIMATE_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlocks.ULTIMATE_ENERGY_CUBE, (pos, state) -> new TileEntityEnergyCube(MekanismBlocks.ULTIMATE_ENERGY_CUBE, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> CREATIVE_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlocks.CREATIVE_ENERGY_CUBE, (pos, state) -> new TileEntityEnergyCube(MekanismBlocks.CREATIVE_ENERGY_CUBE, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    //Chemical Tanks
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> BASIC_CHEMICAL_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.BASIC_CHEMICAL_TANK, (pos, state) -> new TileEntityChemicalTank(MekanismBlocks.BASIC_CHEMICAL_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> ADVANCED_CHEMICAL_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.ADVANCED_CHEMICAL_TANK, (pos, state) -> new TileEntityChemicalTank(MekanismBlocks.ADVANCED_CHEMICAL_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> ELITE_CHEMICAL_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.ELITE_CHEMICAL_TANK, (pos, state) -> new TileEntityChemicalTank(MekanismBlocks.ELITE_CHEMICAL_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> ULTIMATE_CHEMICAL_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.ULTIMATE_CHEMICAL_TANK, (pos, state) -> new TileEntityChemicalTank(MekanismBlocks.ULTIMATE_CHEMICAL_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalTank> CREATIVE_CHEMICAL_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.CREATIVE_CHEMICAL_TANK, (pos, state) -> new TileEntityChemicalTank(MekanismBlocks.CREATIVE_CHEMICAL_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    //Fluid Tanks
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> BASIC_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.BASIC_FLUID_TANK, (pos, state) -> new TileEntityFluidTank(MekanismBlocks.BASIC_FLUID_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ADVANCED_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.ADVANCED_FLUID_TANK, (pos, state) -> new TileEntityFluidTank(MekanismBlocks.ADVANCED_FLUID_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ELITE_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.ELITE_FLUID_TANK, (pos, state) -> new TileEntityFluidTank(MekanismBlocks.ELITE_FLUID_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ULTIMATE_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.ULTIMATE_FLUID_TANK, (pos, state) -> new TileEntityFluidTank(MekanismBlocks.ULTIMATE_FLUID_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> CREATIVE_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlocks.CREATIVE_FLUID_TANK, (pos, state) -> new TileEntityFluidTank(MekanismBlocks.CREATIVE_FLUID_TANK, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    //Bins
    public static final TileEntityTypeRegistryObject<TileEntityBin> BASIC_BIN = TILE_ENTITY_TYPES.register(MekanismBlocks.BASIC_BIN, (pos, state) -> new TileEntityBin(MekanismBlocks.BASIC_BIN, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityBin> ADVANCED_BIN = TILE_ENTITY_TYPES.register(MekanismBlocks.ADVANCED_BIN, (pos, state) -> new TileEntityBin(MekanismBlocks.ADVANCED_BIN, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityBin> ELITE_BIN = TILE_ENTITY_TYPES.register(MekanismBlocks.ELITE_BIN, (pos, state) -> new TileEntityBin(MekanismBlocks.ELITE_BIN, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityBin> ULTIMATE_BIN = TILE_ENTITY_TYPES.register(MekanismBlocks.ULTIMATE_BIN, (pos, state) -> new TileEntityBin(MekanismBlocks.ULTIMATE_BIN, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityBin> CREATIVE_BIN = TILE_ENTITY_TYPES.register(MekanismBlocks.CREATIVE_BIN, (pos, state) -> new TileEntityBin(MekanismBlocks.CREATIVE_BIN, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    //Induction Cells
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> BASIC_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlocks.BASIC_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.BASIC_INDUCTION_CELL, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ADVANCED_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlocks.ADVANCED_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.ADVANCED_INDUCTION_CELL, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ELITE_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlocks.ELITE_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.ELITE_INDUCTION_CELL, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ULTIMATE_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlocks.ULTIMATE_INDUCTION_CELL, (pos, state) -> new TileEntityInductionCell(MekanismBlocks.ULTIMATE_INDUCTION_CELL, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    //Induction Providers
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> BASIC_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlocks.BASIC_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.BASIC_INDUCTION_PROVIDER, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ADVANCED_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ELITE_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlocks.ELITE_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.ELITE_INDUCTION_PROVIDER, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ULTIMATE_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, (pos, state) -> new TileEntityInductionProvider(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, pos, state), TileEntityMekanism::tickServer, TileEntityMekanism::tickClient);

    public static TileEntityTypeRegistryObject<? extends TileEntityFactory<?>> getFactoryTile(FactoryTier tier, FactoryType type) {
        return FACTORIES.get(tier, type);
    }

    @SuppressWarnings("unchecked")
    public static TileEntityTypeRegistryObject<? extends TileEntityFactory<?>>[] getFactoryTiles() {
        return FACTORIES.values().toArray(new TileEntityTypeRegistryObject[0]);
    }

    private static <BE extends TileEntityTransmitter> TileEntityTypeRegistryObject<BE> registerTransmitter(BlockRegistryObject<?, ?> block,
          BlockEntitySupplier<? extends BE> factory, BlockEntityTicker<BE> serverTicker) {
        //Note: There is no data fixer type as forge does not currently have a way exposing data fixers to mods yet
        return TILE_ENTITY_TYPES.<BE>builder(block, factory).serverTicker(serverTicker).build();
    }
}