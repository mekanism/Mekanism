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
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;

public class GeneratorsContainerTypes {

    private GeneratorsContainerTypes() {
    }

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekanismGenerators.MODID);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityBioGenerator>> BIO_GENERATOR = CONTAINER_TYPES.custom(GeneratorsBlocks.BIO_GENERATOR, TileEntityBioGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityGasGenerator>> GAS_BURNING_GENERATOR = CONTAINER_TYPES.custom(GeneratorsBlocks.GAS_BURNING_GENERATOR, TileEntityGasGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityHeatGenerator>> HEAT_GENERATOR = CONTAINER_TYPES.custom(GeneratorsBlocks.HEAT_GENERATOR, TileEntityHeatGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityTurbineCasing>> INDUSTRIAL_TURBINE = CONTAINER_TYPES.custom("industrial_turbine", TileEntityTurbineCasing.class).offset(7, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFissionReactorCasing>> FISSION_REACTOR = CONTAINER_TYPES.custom("fission_reactor", TileEntityFissionReactorCasing.class).offset(10, 91).build();
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityFissionReactorCasing>> FISSION_REACTOR_STATS = CONTAINER_TYPES.registerEmpty("fission_reactor_stats", TileEntityFissionReactorCasing.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityFissionReactorLogicAdapter>> FISSION_REACTOR_LOGIC_ADAPTER = CONTAINER_TYPES.registerEmpty(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, TileEntityFissionReactorLogicAdapter.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFusionReactorController>> FUSION_REACTOR_CONTROLLER = CONTAINER_TYPES.custom(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER, TileEntityFusionReactorController.class).offset(5, 0).build();
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityFusionReactorController>> FUSION_REACTOR_FUEL = CONTAINER_TYPES.registerEmpty("fusion_reactor_fuel", TileEntityFusionReactorController.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityFusionReactorController>> FUSION_REACTOR_HEAT = CONTAINER_TYPES.registerEmpty("fusion_reactor_heat", TileEntityFusionReactorController.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityFusionReactorLogicAdapter>> FUSION_REACTOR_LOGIC_ADAPTER = CONTAINER_TYPES.registerEmpty(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER, TileEntityFusionReactorLogicAdapter.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityFusionReactorController>> FUSION_REACTOR_STATS = CONTAINER_TYPES.registerEmpty("fusion_reactor_stats", TileEntityFusionReactorController.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySolarGenerator>> SOLAR_GENERATOR = CONTAINER_TYPES.custom("solar_generator", TileEntitySolarGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityAdvancedSolarGenerator>> ADVANCED_SOLAR_GENERATOR = CONTAINER_TYPES.custom("advanced_solar_generator", TileEntityAdvancedSolarGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityTurbineCasing>> TURBINE_STATS = CONTAINER_TYPES.registerEmpty("turbine_stats", TileEntityTurbineCasing.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityWindGenerator>> WIND_GENERATOR = CONTAINER_TYPES.custom(GeneratorsBlocks.WIND_GENERATOR, TileEntityWindGenerator.class).armorSideBar(-20, 11, 0).build();
}