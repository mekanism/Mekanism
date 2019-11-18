package mekanism.generators.common.inventory.container;

import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.inventory.container.reactor.ReactorControllerContainer;
import mekanism.generators.common.inventory.container.reactor.ReactorLogicAdapterContainer;
import mekanism.generators.common.inventory.container.reactor.info.ReactorFuelContainer;
import mekanism.generators.common.inventory.container.reactor.info.ReactorHeatContainer;
import mekanism.generators.common.inventory.container.reactor.info.ReactorStatsContainer;
import mekanism.generators.common.inventory.container.turbine.TurbineContainer;
import mekanism.generators.common.inventory.container.turbine.TurbineStatsContainer;

public class GeneratorsContainerTypes {

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekanismGenerators.MODID);

    public static final ContainerTypeRegistryObject<BioGeneratorContainer> BIO_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlock.BIO_GENERATOR, BioGeneratorContainer::new);
    public static final ContainerTypeRegistryObject<GasBurningGeneratorContainer> GAS_BURNING_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlock.GAS_BURNING_GENERATOR, GasBurningGeneratorContainer::new);
    public static final ContainerTypeRegistryObject<HeatGeneratorContainer> HEAT_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlock.HEAT_GENERATOR, HeatGeneratorContainer::new);
    public static final ContainerTypeRegistryObject<TurbineContainer> INDUSTRIAL_TURBINE = CONTAINER_TYPES.register("industrial_turbine", TurbineContainer::new);
    public static final ContainerTypeRegistryObject<ReactorControllerContainer> REACTOR_CONTROLLER = CONTAINER_TYPES.register(GeneratorsBlock.REACTOR_CONTROLLER, ReactorControllerContainer::new);
    public static final ContainerTypeRegistryObject<ReactorFuelContainer> REACTOR_FUEL = CONTAINER_TYPES.register("reactor_fuel", ReactorFuelContainer::new);
    public static final ContainerTypeRegistryObject<ReactorHeatContainer> REACTOR_HEAT = CONTAINER_TYPES.register("reactor_heat", ReactorHeatContainer::new);
    public static final ContainerTypeRegistryObject<ReactorLogicAdapterContainer> REACTOR_LOGIC_ADAPTER = CONTAINER_TYPES.register(GeneratorsBlock.REACTOR_LOGIC_ADAPTER, ReactorLogicAdapterContainer::new);
    public static final ContainerTypeRegistryObject<ReactorStatsContainer> REACTOR_STATS = CONTAINER_TYPES.register("reactor_stats", ReactorStatsContainer::new);
    public static final ContainerTypeRegistryObject<SolarGeneratorContainer> SOLAR_GENERATOR = CONTAINER_TYPES.register("solar_generator", SolarGeneratorContainer::new);
    public static final ContainerTypeRegistryObject<TurbineStatsContainer> TURBINE_STATS = CONTAINER_TYPES.register("turbine_stats", TurbineStatsContainer::new);
    public static final ContainerTypeRegistryObject<WindGeneratorContainer> WIND_GENERATOR = CONTAINER_TYPES.register(GeneratorsBlock.WIND_GENERATOR, WindGeneratorContainer::new);
}