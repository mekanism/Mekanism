package mekanism.generators.common.registries;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntityReactorGlass;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorPort;
import mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;

public class GeneratorsTileEntityTypes {

    private GeneratorsTileEntityTypes() {
    }

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MekanismGenerators.MODID);

    //Generators
    public static final TileEntityTypeRegistryObject<TileEntityAdvancedSolarGenerator> ADVANCED_SOLAR_GENERATOR = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, TileEntityAdvancedSolarGenerator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .withSimple(Capabilities.EVAPORATION_SOLAR)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityBioGenerator> BIO_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.BIO_GENERATOR, TileEntityBioGenerator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityGasGenerator> GAS_BURNING_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.GAS_BURNING_GENERATOR, TileEntityGasGenerator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityHeatGenerator> HEAT_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.HEAT_GENERATOR, TileEntityHeatGenerator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySolarGenerator> SOLAR_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.SOLAR_GENERATOR, TileEntitySolarGenerator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityWindGenerator> WIND_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.WIND_GENERATOR, TileEntityWindGenerator::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    //Misc
    public static final TileEntityTypeRegistryObject<TileEntityReactorGlass> REACTOR_GLASS = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.REACTOR_GLASS, TileEntityReactorGlass::new)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    //Fission Reactor
    public static final TileEntityTypeRegistryObject<TileEntityFissionReactorCasing> FISSION_REACTOR_CASING = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.FISSION_REACTOR_CASING, TileEntityFissionReactorCasing::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFissionReactorPort> FISSION_REACTOR_PORT = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.FISSION_REACTOR_PORT, TileEntityFissionReactorPort::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFissionReactorLogicAdapter> FISSION_REACTOR_LOGIC_ADAPTER = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, TileEntityFissionReactorLogicAdapter::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFissionFuelAssembly> FISSION_FUEL_ASSEMBLY = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, TileEntityFissionFuelAssembly::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityControlRodAssembly> CONTROL_ROD_ASSEMBLY = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY, TileEntityControlRodAssembly::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    //Fusion Reactor
    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorController> FUSION_REACTOR_CONTROLLER = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER, TileEntityFusionReactorController::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          //Never allow the gas handler, fluid handler, or energy cap to be enabled here even though internally we can handle both of them
          .without(Capabilities.CHEMICAL.block(), Capabilities.FLUID.block(), Capabilities.HEAT)
          .without(EnergyCompatUtils.getLoadedEnergyCapabilities())
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorBlock> FUSION_REACTOR_FRAME = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.FUSION_REACTOR_FRAME, TileEntityFusionReactorBlock::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityLaserFocusMatrix> LASER_FOCUS_MATRIX = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.LASER_FOCUS_MATRIX, TileEntityLaserFocusMatrix::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.LASER_RECEPTOR)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorLogicAdapter> FUSION_REACTOR_LOGIC_ADAPTER = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER, TileEntityFusionReactorLogicAdapter::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .withSimple(Capabilities.CONFIG_CARD)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorPort> FUSION_REACTOR_PORT = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.FUSION_REACTOR_PORT, TileEntityFusionReactorPort::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    //Turbine
    public static final TileEntityTypeRegistryObject<TileEntityElectromagneticCoil> ELECTROMAGNETIC_COIL = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.ELECTROMAGNETIC_COIL, TileEntityElectromagneticCoil::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityRotationalComplex> ROTATIONAL_COMPLEX = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.ROTATIONAL_COMPLEX, TileEntityRotationalComplex::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntitySaturatingCondenser> SATURATING_CONDENSER = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.SATURATING_CONDENSER, TileEntitySaturatingCondenser::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityTurbineCasing> TURBINE_CASING = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.TURBINE_CASING, TileEntityTurbineCasing::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityTurbineRotor> TURBINE_ROTOR = TILE_ENTITY_TYPES.mekBuilder(GeneratorsBlocks.TURBINE_ROTOR, TileEntityTurbineRotor::new)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityTurbineValve> TURBINE_VALVE = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.TURBINE_VALVE, TileEntityTurbineValve::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityTurbineVent> TURBINE_VENT = TILE_ENTITY_TYPES
          .mekBuilder(GeneratorsBlocks.TURBINE_VENT, TileEntityTurbineVent::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .withSimple(Capabilities.CONFIGURABLE)
          .build();
}