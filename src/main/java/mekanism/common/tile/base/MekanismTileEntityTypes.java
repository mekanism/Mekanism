package mekanism.common.tile.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.MekanismBlock;
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
import mekanism.common.tile.factory.TileEntityFactory;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Evaluate various subclasses as potentially they can be removed if all can be done via constructor
public class MekanismTileEntityTypes {

    private static final List<TileEntityType<?>> types = new ArrayList<>();

    //TODO: Tile types that need to be evaluated further
    public static final TileEntityType<TileEntityBoundingBlock> BOUNDING_BLOCK = create(MekanismBlock.BOUNDING_BLOCK, TileEntityBoundingBlock::new);
    public static final TileEntityType<TileEntityBoundingBlock> ADVANCED_BOUNDING_BLOCK = create(MekanismBlock.ADVANCED_BOUNDING_BLOCK, TileEntityAdvancedBoundingBlock::new);

    //Regular Tiles
    public static final TileEntityType<TileEntityBoilerCasing> BOILER_CASING = create(MekanismBlock.BOILER_CASING, TileEntityBoilerCasing::new);
    public static final TileEntityType<TileEntityBoilerValve> BOILER_VALVE = create(MekanismBlock.BOILER_VALVE, TileEntityBoilerValve::new);
    public static final TileEntityType<TileEntityCardboardBox> CARDBOARD_BOX = create(MekanismBlock.CARDBOARD_BOX, TileEntityCardboardBox::new);
    public static final TileEntityType<TileEntityChargepad> CHARGEPAD = create(MekanismBlock.CHARGEPAD, TileEntityChargepad::new);
    public static final TileEntityType<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = create(MekanismBlock.CHEMICAL_CRYSTALLIZER, TileEntityChemicalCrystallizer::new);
    public static final TileEntityType<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = create(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, TileEntityChemicalDissolutionChamber::new);
    public static final TileEntityType<TileEntityChemicalInfuser> CHEMICAL_INFUSER = create(MekanismBlock.CHEMICAL_INFUSER, TileEntityChemicalInfuser::new);
    public static final TileEntityType<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = create(MekanismBlock.CHEMICAL_INJECTION_CHAMBER, TileEntityChemicalInjectionChamber::new);
    public static final TileEntityType<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = create(MekanismBlock.CHEMICAL_OXIDIZER, TileEntityChemicalOxidizer::new);
    public static final TileEntityType<TileEntityChemicalWasher> CHEMICAL_WASHER = create(MekanismBlock.CHEMICAL_WASHER, TileEntityChemicalWasher::new);
    public static final TileEntityType<TileEntityCombiner> COMBINER = create(MekanismBlock.COMBINER, TileEntityCombiner::new);
    public static final TileEntityType<TileEntityCrusher> CRUSHER = create(MekanismBlock.CRUSHER, TileEntityCrusher::new);
    public static final TileEntityType<TileEntityDigitalMiner> DIGITAL_MINER = create(MekanismBlock.DIGITAL_MINER, TileEntityDigitalMiner::new);
    public static final TileEntityType<TileEntityDynamicTank> DYNAMIC_TANK = create(MekanismBlock.DYNAMIC_TANK, TileEntityDynamicTank::new);
    public static final TileEntityType<TileEntityDynamicValve> DYNAMIC_VALVE = create(MekanismBlock.DYNAMIC_VALVE, TileEntityDynamicValve::new);
    public static final TileEntityType<TileEntityElectricPump> ELECTRIC_PUMP = create(MekanismBlock.ELECTRIC_PUMP, TileEntityElectricPump::new);
    public static final TileEntityType<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = create(MekanismBlock.ELECTROLYTIC_SEPARATOR, TileEntityElectrolyticSeparator::new);
    public static final TileEntityType<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = create(MekanismBlock.ENERGIZED_SMELTER, TileEntityEnergizedSmelter::new);
    public static final TileEntityType<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = create(MekanismBlock.ENRICHMENT_CHAMBER, TileEntityEnrichmentChamber::new);
    public static final TileEntityType<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = create(MekanismBlock.FLUIDIC_PLENISHER, TileEntityFluidicPlenisher::new);
    public static final TileEntityType<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = create(MekanismBlock.FORMULAIC_ASSEMBLICATOR, TileEntityFormulaicAssemblicator::new);
    public static final TileEntityType<TileEntityFuelwoodHeater> FUELWOOD_HEATER = create(MekanismBlock.FUELWOOD_HEATER, TileEntityFuelwoodHeater::new);
    public static final TileEntityType<TileEntityInductionCasing> INDUCTION_CASING = create(MekanismBlock.INDUCTION_CASING, TileEntityInductionCasing::new);
    public static final TileEntityType<TileEntityInductionPort> INDUCTION_PORT = create(MekanismBlock.INDUCTION_PORT, TileEntityInductionPort::new);
    public static final TileEntityType<TileEntityLaser> LASER = create(MekanismBlock.LASER, TileEntityLaser::new);
    public static final TileEntityType<TileEntityLaserAmplifier> LASER_AMPLIFIER = create(MekanismBlock.LASER_AMPLIFIER, TileEntityLaserAmplifier::new);
    public static final TileEntityType<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = create(MekanismBlock.LASER_TRACTOR_BEAM, TileEntityLaserTractorBeam::new);
    public static final TileEntityType<TileEntityLogisticalSorter> LOGISTICAL_SORTER = create(MekanismBlock.LOGISTICAL_SORTER, TileEntityLogisticalSorter::new);
    public static final TileEntityType<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = create(MekanismBlock.METALLURGIC_INFUSER, TileEntityMetallurgicInfuser::new);
    public static final TileEntityType<TileEntityOredictionificator> OREDICTIONIFICATOR = create(MekanismBlock.OREDICTIONIFICATOR, TileEntityOredictionificator::new);
    public static final TileEntityType<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = create(MekanismBlock.OSMIUM_COMPRESSOR, TileEntityOsmiumCompressor::new);
    public static final TileEntityType<TileEntityPersonalChest> PERSONAL_CHEST = create(MekanismBlock.PERSONAL_CHEST, TileEntityPersonalChest::new);
    public static final TileEntityType<TileEntityPrecisionSawmill> PRECISION_SAWMILL = create(MekanismBlock.PRECISION_SAWMILL, TileEntityPrecisionSawmill::new);
    public static final TileEntityType<TileEntityPressureDisperser> PRESSURE_DISPERSER = create(MekanismBlock.PRESSURE_DISPERSER, TileEntityPressureDisperser::new);
    public static final TileEntityType<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = create(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, TileEntityPressurizedReactionChamber::new);
    public static final TileEntityType<TileEntityPurificationChamber> PURIFICATION_CHAMBER = create(MekanismBlock.PURIFICATION_CHAMBER, TileEntityPurificationChamber::new);
    public static final TileEntityType<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = create(MekanismBlock.QUANTUM_ENTANGLOPORTER, TileEntityQuantumEntangloporter::new);
    public static final TileEntityType<TileEntityResistiveHeater> RESISTIVE_HEATER = create(MekanismBlock.RESISTIVE_HEATER, TileEntityResistiveHeater::new);
    public static final TileEntityType<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = create(MekanismBlock.ROTARY_CONDENSENTRATOR, TileEntityRotaryCondensentrator::new);
    public static final TileEntityType<TileEntitySecurityDesk> SECURITY_DESK = create(MekanismBlock.SECURITY_DESK, TileEntitySecurityDesk::new);
    public static final TileEntityType<TileEntitySeismicVibrator> SEISMIC_VIBRATOR = create(MekanismBlock.SEISMIC_VIBRATOR, TileEntitySeismicVibrator::new);
    public static final TileEntityType<TileEntitySolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = create(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, TileEntitySolarNeutronActivator::new);
    public static final TileEntityType<TileEntityStructuralGlass> STRUCTURAL_GLASS = create(MekanismBlock.STRUCTURAL_GLASS, TileEntityStructuralGlass::new);
    public static final TileEntityType<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT = create(MekanismBlock.SUPERHEATING_ELEMENT, TileEntitySuperheatingElement::new);
    public static final TileEntityType<TileEntityTeleporter> TELEPORTER = create(MekanismBlock.TELEPORTER, TileEntityTeleporter::new);
    public static final TileEntityType<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = create(MekanismBlock.THERMAL_EVAPORATION_BLOCK, TileEntityThermalEvaporationBlock::new);
    public static final TileEntityType<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = create(MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, TileEntityThermalEvaporationController::new);
    public static final TileEntityType<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = create(MekanismBlock.THERMAL_EVAPORATION_VALVE, TileEntityThermalEvaporationValve::new);

    //Transmitters
    public static final TileEntityType<TileEntityDiversionTransporter> DIVERSION_TRANSPORTER = create(MekanismBlock.DIVERSION_TRANSPORTER, TileEntityDiversionTransporter::new);
    public static final TileEntityType<TileEntityRestrictiveTransporter> RESTRICTIVE_TRANSPORTER = create(MekanismBlock.RESTRICTIVE_TRANSPORTER, TileEntityRestrictiveTransporter::new);
    //Logistic Transporters
    public static final TileEntityType<TileEntityLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER = create(MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER));
    public static final TileEntityType<TileEntityLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER = create(MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER));
    public static final TileEntityType<TileEntityLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER = create(MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER));
    public static final TileEntityType<TileEntityLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER = create(MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER, () -> new TileEntityLogisticalTransporter(MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER));
    //Mechanical Pipes
    public static final TileEntityType<TileEntityMechanicalPipe> BASIC_MECHANICAL_PIPE = create(MekanismBlock.BASIC_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.BASIC_MECHANICAL_PIPE));
    public static final TileEntityType<TileEntityMechanicalPipe> ADVANCED_MECHANICAL_PIPE = create(MekanismBlock.ADVANCED_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.ADVANCED_MECHANICAL_PIPE));
    public static final TileEntityType<TileEntityMechanicalPipe> ELITE_MECHANICAL_PIPE = create(MekanismBlock.ELITE_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.ELITE_MECHANICAL_PIPE));
    public static final TileEntityType<TileEntityMechanicalPipe> ULTIMATE_MECHANICAL_PIPE = create(MekanismBlock.ULTIMATE_MECHANICAL_PIPE, () -> new TileEntityMechanicalPipe(MekanismBlock.ULTIMATE_MECHANICAL_PIPE));
    //Pressurized Tubes
    public static final TileEntityType<TileEntityPressurizedTube> BASIC_PRESSURIZED_TUBE = create(MekanismBlock.BASIC_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.BASIC_PRESSURIZED_TUBE));
    public static final TileEntityType<TileEntityPressurizedTube> ADVANCED_PRESSURIZED_TUBE = create(MekanismBlock.ADVANCED_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.ADVANCED_PRESSURIZED_TUBE));
    public static final TileEntityType<TileEntityPressurizedTube> ELITE_PRESSURIZED_TUBE = create(MekanismBlock.ELITE_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.ELITE_PRESSURIZED_TUBE));
    public static final TileEntityType<TileEntityPressurizedTube> ULTIMATE_PRESSURIZED_TUBE = create(MekanismBlock.ULTIMATE_PRESSURIZED_TUBE, () -> new TileEntityPressurizedTube(MekanismBlock.ULTIMATE_PRESSURIZED_TUBE));
    //Thermodynamic Conductors
    public static final TileEntityType<TileEntityThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR = create(MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR));
    public static final TileEntityType<TileEntityThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR = create(MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR));
    public static final TileEntityType<TileEntityThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR = create(MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR));
    public static final TileEntityType<TileEntityThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR = create(MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR, () -> new TileEntityThermodynamicConductor(MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR));
    //Universal Cables
    public static final TileEntityType<TileEntityUniversalCable> BASIC_UNIVERSAL_CABLE = create(MekanismBlock.BASIC_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.BASIC_UNIVERSAL_CABLE));
    public static final TileEntityType<TileEntityUniversalCable> ADVANCED_UNIVERSAL_CABLE = create(MekanismBlock.ADVANCED_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.ADVANCED_UNIVERSAL_CABLE));
    public static final TileEntityType<TileEntityUniversalCable> ELITE_UNIVERSAL_CABLE = create(MekanismBlock.ELITE_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.ELITE_UNIVERSAL_CABLE));
    public static final TileEntityType<TileEntityUniversalCable> ULTIMATE_UNIVERSAL_CABLE = create(MekanismBlock.ULTIMATE_UNIVERSAL_CABLE, () -> new TileEntityUniversalCable(MekanismBlock.ULTIMATE_UNIVERSAL_CABLE));

    //Tiered Tiles
    //Energy Cubes
    public static final TileEntityType<TileEntityEnergyCube> BASIC_ENERGY_CUBE = create(MekanismBlock.BASIC_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.BASIC_ENERGY_CUBE));
    public static final TileEntityType<TileEntityEnergyCube> ADVANCED_ENERGY_CUBE = create(MekanismBlock.ADVANCED_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.ADVANCED_ENERGY_CUBE));
    public static final TileEntityType<TileEntityEnergyCube> ELITE_ENERGY_CUBE = create(MekanismBlock.ELITE_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.ELITE_ENERGY_CUBE));
    public static final TileEntityType<TileEntityEnergyCube> ULTIMATE_ENERGY_CUBE = create(MekanismBlock.ULTIMATE_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.ULTIMATE_ENERGY_CUBE));
    public static final TileEntityType<TileEntityEnergyCube> CREATIVE_ENERGY_CUBE = create(MekanismBlock.CREATIVE_ENERGY_CUBE, () -> new TileEntityEnergyCube(MekanismBlock.CREATIVE_ENERGY_CUBE));
    //Gas Tanks
    public static final TileEntityType<TileEntityGasTank> BASIC_GAS_TANK = create(MekanismBlock.BASIC_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.BASIC_GAS_TANK));
    public static final TileEntityType<TileEntityGasTank> ADVANCED_GAS_TANK = create(MekanismBlock.ADVANCED_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.ADVANCED_GAS_TANK));
    public static final TileEntityType<TileEntityGasTank> ELITE_GAS_TANK = create(MekanismBlock.ELITE_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.ELITE_GAS_TANK));
    public static final TileEntityType<TileEntityGasTank> ULTIMATE_GAS_TANK = create(MekanismBlock.ULTIMATE_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.ULTIMATE_GAS_TANK));
    public static final TileEntityType<TileEntityGasTank> CREATIVE_GAS_TANK = create(MekanismBlock.CREATIVE_GAS_TANK, () -> new TileEntityGasTank(MekanismBlock.CREATIVE_GAS_TANK));
    //Fluid Tanks
    public static final TileEntityType<TileEntityFluidTank> BASIC_FLUID_TANK = create(MekanismBlock.BASIC_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.BASIC_FLUID_TANK));
    public static final TileEntityType<TileEntityFluidTank> ADVANCED_FLUID_TANK = create(MekanismBlock.ADVANCED_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.ADVANCED_FLUID_TANK));
    public static final TileEntityType<TileEntityFluidTank> ELITE_FLUID_TANK = create(MekanismBlock.ELITE_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.ELITE_FLUID_TANK));
    public static final TileEntityType<TileEntityFluidTank> ULTIMATE_FLUID_TANK = create(MekanismBlock.ULTIMATE_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.ULTIMATE_FLUID_TANK));
    public static final TileEntityType<TileEntityFluidTank> CREATIVE_FLUID_TANK = create(MekanismBlock.CREATIVE_FLUID_TANK, () -> new TileEntityFluidTank(MekanismBlock.CREATIVE_FLUID_TANK));
    //Bins
    public static final TileEntityType<TileEntityBin> BASIC_BIN = create(MekanismBlock.BASIC_BIN, () -> new TileEntityBin(MekanismBlock.BASIC_BIN));
    public static final TileEntityType<TileEntityBin> ADVANCED_BIN = create(MekanismBlock.ADVANCED_BIN, () -> new TileEntityBin(MekanismBlock.ADVANCED_BIN));
    public static final TileEntityType<TileEntityBin> ELITE_BIN = create(MekanismBlock.ELITE_BIN, () -> new TileEntityBin(MekanismBlock.ELITE_BIN));
    public static final TileEntityType<TileEntityBin> ULTIMATE_BIN = create(MekanismBlock.ULTIMATE_BIN, () -> new TileEntityBin(MekanismBlock.ULTIMATE_BIN));
    public static final TileEntityType<TileEntityBin> CREATIVE_BIN = create(MekanismBlock.CREATIVE_BIN, () -> new TileEntityBin(MekanismBlock.CREATIVE_BIN));
    //Induction Cells
    public static final TileEntityType<TileEntityInductionCell> BASIC_INDUCTION_CELL = create(MekanismBlock.BASIC_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.BASIC_INDUCTION_CELL));
    public static final TileEntityType<TileEntityInductionCell> ADVANCED_INDUCTION_CELL = create(MekanismBlock.ADVANCED_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.ADVANCED_INDUCTION_CELL));
    public static final TileEntityType<TileEntityInductionCell> ELITE_INDUCTION_CELL = create(MekanismBlock.ELITE_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.ELITE_INDUCTION_CELL));
    public static final TileEntityType<TileEntityInductionCell> ULTIMATE_INDUCTION_CELL = create(MekanismBlock.ULTIMATE_INDUCTION_CELL, () -> new TileEntityInductionCell(MekanismBlock.ULTIMATE_INDUCTION_CELL));
    //Induction Providers
    public static final TileEntityType<TileEntityInductionProvider> BASIC_INDUCTION_PROVIDER = create(MekanismBlock.BASIC_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.BASIC_INDUCTION_PROVIDER));
    public static final TileEntityType<TileEntityInductionProvider> ADVANCED_INDUCTION_PROVIDER = create(MekanismBlock.ADVANCED_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.ADVANCED_INDUCTION_PROVIDER));
    public static final TileEntityType<TileEntityInductionProvider> ELITE_INDUCTION_PROVIDER = create(MekanismBlock.ELITE_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.ELITE_INDUCTION_PROVIDER));
    public static final TileEntityType<TileEntityInductionProvider> ULTIMATE_INDUCTION_PROVIDER = create(MekanismBlock.ULTIMATE_INDUCTION_PROVIDER, () -> new TileEntityInductionProvider(MekanismBlock.ULTIMATE_INDUCTION_PROVIDER));

    //Factories
    //Combining
    public static final TileEntityType<TileEntityFactory> BASIC_COMBINING_FACTORY = create(MekanismBlock.BASIC_COMBINING_FACTORY, () -> new TileEntityCombiningFactory(MekanismBlock.BASIC_COMBINING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_COMBINING_FACTORY = create(MekanismBlock.ADVANCED_COMBINING_FACTORY, () -> new TileEntityCombiningFactory(MekanismBlock.ADVANCED_COMBINING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_COMBINING_FACTORY = create(MekanismBlock.ELITE_COMBINING_FACTORY, () -> new TileEntityCombiningFactory(MekanismBlock.ELITE_COMBINING_FACTORY));
    //Compressing
    public static final TileEntityType<TileEntityFactory> BASIC_COMPRESSING_FACTORY = create(MekanismBlock.BASIC_COMPRESSING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.BASIC_COMPRESSING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_COMPRESSING_FACTORY = create(MekanismBlock.ADVANCED_COMPRESSING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ADVANCED_COMPRESSING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_COMPRESSING_FACTORY = create(MekanismBlock.ELITE_COMPRESSING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ELITE_COMPRESSING_FACTORY));
    //Crushing
    public static final TileEntityType<TileEntityFactory> BASIC_CRUSHING_FACTORY = create(MekanismBlock.BASIC_CRUSHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.BASIC_CRUSHING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_CRUSHING_FACTORY = create(MekanismBlock.ADVANCED_CRUSHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ADVANCED_CRUSHING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_CRUSHING_FACTORY = create(MekanismBlock.ELITE_CRUSHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ELITE_CRUSHING_FACTORY));
    //Enriching
    public static final TileEntityType<TileEntityFactory> BASIC_ENRICHING_FACTORY = create(MekanismBlock.BASIC_ENRICHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.BASIC_ENRICHING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_ENRICHING_FACTORY = create(MekanismBlock.ADVANCED_ENRICHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ADVANCED_ENRICHING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_ENRICHING_FACTORY = create(MekanismBlock.ELITE_ENRICHING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ELITE_ENRICHING_FACTORY));
    //Infusing
    public static final TileEntityType<TileEntityFactory> BASIC_INFUSING_FACTORY = create(MekanismBlock.BASIC_INFUSING_FACTORY, () -> new TileEntityMetallurgicInfuserFactory(MekanismBlock.BASIC_INFUSING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_INFUSING_FACTORY = create(MekanismBlock.ADVANCED_INFUSING_FACTORY, () -> new TileEntityMetallurgicInfuserFactory(MekanismBlock.ADVANCED_INFUSING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_INFUSING_FACTORY = create(MekanismBlock.ELITE_INFUSING_FACTORY, () -> new TileEntityMetallurgicInfuserFactory(MekanismBlock.ELITE_INFUSING_FACTORY));
    //Injecting
    public static final TileEntityType<TileEntityFactory> BASIC_INJECTING_FACTORY = create(MekanismBlock.BASIC_INJECTING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.BASIC_INJECTING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_INJECTING_FACTORY = create(MekanismBlock.ADVANCED_INJECTING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ADVANCED_INJECTING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_INJECTING_FACTORY = create(MekanismBlock.ELITE_INJECTING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ELITE_INJECTING_FACTORY));
    //Purifying
    public static final TileEntityType<TileEntityFactory> BASIC_PURIFYING_FACTORY = create(MekanismBlock.BASIC_PURIFYING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.BASIC_PURIFYING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_PURIFYING_FACTORY = create(MekanismBlock.ADVANCED_PURIFYING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ADVANCED_PURIFYING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_PURIFYING_FACTORY = create(MekanismBlock.ELITE_PURIFYING_FACTORY, () -> new TileEntityItemStackGasToItemStackFactory(MekanismBlock.ELITE_PURIFYING_FACTORY));
    //Sawing
    public static final TileEntityType<TileEntityFactory> BASIC_SAWING_FACTORY = create(MekanismBlock.BASIC_SAWING_FACTORY, () -> new TileEntitySawingFactory(MekanismBlock.BASIC_SAWING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_SAWING_FACTORY = create(MekanismBlock.ADVANCED_SAWING_FACTORY, () -> new TileEntitySawingFactory(MekanismBlock.ADVANCED_SAWING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_SAWING_FACTORY = create(MekanismBlock.ELITE_SAWING_FACTORY, () -> new TileEntitySawingFactory(MekanismBlock.ELITE_SAWING_FACTORY));
    //Smelting
    public static final TileEntityType<TileEntityFactory> BASIC_SMELTING_FACTORY = create(MekanismBlock.BASIC_SMELTING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.BASIC_SMELTING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ADVANCED_SMELTING_FACTORY = create(MekanismBlock.ADVANCED_SMELTING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ADVANCED_SMELTING_FACTORY));
    public static final TileEntityType<TileEntityFactory> ELITE_SMELTING_FACTORY = create(MekanismBlock.ELITE_SMELTING_FACTORY, () -> new TileEntityItemStackToItemStackFactory(MekanismBlock.ELITE_SMELTING_FACTORY));

    private static <T extends TileEntity> TileEntityType<T> create(IBlockProvider provider, Supplier<? extends T> factory) {
        return create(provider.getRegistryName(), TileEntityType.Builder.create(factory, provider.getBlock()));
    }

    private static <T extends TileEntity> TileEntityType<T> create(ResourceLocation registryName, TileEntityType.Builder<T> builder) {
        //fixerType = DataFixesManager.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getVersion().getWorldVersion())).getChoiceType(TypeReferences.BLOCK_ENTITY, registryName.getPath());
        //TODO: I don't believe we have a data fixer type for our stuff so it is technically null not the above thing which is taken from TileEntityTypes#register
        // Note: If above is needed, we should add the try catch that TileEntityTypes#register includes
        TileEntityType<T> type = builder.build(null);
        type.setRegistryName(registryName);
        types.add(type);
        return type;
    }

    public static void registerTileEntities(IForgeRegistry<TileEntityType<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}