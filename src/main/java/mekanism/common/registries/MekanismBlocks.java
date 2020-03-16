package mekanism.common.registries;

import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import mekanism.api.tier.ITier;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.Mekanism;
import mekanism.common.Resource;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockGasTank;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockSalt;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.basic.BlockBoilerCasing;
import mekanism.common.block.basic.BlockBoilerValve;
import mekanism.common.block.basic.BlockDynamicTank;
import mekanism.common.block.basic.BlockDynamicValve;
import mekanism.common.block.basic.BlockInductionCasing;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.block.basic.BlockInductionPort;
import mekanism.common.block.basic.BlockInductionProvider;
import mekanism.common.block.basic.BlockPressureDisperser;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.block.basic.BlockSecurityDesk;
import mekanism.common.block.basic.BlockSteelCasing;
import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.block.basic.BlockSuperheatingElement;
import mekanism.common.block.basic.BlockTeleporterFrame;
import mekanism.common.block.basic.BlockThermalEvaporation;
import mekanism.common.block.basic.BlockThermalEvaporationController;
import mekanism.common.block.basic.BlockThermalEvaporationValve;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.machine.BlockChargepad;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.block.machine.BlockFuelwoodHeater;
import mekanism.common.block.machine.BlockLaser;
import mekanism.common.block.machine.BlockLaserAmplifier;
import mekanism.common.block.machine.BlockLaserTractorBeam;
import mekanism.common.block.machine.BlockLogisticalSorter;
import mekanism.common.block.machine.BlockOredictionificator;
import mekanism.common.block.machine.BlockPersonalChest;
import mekanism.common.block.machine.BlockQuantumEntangloporter;
import mekanism.common.block.machine.BlockResistiveHeater;
import mekanism.common.block.machine.BlockSeismicVibrator;
import mekanism.common.block.machine.BlockSolarNeutronActivator;
import mekanism.common.block.machine.BlockTeleporter;
import mekanism.common.block.machine.prefab.BlockFactoryMachine;
import mekanism.common.block.machine.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.block.machine.prefab.BlockFactoryMachine.BlockFactoryMachineModel;
import mekanism.common.block.machine.prefab.BlockMachine;
import mekanism.common.block.machine.prefab.BlockMachine.BlockMachineModel;
import mekanism.common.block.transmitter.BlockDiversionTransporter;
import mekanism.common.block.transmitter.BlockLogisticalTransporter;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.block.transmitter.BlockPressurizedTube;
import mekanism.common.block.transmitter.BlockRestrictiveTransporter;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.block.transmitter.BlockUniversalCable;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.ItemBlockGasTank;
import mekanism.common.item.block.ItemBlockInductionCell;
import mekanism.common.item.block.ItemBlockInductionProvider;
import mekanism.common.item.block.ItemBlockResource;
import mekanism.common.item.block.ItemBlockSecurityDesk;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockChargepad;
import mekanism.common.item.block.machine.ItemBlockDigitalMiner;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.block.machine.ItemBlockFuelwoodHeater;
import mekanism.common.item.block.machine.ItemBlockLaser;
import mekanism.common.item.block.machine.ItemBlockLaserAmplifier;
import mekanism.common.item.block.machine.ItemBlockLaserTractorBeam;
import mekanism.common.item.block.machine.ItemBlockLogisticalSorter;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.item.block.machine.ItemBlockOredictionificator;
import mekanism.common.item.block.machine.ItemBlockPersonalChest;
import mekanism.common.item.block.machine.ItemBlockQuantumEntangloporter;
import mekanism.common.item.block.machine.ItemBlockResistiveHeater;
import mekanism.common.item.block.machine.ItemBlockSeismicVibrator;
import mekanism.common.item.block.machine.ItemBlockSolarNeutronActivator;
import mekanism.common.item.block.machine.ItemBlockTeleporter;
import mekanism.common.item.block.transmitter.ItemBlockDiversionTransporter;
import mekanism.common.item.block.transmitter.ItemBlockLogisticalTransporter;
import mekanism.common.item.block.transmitter.ItemBlockMechanicalPipe;
import mekanism.common.item.block.transmitter.ItemBlockPressurizedTube;
import mekanism.common.item.block.transmitter.ItemBlockRestrictiveTransporter;
import mekanism.common.item.block.transmitter.ItemBlockThermodynamicConductor;
import mekanism.common.item.block.transmitter.ItemBlockUniversalCable;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

//TODO: Ensure all IBlockMekanism's set the required information
public class MekanismBlocks {

    public static BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Mekanism.MODID);

    private static final Table<FactoryTier, FactoryType, BlockRegistryObject<BlockFactory<?>, ItemBlockFactory>> FACTORIES = HashBasedTable.create();

    static {
        for (FactoryTier tier : FactoryTier.values()) {
            for (FactoryType type : FactoryType.values()) {
                FACTORIES.put(tier, type, registerFactory(MekanismMachineTypes.getFactory(tier, type)));
            }
        }
    }

    public static final BlockRegistryObject<BlockResource, ItemBlockResource> OSMIUM_BLOCK = registerResourceBlock(BlockResourceInfo.OSMIUM);
    public static final BlockRegistryObject<BlockResource, ItemBlockResource> BRONZE_BLOCK = registerResourceBlock(BlockResourceInfo.BRONZE);
    public static final BlockRegistryObject<BlockResource, ItemBlockResource> REFINED_OBSIDIAN_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_OBSIDIAN);
    public static final BlockRegistryObject<BlockResource, ItemBlockResource> CHARCOAL_BLOCK = registerResourceBlock(BlockResourceInfo.CHARCOAL);
    public static final BlockRegistryObject<BlockResource, ItemBlockResource> REFINED_GLOWSTONE_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_GLOWSTONE);
    public static final BlockRegistryObject<BlockResource, ItemBlockResource> STEEL_BLOCK = registerResourceBlock(BlockResourceInfo.STEEL);
    public static final BlockRegistryObject<BlockResource, ItemBlockResource> COPPER_BLOCK = registerResourceBlock(BlockResourceInfo.COPPER);
    public static final BlockRegistryObject<BlockResource, ItemBlockResource> TIN_BLOCK = registerResourceBlock(BlockResourceInfo.TIN);

    public static final BlockRegistryObject<BlockBin, ItemBlockBin> BASIC_BIN = registerBin(BinTier.BASIC);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ADVANCED_BIN = registerBin(BinTier.ADVANCED);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ELITE_BIN = registerBin(BinTier.ELITE);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> ULTIMATE_BIN = registerBin(BinTier.ULTIMATE);
    public static final BlockRegistryObject<BlockBin, ItemBlockBin> CREATIVE_BIN = registerBin(BinTier.CREATIVE);

    public static final BlockRegistryObject<BlockTeleporterFrame, ItemBlockTooltip<BlockTeleporterFrame>> TELEPORTER_FRAME = registerBlock("teleporter_frame", BlockTeleporterFrame::new);
    public static final BlockRegistryObject<BlockSteelCasing, ItemBlockTooltip<BlockSteelCasing>> STEEL_CASING = registerBlock("steel_casing", BlockSteelCasing::new);
    public static final BlockRegistryObject<BlockDynamicTank, ItemBlockTooltip<BlockDynamicTank>> DYNAMIC_TANK = registerBlock("dynamic_tank", BlockDynamicTank::new);
    public static final BlockRegistryObject<BlockStructuralGlass, ItemBlockTooltip<BlockStructuralGlass>> STRUCTURAL_GLASS = registerBlock("structural_glass", BlockStructuralGlass::new);
    public static final BlockRegistryObject<BlockDynamicValve, ItemBlockTooltip<BlockDynamicValve>> DYNAMIC_VALVE = registerBlock("dynamic_valve", BlockDynamicValve::new);
    public static final BlockRegistryObject<BlockThermalEvaporationController, ItemBlockTooltip<BlockThermalEvaporationController>> THERMAL_EVAPORATION_CONTROLLER = registerBlock("thermal_evaporation_controller", BlockThermalEvaporationController::new);
    public static final BlockRegistryObject<BlockThermalEvaporationValve, ItemBlockTooltip<BlockThermalEvaporationValve>> THERMAL_EVAPORATION_VALVE = registerBlock("thermal_evaporation_valve", BlockThermalEvaporationValve::new);
    public static final BlockRegistryObject<BlockThermalEvaporation, ItemBlockTooltip<BlockThermalEvaporation>> THERMAL_EVAPORATION_BLOCK = registerBlock("thermal_evaporation_block", BlockThermalEvaporation::new);
    public static final BlockRegistryObject<BlockInductionCasing, ItemBlockTooltip<BlockInductionCasing>> INDUCTION_CASING = registerBlock("induction_casing", BlockInductionCasing::new);
    public static final BlockRegistryObject<BlockInductionPort, ItemBlockTooltip<BlockInductionPort>> INDUCTION_PORT = registerBlock("induction_port", BlockInductionPort::new);

    public static final BlockRegistryObject<BlockInductionCell, ItemBlockInductionCell> BASIC_INDUCTION_CELL = registerInductionCell(InductionCellTier.BASIC);
    public static final BlockRegistryObject<BlockInductionCell, ItemBlockInductionCell> ADVANCED_INDUCTION_CELL = registerInductionCell(InductionCellTier.ADVANCED);
    public static final BlockRegistryObject<BlockInductionCell, ItemBlockInductionCell> ELITE_INDUCTION_CELL = registerInductionCell(InductionCellTier.ELITE);
    public static final BlockRegistryObject<BlockInductionCell, ItemBlockInductionCell> ULTIMATE_INDUCTION_CELL = registerInductionCell(InductionCellTier.ULTIMATE);

    public static final BlockRegistryObject<BlockInductionProvider, ItemBlockInductionProvider> BASIC_INDUCTION_PROVIDER = registerInductionProvider(InductionProviderTier.BASIC);
    public static final BlockRegistryObject<BlockInductionProvider, ItemBlockInductionProvider> ADVANCED_INDUCTION_PROVIDER = registerInductionProvider(InductionProviderTier.ADVANCED);
    public static final BlockRegistryObject<BlockInductionProvider, ItemBlockInductionProvider> ELITE_INDUCTION_PROVIDER = registerInductionProvider(InductionProviderTier.ELITE);
    public static final BlockRegistryObject<BlockInductionProvider, ItemBlockInductionProvider> ULTIMATE_INDUCTION_PROVIDER = registerInductionProvider(InductionProviderTier.ULTIMATE);

    public static final BlockRegistryObject<BlockSuperheatingElement, ItemBlockTooltip<BlockSuperheatingElement>> SUPERHEATING_ELEMENT = registerBlock("superheating_element", BlockSuperheatingElement::new);
    public static final BlockRegistryObject<BlockPressureDisperser, ItemBlockTooltip<BlockPressureDisperser>> PRESSURE_DISPERSER = registerBlock("pressure_disperser", BlockPressureDisperser::new);
    public static final BlockRegistryObject<BlockBoilerCasing, ItemBlockTooltip<BlockBoilerCasing>> BOILER_CASING = registerBlock("boiler_casing", BlockBoilerCasing::new);
    public static final BlockRegistryObject<BlockBoilerValve, ItemBlockTooltip<BlockBoilerValve>> BOILER_VALVE = registerBlock("boiler_valve", BlockBoilerValve::new);
    public static final BlockRegistryObject<BlockSecurityDesk, ItemBlockSecurityDesk> SECURITY_DESK = BLOCKS.register("security_desk", BlockSecurityDesk::new, ItemBlockSecurityDesk::new);

    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnrichmentChamber, FactoryMachine<TileEntityEnrichmentChamber>>, ItemBlockMachine> ENRICHMENT_CHAMBER = BLOCKS.register("enrichment_chamber", () -> new BlockFactoryMachine<>(MekanismMachineTypes.ENRICHMENT_CHAMBER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityOsmiumCompressor, FactoryMachine<TileEntityOsmiumCompressor>>, ItemBlockMachine> OSMIUM_COMPRESSOR = BLOCKS.register("osmium_compressor", () -> new BlockFactoryMachine<>(MekanismMachineTypes.OSMIUM_COMPRESSOR), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCombiner, FactoryMachine<TileEntityCombiner>>, ItemBlockMachine> COMBINER = BLOCKS.register("combiner", () -> new BlockFactoryMachine<>(MekanismMachineTypes.COMBINER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCrusher, FactoryMachine<TileEntityCrusher>>, ItemBlockMachine> CRUSHER = BLOCKS.register("crusher", () -> new BlockFactoryMachine<>(MekanismMachineTypes.CRUSHER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>>, ItemBlockDigitalMiner> DIGITAL_MINER = BLOCKS.register("digital_miner", () -> new BlockMachineModel<>(MekanismMachineTypes.DIGITAL_MINER), ItemBlockDigitalMiner::new);

    public static final BlockRegistryObject<BlockFactoryMachineModel<TileEntityMetallurgicInfuser>, ItemBlockMachine> METALLURGIC_INFUSER = BLOCKS.register("metallurgic_infuser", () -> new BlockFactoryMachineModel<>(MekanismMachineTypes.METALLURGIC_INFUSER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPurificationChamber, FactoryMachine<TileEntityPurificationChamber>>, ItemBlockMachine> PURIFICATION_CHAMBER = BLOCKS.register("purification_chamber", () -> new BlockFactoryMachine<>(MekanismMachineTypes.PURIFICATION_CHAMBER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnergizedSmelter, FactoryMachine<TileEntityEnergizedSmelter>>, ItemBlockMachine> ENERGIZED_SMELTER = BLOCKS.register("energized_smelter", () -> new BlockFactoryMachine<>(MekanismMachineTypes.ENERGIZED_SMELTER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockTeleporter, ItemBlockTeleporter> TELEPORTER = BLOCKS.register("teleporter", BlockTeleporter::new, ItemBlockTeleporter::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityElectricPump, Machine<TileEntityElectricPump>>, ItemBlockMachine> ELECTRIC_PUMP = BLOCKS.register("electric_pump", () -> new BlockMachineModel<>(MekanismMachineTypes.ELECTRIC_PUMP), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockPersonalChest, ItemBlockPersonalChest> PERSONAL_CHEST = BLOCKS.register("personal_chest", BlockPersonalChest::new, ItemBlockPersonalChest::new);
    public static final BlockRegistryObject<BlockChargepad, ItemBlockChargepad> CHARGEPAD = BLOCKS.register("chargepad", BlockChargepad::new, ItemBlockChargepad::new);
    public static final BlockRegistryObject<BlockLogisticalSorter, ItemBlockLogisticalSorter> LOGISTICAL_SORTER = BLOCKS.register("logistical_sorter", BlockLogisticalSorter::new, ItemBlockLogisticalSorter::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityRotaryCondensentrator, Machine<TileEntityRotaryCondensentrator>>, ItemBlockMachine> ROTARY_CONDENSENTRATOR = BLOCKS.register("rotary_condensentrator", () -> new BlockMachineModel<>(MekanismMachineTypes.ROTARY_CONDENSENTRATOR), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityChemicalOxidizer, Machine<TileEntityChemicalOxidizer>>, ItemBlockMachine> CHEMICAL_OXIDIZER = BLOCKS.register("chemical_oxidizer", () -> new BlockMachineModel<>(MekanismMachineTypes.CHEMICAL_OXIDIZER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityChemicalInfuser, Machine<TileEntityChemicalInfuser>>, ItemBlockMachine> CHEMICAL_INFUSER = BLOCKS.register("chemical_infuser", () -> new BlockMachineModel<>(MekanismMachineTypes.CHEMICAL_INFUSER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityChemicalInjectionChamber, FactoryMachine<TileEntityChemicalInjectionChamber>>, ItemBlockMachine> CHEMICAL_INJECTION_CHAMBER = BLOCKS.register("chemical_injection_chamber", () -> new BlockFactoryMachine<>(MekanismMachineTypes.CHEMICAL_INJECTION_CHAMBER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityElectrolyticSeparator, Machine<TileEntityElectrolyticSeparator>>, ItemBlockMachine> ELECTROLYTIC_SEPARATOR = BLOCKS.register("electrolytic_separator", () -> new BlockMachineModel<>(MekanismMachineTypes.ELECTROLYTIC_SEPARATOR), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPrecisionSawmill, FactoryMachine<TileEntityPrecisionSawmill>>, ItemBlockMachine> PRECISION_SAWMILL = BLOCKS.register("precision_sawmill", () -> new BlockFactoryMachine<>(MekanismMachineTypes.PRECISION_SAWMILL), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityChemicalDissolutionChamber, Machine<TileEntityChemicalDissolutionChamber>>, ItemBlockMachine> CHEMICAL_DISSOLUTION_CHAMBER = BLOCKS.register("chemical_dissolution_chamber", () -> new BlockMachineModel<>(MekanismMachineTypes.CHEMICAL_DISSOLUTION_CHAMBER), (block) -> new ItemBlockMachine(block, ISTERProvider::dissolution));
    public static final BlockRegistryObject<BlockMachineModel<TileEntityChemicalWasher, Machine<TileEntityChemicalWasher>>, ItemBlockMachine> CHEMICAL_WASHER = BLOCKS.register("chemical_washer", () -> new BlockMachineModel<>(MekanismMachineTypes.CHEMICAL_WASHER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityChemicalCrystallizer, Machine<TileEntityChemicalCrystallizer>>, ItemBlockMachine> CHEMICAL_CRYSTALLIZER = BLOCKS.register("chemical_crystallizer", () -> new BlockMachineModel<>(MekanismMachineTypes.CHEMICAL_CRYSTALLIZER), (block) -> new ItemBlockMachine(block, ISTERProvider::crystallizer));
    public static final BlockRegistryObject<BlockSeismicVibrator, ItemBlockSeismicVibrator> SEISMIC_VIBRATOR = BLOCKS.register("seismic_vibrator", BlockSeismicVibrator::new, ItemBlockSeismicVibrator::new);
    public static final BlockRegistryObject<BlockMachineModel<TileEntityPressurizedReactionChamber, Machine<TileEntityPressurizedReactionChamber>>, ItemBlockMachine> PRESSURIZED_REACTION_CHAMBER = BLOCKS.register("pressurized_reaction_chamber", () -> new BlockMachineModel<>(MekanismMachineTypes.PRESSURIZED_REACTION_CHAMBER), ItemBlockMachine::new);

    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> BASIC_FLUID_TANK = registerFluidTank(FluidTankTier.BASIC);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ADVANCED_FLUID_TANK = registerFluidTank(FluidTankTier.ADVANCED);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ELITE_FLUID_TANK = registerFluidTank(FluidTankTier.ELITE);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ULTIMATE_FLUID_TANK = registerFluidTank(FluidTankTier.ULTIMATE);
    public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> CREATIVE_FLUID_TANK = registerFluidTank(FluidTankTier.CREATIVE);

    public static final BlockRegistryObject<BlockMachineModel<TileEntityFluidicPlenisher, Machine<TileEntityFluidicPlenisher>>, ItemBlockMachine> FLUIDIC_PLENISHER = BLOCKS.register("fluidic_plenisher", () -> new BlockMachineModel<>(MekanismMachineTypes.FLUIDIC_PLENISHER), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockLaser, ItemBlockLaser> LASER = BLOCKS.register("laser", BlockLaser::new, ItemBlockLaser::new);
    public static final BlockRegistryObject<BlockLaserAmplifier, ItemBlockLaserAmplifier> LASER_AMPLIFIER = BLOCKS.register("laser_amplifier", BlockLaserAmplifier::new, ItemBlockLaserAmplifier::new);
    public static final BlockRegistryObject<BlockLaserTractorBeam, ItemBlockLaserTractorBeam> LASER_TRACTOR_BEAM = BLOCKS.register("laser_tractor_beam", BlockLaserTractorBeam::new, ItemBlockLaserTractorBeam::new);
    public static final BlockRegistryObject<BlockQuantumEntangloporter, ItemBlockQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = BLOCKS.register("quantum_entangloporter", BlockQuantumEntangloporter::new, ItemBlockQuantumEntangloporter::new);
    public static final BlockRegistryObject<BlockSolarNeutronActivator, ItemBlockSolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = BLOCKS.register("solar_neutron_activator", BlockSolarNeutronActivator::new, ItemBlockSolarNeutronActivator::new);
    public static final BlockRegistryObject<BlockOredictionificator, ItemBlockOredictionificator> OREDICTIONIFICATOR = BLOCKS.register("oredictionificator", BlockOredictionificator::new, ItemBlockOredictionificator::new);
    public static final BlockRegistryObject<BlockResistiveHeater, ItemBlockResistiveHeater> RESISTIVE_HEATER = BLOCKS.register("resistive_heater", BlockResistiveHeater::new, ItemBlockResistiveHeater::new);
    public static final BlockRegistryObject<BlockMachine<TileEntityFormulaicAssemblicator, Machine<TileEntityFormulaicAssemblicator>>, ItemBlockMachine> FORMULAIC_ASSEMBLICATOR = BLOCKS.register("formulaic_assemblicator", () -> new BlockMachine<>(MekanismMachineTypes.FORMULAIC_ASSEMBLICATOR), ItemBlockMachine::new);
    public static final BlockRegistryObject<BlockFuelwoodHeater, ItemBlockFuelwoodHeater> FUELWOOD_HEATER = BLOCKS.register("fuelwood_heater", BlockFuelwoodHeater::new, ItemBlockFuelwoodHeater::new);

    public static final BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> OSMIUM_ORE = registerOre(Resource.OSMIUM);
    public static final BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> COPPER_ORE = registerOre(Resource.COPPER);
    public static final BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> TIN_ORE = registerOre(Resource.TIN);

    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> BASIC_ENERGY_CUBE = registerEnergyCube(EnergyCubeTier.BASIC);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ADVANCED_ENERGY_CUBE = registerEnergyCube(EnergyCubeTier.ADVANCED);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ELITE_ENERGY_CUBE = registerEnergyCube(EnergyCubeTier.ELITE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ULTIMATE_ENERGY_CUBE = registerEnergyCube(EnergyCubeTier.ULTIMATE);
    public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> CREATIVE_ENERGY_CUBE = registerEnergyCube(EnergyCubeTier.CREATIVE);

    public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> BASIC_UNIVERSAL_CABLE = registerUniversalCable(CableTier.BASIC);
    public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> ADVANCED_UNIVERSAL_CABLE = registerUniversalCable(CableTier.ADVANCED);
    public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> ELITE_UNIVERSAL_CABLE = registerUniversalCable(CableTier.ELITE);
    public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> ULTIMATE_UNIVERSAL_CABLE = registerUniversalCable(CableTier.ULTIMATE);

    public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> BASIC_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.BASIC);
    public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> ADVANCED_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.ADVANCED);
    public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> ELITE_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.ELITE);
    public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> ULTIMATE_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.ULTIMATE);

    public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> BASIC_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.BASIC);
    public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> ADVANCED_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.ADVANCED);
    public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> ELITE_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.ELITE);
    public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> ULTIMATE_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.ULTIMATE);

    public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.BASIC);
    public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.ADVANCED);
    public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.ELITE);
    public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.ULTIMATE);

    public static final BlockRegistryObject<BlockRestrictiveTransporter, ItemBlockRestrictiveTransporter> RESTRICTIVE_TRANSPORTER = BLOCKS.register("restrictive_transporter", BlockRestrictiveTransporter::new, ItemBlockRestrictiveTransporter::new);
    public static final BlockRegistryObject<BlockDiversionTransporter, ItemBlockDiversionTransporter> DIVERSION_TRANSPORTER = BLOCKS.register("diversion_transporter", BlockDiversionTransporter::new, ItemBlockDiversionTransporter::new);

    public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.BASIC);
    public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.ADVANCED);
    public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.ELITE);
    public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.ULTIMATE);

    public static final BlockRegistryObject<BlockBounding, BlockItem> BOUNDING_BLOCK = registerBoundingBlock("bounding_block", () -> new BlockBounding(false));
    public static final BlockRegistryObject<BlockBounding, BlockItem> ADVANCED_BOUNDING_BLOCK = registerBoundingBlock("advanced_bounding_block", () -> new BlockBounding(true));

    public static final BlockRegistryObject<BlockGasTank, ItemBlockGasTank> BASIC_GAS_TANK = registerGasTank(GasTankTier.BASIC);
    public static final BlockRegistryObject<BlockGasTank, ItemBlockGasTank> ADVANCED_GAS_TANK = registerGasTank(GasTankTier.ADVANCED);
    public static final BlockRegistryObject<BlockGasTank, ItemBlockGasTank> ELITE_GAS_TANK = registerGasTank(GasTankTier.ELITE);
    public static final BlockRegistryObject<BlockGasTank, ItemBlockGasTank> ULTIMATE_GAS_TANK = registerGasTank(GasTankTier.ULTIMATE);
    public static final BlockRegistryObject<BlockGasTank, ItemBlockGasTank> CREATIVE_GAS_TANK = registerGasTank(GasTankTier.CREATIVE);

    public static final BlockRegistryObject<BlockCardboardBox, ItemBlockCardboardBox> CARDBOARD_BOX = BLOCKS.register("cardboard_box", BlockCardboardBox::new, ItemBlockCardboardBox::new);
    //TODO: Tag Entry
    public static final BlockRegistryObject<BlockSalt, BlockItem> SALT_BLOCK = BLOCKS.register("block_salt", BlockSalt::new);

    private static BlockRegistryObject<BlockBounding, BlockItem> registerBoundingBlock(String name, Supplier<BlockBounding> blockSupplier) {
        return BLOCKS.register(name, blockSupplier, block -> new BlockItem(block, new Item.Properties().group(null)));
    }

    private static BlockRegistryObject<BlockResource, ItemBlockResource> registerResourceBlock(BlockResourceInfo resource) {
        return BLOCKS.register("block_" + resource.getRegistrySuffix(), () -> new BlockResource(resource), ItemBlockResource::new);
    }

    private static BlockRegistryObject<BlockBin, ItemBlockBin> registerBin(BinTier tier) {
        return registerTieredBlock(tier, "_bin", () -> new BlockBin(tier), ItemBlockBin::new);
    }

    private static BlockRegistryObject<BlockInductionCell, ItemBlockInductionCell> registerInductionCell(InductionCellTier tier) {
        return registerTieredBlock(tier, "_induction_cell", () -> new BlockInductionCell(tier), ItemBlockInductionCell::new);
    }

    private static BlockRegistryObject<BlockInductionProvider, ItemBlockInductionProvider> registerInductionProvider(InductionProviderTier tier) {
        return registerTieredBlock(tier, "_induction_provider", () -> new BlockInductionProvider(tier), ItemBlockInductionProvider::new);
    }

    private static BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> registerFluidTank(FluidTankTier tier) {
        return registerTieredBlock(tier, "_fluid_tank", () -> new BlockFluidTank(tier), ItemBlockFluidTank::new);
    }

    private static BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> registerEnergyCube(EnergyCubeTier tier) {
        return registerTieredBlock(tier, "_energy_cube", () -> new BlockEnergyCube(tier), ItemBlockEnergyCube::new);
    }

    private static BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> registerUniversalCable(CableTier tier) {
        return registerTieredBlock(tier, "_universal_cable", () -> new BlockUniversalCable(tier), ItemBlockUniversalCable::new);
    }

    private static BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> registerMechanicalPipe(PipeTier tier) {
        return registerTieredBlock(tier, "_mechanical_pipe", () -> new BlockMechanicalPipe(tier), ItemBlockMechanicalPipe::new);
    }

    private static BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> registerPressurizedTube(TubeTier tier) {
        return registerTieredBlock(tier, "_pressurized_tube", () -> new BlockPressurizedTube(tier), ItemBlockPressurizedTube::new);
    }

    private static BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> registerLogisticalTransporter(TransporterTier tier) {
        return registerTieredBlock(tier, "_logistical_transporter", () -> new BlockLogisticalTransporter(tier), ItemBlockLogisticalTransporter::new);
    }

    private static BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> registerThermodynamicConductor(ConductorTier tier) {
        return registerTieredBlock(tier, "_thermodynamic_conductor", () -> new BlockThermodynamicConductor(tier), ItemBlockThermodynamicConductor::new);
    }

    private static BlockRegistryObject<BlockGasTank, ItemBlockGasTank> registerGasTank(GasTankTier tier) {
        return registerTieredBlock(tier, "_gas_tank", () -> new BlockGasTank(tier), ItemBlockGasTank::new);
    }

    private static <TILE extends TileEntityFactory<?>> BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> registerFactory(Factory<TILE> type) {
        return registerTieredBlock(type.getTier(), "_" + type.get(AttributeFactoryType.class).getFactoryType().getRegistryNameComponent() + "_factory", () -> new BlockFactory<TILE>(type), ItemBlockFactory::new);
    }

    private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(ITier tier, String suffix,
          Supplier<? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator) {
        return BLOCKS.register(tier.getBaseTier().getLowerName() + suffix, blockSupplier, itemCreator);
    }

    private static BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> registerOre(Resource resource) {
        return BLOCKS.register(resource.getRegistrySuffix() + "_ore", () -> new BlockOre(resource), ItemBlockTooltip::new);
    }

    private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerBlock(String name, Supplier<? extends BLOCK> blockSupplier) {
        return BLOCKS.register(name, blockSupplier, ItemBlockTooltip::new);
    }

    /**
     * Retrieves a Factory with a defined tier and recipe type.
     *
     * @param tier - tier to add to the Factory
     * @param type - recipe type to add to the Factory
     *
     * @return factory with defined tier and recipe type
     */
    public static BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> getFactory(@Nonnull FactoryTier tier, @Nonnull FactoryType type) {
        return FACTORIES.get(tier, type);
    }

    @SuppressWarnings("unchecked")
    public static BlockRegistryObject<BlockFactory<?>, ItemBlockFactory>[] getFactoryBlocks() {
        return FACTORIES.values().toArray(new BlockRegistryObject[0]);
    }
}