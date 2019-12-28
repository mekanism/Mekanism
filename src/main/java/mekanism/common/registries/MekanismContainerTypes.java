package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.inventory.container.tile.BoilerStatsContainer;
import mekanism.common.inventory.container.tile.ChemicalCrystallizerContainer;
import mekanism.common.inventory.container.tile.ChemicalDissolutionChamberContainer;
import mekanism.common.inventory.container.tile.ChemicalInfuserContainer;
import mekanism.common.inventory.container.tile.ChemicalInjectionChamberContainer;
import mekanism.common.inventory.container.tile.ChemicalOxidizerContainer;
import mekanism.common.inventory.container.tile.ChemicalWasherContainer;
import mekanism.common.inventory.container.tile.CombinerContainer;
import mekanism.common.inventory.container.tile.CrusherContainer;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.inventory.container.tile.DynamicTankContainer;
import mekanism.common.inventory.container.tile.ElectricPumpContainer;
import mekanism.common.inventory.container.tile.ElectrolyticSeparatorContainer;
import mekanism.common.inventory.container.tile.EnergizedSmelterContainer;
import mekanism.common.inventory.container.tile.EnergyCubeContainer;
import mekanism.common.inventory.container.tile.EnrichmentChamberContainer;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.inventory.container.tile.FluidTankContainer;
import mekanism.common.inventory.container.tile.FluidicPlenisherContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.container.tile.FuelwoodHeaterContainer;
import mekanism.common.inventory.container.tile.GasTankContainer;
import mekanism.common.inventory.container.tile.InductionMatrixContainer;
import mekanism.common.inventory.container.tile.LaserAmplifierContainer;
import mekanism.common.inventory.container.tile.LaserTractorBeamContainer;
import mekanism.common.inventory.container.tile.MatrixStatsContainer;
import mekanism.common.inventory.container.tile.MetallurgicInfuserContainer;
import mekanism.common.inventory.container.tile.OredictionificatorContainer;
import mekanism.common.inventory.container.tile.OsmiumCompressorContainer;
import mekanism.common.inventory.container.tile.PersonalChestTileContainer;
import mekanism.common.inventory.container.tile.PrecisionSawmillContainer;
import mekanism.common.inventory.container.tile.PressurizedReactionChamberContainer;
import mekanism.common.inventory.container.tile.PurificationChamberContainer;
import mekanism.common.inventory.container.tile.QuantumEntangloporterContainer;
import mekanism.common.inventory.container.tile.ResistiveHeaterContainer;
import mekanism.common.inventory.container.tile.RotaryCondensentratorContainer;
import mekanism.common.inventory.container.tile.SecurityDeskContainer;
import mekanism.common.inventory.container.tile.SeismicVibratorContainer;
import mekanism.common.inventory.container.tile.SideConfigurationContainer;
import mekanism.common.inventory.container.tile.SolarNeutronActivatorContainer;
import mekanism.common.inventory.container.tile.TeleporterContainer;
import mekanism.common.inventory.container.tile.ThermalEvaporationControllerContainer;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.inventory.container.tile.TransporterConfigurationContainer;
import mekanism.common.inventory.container.tile.UpgradeManagementContainer;
import mekanism.common.inventory.container.tile.filter.DMItemStackFilterContainer;
import mekanism.common.inventory.container.tile.filter.DMMaterialFilterContainer;
import mekanism.common.inventory.container.tile.filter.DMModIDFilterContainer;
import mekanism.common.inventory.container.tile.filter.DMTagFilterContainer;
import mekanism.common.inventory.container.tile.filter.LSItemStackFilterContainer;
import mekanism.common.inventory.container.tile.filter.LSMaterialFilterContainer;
import mekanism.common.inventory.container.tile.filter.LSModIDFilterContainer;
import mekanism.common.inventory.container.tile.filter.LSTagFilterContainer;
import mekanism.common.inventory.container.tile.filter.OredictionificatorFilterContainer;
import mekanism.common.inventory.container.tile.filter.list.DigitalMinerConfigContainer;
import mekanism.common.inventory.container.tile.filter.list.LogisticalSorterContainer;
import mekanism.common.inventory.container.tile.filter.select.DMFilterSelectContainer;
import mekanism.common.inventory.container.tile.filter.select.LSFilterSelectContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

//TODO: Go through each container and double check no copy paste error was made with what ContainerType the container is using
public class MekanismContainerTypes {

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(Mekanism.MODID);

    //Items
    public static final ContainerTypeRegistryObject<DictionaryContainer> DICTIONARY = CONTAINER_TYPES.register(MekanismItems.DICTIONARY, DictionaryContainer::new);
    public static final ContainerTypeRegistryObject<PortableTeleporterContainer> PORTABLE_TELEPORTER = CONTAINER_TYPES.register(MekanismItems.PORTABLE_TELEPORTER, PortableTeleporterContainer::new);
    public static final ContainerTypeRegistryObject<SeismicReaderContainer> SEISMIC_READER = CONTAINER_TYPES.register(MekanismItems.SEISMIC_READER, SeismicReaderContainer::new);

    //Entity
    public static final ContainerTypeRegistryObject<MainRobitContainer> MAIN_ROBIT = CONTAINER_TYPES.register("main_robit", MainRobitContainer::new);
    public static final ContainerTypeRegistryObject<InventoryRobitContainer> INVENTORY_ROBIT = CONTAINER_TYPES.register("inventory_robit", InventoryRobitContainer::new);
    //TODO: Should this be like Crafting/Repair except with FurnaceContainer??
    public static final ContainerTypeRegistryObject<SmeltingRobitContainer> SMELTING_ROBIT = CONTAINER_TYPES.register("smelting_robit", SmeltingRobitContainer::new);
    public static final ContainerTypeRegistryObject<CraftingRobitContainer> CRAFTING_ROBIT = CONTAINER_TYPES.register("crafting_robit", CraftingRobitContainer::new);
    public static final ContainerTypeRegistryObject<RepairRobitContainer> REPAIR_ROBIT = CONTAINER_TYPES.register("repair_robit", RepairRobitContainer::new);

    //Blocks
    public static final ContainerTypeRegistryObject<ChemicalCrystallizerContainer> CHEMICAL_CRYSTALLIZER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_CRYSTALLIZER, ChemicalCrystallizerContainer::new);
    public static final ContainerTypeRegistryObject<ChemicalDissolutionChamberContainer> CHEMICAL_DISSOLUTION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, ChemicalDissolutionChamberContainer::new);
    public static final ContainerTypeRegistryObject<ChemicalInfuserContainer> CHEMICAL_INFUSER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_INFUSER, ChemicalInfuserContainer::new);
    public static final ContainerTypeRegistryObject<ChemicalInjectionChamberContainer> CHEMICAL_INJECTION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, ChemicalInjectionChamberContainer::new);
    public static final ContainerTypeRegistryObject<ChemicalOxidizerContainer> CHEMICAL_OXIDIZER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_OXIDIZER, ChemicalOxidizerContainer::new);
    public static final ContainerTypeRegistryObject<ChemicalWasherContainer> CHEMICAL_WASHER = CONTAINER_TYPES.register(MekanismBlocks.CHEMICAL_WASHER, ChemicalWasherContainer::new);
    public static final ContainerTypeRegistryObject<CombinerContainer> COMBINER = CONTAINER_TYPES.register(MekanismBlocks.COMBINER, CombinerContainer::new);
    public static final ContainerTypeRegistryObject<CrusherContainer> CRUSHER = CONTAINER_TYPES.register(MekanismBlocks.CRUSHER, CrusherContainer::new);
    public static final ContainerTypeRegistryObject<DigitalMinerContainer> DIGITAL_MINER = CONTAINER_TYPES.register(MekanismBlocks.DIGITAL_MINER, DigitalMinerContainer::new);
    public static final ContainerTypeRegistryObject<DynamicTankContainer> DYNAMIC_TANK = CONTAINER_TYPES.register(MekanismBlocks.DYNAMIC_TANK, DynamicTankContainer::new);
    public static final ContainerTypeRegistryObject<ElectricPumpContainer> ELECTRIC_PUMP = CONTAINER_TYPES.register(MekanismBlocks.ELECTRIC_PUMP, ElectricPumpContainer::new);
    public static final ContainerTypeRegistryObject<ElectrolyticSeparatorContainer> ELECTROLYTIC_SEPARATOR = CONTAINER_TYPES.register(MekanismBlocks.ELECTROLYTIC_SEPARATOR, ElectrolyticSeparatorContainer::new);
    public static final ContainerTypeRegistryObject<EnergizedSmelterContainer> ENERGIZED_SMELTER = CONTAINER_TYPES.register(MekanismBlocks.ENERGIZED_SMELTER, EnergizedSmelterContainer::new);
    public static final ContainerTypeRegistryObject<EnrichmentChamberContainer> ENRICHMENT_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.ENRICHMENT_CHAMBER, EnrichmentChamberContainer::new);
    public static final ContainerTypeRegistryObject<FluidicPlenisherContainer> FLUIDIC_PLENISHER = CONTAINER_TYPES.register(MekanismBlocks.FLUIDIC_PLENISHER, FluidicPlenisherContainer::new);
    public static final ContainerTypeRegistryObject<FormulaicAssemblicatorContainer> FORMULAIC_ASSEMBLICATOR = CONTAINER_TYPES.register(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, FormulaicAssemblicatorContainer::new);
    public static final ContainerTypeRegistryObject<FuelwoodHeaterContainer> FUELWOOD_HEATER = CONTAINER_TYPES.register(MekanismBlocks.FUELWOOD_HEATER, FuelwoodHeaterContainer::new);
    public static final ContainerTypeRegistryObject<LaserAmplifierContainer> LASER_AMPLIFIER = CONTAINER_TYPES.register(MekanismBlocks.LASER_AMPLIFIER, LaserAmplifierContainer::new);
    public static final ContainerTypeRegistryObject<LaserTractorBeamContainer> LASER_TRACTOR_BEAM = CONTAINER_TYPES.register(MekanismBlocks.LASER_TRACTOR_BEAM, LaserTractorBeamContainer::new);
    public static final ContainerTypeRegistryObject<MetallurgicInfuserContainer> METALLURGIC_INFUSER = CONTAINER_TYPES.register(MekanismBlocks.METALLURGIC_INFUSER, MetallurgicInfuserContainer::new);
    public static final ContainerTypeRegistryObject<OredictionificatorContainer> OREDICTIONIFICATOR = CONTAINER_TYPES.register(MekanismBlocks.OREDICTIONIFICATOR, OredictionificatorContainer::new);
    public static final ContainerTypeRegistryObject<OsmiumCompressorContainer> OSMIUM_COMPRESSOR = CONTAINER_TYPES.register(MekanismBlocks.OSMIUM_COMPRESSOR, OsmiumCompressorContainer::new);
    public static final ContainerTypeRegistryObject<PrecisionSawmillContainer> PRECISION_SAWMILL = CONTAINER_TYPES.register(MekanismBlocks.PRECISION_SAWMILL, PrecisionSawmillContainer::new);
    public static final ContainerTypeRegistryObject<PressurizedReactionChamberContainer> PRESSURIZED_REACTION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, PressurizedReactionChamberContainer::new);
    public static final ContainerTypeRegistryObject<PurificationChamberContainer> PURIFICATION_CHAMBER = CONTAINER_TYPES.register(MekanismBlocks.PURIFICATION_CHAMBER, PurificationChamberContainer::new);
    public static final ContainerTypeRegistryObject<QuantumEntangloporterContainer> QUANTUM_ENTANGLOPORTER = CONTAINER_TYPES.register(MekanismBlocks.QUANTUM_ENTANGLOPORTER, QuantumEntangloporterContainer::new);
    public static final ContainerTypeRegistryObject<ResistiveHeaterContainer> RESISTIVE_HEATER = CONTAINER_TYPES.register(MekanismBlocks.RESISTIVE_HEATER, ResistiveHeaterContainer::new);
    public static final ContainerTypeRegistryObject<RotaryCondensentratorContainer> ROTARY_CONDENSENTRATOR = CONTAINER_TYPES.register(MekanismBlocks.ROTARY_CONDENSENTRATOR, RotaryCondensentratorContainer::new);
    public static final ContainerTypeRegistryObject<SecurityDeskContainer> SECURITY_DESK = CONTAINER_TYPES.register(MekanismBlocks.SECURITY_DESK, SecurityDeskContainer::new);
    public static final ContainerTypeRegistryObject<SeismicVibratorContainer> SEISMIC_VIBRATOR = CONTAINER_TYPES.register(MekanismBlocks.SEISMIC_VIBRATOR, SeismicVibratorContainer::new);
    public static final ContainerTypeRegistryObject<SolarNeutronActivatorContainer> SOLAR_NEUTRON_ACTIVATOR = CONTAINER_TYPES.register(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, SolarNeutronActivatorContainer::new);
    public static final ContainerTypeRegistryObject<TeleporterContainer> TELEPORTER = CONTAINER_TYPES.register(MekanismBlocks.TELEPORTER, TeleporterContainer::new);
    public static final ContainerTypeRegistryObject<ThermalEvaporationControllerContainer> THERMAL_EVAPORATION_CONTROLLER = CONTAINER_TYPES.register(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, ThermalEvaporationControllerContainer::new);

    //TODO: Decide if tiered ones should be done differently/evaluate how their container name is done
    //Named
    public static final ContainerTypeRegistryObject<FactoryContainer> FACTORY = CONTAINER_TYPES.register("factory", FactoryContainer::new);
    public static final ContainerTypeRegistryObject<GasTankContainer> GAS_TANK = CONTAINER_TYPES.register("gas_tank", GasTankContainer::new);
    public static final ContainerTypeRegistryObject<FluidTankContainer> FLUID_TANK = CONTAINER_TYPES.register("fluid_tank", FluidTankContainer::new);
    public static final ContainerTypeRegistryObject<EnergyCubeContainer> ENERGY_CUBE = CONTAINER_TYPES.register("energy_cube", EnergyCubeContainer::new);
    public static final ContainerTypeRegistryObject<InductionMatrixContainer> INDUCTION_MATRIX = CONTAINER_TYPES.register("induction_matrix", InductionMatrixContainer::new);
    public static final ContainerTypeRegistryObject<ThermoelectricBoilerContainer> THERMOELECTRIC_BOILER = CONTAINER_TYPES.register("thermoelectric_boiler", ThermoelectricBoilerContainer::new);
    public static final ContainerTypeRegistryObject<UpgradeManagementContainer> UPGRADE_MANAGEMENT = CONTAINER_TYPES.register("upgrade_management", UpgradeManagementContainer::new);
    public static final ContainerTypeRegistryObject<PersonalChestItemContainer> PERSONAL_CHEST_ITEM = CONTAINER_TYPES.register("personal_chest_item", PersonalChestItemContainer::new);
    public static final ContainerTypeRegistryObject<PersonalChestTileContainer> PERSONAL_CHEST_BLOCK = CONTAINER_TYPES.register("personal_chest_block", PersonalChestTileContainer::new);

    public static final ContainerTypeRegistryObject<BoilerStatsContainer> BOILER_STATS = CONTAINER_TYPES.register("boiler_stats", BoilerStatsContainer::new);
    public static final ContainerTypeRegistryObject<MatrixStatsContainer> MATRIX_STATS = CONTAINER_TYPES.register("matrix_stats", MatrixStatsContainer::new);
    public static final ContainerTypeRegistryObject<SideConfigurationContainer> SIDE_CONFIGURATION = CONTAINER_TYPES.register("side_configuration", SideConfigurationContainer::new);
    public static final ContainerTypeRegistryObject<TransporterConfigurationContainer> TRANSPORTER_CONFIGURATION = CONTAINER_TYPES.register("transporter_configuration", TransporterConfigurationContainer::new);

    public static final ContainerTypeRegistryObject<DigitalMinerConfigContainer> DIGITAL_MINER_CONFIG = CONTAINER_TYPES.register("digital_miner_config", DigitalMinerConfigContainer::new);
    public static final ContainerTypeRegistryObject<LogisticalSorterContainer> LOGISTICAL_SORTER = CONTAINER_TYPES.register(MekanismBlocks.LOGISTICAL_SORTER, LogisticalSorterContainer::new);
    public static final ContainerTypeRegistryObject<DMFilterSelectContainer> DM_FILTER_SELECT = CONTAINER_TYPES.register("digital_miner_filter_select", DMFilterSelectContainer::new);
    public static final ContainerTypeRegistryObject<LSFilterSelectContainer> LS_FILTER_SELECT = CONTAINER_TYPES.register("logistical_sorter_filter_select", LSFilterSelectContainer::new);

    public static final ContainerTypeRegistryObject<DMTagFilterContainer> DM_TAG_FILTER = CONTAINER_TYPES.register("digital_miner_tag_filter", DMTagFilterContainer::new);
    public static final ContainerTypeRegistryObject<LSTagFilterContainer> LS_TAG_FILTER = CONTAINER_TYPES.register("logistical_sorter_tag_filter", LSTagFilterContainer::new);

    public static final ContainerTypeRegistryObject<DMModIDFilterContainer> DM_MOD_ID_FILTER = CONTAINER_TYPES.register("digital_miner_mod_id_filter", DMModIDFilterContainer::new);
    public static final ContainerTypeRegistryObject<LSModIDFilterContainer> LS_MOD_ID_FILTER = CONTAINER_TYPES.register("logistical_sorter_mod_id_filter", LSModIDFilterContainer::new);

    public static final ContainerTypeRegistryObject<DMMaterialFilterContainer> DM_MATERIAL_FILTER = CONTAINER_TYPES.register("digital_miner_material_filter", DMMaterialFilterContainer::new);
    public static final ContainerTypeRegistryObject<LSMaterialFilterContainer> LS_MATERIAL_FILTER = CONTAINER_TYPES.register("logistical_sorter_material_filter", LSMaterialFilterContainer::new);

    public static final ContainerTypeRegistryObject<DMItemStackFilterContainer> DM_ITEMSTACK_FILTER = CONTAINER_TYPES.register("digital_miner_itemstack_filter", DMItemStackFilterContainer::new);
    public static final ContainerTypeRegistryObject<LSItemStackFilterContainer> LS_ITEMSTACK_FILTER = CONTAINER_TYPES.register("logistical_sorter_itemstack_filter", LSItemStackFilterContainer::new);

    public static final ContainerTypeRegistryObject<OredictionificatorFilterContainer> OREDICTIONIFICATOR_FILTER = CONTAINER_TYPES.register("oredictionificator_filter", OredictionificatorFilterContainer::new);
}