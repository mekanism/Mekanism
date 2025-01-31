package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.inventory.container.item.QIOFrequencySelectItemContainer;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.inventory.container.tile.DigitalMinerConfigContainer;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPersonalStorage;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
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
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.tile.qio.TileEntityQIOComponent;
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
    public static final ContainerTypeRegistryObject<DictionaryContainer> DICTIONARY = CONTAINER_TYPES.register(MekanismItems.DICTIONARY, ItemDictionary.class, DictionaryContainer::new);
    public static final ContainerTypeRegistryObject<PortableTeleporterContainer> PORTABLE_TELEPORTER = CONTAINER_TYPES.register(MekanismItems.PORTABLE_TELEPORTER, ItemPortableTeleporter.class, PortableTeleporterContainer::new);
    public static final ContainerTypeRegistryObject<SeismicReaderContainer> SEISMIC_READER = CONTAINER_TYPES.register(MekanismItems.SEISMIC_READER, ItemSeismicReader.class, SeismicReaderContainer::new);
    public static final ContainerTypeRegistryObject<QIOFrequencySelectItemContainer> QIO_FREQUENCY_SELECT_ITEM = CONTAINER_TYPES.register("qio_frequency_select_item", ItemPortableQIODashboard.class, QIOFrequencySelectItemContainer::new);
    public static final ContainerTypeRegistryObject<PortableQIODashboardContainer> PORTABLE_QIO_DASHBOARD = CONTAINER_TYPES.register(MekanismItems.PORTABLE_QIO_DASHBOARD, MekanismItemContainerType::qioDashboard);

    //Entity
    public static final ContainerTypeRegistryObject<MainRobitContainer> MAIN_ROBIT = CONTAINER_TYPES.registerEntity("main_robit", EntityRobit.class, MainRobitContainer::new);
    public static final ContainerTypeRegistryObject<RobitContainer> INVENTORY_ROBIT = CONTAINER_TYPES.register("inventory_robit");
    public static final ContainerTypeRegistryObject<RobitContainer> SMELTING_ROBIT = CONTAINER_TYPES.register("smelting_robit");
    public static final ContainerTypeRegistryObject<CraftingRobitContainer> CRAFTING_ROBIT = CONTAINER_TYPES.registerEntity("crafting_robit", EntityRobit.class, CraftingRobitContainer::new);
    public static final ContainerTypeRegistryObject<RepairRobitContainer> REPAIR_ROBIT = CONTAINER_TYPES.registerEntity("repair_robit", EntityRobit.class, RepairRobitContainer::new);

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
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDynamicTank>> DYNAMIC_TANK = CONTAINER_TYPES.custom(MekanismBlocks.DYNAMIC_TANK, TileEntityDynamicTank.class).armorSideBar().build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityElectricPump>> ELECTRIC_PUMP = CONTAINER_TYPES.register(MekanismBlocks.ELECTRIC_PUMP, TileEntityElectricPump.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityElectrolyticSeparator>> ELECTROLYTIC_SEPARATOR = CONTAINER_TYPES.register(MekanismBlocks.ELECTROLYTIC_SEPARATOR, TileEntityElectrolyticSeparator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityEnergizedSmelter>> ENERGIZED_SMELTER = CONTAINER_TYPES.register(MekanismBlocks.ENERGIZED_SMELTER, TileEntityEnergizedSmelter.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityEnrichmentChamber>> ENRICHMENT_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.ENRICHMENT_CHAMBER, TileEntityEnrichmentChamber.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFluidicPlenisher>> FLUIDIC_PLENISHER = CONTAINER_TYPES.register(MekanismBlocks.FLUIDIC_PLENISHER, TileEntityFluidicPlenisher.class);
    public static final ContainerTypeRegistryObject<FormulaicAssemblicatorContainer> FORMULAIC_ASSEMBLICATOR = CONTAINER_TYPES.register(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, TileEntityFormulaicAssemblicator.class, FormulaicAssemblicatorContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFuelwoodHeater>> FUELWOOD_HEATER = CONTAINER_TYPES.register(MekanismBlocks.FUELWOOD_HEATER, TileEntityFuelwoodHeater.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityLaserAmplifier>> LASER_AMPLIFIER = CONTAINER_TYPES.register(MekanismBlocks.LASER_AMPLIFIER, TileEntityLaserAmplifier.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityLaserTractorBeam>> LASER_TRACTOR_BEAM = CONTAINER_TYPES.register(MekanismBlocks.LASER_TRACTOR_BEAM, TileEntityLaserTractorBeam.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityMetallurgicInfuser>> METALLURGIC_INFUSER = CONTAINER_TYPES.register(MekanismBlocks.METALLURGIC_INFUSER, TileEntityMetallurgicInfuser.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityOredictionificator>> OREDICTIONIFICATOR = CONTAINER_TYPES.custom(MekanismBlocks.OREDICTIONIFICATOR, TileEntityOredictionificator.class).offset(30, 64).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityOsmiumCompressor>> OSMIUM_COMPRESSOR = CONTAINER_TYPES.register(MekanismBlocks.OSMIUM_COMPRESSOR, TileEntityOsmiumCompressor.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPrecisionSawmill>> PRECISION_SAWMILL = CONTAINER_TYPES.register(MekanismBlocks.PRECISION_SAWMILL, TileEntityPrecisionSawmill.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPressurizedReactionChamber>> PRESSURIZED_REACTION_CHAMBER = CONTAINER_TYPES.custom(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, TileEntityPressurizedReactionChamber.class).offset(0, 5).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPurificationChamber>> PURIFICATION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.PURIFICATION_CHAMBER, TileEntityPurificationChamber.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQuantumEntangloporter>> QUANTUM_ENTANGLOPORTER = CONTAINER_TYPES.custom(MekanismBlocks.QUANTUM_ENTANGLOPORTER, TileEntityQuantumEntangloporter.class).offset(0, 74).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityResistiveHeater>> RESISTIVE_HEATER = CONTAINER_TYPES.register(MekanismBlocks.RESISTIVE_HEATER, TileEntityResistiveHeater.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityRotaryCondensentrator>> ROTARY_CONDENSENTRATOR = CONTAINER_TYPES.register(MekanismBlocks.ROTARY_CONDENSENTRATOR, TileEntityRotaryCondensentrator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySecurityDesk>> SECURITY_DESK = CONTAINER_TYPES.custom(MekanismBlocks.SECURITY_DESK, TileEntitySecurityDesk.class).offset(0, 64).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityModificationStation>> MODIFICATION_STATION = CONTAINER_TYPES.custom(MekanismBlocks.MODIFICATION_STATION, TileEntityModificationStation.class).offset(0, 64).armorSideBar(8, 8, 8).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityIsotopicCentrifuge>> ISOTOPIC_CENTRIFUGE = CONTAINER_TYPES.register(MekanismBlocks.ISOTOPIC_CENTRIFUGE, TileEntityIsotopicCentrifuge.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityNutritionalLiquifier>> NUTRITIONAL_LIQUIFIER = CONTAINER_TYPES.register(MekanismBlocks.NUTRITIONAL_LIQUIFIER, TileEntityNutritionalLiquifier.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySeismicVibrator>> SEISMIC_VIBRATOR = CONTAINER_TYPES.register(MekanismBlocks.SEISMIC_VIBRATOR, TileEntitySeismicVibrator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySolarNeutronActivator>> SOLAR_NEUTRON_ACTIVATOR = CONTAINER_TYPES.register(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, TileEntitySolarNeutronActivator.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityTeleporter>> TELEPORTER = CONTAINER_TYPES.custom(MekanismBlocks.TELEPORTER, TileEntityTeleporter.class).offset(0, 74).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityThermalEvaporationController>> THERMAL_EVAPORATION_CONTROLLER = CONTAINER_TYPES.custom(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, TileEntityThermalEvaporationController.class).offset(10, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> ANTIPROTONIC_NUCLEOSYNTHESIZER = CONTAINER_TYPES.custom(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, TileEntityAntiprotonicNucleosynthesizer.class).offset(10, 27).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPigmentExtractor>> PIGMENT_EXTRACTOR = CONTAINER_TYPES.register(MekanismBlocks.PIGMENT_EXTRACTOR, TileEntityPigmentExtractor.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPigmentMixer>> PIGMENT_MIXER = CONTAINER_TYPES.register(MekanismBlocks.PIGMENT_MIXER, TileEntityPigmentMixer.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPaintingMachine>> PAINTING_MACHINE = CONTAINER_TYPES.register(MekanismBlocks.PAINTING_MACHINE, TileEntityPaintingMachine.class);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIODriveArray>> QIO_DRIVE_ARRAY = CONTAINER_TYPES.custom(MekanismBlocks.QIO_DRIVE_ARRAY, TileEntityQIODriveArray.class).offset(0, 40).build();
    public static final ContainerTypeRegistryObject<QIODashboardContainer> QIO_DASHBOARD = CONTAINER_TYPES.register(MekanismBlocks.QIO_DASHBOARD, MekanismContainerType::qioDashboard);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIOImporter>> QIO_IMPORTER = CONTAINER_TYPES.custom(MekanismBlocks.QIO_IMPORTER, TileEntityQIOImporter.class).offset(30, 74).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIOExporter>> QIO_EXPORTER = CONTAINER_TYPES.custom(MekanismBlocks.QIO_EXPORTER, TileEntityQIOExporter.class).offset(30, 74).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityQIORedstoneAdapter>> QIO_REDSTONE_ADAPTER = CONTAINER_TYPES.custom(MekanismBlocks.QIO_REDSTONE_ADAPTER, TileEntityQIORedstoneAdapter.class).offset(0, 26).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntitySPSCasing>> SPS = CONTAINER_TYPES.custom(MekanismBlocks.SPS_CASING, TileEntitySPSCasing.class).offset(0, 16).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDimensionalStabilizer>> DIMENSIONAL_STABILIZER = CONTAINER_TYPES.register(MekanismBlocks.DIMENSIONAL_STABILIZER, TileEntityDimensionalStabilizer.class);

    //Named
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFactory<?>>> FACTORY = CONTAINER_TYPES.register("factory", factoryClass(), FactoryContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityChemicalTank>> CHEMICAL_TANK = CONTAINER_TYPES.custom("chemical_tank", TileEntityChemicalTank.class).armorSideBar().build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFluidTank>> FLUID_TANK = CONTAINER_TYPES.custom("fluid_tank", TileEntityFluidTank.class).armorSideBar().build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityEnergyCube>> ENERGY_CUBE = CONTAINER_TYPES.custom("energy_cube", TileEntityEnergyCube.class).armorSideBar(180, 41, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityInductionCasing>> INDUCTION_MATRIX = CONTAINER_TYPES.custom("induction_matrix", TileEntityInductionCasing.class).armorSideBar(-20, 41, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityBoilerCasing>> THERMOELECTRIC_BOILER = CONTAINER_TYPES.custom("thermoelectric_boiler", TileEntityBoilerCasing.class).offset(21, 0).build();
    public static final ContainerTypeRegistryObject<PersonalStorageItemContainer> PERSONAL_STORAGE_ITEM = CONTAINER_TYPES.registerMenu("personal_storage_item", () -> MekanismItemContainerType.item(ItemBlockPersonalStorage.class, PersonalStorageItemContainer::new));
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityPersonalStorage>> PERSONAL_STORAGE_BLOCK = CONTAINER_TYPES.custom("personal_storage_block", TileEntityPersonalStorage.class).offset(0, 56).build();
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityQIOComponent>> QIO_FREQUENCY_SELECT_TILE = CONTAINER_TYPES.registerEmpty("qio_frequency_select_tile", TileEntityQIOComponent.class);

    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityBoilerCasing>> BOILER_STATS = CONTAINER_TYPES.registerEmpty("boiler_stats", TileEntityBoilerCasing.class);
    public static final ContainerTypeRegistryObject<EmptyTileContainer<TileEntityInductionCasing>> MATRIX_STATS = CONTAINER_TYPES.registerEmpty("matrix_stats", TileEntityInductionCasing.class);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityDigitalMiner>> DIGITAL_MINER_CONFIG = CONTAINER_TYPES.register("digital_miner_config", TileEntityDigitalMiner.class, DigitalMinerConfigContainer::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityLogisticalSorter>> LOGISTICAL_SORTER = CONTAINER_TYPES.custom(MekanismBlocks.LOGISTICAL_SORTER, TileEntityLogisticalSorter.class).offset(50, 88).build();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Class<TileEntityFactory<?>> factoryClass() {
        return (Class) TileEntityFactory.class;
    }
}
