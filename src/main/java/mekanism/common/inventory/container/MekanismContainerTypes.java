package mekanism.common.inventory.container;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismItem;
import mekanism.common.base.IItemProvider;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.inventory.container.tile.ChemicalCrystallizerContainer;
import mekanism.common.inventory.container.tile.ChemicalDissolutionChamberContainer;
import mekanism.common.inventory.container.tile.ChemicalInfuserContainer;
import mekanism.common.inventory.container.tile.ChemicalOxidizerContainer;
import mekanism.common.inventory.container.tile.ChemicalWasherContainer;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.inventory.container.tile.ElectricPumpContainer;
import mekanism.common.inventory.container.tile.ElectrolyticSeparatorContainer;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.inventory.container.tile.FluidicPlenisherContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.container.tile.FuelwoodHeaterContainer;
import mekanism.common.inventory.container.tile.GasTankContainer;
import mekanism.common.inventory.container.tile.LaserAmplifierContainer;
import mekanism.common.inventory.container.tile.LaserTractorBeamContainer;
import mekanism.common.inventory.container.tile.MetallurgicInfuserContainer;
import mekanism.common.inventory.container.tile.OredictionificatorContainer;
import mekanism.common.inventory.container.tile.PersonalChestTileContainer;
import mekanism.common.inventory.container.tile.PressurizedReactionChamberContainer;
import mekanism.common.inventory.container.tile.QuantumEntangloporterContainer;
import mekanism.common.inventory.container.tile.ResistiveHeaterContainer;
import mekanism.common.inventory.container.tile.RotaryCondensentratorContainer;
import mekanism.common.inventory.container.tile.SecurityDeskContainer;
import mekanism.common.inventory.container.tile.SeismicVibratorContainer;
import mekanism.common.inventory.container.tile.SolarNeutronActivatorContainer;
import mekanism.common.inventory.container.tile.TeleporterContainer;
import mekanism.common.inventory.container.tile.ThermalEvaporationControllerContainer;
import mekanism.common.inventory.container.tile.UpgradeManagementContainer;
import mekanism.common.inventory.container.tile.energy.EnergyCubeContainer;
import mekanism.common.inventory.container.tile.energy.InductionMatrixContainer;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.inventory.container.tile.fluid.DynamicTankContainer;
import mekanism.common.inventory.container.tile.fluid.FluidTankContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Go through each container and double check no copy paste error was made with what ContainerType the container is using
public class MekanismContainerTypes {

    private static final List<ContainerType<?>> types = new ArrayList<>();

    //Items
    public static final ContainerType<DictionaryContainer> DICTIONARY = create(MekanismItem.DICTIONARY, DictionaryContainer::new);

    //Entity
    public static final ContainerType<MainRobitContainer> MAIN_ROBIT = create("main_robit", MainRobitContainer::new);
    public static final ContainerType<InventoryRobitContainer> INVENTORY_ROBIT = create("inventory_robit", InventoryRobitContainer::new);
    public static final ContainerType<SmeltingRobitContainer> SMELTING_ROBIT = create("smelting_robit", SmeltingRobitContainer::new);


    //TODO: Decide if tiered ones should be done differently/evaluate how their container name is done
    //Blocks
    public static final ContainerType<ElectricPumpContainer> ELECTRIC_PUMP = create(MekanismBlock.ELECTRIC_PUMP, ElectricPumpContainer::new);
    public static final ContainerType<ElectrolyticSeparatorContainer> ELECTROLYTIC_SEPARATOR = create(MekanismBlock.ELECTROLYTIC_SEPARATOR, ElectrolyticSeparatorContainer::new);
    public static final ContainerType<OredictionificatorContainer> OREDICTIONIFICATOR = create(MekanismBlock.OREDICTIONIFICATOR, OredictionificatorContainer::new);
    public static final ContainerType<PressurizedReactionChamberContainer> PRESSURIZED_REACTION_CHAMBER = create(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, PressurizedReactionChamberContainer::new);
    public static final ContainerType<SecurityDeskContainer> SECURITY_DESK = create(MekanismBlock.SECURITY_DESK, SecurityDeskContainer::new);
    public static final ContainerType<LaserAmplifierContainer> LASER_AMPLIFIER = create(MekanismBlock.LASER_AMPLIFIER, LaserAmplifierContainer::new);
    public static final ContainerType<LaserTractorBeamContainer> LASER_TRACTOR_BEAM = create(MekanismBlock.LASER_TRACTOR_BEAM, LaserTractorBeamContainer::new);
    public static final ContainerType<ResistiveHeaterContainer> RESISTIVE_HEATER = create(MekanismBlock.RESISTIVE_HEATER, ResistiveHeaterContainer::new);
    public static final ContainerType<QuantumEntangloporterContainer> QUANTUM_ENTANGLOPORTER = create(MekanismBlock.QUANTUM_ENTANGLOPORTER, QuantumEntangloporterContainer::new);
    public static final ContainerType<ThermalEvaporationControllerContainer> THERMAL_EVAPORATION_CONTROLLER = create(MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, ThermalEvaporationControllerContainer::new);
    public static final ContainerType<SeismicVibratorContainer> SEISMIC_VIBRATOR = create(MekanismBlock.SEISMIC_VIBRATOR, SeismicVibratorContainer::new);
    public static final ContainerType<FuelwoodHeaterContainer> FUELWOOD_HEATER = create(MekanismBlock.FUELWOOD_HEATER, FuelwoodHeaterContainer::new);
    public static final ContainerType<FormulaicAssemblicatorContainer> FORMULAIC_ASSEMBLICATOR = create(MekanismBlock.FORMULAIC_ASSEMBLICATOR, FormulaicAssemblicatorContainer::new);
    public static final ContainerType<MetallurgicInfuserContainer> METALLURGIC_INFUSER = create(MekanismBlock.METALLURGIC_INFUSER, MetallurgicInfuserContainer::new);
    public static final ContainerType<SolarNeutronActivatorContainer> SOLAR_NEUTRON_ACTIVATOR = create(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, SolarNeutronActivatorContainer::new);
    public static final ContainerType<FluidicPlenisherContainer> FLUIDIC_PLENISHER = create(MekanismBlock.FLUIDIC_PLENISHER, FluidicPlenisherContainer::new);
    public static final ContainerType<DigitalMinerContainer> DIGITAL_MINER = create(MekanismBlock.DIGITAL_MINER, DigitalMinerContainer::new);
    public static final ContainerType<DynamicTankContainer> DYNAMIC_TANK = create(MekanismBlock.DYNAMIC_TANK, DynamicTankContainer::new);
    public static final ContainerType<RotaryCondensentratorContainer> ROTARY_CONDENSENTRATOR = create(MekanismBlock.ROTARY_CONDENSENTRATOR, RotaryCondensentratorContainer::new);
    public static final ContainerType<ChemicalCrystallizerContainer> CHEMICAL_CRYSTALLIZER = create(MekanismBlock.CHEMICAL_CRYSTALLIZER, ChemicalCrystallizerContainer::new);
    public static final ContainerType<ChemicalDissolutionChamberContainer> CHEMICAL_DISSOLUTION_CHAMBER = create(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, ChemicalDissolutionChamberContainer::new);
    public static final ContainerType<ChemicalInfuserContainer> CHEMICAL_INFUSER = create(MekanismBlock.CHEMICAL_INFUSER, ChemicalInfuserContainer::new);
    public static final ContainerType<ChemicalOxidizerContainer> CHEMICAL_OXIDIZER = create(MekanismBlock.CHEMICAL_OXIDIZER, ChemicalOxidizerContainer::new);
    public static final ContainerType<ChemicalWasherContainer> CHEMICAL_WASHER = create(MekanismBlock.CHEMICAL_WASHER, ChemicalWasherContainer::new);
    public static final ContainerType<TeleporterContainer> TELEPORTER = create(MekanismBlock.TELEPORTER, TeleporterContainer::new);

    //Named
    public static final ContainerType<FactoryContainer> FACTORY = create("factory", FactoryContainer::new);
    public static final ContainerType<GasTankContainer> GAS_TANK = create("gas_tank", GasTankContainer::new);
    public static final ContainerType<FluidTankContainer> FLUID_TANK = create("fluid_tank", FluidTankContainer::new);
    public static final ContainerType<EnergyCubeContainer> ENERGY_CUBE = create("energy_cube", EnergyCubeContainer::new);
    public static final ContainerType<InductionMatrixContainer> INDUCTION_MATRIX = create("induction_matrix", InductionMatrixContainer::new);
    public static final ContainerType<FilterContainer> FILTER = create("filter", FilterContainer::new);
    public static final ContainerType<UpgradeManagementContainer> UPGRADE_MANAGEMENT = create("upgrade_management", UpgradeManagementContainer::new);
    public static final ContainerType<PersonalChestItemContainer> PERSONAL_CHEST_ITEM = create("personal_chest_item", PersonalChestItemContainer::new);
    public static final ContainerType<PersonalChestTileContainer> PERSONAL_CHEST_BLOCK = create("personal_chest_block", PersonalChestTileContainer::new);





    //Can just use IItemProvider because IBlockProvider extends it. This way we support both tiles and items
    private static <T extends Container> ContainerType<T> create(IItemProvider provider, IContainerFactory<T> factory) {
        return create(provider.getRegistryName(), factory);
    }

    private static <T extends Container> ContainerType<T> create(String name, IContainerFactory<T> factory) {
        return create(new ResourceLocation(Mekanism.MODID, name), factory);
    }

    private static <T extends Container> ContainerType<T> create(ResourceLocation registryName, IContainerFactory<T> factory) {
        ContainerType<T> type = IForgeContainerType.create(factory);
        type.setRegistryName(registryName);
        types.add(type);
        return type;
    }

    public static void registerContainers(IForgeRegistry<ContainerType<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}