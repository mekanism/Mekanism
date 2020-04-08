package mekanism.generators.common.registries;

import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorFrame;
import mekanism.generators.common.tile.reactor.TileEntityReactorGlass;
import mekanism.generators.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;

public class GeneratorsTileEntityTypes {

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MekanismGenerators.MODID);

    //Generators
    public static final TileEntityTypeRegistryObject<TileEntityAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = TILE_ENTITY_TYPES.register(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, TileEntityAdvancedSolarGenerator::new);
    public static final TileEntityTypeRegistryObject<TileEntityBioGenerator> BIO_GENERATOR = TILE_ENTITY_TYPES.register(GeneratorsBlocks.BIO_GENERATOR, TileEntityBioGenerator::new);
    public static final TileEntityTypeRegistryObject<TileEntityGasGenerator> GAS_BURNING_GENERATOR = TILE_ENTITY_TYPES.register(GeneratorsBlocks.GAS_BURNING_GENERATOR, TileEntityGasGenerator::new);
    public static final TileEntityTypeRegistryObject<TileEntityHeatGenerator> HEAT_GENERATOR = TILE_ENTITY_TYPES.register(GeneratorsBlocks.HEAT_GENERATOR, TileEntityHeatGenerator::new);
    public static final TileEntityTypeRegistryObject<TileEntitySolarGenerator> SOLAR_GENERATOR = TILE_ENTITY_TYPES.register(GeneratorsBlocks.SOLAR_GENERATOR, TileEntitySolarGenerator::new);
    public static final TileEntityTypeRegistryObject<TileEntityWindGenerator> WIND_GENERATOR = TILE_ENTITY_TYPES.register(GeneratorsBlocks.WIND_GENERATOR, TileEntityWindGenerator::new);
    //Fission Reactor
    public static final TileEntityTypeRegistryObject<TileEntityFissionReactorCasing> FISSION_REACTOR_CASING = TILE_ENTITY_TYPES.register(GeneratorsBlocks.FISSION_REACTOR_CASING, TileEntityFissionReactorCasing::new);
    public static final TileEntityTypeRegistryObject<TileEntityFissionFuelAssembly> FISSION_FUEL_ASSEMBLY = TILE_ENTITY_TYPES.register(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, TileEntityFissionFuelAssembly::new);
    //Fusion Reactor
    public static final TileEntityTypeRegistryObject<TileEntityReactorController> REACTOR_CONTROLLER = TILE_ENTITY_TYPES.register(GeneratorsBlocks.REACTOR_CONTROLLER, TileEntityReactorController::new);
    public static final TileEntityTypeRegistryObject<TileEntityReactorFrame> REACTOR_FRAME = TILE_ENTITY_TYPES.register(GeneratorsBlocks.REACTOR_FRAME, TileEntityReactorFrame::new);
    public static final TileEntityTypeRegistryObject<TileEntityReactorGlass> REACTOR_GLASS = TILE_ENTITY_TYPES.register(GeneratorsBlocks.REACTOR_GLASS, TileEntityReactorGlass::new);
    public static final TileEntityTypeRegistryObject<TileEntityReactorLaserFocusMatrix> LASER_FOCUS_MATRIX = TILE_ENTITY_TYPES.register(GeneratorsBlocks.LASER_FOCUS_MATRIX, TileEntityReactorLaserFocusMatrix::new);
    public static final TileEntityTypeRegistryObject<TileEntityReactorLogicAdapter> REACTOR_LOGIC_ADAPTER = TILE_ENTITY_TYPES.register(GeneratorsBlocks.REACTOR_LOGIC_ADAPTER, TileEntityReactorLogicAdapter::new);
    public static final TileEntityTypeRegistryObject<TileEntityReactorPort> REACTOR_PORT = TILE_ENTITY_TYPES.register(GeneratorsBlocks.REACTOR_PORT, TileEntityReactorPort::new);
    //Turbine
    public static final TileEntityTypeRegistryObject<TileEntityElectromagneticCoil> ELECTROMAGNETIC_COIL = TILE_ENTITY_TYPES.register(GeneratorsBlocks.ELECTROMAGNETIC_COIL, TileEntityElectromagneticCoil::new);
    public static final TileEntityTypeRegistryObject<TileEntityRotationalComplex> ROTATIONAL_COMPLEX = TILE_ENTITY_TYPES.register(GeneratorsBlocks.ROTATIONAL_COMPLEX, TileEntityRotationalComplex::new);
    public static final TileEntityTypeRegistryObject<TileEntitySaturatingCondenser> SATURATING_CONDENSER = TILE_ENTITY_TYPES.register(GeneratorsBlocks.SATURATING_CONDENSER, TileEntitySaturatingCondenser::new);
    public static final TileEntityTypeRegistryObject<TileEntityTurbineCasing> TURBINE_CASING = TILE_ENTITY_TYPES.register(GeneratorsBlocks.TURBINE_CASING, TileEntityTurbineCasing::new);
    public static final TileEntityTypeRegistryObject<TileEntityTurbineRotor> TURBINE_ROTOR = TILE_ENTITY_TYPES.register(GeneratorsBlocks.TURBINE_ROTOR, TileEntityTurbineRotor::new);
    public static final TileEntityTypeRegistryObject<TileEntityTurbineValve> TURBINE_VALVE = TILE_ENTITY_TYPES.register(GeneratorsBlocks.TURBINE_VALVE, TileEntityTurbineValve::new);
    public static final TileEntityTypeRegistryObject<TileEntityTurbineVent> TURBINE_VENT = TILE_ENTITY_TYPES.register(GeneratorsBlocks.TURBINE_VENT, TileEntityTurbineVent::new);
}