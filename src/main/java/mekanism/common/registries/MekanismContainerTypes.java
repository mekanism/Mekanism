package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.inventory.container.item.QIOFrequencySelectItemContainer;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.inventory.container.tile.DigitalMinerConfigContainer;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.container.tile.MatrixStatsTabContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.tile.ModificationStationContainer;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.inventory.container.tile.QIOFrequencySelectTileContainer;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.inventory.container.tile.UpgradeManagementContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
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
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.machine.TileEntityPurificationChamber;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;

public class MekanismContainerTypes {

    private MekanismContainerTypes() {
    }

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(Mekanism.MODID);

    //No bound
    public static final ContainerTypeRegistryObject<ModuleTweakerContainer> MODULE_TWEAKER = CONTAINER_TYPES.register("module_tweaker", ModuleTweakerContainer::new);

    //Items
    public static final ContainerTypeRegistryObject<DictionaryContainer> DICTIONARY = CONTAINER_TYPES.register(MekanismItems.DICTIONARY, DictionaryContainer::new);
    public static final ContainerTypeRegistryObject<PortableTeleporterContainer> PORTABLE_TELEPORTER = CONTAINER_TYPES.register(MekanismItems.PORTABLE_TELEPORTER, PortableTeleporterContainer::new);
    public static final ContainerTypeRegistryObject<SeismicReaderContainer> SEISMIC_READER = CONTAINER_TYPES.register(MekanismItems.SEISMIC_READER, SeismicReaderContainer::new);
    public static final ContainerTypeRegistryObject<QIOFrequencySelectItemContainer> QIO_FREQUENCY_SELECT_ITEM = CONTAINER_TYPES.register("qio_frequency_select_item", QIOFrequencySelectItemContainer::new);
    public static final ContainerTypeRegistryObject<PortableQIODashboardContainer> PORTABLE_QIO_DASHBOARD = CONTAINER_TYPES.register(MekanismItems.PORTABLE_QIO_DASHBOARD, PortableQIODashboardContainer::new);

    //Entity
    public static final ContainerTypeRegistryObject<MainRobitContainer> MAIN_ROBIT = CONTAINER_TYPES.register("main_robit", MainRobitContainer::new);
    public static final ContainerTypeRegistryObject<InventoryRobitContainer> INVENTORY_ROBIT = CONTAINER_TYPES.register("inventory_robit", InventoryRobitContainer::new);
    public static final ContainerTypeRegistryObject<SmeltingRobitContainer> SMELTING_ROBIT = CONTAINER_TYPES.register("smelting_robit", SmeltingRobitContainer::new);
    public static final ContainerTypeRegistryObject<CraftingRobitContainer> CRAFTING_ROBIT = CONTAINER_TYPES.register("crafting_robit", CraftingRobitContainer::new);
    public static final ContainerTypeRegistryObject<RepairRobitContainer> REPAIR_ROBIT = CONTAINER_TYPES.register("repair_robit", RepairRobitContainer::new);

    //Blocks
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalCrystallizer>> CHEMICAL_CRYSTALLIZER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_CRYSTALLIZER, TileEntityChemicalCrystallizer.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalDissolutionChamber>> CHEMICAL_DISSOLUTION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, TileEntityChemicalDissolutionChamber.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalInfuser>> CHEMICAL_INFUSER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_INFUSER, TileEntityChemicalInfuser.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalInjectionChamber>> CHEMICAL_INJECTION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, TileEntityChemicalInjectionChamber.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalOxidizer>> CHEMICAL_OXIDIZER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_OXIDIZER, TileEntityChemicalOxidizer.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalWasher>> CHEMICAL_WASHER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_WASHER, TileEntityChemicalWasher.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityCombiner>> COMBINER = CONTAINER_TYPES.register(MekanismBlocks.COMBINER, TileEntityCombiner.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityCrusher>> CRUSHER = CONTAINER_TYPES.register(MekanismBlocks.CRUSHER, TileEntityCrusher.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDigitalMiner>> DIGITAL_MINER = CONTAINER_TYPES.custom(MekanismBlocks.DIGITAL_MINER, TileEntityDigitalMiner.class).offset(0, 76).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDynamicTank>> DYNAMIC_TANK = CONTAINER_TYPES.register(MekanismBlocks.DYNAMIC_TANK, TileEntityDynamicTank.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityElectricPump>> ELECTRIC_PUMP = CONTAINER_TYPES.register(MekanismBlocks.ELECTRIC_PUMP, TileEntityElectricPump.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityElectrolyticSeparator>> ELECTROLYTIC_SEPARATOR = CONTAINER_TYPES.register(MekanismBlocks.ELECTROLYTIC_SEPARATOR, TileEntityElectrolyticSeparator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityEnergizedSmelter>> ENERGIZED_SMELTER = CONTAINER_TYPES.register(MekanismBlocks.ENERGIZED_SMELTER, TileEntityEnergizedSmelter.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityEnrichmentChamber>> ENRICHMENT_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.ENRICHMENT_CHAMBER, TileEntityEnrichmentChamber.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFluidicPlenisher>> FLUIDIC_PLENISHER = CONTAINER_TYPES.register(MekanismBlocks.FLUIDIC_PLENISHER, TileEntityFluidicPlenisher.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFormulaicAssemblicator>> FORMULAIC_ASSEMBLICATOR = CONTAINER_TYPES.register(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, FormulaicAssemblicatorContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFuelwoodHeater>> FUELWOOD_HEATER = CONTAINER_TYPES.register(MekanismBlocks.FUELWOOD_HEATER, TileEntityFuelwoodHeater.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityLaserAmplifier>> LASER_AMPLIFIER = CONTAINER_TYPES.register(MekanismBlocks.LASER_AMPLIFIER, TileEntityLaserAmplifier.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityLaserTractorBeam>> LASER_TRACTOR_BEAM = CONTAINER_TYPES.register(MekanismBlocks.LASER_TRACTOR_BEAM, TileEntityLaserTractorBeam.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityMetallurgicInfuser>> METALLURGIC_INFUSER = CONTAINER_TYPES.register(MekanismBlocks.METALLURGIC_INFUSER, TileEntityMetallurgicInfuser.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityOredictionificator>> OREDICTIONIFICATOR = CONTAINER_TYPES.custom(MekanismBlocks.OREDICTIONIFICATOR, TileEntityOredictionificator.class).offset(0, 64).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityOsmiumCompressor>> OSMIUM_COMPRESSOR = CONTAINER_TYPES.register(MekanismBlocks.OSMIUM_COMPRESSOR, TileEntityOsmiumCompressor.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPrecisionSawmill>> PRECISION_SAWMILL = CONTAINER_TYPES.register(MekanismBlocks.PRECISION_SAWMILL, TileEntityPrecisionSawmill.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPressurizedReactionChamber>> PRESSURIZED_REACTION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, TileEntityPressurizedReactionChamber.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPurificationChamber>> PURIFICATION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.PURIFICATION_CHAMBER, TileEntityPurificationChamber.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQuantumEntangloporter>> QUANTUM_ENTANGLOPORTER = CONTAINER_TYPES.custom(MekanismBlocks.QUANTUM_ENTANGLOPORTER, TileEntityQuantumEntangloporter.class).offset(0, 64).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityResistiveHeater>> RESISTIVE_HEATER = CONTAINER_TYPES.register(MekanismBlocks.RESISTIVE_HEATER, TileEntityResistiveHeater.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityRotaryCondensentrator>> ROTARY_CONDENSENTRATOR = CONTAINER_TYPES.register(MekanismBlocks.ROTARY_CONDENSENTRATOR, TileEntityRotaryCondensentrator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySecurityDesk>> SECURITY_DESK = CONTAINER_TYPES.custom(MekanismBlocks.SECURITY_DESK, TileEntitySecurityDesk.class).offset(0, 64).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityModificationStation>> MODIFICATION_STATION = CONTAINER_TYPES.register(MekanismBlocks.MODIFICATION_STATION, ModificationStationContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityIsotopicCentrifuge>> ISOTOPIC_CENTRIFUGE = CONTAINER_TYPES.register(MekanismBlocks.ISOTOPIC_CENTRIFUGE, TileEntityIsotopicCentrifuge.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityNutritionalLiquifier>> NUTRITIONAL_LIQUIFIER = CONTAINER_TYPES.register(MekanismBlocks.NUTRITIONAL_LIQUIFIER, TileEntityNutritionalLiquifier.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySeismicVibrator>> SEISMIC_VIBRATOR = CONTAINER_TYPES.register(MekanismBlocks.SEISMIC_VIBRATOR, TileEntitySeismicVibrator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySolarNeutronActivator>> SOLAR_NEUTRON_ACTIVATOR = CONTAINER_TYPES.register(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, TileEntitySolarNeutronActivator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityTeleporter>> TELEPORTER = CONTAINER_TYPES.custom(MekanismBlocks.TELEPORTER, TileEntityTeleporter.class).offset(0, 64).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityThermalEvaporationController>> THERMAL_EVAPORATION_CONTROLLER = CONTAINER_TYPES.register(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, TileEntityThermalEvaporationController.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> ANTIPROTONIC_NUCLEOSYNTHESIZER = CONTAINER_TYPES.custom(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, TileEntityAntiprotonicNucleosynthesizer.class).offset(10, 27).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIODriveArray>> QIO_DRIVE_ARRAY = CONTAINER_TYPES.custom(MekanismBlocks.QIO_DRIVE_ARRAY, TileEntityQIODriveArray.class).offset(0, 40).build();
    public static final ContainerTypeRegistryObject<QIODashboardContainer> QIO_DASHBOARD = CONTAINER_TYPES.register(MekanismBlocks.QIO_DASHBOARD, QIODashboardContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIOImporter>> QIO_IMPORTER = CONTAINER_TYPES.custom(MekanismBlocks.QIO_IMPORTER, TileEntityQIOImporter.class).offset(0, 74).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIOExporter>> QIO_EXPORTER = CONTAINER_TYPES.custom(MekanismBlocks.QIO_EXPORTER, TileEntityQIOExporter.class).offset(0, 74).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIORedstoneAdapter>> QIO_REDSTONE_ADAPTER = CONTAINER_TYPES.custom(MekanismBlocks.QIO_REDSTONE_ADAPTER, TileEntityQIORedstoneAdapter.class).offset(0, 16).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySPSCasing>> SPS = CONTAINER_TYPES.custom(MekanismBlocks.SPS_CASING, TileEntitySPSCasing.class).offset(0, 16).build();

    //Named
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFactory<?>>> FACTORY = CONTAINER_TYPES.register("factory", FactoryContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalTank>> CHEMICAL_TANK = CONTAINER_TYPES.register("chemical_tank", TileEntityChemicalTank.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFluidTank>> FLUID_TANK = CONTAINER_TYPES.register("fluid_tank", TileEntityFluidTank.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityEnergyCube>> ENERGY_CUBE = CONTAINER_TYPES.register("energy_cube", TileEntityEnergyCube.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityInductionCasing>> INDUCTION_MATRIX = CONTAINER_TYPES.register("induction_matrix", TileEntityInductionCasing.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityBoilerCasing>> THERMOELECTRIC_BOILER = CONTAINER_TYPES.register("thermoelectric_boiler", ThermoelectricBoilerContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityMekanism>> UPGRADE_MANAGEMENT = CONTAINER_TYPES.register("upgrade_management", UpgradeManagementContainer::new);
    public static final ContainerTypeRegistryObject<PersonalChestItemContainer> PERSONAL_CHEST_ITEM = CONTAINER_TYPES.register("personal_chest_item", PersonalChestItemContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPersonalChest>> PERSONAL_CHEST_BLOCK = CONTAINER_TYPES.custom("personal_chest_block", TileEntityPersonalChest.class).offset(0, 64).build();
    public static final ContainerTypeRegistryObject<QIOFrequencySelectTileContainer> QIO_FREQUENCY_SELECT_TILE = CONTAINER_TYPES.register("qio_frequency_select_tile", QIOFrequencySelectTileContainer::new);

    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityBoilerCasing>> BOILER_STATS = CONTAINER_TYPES.registerEmpty("boiler_stats", TileEntityBoilerCasing.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityInductionCasing>> MATRIX_STATS = CONTAINER_TYPES.register("matrix_stats", MatrixStatsTabContainer::new);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDigitalMiner>> DIGITAL_MINER_CONFIG = CONTAINER_TYPES.register("digital_miner_config", DigitalMinerConfigContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityLogisticalSorter>> LOGISTICAL_SORTER = CONTAINER_TYPES.custom(MekanismBlocks.LOGISTICAL_SORTER, TileEntityLogisticalSorter.class).offset(0, 86).build();
}