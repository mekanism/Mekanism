package mekanism.common.tile.base;

import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.TileEntityThermalEvaporationValve;
import mekanism.common.tile.factory.TileEntityCombiningFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;

public class MekanismTileEntityTypes {

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(Mekanism.MODID);

    //TODO: Tile types that need to be evaluated further
    public static final TileEntityTypeRegistryObject<TileEntityBoundingBlock> BOUNDING_BLOCK = TILE_ENTITY_TYPES.register(MekanismBlock.BOUNDING_BLOCK, TileEntityBoundingBlock::new);
    public static final TileEntityTypeRegistryObject<TileEntityBoundingBlock> ADVANCED_BOUNDING_BLOCK = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_BOUNDING_BLOCK, TileEntityAdvancedBoundingBlock::new);

    //Regular Tiles
    public static final TileEntityTypeRegistryObject<TileEntityBoilerCasing> BOILER_CASING = TILE_ENTITY_TYPES.register(MekanismBlock.BOILER_CASING, TileEntityBoilerCasing::new);
    public static final TileEntityTypeRegistryObject<TileEntityBoilerValve> BOILER_VALVE = TILE_ENTITY_TYPES.register(MekanismBlock.BOILER_VALVE, TileEntityBoilerValve::new);
    public static final TileEntityTypeRegistryObject<TileEntityCardboardBox> CARDBOARD_BOX = TILE_ENTITY_TYPES.register(MekanismBlock.CARDBOARD_BOX, TileEntityCardboardBox::new);
    public static final TileEntityTypeRegistryObject<TileEntityChargepad> CHARGEPAD = TILE_ENTITY_TYPES.register(MekanismBlock.CHARGEPAD, TileEntityChargepad::new);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = TILE_ENTITY_TYPES.register(MekanismBlock.CHEMICAL_CRYSTALLIZER, TileEntityChemicalCrystallizer::new);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, TileEntityChemicalDissolutionChamber::new);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalInfuser> CHEMICAL_INFUSER = TILE_ENTITY_TYPES.register(MekanismBlock.CHEMICAL_INFUSER, TileEntityChemicalInfuser::new);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlock.CHEMICAL_INJECTION_CHAMBER, TileEntityChemicalInjectionChamber::new);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = TILE_ENTITY_TYPES.register(MekanismBlock.CHEMICAL_OXIDIZER, TileEntityChemicalOxidizer::new);
    public static final TileEntityTypeRegistryObject<TileEntityChemicalWasher> CHEMICAL_WASHER = TILE_ENTITY_TYPES.register(MekanismBlock.CHEMICAL_WASHER, TileEntityChemicalWasher::new);
    public static final TileEntityTypeRegistryObject<TileEntityCombiner> COMBINER = TILE_ENTITY_TYPES.register(MekanismBlock.COMBINER, TileEntityCombiner::new);
    public static final TileEntityTypeRegistryObject<TileEntityCrusher> CRUSHER = TILE_ENTITY_TYPES.register(MekanismBlock.CRUSHER, TileEntityCrusher::new);
    public static final TileEntityTypeRegistryObject<TileEntityDigitalMiner> DIGITAL_MINER = TILE_ENTITY_TYPES.register(MekanismBlock.DIGITAL_MINER, TileEntityDigitalMiner::new);
    public static final TileEntityTypeRegistryObject<TileEntityDynamicTank> DYNAMIC_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.DYNAMIC_TANK, TileEntityDynamicTank::new);
    public static final TileEntityTypeRegistryObject<TileEntityDynamicValve> DYNAMIC_VALVE = TILE_ENTITY_TYPES.register(MekanismBlock.DYNAMIC_VALVE, TileEntityDynamicValve::new);
    public static final TileEntityTypeRegistryObject<TileEntityElectricPump> ELECTRIC_PUMP = TILE_ENTITY_TYPES.register(MekanismBlock.ELECTRIC_PUMP, TileEntityElectricPump::new);
    public static final TileEntityTypeRegistryObject<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = TILE_ENTITY_TYPES.register(MekanismBlock.ELECTROLYTIC_SEPARATOR, TileEntityElectrolyticSeparator::new);
    public static final TileEntityTypeRegistryObject<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = TILE_ENTITY_TYPES.register(MekanismBlock.ENERGIZED_SMELTER, TileEntityEnergizedSmelter::new);
    public static final TileEntityTypeRegistryObject<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlock.ENRICHMENT_CHAMBER, TileEntityEnrichmentChamber::new);
    public static final TileEntityTypeRegistryObject<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = TILE_ENTITY_TYPES.register(MekanismBlock.FLUIDIC_PLENISHER, TileEntityFluidicPlenisher::new);
    public static final TileEntityTypeRegistryObject<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = TILE_ENTITY_TYPES.register(MekanismBlock.FORMULAIC_ASSEMBLICATOR, TileEntityFormulaicAssemblicator::new);
    public static final TileEntityTypeRegistryObject<TileEntityFuelwoodHeater> FUELWOOD_HEATER = TILE_ENTITY_TYPES.register(MekanismBlock.FUELWOOD_HEATER, TileEntityFuelwoodHeater::new);
    public static final TileEntityTypeRegistryObject<TileEntityInductionCasing> INDUCTION_CASING = TILE_ENTITY_TYPES.register(MekanismBlock.INDUCTION_CASING, TileEntityInductionCasing::new);
    public static final TileEntityTypeRegistryObject<TileEntityInductionPort> INDUCTION_PORT = TILE_ENTITY_TYPES.register(MekanismBlock.INDUCTION_PORT, TileEntityInductionPort::new);
    public static final TileEntityTypeRegistryObject<TileEntityLaser> LASER = TILE_ENTITY_TYPES.register(MekanismBlock.LASER, TileEntityLaser::new);
    public static final TileEntityTypeRegistryObject<TileEntityLaserAmplifier> LASER_AMPLIFIER = TILE_ENTITY_TYPES.register(MekanismBlock.LASER_AMPLIFIER, TileEntityLaserAmplifier::new);
    public static final TileEntityTypeRegistryObject<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = TILE_ENTITY_TYPES.register(MekanismBlock.LASER_TRACTOR_BEAM, TileEntityLaserTractorBeam::new);
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalSorter> LOGISTICAL_SORTER = TILE_ENTITY_TYPES.register(MekanismBlock.LOGISTICAL_SORTER, TileEntityLogisticalSorter::new);
    public static final TileEntityTypeRegistryObject<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = TILE_ENTITY_TYPES.register(MekanismBlock.METALLURGIC_INFUSER, TileEntityMetallurgicInfuser::new);
    public static final TileEntityTypeRegistryObject<TileEntityOredictionificator> OREDICTIONIFICATOR = TILE_ENTITY_TYPES.register(MekanismBlock.OREDICTIONIFICATOR, TileEntityOredictionificator::new);
    public static final TileEntityTypeRegistryObject<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = TILE_ENTITY_TYPES.register(MekanismBlock.OSMIUM_COMPRESSOR, TileEntityOsmiumCompressor::new);
    public static final TileEntityTypeRegistryObject<TileEntityPersonalChest> PERSONAL_CHEST = TILE_ENTITY_TYPES.register(MekanismBlock.PERSONAL_CHEST, TileEntityPersonalChest::new);
    public static final TileEntityTypeRegistryObject<TileEntityPrecisionSawmill> PRECISION_SAWMILL = TILE_ENTITY_TYPES.register(MekanismBlock.PRECISION_SAWMILL, TileEntityPrecisionSawmill::new);
    public static final TileEntityTypeRegistryObject<TileEntityPressureDisperser> PRESSURE_DISPERSER = TILE_ENTITY_TYPES.register(MekanismBlock.PRESSURE_DISPERSER, TileEntityPressureDisperser::new);
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, TileEntityPressurizedReactionChamber::new);
    public static final TileEntityTypeRegistryObject<TileEntityPurificationChamber> PURIFICATION_CHAMBER = TILE_ENTITY_TYPES.register(MekanismBlock.PURIFICATION_CHAMBER, TileEntityPurificationChamber::new);
    public static final TileEntityTypeRegistryObject<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.QUANTUM_ENTANGLOPORTER, TileEntityQuantumEntangloporter::new);
    public static final TileEntityTypeRegistryObject<TileEntityResistiveHeater> RESISTIVE_HEATER = TILE_ENTITY_TYPES.register(MekanismBlock.RESISTIVE_HEATER, TileEntityResistiveHeater::new);
    public static final TileEntityTypeRegistryObject<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = TILE_ENTITY_TYPES.register(MekanismBlock.ROTARY_CONDENSENTRATOR, TileEntityRotaryCondensentrator::new);
    public static final TileEntityTypeRegistryObject<TileEntitySecurityDesk> SECURITY_DESK = TILE_ENTITY_TYPES.register(MekanismBlock.SECURITY_DESK, TileEntitySecurityDesk::new);
    public static final TileEntityTypeRegistryObject<TileEntitySeismicVibrator> SEISMIC_VIBRATOR = TILE_ENTITY_TYPES.register(MekanismBlock.SEISMIC_VIBRATOR, TileEntitySeismicVibrator::new);
    public static final TileEntityTypeRegistryObject<TileEntitySolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = TILE_ENTITY_TYPES.register(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, TileEntitySolarNeutronActivator::new);
    public static final TileEntityTypeRegistryObject<TileEntityStructuralGlass> STRUCTURAL_GLASS = TILE_ENTITY_TYPES.register(MekanismBlock.STRUCTURAL_GLASS, TileEntityStructuralGlass::new);
    public static final TileEntityTypeRegistryObject<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT = TILE_ENTITY_TYPES.register(MekanismBlock.SUPERHEATING_ELEMENT, TileEntitySuperheatingElement::new);
    public static final TileEntityTypeRegistryObject<TileEntityTeleporter> TELEPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.TELEPORTER, TileEntityTeleporter::new);
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = TILE_ENTITY_TYPES.register(MekanismBlock.THERMAL_EVAPORATION_BLOCK, TileEntityThermalEvaporationBlock::new);
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = TILE_ENTITY_TYPES.register(MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, TileEntityThermalEvaporationController::new);
    public static final TileEntityTypeRegistryObject<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = TILE_ENTITY_TYPES.register(MekanismBlock.THERMAL_EVAPORATION_VALVE, TileEntityThermalEvaporationValve::new);

    //Transmitters
    public static final TileEntityTypeRegistryObject<TileEntityDiversionTransporter> DIVERSION_TRANSPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.DIVERSION_TRANSPORTER, TileEntityDiversionTransporter::new);
    public static final TileEntityTypeRegistryObject<TileEntityRestrictiveTransporter> RESTRICTIVE_TRANSPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.RESTRICTIVE_TRANSPORTER, TileEntityRestrictiveTransporter::new);
    //Logistic Transporters
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER));
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER));
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER));
    public static final TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER));
    //Mechanical Pipes
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> BASIC_MECHANICAL_PIPE = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.BASIC_MECHANICAL_PIPE));
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ADVANCED_MECHANICAL_PIPE = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.ADVANCED_MECHANICAL_PIPE));
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ELITE_MECHANICAL_PIPE = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.ELITE_MECHANICAL_PIPE));
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalPipe> ULTIMATE_MECHANICAL_PIPE = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.ULTIMATE_MECHANICAL_PIPE));
    //Pressurized Tubes
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> BASIC_PRESSURIZED_TUBE = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.BASIC_PRESSURIZED_TUBE));
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ADVANCED_PRESSURIZED_TUBE = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.ADVANCED_PRESSURIZED_TUBE));
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ELITE_PRESSURIZED_TUBE = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.ELITE_PRESSURIZED_TUBE));
    public static final TileEntityTypeRegistryObject<TileEntityPressurizedTube> ULTIMATE_PRESSURIZED_TUBE = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.ULTIMATE_PRESSURIZED_TUBE));
    //Thermodynamic Conductors
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR));
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR));
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR));
    public static final TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR));
    //Universal Cables
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> BASIC_UNIVERSAL_CABLE = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.BASIC_UNIVERSAL_CABLE));
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ADVANCED_UNIVERSAL_CABLE = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.ADVANCED_UNIVERSAL_CABLE));
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ELITE_UNIVERSAL_CABLE = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.ELITE_UNIVERSAL_CABLE));
    public static final TileEntityTypeRegistryObject<TileEntityUniversalCable> ULTIMATE_UNIVERSAL_CABLE = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.ULTIMATE_UNIVERSAL_CABLE));

    //Tiered Tiles
    //Energy Cubes
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> BASIC_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.BASIC_ENERGY_CUBE));
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ADVANCED_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.ADVANCED_ENERGY_CUBE));
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ELITE_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.ELITE_ENERGY_CUBE));
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> ULTIMATE_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.ULTIMATE_ENERGY_CUBE));
    public static final TileEntityTypeRegistryObject<TileEntityEnergyCube> CREATIVE_ENERGY_CUBE = TILE_ENTITY_TYPES.register(MekanismBlock.CREATIVE_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.CREATIVE_ENERGY_CUBE));
    //Gas Tanks
    public static final TileEntityTypeRegistryObject<TileEntityGasTank> BASIC_GAS_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.BASIC_GAS_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityGasTank> ADVANCED_GAS_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.ADVANCED_GAS_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityGasTank> ELITE_GAS_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.ELITE_GAS_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityGasTank> ULTIMATE_GAS_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.ULTIMATE_GAS_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityGasTank> CREATIVE_GAS_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.CREATIVE_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.CREATIVE_GAS_TANK));
    //Fluid Tanks
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> BASIC_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.BASIC_FLUID_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ADVANCED_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.ADVANCED_FLUID_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ELITE_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.ELITE_FLUID_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> ULTIMATE_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.ULTIMATE_FLUID_TANK));
    public static final TileEntityTypeRegistryObject<TileEntityFluidTank> CREATIVE_FLUID_TANK = TILE_ENTITY_TYPES.register(MekanismBlock.CREATIVE_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.CREATIVE_FLUID_TANK));
    //Bins
    public static final TileEntityTypeRegistryObject<TileEntityBin> BASIC_BIN = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_BIN, () -> new TileEntityBin(MekanismBlock.BASIC_BIN));
    public static final TileEntityTypeRegistryObject<TileEntityBin> ADVANCED_BIN = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_BIN, () -> new TileEntityBin(MekanismBlock.ADVANCED_BIN));
    public static final TileEntityTypeRegistryObject<TileEntityBin> ELITE_BIN = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_BIN, () -> new TileEntityBin(MekanismBlock.ELITE_BIN));
    public static final TileEntityTypeRegistryObject<TileEntityBin> ULTIMATE_BIN = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_BIN, () -> new TileEntityBin(MekanismBlock.ULTIMATE_BIN));
    public static final TileEntityTypeRegistryObject<TileEntityBin> CREATIVE_BIN = TILE_ENTITY_TYPES.register(MekanismBlock.CREATIVE_BIN, () -> new TileEntityBin(MekanismBlock.CREATIVE_BIN));
    //Induction Cells
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> BASIC_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.BASIC_INDUCTION_CELL));
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ADVANCED_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.ADVANCED_INDUCTION_CELL));
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ELITE_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.ELITE_INDUCTION_CELL));
    public static final TileEntityTypeRegistryObject<TileEntityInductionCell> ULTIMATE_INDUCTION_CELL = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.ULTIMATE_INDUCTION_CELL));
    //Induction Providers
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> BASIC_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.BASIC_INDUCTION_PROVIDER));
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ADVANCED_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.ADVANCED_INDUCTION_PROVIDER));
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ELITE_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.ELITE_INDUCTION_PROVIDER));
    public static final TileEntityTypeRegistryObject<TileEntityInductionProvider> ULTIMATE_INDUCTION_PROVIDER = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.ULTIMATE_INDUCTION_PROVIDER));

    //Factories
    //Combining
    public static final TileEntityTypeRegistryObject<TileEntityCombiningFactory> BASIC_COMBINING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_COMBINING_FACTORY, () -> new TileEntityCombiningFactory(MekanismBlock.BASIC_COMBINING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityCombiningFactory> ADVANCED_COMBINING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_COMBINING_FACTORY, () -> new TileEntityCombiningFactory(MekanismBlock.ADVANCED_COMBINING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityCombiningFactory> ELITE_COMBINING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_COMBINING_FACTORY, () -> new TileEntityCombiningFactory(MekanismBlock.ELITE_COMBINING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityCombiningFactory> ULTIMATE_COMBINING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_COMBINING_FACTORY, () -> new TileEntityCombiningFactory(MekanismBlock.ULTIMATE_COMBINING_FACTORY));
    //Compressing
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> BASIC_COMPRESSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_COMPRESSING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.BASIC_COMPRESSING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ADVANCED_COMPRESSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_COMPRESSING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ADVANCED_COMPRESSING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ELITE_COMPRESSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_COMPRESSING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ELITE_COMPRESSING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ULTIMATE_COMPRESSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_COMPRESSING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ULTIMATE_COMPRESSING_FACTORY));
    //Crushing
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> BASIC_CRUSHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_CRUSHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.BASIC_CRUSHING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ADVANCED_CRUSHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_CRUSHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ADVANCED_CRUSHING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ELITE_CRUSHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_CRUSHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ELITE_CRUSHING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ULTIMATE_CRUSHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_CRUSHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ULTIMATE_CRUSHING_FACTORY));
    //Enriching
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> BASIC_ENRICHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_ENRICHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.BASIC_ENRICHING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ADVANCED_ENRICHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_ENRICHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ADVANCED_ENRICHING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ELITE_ENRICHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_ENRICHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ELITE_ENRICHING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ULTIMATE_ENRICHING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_ENRICHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ULTIMATE_ENRICHING_FACTORY));
    //Infusing
    public static final TileEntityTypeRegistryObject<TileEntityMetallurgicInfuserFactory> BASIC_INFUSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_INFUSING_FACTORY, () -> new TileEntityMetallurgicInfuserFactory(MekanismBlock.BASIC_INFUSING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityMetallurgicInfuserFactory> ADVANCED_INFUSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_INFUSING_FACTORY, () -> new TileEntityMetallurgicInfuserFactory(MekanismBlock.ADVANCED_INFUSING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityMetallurgicInfuserFactory> ELITE_INFUSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_INFUSING_FACTORY, () -> new TileEntityMetallurgicInfuserFactory(MekanismBlock.ELITE_INFUSING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityMetallurgicInfuserFactory> ULTIMATE_INFUSING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_INFUSING_FACTORY, () -> new TileEntityMetallurgicInfuserFactory(MekanismBlock.ULTIMATE_INFUSING_FACTORY));
    //Injecting
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> BASIC_INJECTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_INJECTING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.BASIC_INJECTING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ADVANCED_INJECTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_INJECTING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ADVANCED_INJECTING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ELITE_INJECTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_INJECTING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ELITE_INJECTING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ULTIMATE_INJECTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_INJECTING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ULTIMATE_INJECTING_FACTORY));
    //Purifying
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> BASIC_PURIFYING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_PURIFYING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.BASIC_PURIFYING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ADVANCED_PURIFYING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_PURIFYING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ADVANCED_PURIFYING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ELITE_PURIFYING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_PURIFYING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ELITE_PURIFYING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackGasToItemStackFactory> ULTIMATE_PURIFYING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_PURIFYING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ULTIMATE_PURIFYING_FACTORY));
    //Sawing
    public static final TileEntityTypeRegistryObject<TileEntitySawingFactory> BASIC_SAWING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_SAWING_FACTORY, () -> new TileEntitySawingFactory(MekanismBlock.BASIC_SAWING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntitySawingFactory> ADVANCED_SAWING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_SAWING_FACTORY, () -> new TileEntitySawingFactory(MekanismBlock.ADVANCED_SAWING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntitySawingFactory> ELITE_SAWING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_SAWING_FACTORY, () -> new TileEntitySawingFactory(MekanismBlock.ELITE_SAWING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntitySawingFactory> ULTIMATE_SAWING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_SAWING_FACTORY, () -> new TileEntitySawingFactory(MekanismBlock.ULTIMATE_SAWING_FACTORY));
    //Smelting
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> BASIC_SMELTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.BASIC_SMELTING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.BASIC_SMELTING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ADVANCED_SMELTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ADVANCED_SMELTING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ADVANCED_SMELTING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ELITE_SMELTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ELITE_SMELTING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ELITE_SMELTING_FACTORY));
    public static final TileEntityTypeRegistryObject<TileEntityItemStackToItemStackFactory> ULTIMATE_SMELTING_FACTORY = TILE_ENTITY_TYPES.register(MekanismBlock.ULTIMATE_SMELTING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ULTIMATE_SMELTING_FACTORY));
}