package mekanism.generators.common.registries;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;

public class GeneratorsContainerTypes {

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekanismGenerators.MODID);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityBioGenerator>> BIO_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlocks.BIO_GENERATOR, TileEntityBioGenerator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityGasGenerator>> GAS_BURNING_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlocks.GAS_BURNING_GENERATOR, TileEntityGasGenerator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityHeatGenerator>> HEAT_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlocks.HEAT_GENERATOR, TileEntityHeatGenerator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityTurbineCasing>> INDUSTRIAL_TURBINE = CONTAINER_TYPES.register("industrial_turbine", TileEntityTurbineCasing.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityReactorController>> REACTOR_CONTROLLER = CONTAINER_TYPES.register(GeneratorsBlocks.REACTOR_CONTROLLER, TileEntityReactorController.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityReactorController>> REACTOR_FUEL = CONTAINER_TYPES.registerEmpty("reactor_fuel", TileEntityReactorController.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityReactorController>> REACTOR_HEAT = CONTAINER_TYPES.registerEmpty("reactor_heat", TileEntityReactorController.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityReactorLogicAdapter>> REACTOR_LOGIC_ADAPTER = CONTAINER_TYPES.registerEmpty(GeneratorsBlocks.REACTOR_LOGIC_ADAPTER, TileEntityReactorLogicAdapter.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityReactorController>> REACTOR_STATS = CONTAINER_TYPES.registerEmpty("reactor_stats", TileEntityReactorController.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySolarGenerator>> SOLAR_GENERATOR = CONTAINER_TYPES.register("solar_generator", TileEntitySolarGenerator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityAdvancedSolarGenerator>> ADVANCED_SOLAR_GENERATOR = CONTAINER_TYPES.register("advanced_solar_generator", TileEntityAdvancedSolarGenerator.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityTurbineCasing>> TURBINE_STATS = CONTAINER_TYPES.registerEmpty("turbine_stats", TileEntityTurbineCasing.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityWindGenerator>> WIND_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlocks.WIND_GENERATOR, TileEntityWindGenerator.class);
}