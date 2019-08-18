package mekanism.common;

import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IBlockProvider;
import mekanism.api.block.FactoryType;
import mekanism.api.block.IHasFactoryType;
import mekanism.api.text.EnumColor;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockGasTank;
import mekanism.common.block.BlockGlowPanel;
import mekanism.common.block.BlockObsidianTNT;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockSalt;
import mekanism.common.block.PortalHelper;
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
import mekanism.common.block.machine.BlockChargepad;
import mekanism.common.block.machine.BlockChemicalCrystallizer;
import mekanism.common.block.machine.BlockChemicalDissolutionChamber;
import mekanism.common.block.machine.BlockChemicalInfuser;
import mekanism.common.block.machine.BlockChemicalInjectionChamber;
import mekanism.common.block.machine.BlockChemicalOxidizer;
import mekanism.common.block.machine.BlockChemicalWasher;
import mekanism.common.block.machine.BlockCombiner;
import mekanism.common.block.machine.BlockCrusher;
import mekanism.common.block.machine.BlockDigitalMiner;
import mekanism.common.block.machine.BlockElectricPump;
import mekanism.common.block.machine.BlockElectrolyticSeparator;
import mekanism.common.block.machine.BlockEnergizedSmelter;
import mekanism.common.block.machine.BlockEnrichmentChamber;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.block.machine.BlockFluidicPlenisher;
import mekanism.common.block.machine.BlockFormulaicAssemblicator;
import mekanism.common.block.machine.BlockFuelwoodHeater;
import mekanism.common.block.machine.BlockLaser;
import mekanism.common.block.machine.BlockLaserAmplifier;
import mekanism.common.block.machine.BlockLaserTractorBeam;
import mekanism.common.block.machine.BlockLogisticalSorter;
import mekanism.common.block.machine.BlockMetallurgicInfuser;
import mekanism.common.block.machine.BlockOredictionificator;
import mekanism.common.block.machine.BlockOsmiumCompressor;
import mekanism.common.block.machine.BlockPersonalChest;
import mekanism.common.block.machine.BlockPrecisionSawmill;
import mekanism.common.block.machine.BlockPressurizedReactionChamber;
import mekanism.common.block.machine.BlockPurificationChamber;
import mekanism.common.block.machine.BlockQuantumEntangloporter;
import mekanism.common.block.machine.BlockResistiveHeater;
import mekanism.common.block.machine.BlockRotaryCondensentrator;
import mekanism.common.block.machine.BlockSeismicVibrator;
import mekanism.common.block.machine.BlockSolarNeutronActivator;
import mekanism.common.block.machine.BlockTeleporter;
import mekanism.common.block.machine.factory.BlockFactory;
import mekanism.common.block.plastic.BlockPlastic;
import mekanism.common.block.plastic.BlockPlasticFence;
import mekanism.common.block.plastic.BlockPlasticGlow;
import mekanism.common.block.plastic.BlockPlasticReinforced;
import mekanism.common.block.plastic.BlockPlasticRoad;
import mekanism.common.block.plastic.BlockPlasticSlick;
import mekanism.common.block.transmitter.BlockDiversionTransporter;
import mekanism.common.block.transmitter.BlockLogisticalTransporter;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.block.transmitter.BlockPressurizedTube;
import mekanism.common.block.transmitter.BlockRestrictiveTransporter;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.block.transmitter.BlockUniversalCable;
import mekanism.common.item.IItemMekanism;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.ItemBlockGasTank;
import mekanism.common.item.block.ItemBlockGlowPanel;
import mekanism.common.item.block.ItemBlockInductionCell;
import mekanism.common.item.block.ItemBlockInductionProvider;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.item.block.ItemBlockResource;
import mekanism.common.item.block.ItemBlockSecurityDesk;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockChargepad;
import mekanism.common.item.block.machine.ItemBlockChemicalCrystallizer;
import mekanism.common.item.block.machine.ItemBlockChemicalDissolutionChamber;
import mekanism.common.item.block.machine.ItemBlockChemicalInfuser;
import mekanism.common.item.block.machine.ItemBlockChemicalInjectionChamber;
import mekanism.common.item.block.machine.ItemBlockChemicalOxidizer;
import mekanism.common.item.block.machine.ItemBlockChemicalWasher;
import mekanism.common.item.block.machine.ItemBlockCombiner;
import mekanism.common.item.block.machine.ItemBlockCrusher;
import mekanism.common.item.block.machine.ItemBlockDigitalMiner;
import mekanism.common.item.block.machine.ItemBlockElectricPump;
import mekanism.common.item.block.machine.ItemBlockElectrolyticSeparator;
import mekanism.common.item.block.machine.ItemBlockEnergizedSmelter;
import mekanism.common.item.block.machine.ItemBlockEnrichmentChamber;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.block.machine.ItemBlockFluidicPlenisher;
import mekanism.common.item.block.machine.ItemBlockFormulaicAssemblicator;
import mekanism.common.item.block.machine.ItemBlockFuelwoodHeater;
import mekanism.common.item.block.machine.ItemBlockLaser;
import mekanism.common.item.block.machine.ItemBlockLaserAmplifier;
import mekanism.common.item.block.machine.ItemBlockLaserTractorBeam;
import mekanism.common.item.block.machine.ItemBlockLogisticalSorter;
import mekanism.common.item.block.machine.ItemBlockMetallurgicInfuser;
import mekanism.common.item.block.machine.ItemBlockOredictionificator;
import mekanism.common.item.block.machine.ItemBlockOsmiumCompressor;
import mekanism.common.item.block.machine.ItemBlockPersonalChest;
import mekanism.common.item.block.machine.ItemBlockPrecisionSawmill;
import mekanism.common.item.block.machine.ItemBlockPressurizedReactionChamber;
import mekanism.common.item.block.machine.ItemBlockPurificationChamber;
import mekanism.common.item.block.machine.ItemBlockQuantumEntangloporter;
import mekanism.common.item.block.machine.ItemBlockResistiveHeater;
import mekanism.common.item.block.machine.ItemBlockRotaryCondensentrator;
import mekanism.common.item.block.machine.ItemBlockSeismicVibrator;
import mekanism.common.item.block.machine.ItemBlockSolarNeutronActivator;
import mekanism.common.item.block.machine.ItemBlockTeleporter;
import mekanism.common.item.block.machine.factory.ItemBlockFactory;
import mekanism.common.item.block.plastic.ItemBlockPlastic;
import mekanism.common.item.block.plastic.ItemBlockPlasticFence;
import mekanism.common.item.block.plastic.ItemBlockPlasticGlow;
import mekanism.common.item.block.plastic.ItemBlockPlasticReinforced;
import mekanism.common.item.block.plastic.ItemBlockPlasticRoad;
import mekanism.common.item.block.plastic.ItemBlockPlasticSlick;
import mekanism.common.item.block.transmitter.ItemBlockDiversionTransporter;
import mekanism.common.item.block.transmitter.ItemBlockLogisticalTransporter;
import mekanism.common.item.block.transmitter.ItemBlockMechanicalPipe;
import mekanism.common.item.block.transmitter.ItemBlockPressurizedTube;
import mekanism.common.item.block.transmitter.ItemBlockRestrictiveTransporter;
import mekanism.common.item.block.transmitter.ItemBlockThermodynamicConductor;
import mekanism.common.item.block.transmitter.ItemBlockUniversalCable;
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
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Ensure all IBlockMekanism's set the required information
public enum MekanismBlock implements IBlockProvider {
    OSMIUM_BLOCK(new BlockResource(BlockResourceInfo.OSMIUM), ItemBlockResource::new),
    BRONZE_BLOCK(new BlockResource(BlockResourceInfo.BRONZE), ItemBlockResource::new),
    REFINED_OBSIDIAN_BLOCK(new BlockResource(BlockResourceInfo.REFINED_OBSIDIAN), ItemBlockResource::new),
    CHARCOAL_BLOCK(new BlockResource(BlockResourceInfo.CHARCOAL), ItemBlockResource::new),
    REFINED_GLOWSTONE_BLOCK(new BlockResource(BlockResourceInfo.REFINED_GLOWSTONE), ItemBlockResource::new),
    STEEL_BLOCK(new BlockResource(BlockResourceInfo.STEEL), ItemBlockResource::new),

    BASIC_BIN(new BlockBin(BinTier.BASIC), ItemBlockBin::new),
    ADVANCED_BIN(new BlockBin(BinTier.ADVANCED), ItemBlockBin::new),
    ELITE_BIN(new BlockBin(BinTier.ELITE), ItemBlockBin::new),
    ULTIMATE_BIN(new BlockBin(BinTier.ULTIMATE), ItemBlockBin::new),
    CREATIVE_BIN(new BlockBin(BinTier.CREATIVE), ItemBlockBin::new),

    TELEPORTER_FRAME(new BlockTeleporterFrame(), ItemBlockTooltip::new),
    STEEL_CASING(new BlockSteelCasing(), ItemBlockTooltip::new),
    DYNAMIC_TANK(new BlockDynamicTank(), ItemBlockTooltip::new),
    STRUCTURAL_GLASS(new BlockStructuralGlass(), ItemBlockTooltip::new),
    DYNAMIC_VALVE(new BlockDynamicValve(), ItemBlockTooltip::new),
    COPPER_BLOCK(new BlockResource(BlockResourceInfo.COPPER), ItemBlockResource::new),
    TIN_BLOCK(new BlockResource(BlockResourceInfo.TIN), ItemBlockResource::new),
    THERMAL_EVAPORATION_CONTROLLER(new BlockThermalEvaporationController(), ItemBlockTooltip::new),
    THERMAL_EVAPORATION_VALVE(new BlockThermalEvaporationValve(), ItemBlockTooltip::new),

    THERMAL_EVAPORATION_BLOCK(new BlockThermalEvaporation(), ItemBlockTooltip::new),
    INDUCTION_CASING(new BlockInductionCasing(), ItemBlockTooltip::new),
    INDUCTION_PORT(new BlockInductionPort(), ItemBlockTooltip::new),

    BASIC_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.BASIC), ItemBlockInductionCell::new),
    ADVANCED_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.ADVANCED), ItemBlockInductionCell::new),
    ELITE_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.ELITE), ItemBlockInductionCell::new),
    ULTIMATE_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.ULTIMATE), ItemBlockInductionCell::new),

    BASIC_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.BASIC), ItemBlockInductionProvider::new),
    ADVANCED_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.ADVANCED), ItemBlockInductionProvider::new),
    ELITE_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.ELITE), ItemBlockInductionProvider::new),
    ULTIMATE_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.ULTIMATE), ItemBlockInductionProvider::new),

    SUPERHEATING_ELEMENT(new BlockSuperheatingElement(), ItemBlockTooltip::new),
    PRESSURE_DISPERSER(new BlockPressureDisperser(), ItemBlockTooltip::new),
    BOILER_CASING(new BlockBoilerCasing(), ItemBlockTooltip::new),
    BOILER_VALVE(new BlockBoilerValve(), ItemBlockTooltip::new),
    SECURITY_DESK(new BlockSecurityDesk(), ItemBlockSecurityDesk::new),

    ENRICHMENT_CHAMBER(new BlockEnrichmentChamber(), ItemBlockEnrichmentChamber::new),
    OSMIUM_COMPRESSOR(new BlockOsmiumCompressor(), ItemBlockOsmiumCompressor::new),
    COMBINER(new BlockCombiner(), ItemBlockCombiner::new),
    CRUSHER(new BlockCrusher(), ItemBlockCrusher::new),
    DIGITAL_MINER(new BlockDigitalMiner(), ItemBlockDigitalMiner::new),

    BASIC_SMELTING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.SMELTING), ItemBlockFactory::new),
    BASIC_ENRICHING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.ENRICHING), ItemBlockFactory::new),
    BASIC_CRUSHING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.CRUSHING), ItemBlockFactory::new),
    BASIC_COMPRESSING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.COMPRESSING), ItemBlockFactory::new),
    BASIC_COMBINING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.COMBINING), ItemBlockFactory::new),
    BASIC_PURIFYING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.PURIFYING), ItemBlockFactory::new),
    BASIC_INJECTING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.INJECTING), ItemBlockFactory::new),
    BASIC_INFUSING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.INFUSING), ItemBlockFactory::new),
    BASIC_SAWING_FACTORY(new BlockFactory(FactoryTier.BASIC, FactoryType.SAWING), ItemBlockFactory::new),

    ADVANCED_SMELTING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.SMELTING), ItemBlockFactory::new),
    ADVANCED_ENRICHING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.ENRICHING), ItemBlockFactory::new),
    ADVANCED_CRUSHING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.CRUSHING), ItemBlockFactory::new),
    ADVANCED_COMPRESSING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.COMPRESSING), ItemBlockFactory::new),
    ADVANCED_COMBINING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.COMBINING), ItemBlockFactory::new),
    ADVANCED_PURIFYING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.PURIFYING), ItemBlockFactory::new),
    ADVANCED_INJECTING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.INJECTING), ItemBlockFactory::new),
    ADVANCED_INFUSING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.INFUSING), ItemBlockFactory::new),
    ADVANCED_SAWING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, FactoryType.SAWING), ItemBlockFactory::new),

    ELITE_SMELTING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.SMELTING), ItemBlockFactory::new),
    ELITE_ENRICHING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.ENRICHING), ItemBlockFactory::new),
    ELITE_CRUSHING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.CRUSHING), ItemBlockFactory::new),
    ELITE_COMPRESSING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.COMPRESSING), ItemBlockFactory::new),
    ELITE_COMBINING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.COMBINING), ItemBlockFactory::new),
    ELITE_PURIFYING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.PURIFYING), ItemBlockFactory::new),
    ELITE_INJECTING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.INJECTING), ItemBlockFactory::new),
    ELITE_INFUSING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.INFUSING), ItemBlockFactory::new),
    ELITE_SAWING_FACTORY(new BlockFactory(FactoryTier.ELITE, FactoryType.SAWING), ItemBlockFactory::new),

    METALLURGIC_INFUSER(new BlockMetallurgicInfuser(), ItemBlockMetallurgicInfuser::new),
    PURIFICATION_CHAMBER(new BlockPurificationChamber(), ItemBlockPurificationChamber::new),
    ENERGIZED_SMELTER(new BlockEnergizedSmelter(), ItemBlockEnergizedSmelter::new),
    TELEPORTER(new BlockTeleporter(), ItemBlockTeleporter::new),
    ELECTRIC_PUMP(new BlockElectricPump(), ItemBlockElectricPump::new),
    PERSONAL_CHEST(new BlockPersonalChest(), ItemBlockPersonalChest::new),
    CHARGEPAD(new BlockChargepad(), ItemBlockChargepad::new),
    LOGISTICAL_SORTER(new BlockLogisticalSorter(), ItemBlockLogisticalSorter::new),
    ROTARY_CONDENSENTRATOR(new BlockRotaryCondensentrator(), ItemBlockRotaryCondensentrator::new),
    CHEMICAL_OXIDIZER(new BlockChemicalOxidizer(), ItemBlockChemicalOxidizer::new),
    CHEMICAL_INFUSER(new BlockChemicalInfuser(), ItemBlockChemicalInfuser::new),
    CHEMICAL_INJECTION_CHAMBER(new BlockChemicalInjectionChamber(), ItemBlockChemicalInjectionChamber::new),
    ELECTROLYTIC_SEPARATOR(new BlockElectrolyticSeparator(), ItemBlockElectrolyticSeparator::new),
    PRECISION_SAWMILL(new BlockPrecisionSawmill(), ItemBlockPrecisionSawmill::new),
    CHEMICAL_DISSOLUTION_CHAMBER(new BlockChemicalDissolutionChamber(), ItemBlockChemicalDissolutionChamber::new),
    CHEMICAL_WASHER(new BlockChemicalWasher(), ItemBlockChemicalWasher::new),
    CHEMICAL_CRYSTALLIZER(new BlockChemicalCrystallizer(), ItemBlockChemicalCrystallizer::new),
    SEISMIC_VIBRATOR(new BlockSeismicVibrator(), ItemBlockSeismicVibrator::new),
    PRESSURIZED_REACTION_CHAMBER(new BlockPressurizedReactionChamber(), ItemBlockPressurizedReactionChamber::new),

    BASIC_FLUID_TANK(new BlockFluidTank(FluidTankTier.BASIC), ItemBlockFluidTank::new),
    ADVANCED_FLUID_TANK(new BlockFluidTank(FluidTankTier.ADVANCED), ItemBlockFluidTank::new),
    ELITE_FLUID_TANK(new BlockFluidTank(FluidTankTier.ELITE), ItemBlockFluidTank::new),
    ULTIMATE_FLUID_TANK(new BlockFluidTank(FluidTankTier.ULTIMATE), ItemBlockFluidTank::new),
    CREATIVE_FLUID_TANK(new BlockFluidTank(FluidTankTier.CREATIVE), ItemBlockFluidTank::new),

    FLUIDIC_PLENISHER(new BlockFluidicPlenisher(), ItemBlockFluidicPlenisher::new),
    LASER(new BlockLaser(), ItemBlockLaser::new),
    LASER_AMPLIFIER(new BlockLaserAmplifier(), ItemBlockLaserAmplifier::new),
    LASER_TRACTOR_BEAM(new BlockLaserTractorBeam(), ItemBlockLaserTractorBeam::new),
    QUANTUM_ENTANGLOPORTER(new BlockQuantumEntangloporter(), ItemBlockQuantumEntangloporter::new),
    SOLAR_NEUTRON_ACTIVATOR(new BlockSolarNeutronActivator(), ItemBlockSolarNeutronActivator::new),
    OREDICTIONIFICATOR(new BlockOredictionificator(), ItemBlockOredictionificator::new),
    RESISTIVE_HEATER(new BlockResistiveHeater(), ItemBlockResistiveHeater::new),
    FORMULAIC_ASSEMBLICATOR(new BlockFormulaicAssemblicator(), ItemBlockFormulaicAssemblicator::new),
    FUELWOOD_HEATER(new BlockFuelwoodHeater(), ItemBlockFuelwoodHeater::new),

    OSMIUM_ORE(new BlockOre(Resource.OSMIUM), ItemBlockTooltip::new),
    COPPER_ORE(new BlockOre(Resource.COPPER), ItemBlockTooltip::new),
    TIN_ORE(new BlockOre(Resource.TIN), ItemBlockTooltip::new),

    BASIC_ENERGY_CUBE(new BlockEnergyCube(EnergyCubeTier.BASIC), ItemBlockEnergyCube::new),
    ADVANCED_ENERGY_CUBE(new BlockEnergyCube(EnergyCubeTier.ADVANCED), ItemBlockEnergyCube::new),
    ELITE_ENERGY_CUBE(new BlockEnergyCube(EnergyCubeTier.ELITE), ItemBlockEnergyCube::new),
    ULTIMATE_ENERGY_CUBE(new BlockEnergyCube(EnergyCubeTier.ULTIMATE), ItemBlockEnergyCube::new),
    CREATIVE_ENERGY_CUBE(new BlockEnergyCube(EnergyCubeTier.CREATIVE), ItemBlockEnergyCube::new),

    BASIC_UNIVERSAL_CABLE(new BlockUniversalCable(CableTier.BASIC), ItemBlockUniversalCable::new),
    ADVANCED_UNIVERSAL_CABLE(new BlockUniversalCable(CableTier.ADVANCED), ItemBlockUniversalCable::new),
    ELITE_UNIVERSAL_CABLE(new BlockUniversalCable(CableTier.ELITE), ItemBlockUniversalCable::new),
    ULTIMATE_UNIVERSAL_CABLE(new BlockUniversalCable(CableTier.ULTIMATE), ItemBlockUniversalCable::new),

    BASIC_MECHANICAL_PIPE(new BlockMechanicalPipe(PipeTier.BASIC), ItemBlockMechanicalPipe::new),
    ADVANCED_MECHANICAL_PIPE(new BlockMechanicalPipe(PipeTier.ADVANCED), ItemBlockMechanicalPipe::new),
    ELITE_MECHANICAL_PIPE(new BlockMechanicalPipe(PipeTier.ELITE), ItemBlockMechanicalPipe::new),
    ULTIMATE_MECHANICAL_PIPE(new BlockMechanicalPipe(PipeTier.ULTIMATE), ItemBlockMechanicalPipe::new),

    BASIC_PRESSURIZED_TUBE(new BlockPressurizedTube(TubeTier.BASIC), ItemBlockPressurizedTube::new),
    ADVANCED_PRESSURIZED_TUBE(new BlockPressurizedTube(TubeTier.ADVANCED), ItemBlockPressurizedTube::new),
    ELITE_PRESSURIZED_TUBE(new BlockPressurizedTube(TubeTier.ELITE), ItemBlockPressurizedTube::new),
    ULTIMATE_PRESSURIZED_TUBE(new BlockPressurizedTube(TubeTier.ULTIMATE), ItemBlockPressurizedTube::new),

    BASIC_LOGISTICAL_TRANSPORTER(new BlockLogisticalTransporter(TransporterTier.BASIC), ItemBlockLogisticalTransporter::new),
    ADVANCED_LOGISTICAL_TRANSPORTER(new BlockLogisticalTransporter(TransporterTier.ADVANCED), ItemBlockLogisticalTransporter::new),
    ELITE_LOGISTICAL_TRANSPORTER(new BlockLogisticalTransporter(TransporterTier.ELITE), ItemBlockLogisticalTransporter::new),
    ULTIMATE_LOGISTICAL_TRANSPORTER(new BlockLogisticalTransporter(TransporterTier.ULTIMATE), ItemBlockLogisticalTransporter::new),

    RESTRICTIVE_TRANSPORTER(new BlockRestrictiveTransporter(), ItemBlockRestrictiveTransporter::new),
    DIVERSION_TRANSPORTER(new BlockDiversionTransporter(), ItemBlockDiversionTransporter::new),

    BASIC_THERMODYNAMIC_CONDUCTOR(new BlockThermodynamicConductor(ConductorTier.BASIC), ItemBlockThermodynamicConductor::new),
    ADVANCED_THERMODYNAMIC_CONDUCTOR(new BlockThermodynamicConductor(ConductorTier.ADVANCED), ItemBlockThermodynamicConductor::new),
    ELITE_THERMODYNAMIC_CONDUCTOR(new BlockThermodynamicConductor(ConductorTier.ELITE), ItemBlockThermodynamicConductor::new),
    ULTIMATE_THERMODYNAMIC_CONDUCTOR(new BlockThermodynamicConductor(ConductorTier.ULTIMATE), ItemBlockThermodynamicConductor::new),

    OBSIDIAN_TNT(new BlockObsidianTNT()),
    BOUNDING_BLOCK(new BlockBounding(false)),
    ADVANCED_BOUNDING_BLOCK(new BlockBounding(true)),

    BASIC_GAS_TANK(new BlockGasTank(GasTankTier.BASIC), ItemBlockGasTank::new),
    ADVANCED_GAS_TANK(new BlockGasTank(GasTankTier.ADVANCED), ItemBlockGasTank::new),
    ELITE_GAS_TANK(new BlockGasTank(GasTankTier.ELITE), ItemBlockGasTank::new),
    ULTIMATE_GAS_TANK(new BlockGasTank(GasTankTier.ULTIMATE), ItemBlockGasTank::new),
    CREATIVE_GAS_TANK(new BlockGasTank(GasTankTier.CREATIVE), ItemBlockGasTank::new),

    CARDBOARD_BOX(new BlockCardboardBox(), ItemBlockCardboardBox::new),

    BLACK_GLOW_PANEL(new BlockGlowPanel(EnumColor.BLACK), ItemBlockGlowPanel::new),
    RED_GLOW_PANEL(new BlockGlowPanel(EnumColor.RED), ItemBlockGlowPanel::new),
    GREEN_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_GREEN), ItemBlockGlowPanel::new),
    BROWN_GLOW_PANEL(new BlockGlowPanel(EnumColor.BROWN), ItemBlockGlowPanel::new),
    BLUE_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_BLUE), ItemBlockGlowPanel::new),
    PURPLE_GLOW_PANEL(new BlockGlowPanel(EnumColor.PURPLE), ItemBlockGlowPanel::new),
    CYAN_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_AQUA), ItemBlockGlowPanel::new),
    LIGHT_GRAY_GLOW_PANEL(new BlockGlowPanel(EnumColor.GRAY), ItemBlockGlowPanel::new),
    GRAY_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_GRAY), ItemBlockGlowPanel::new),
    PINK_GLOW_PANEL(new BlockGlowPanel(EnumColor.BRIGHT_PINK), ItemBlockGlowPanel::new),
    LIME_GLOW_PANEL(new BlockGlowPanel(EnumColor.BRIGHT_GREEN), ItemBlockGlowPanel::new),
    YELLOW_GLOW_PANEL(new BlockGlowPanel(EnumColor.YELLOW), ItemBlockGlowPanel::new),
    LIGHT_BLUE_GLOW_PANEL(new BlockGlowPanel(EnumColor.INDIGO), ItemBlockGlowPanel::new),
    MAGENTA_GLOW_PANEL(new BlockGlowPanel(EnumColor.PINK), ItemBlockGlowPanel::new),
    ORANGE_GLOW_PANEL(new BlockGlowPanel(EnumColor.ORANGE), ItemBlockGlowPanel::new),
    WHITE_GLOW_PANEL(new BlockGlowPanel(EnumColor.WHITE), ItemBlockGlowPanel::new),

    BLACK_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BLACK), ItemBlockPlastic::new),
    RED_PLASTIC_BLOCK(new BlockPlastic(EnumColor.RED), ItemBlockPlastic::new),
    GREEN_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_GREEN), ItemBlockPlastic::new),
    BROWN_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BROWN), ItemBlockPlastic::new),
    BLUE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_BLUE), ItemBlockPlastic::new),
    PURPLE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.PURPLE), ItemBlockPlastic::new),
    CYAN_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_AQUA), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_BLOCK(new BlockPlastic(EnumColor.GRAY), ItemBlockPlastic::new),
    GRAY_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_GRAY), ItemBlockPlastic::new),
    PINK_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BRIGHT_PINK), ItemBlockPlastic::new),
    LIME_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BRIGHT_GREEN), ItemBlockPlastic::new),
    YELLOW_PLASTIC_BLOCK(new BlockPlastic(EnumColor.YELLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.INDIGO), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_BLOCK(new BlockPlastic(EnumColor.PINK), ItemBlockPlastic::new),
    ORANGE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.ORANGE), ItemBlockPlastic::new),
    WHITE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.WHITE), ItemBlockPlastic::new),

    BLACK_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BLACK), ItemBlockPlasticSlick::new),
    RED_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.RED), ItemBlockPlasticSlick::new),
    GREEN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_GREEN), ItemBlockPlasticSlick::new),
    BROWN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BROWN), ItemBlockPlasticSlick::new),
    BLUE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_BLUE), ItemBlockPlasticSlick::new),
    PURPLE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.PURPLE), ItemBlockPlasticSlick::new),
    CYAN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_AQUA), ItemBlockPlasticSlick::new),
    LIGHT_GRAY_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.GRAY), ItemBlockPlasticSlick::new),
    GRAY_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_GRAY), ItemBlockPlasticSlick::new),
    PINK_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BRIGHT_PINK), ItemBlockPlasticSlick::new),
    LIME_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BRIGHT_GREEN), ItemBlockPlasticSlick::new),
    YELLOW_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.YELLOW), ItemBlockPlasticSlick::new),
    LIGHT_BLUE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.INDIGO), ItemBlockPlasticSlick::new),
    MAGENTA_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.PINK), ItemBlockPlasticSlick::new),
    ORANGE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.ORANGE), ItemBlockPlasticSlick::new),
    WHITE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.WHITE), ItemBlockPlasticSlick::new),

    BLACK_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BLACK), ItemBlockPlasticGlow::new),
    RED_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.RED), ItemBlockPlasticGlow::new),
    GREEN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_GREEN), ItemBlockPlasticGlow::new),
    BROWN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BROWN), ItemBlockPlasticGlow::new),
    BLUE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_BLUE), ItemBlockPlasticGlow::new),
    PURPLE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.PURPLE), ItemBlockPlasticGlow::new),
    CYAN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_AQUA), ItemBlockPlasticGlow::new),
    LIGHT_GRAY_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.GRAY), ItemBlockPlasticGlow::new),
    GRAY_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_GRAY), ItemBlockPlasticGlow::new),
    PINK_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BRIGHT_PINK), ItemBlockPlasticGlow::new),
    LIME_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BRIGHT_GREEN), ItemBlockPlasticGlow::new),
    YELLOW_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.YELLOW), ItemBlockPlasticGlow::new),
    LIGHT_BLUE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.INDIGO), ItemBlockPlasticGlow::new),
    MAGENTA_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.PINK), ItemBlockPlasticGlow::new),
    ORANGE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.ORANGE), ItemBlockPlasticGlow::new),
    WHITE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.WHITE), ItemBlockPlasticGlow::new),

    BLACK_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BLACK), ItemBlockPlasticReinforced::new),
    RED_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.RED), ItemBlockPlasticReinforced::new),
    GREEN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_GREEN), ItemBlockPlasticReinforced::new),
    BROWN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BROWN), ItemBlockPlasticReinforced::new),
    BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_BLUE), ItemBlockPlasticReinforced::new),
    PURPLE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.PURPLE), ItemBlockPlasticReinforced::new),
    CYAN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_AQUA), ItemBlockPlasticReinforced::new),
    LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.GRAY), ItemBlockPlasticReinforced::new),
    GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_GRAY), ItemBlockPlasticReinforced::new),
    PINK_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BRIGHT_PINK), ItemBlockPlasticReinforced::new),
    LIME_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BRIGHT_GREEN), ItemBlockPlasticReinforced::new),
    YELLOW_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.YELLOW), ItemBlockPlasticReinforced::new),
    LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.INDIGO), ItemBlockPlasticReinforced::new),
    MAGENTA_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.PINK), ItemBlockPlasticReinforced::new),
    ORANGE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.ORANGE), ItemBlockPlasticReinforced::new),
    WHITE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.WHITE), ItemBlockPlasticReinforced::new),

    BLACK_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BLACK), ItemBlockPlasticRoad::new),
    RED_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.RED), ItemBlockPlasticRoad::new),
    GREEN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_GREEN), ItemBlockPlasticRoad::new),
    BROWN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BROWN), ItemBlockPlasticRoad::new),
    BLUE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_BLUE), ItemBlockPlasticRoad::new),
    PURPLE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.PURPLE), ItemBlockPlasticRoad::new),
    CYAN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_AQUA), ItemBlockPlasticRoad::new),
    LIGHT_GRAY_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.GRAY), ItemBlockPlasticRoad::new),
    GRAY_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_GRAY), ItemBlockPlasticRoad::new),
    PINK_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BRIGHT_PINK), ItemBlockPlasticRoad::new),
    LIME_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BRIGHT_GREEN), ItemBlockPlasticRoad::new),
    YELLOW_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.YELLOW), ItemBlockPlasticRoad::new),
    LIGHT_BLUE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.INDIGO), ItemBlockPlasticRoad::new),
    MAGENTA_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.PINK), ItemBlockPlasticRoad::new),
    ORANGE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.ORANGE), ItemBlockPlasticRoad::new),
    WHITE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.WHITE), ItemBlockPlasticRoad::new),

    BLACK_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BLACK), ItemBlockPlasticFence::new),
    RED_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.RED), ItemBlockPlasticFence::new),
    GREEN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_GREEN), ItemBlockPlasticFence::new),
    BROWN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BROWN), ItemBlockPlasticFence::new),
    BLUE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_BLUE), ItemBlockPlasticFence::new),
    PURPLE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.PURPLE), ItemBlockPlasticFence::new),
    CYAN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_AQUA), ItemBlockPlasticFence::new),
    LIGHT_GRAY_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.GRAY), ItemBlockPlasticFence::new),
    GRAY_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_GRAY), ItemBlockPlasticFence::new),
    PINK_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BRIGHT_PINK), ItemBlockPlasticFence::new),
    LIME_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BRIGHT_GREEN), ItemBlockPlasticFence::new),
    YELLOW_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.YELLOW), ItemBlockPlasticFence::new),
    LIGHT_BLUE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.INDIGO), ItemBlockPlasticFence::new),
    MAGENTA_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.PINK), ItemBlockPlasticFence::new),
    ORANGE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.ORANGE), ItemBlockPlasticFence::new),
    WHITE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.WHITE), ItemBlockPlasticFence::new),

    //TODO: Tag Etnry
    SALT_BLOCK(new BlockSalt());

    @Nonnull
    private final BlockItem item;
    @Nonnull
    private final Block block;

    MekanismBlock(@Nonnull Block block) {
        this(block, ItemBlockMekanism::new);
    }

    <ITEM extends BlockItem & IItemMekanism, BLOCK extends Block> MekanismBlock(@Nonnull BLOCK block, Function<BLOCK, ITEM> itemCreator) {
        this.block = block;
        this.item = itemCreator.apply(block);
        //TODO: Fix all translation keys so that they have mekanism in them
    }

    @Nonnull
    @Override
    public Block getBlock() {
        return block;
    }

    @Nonnull
    @Override
    public BlockItem getItem() {
        return item;
    }

    public ResourceLocation getJEICategory() {
        return new ResourceLocation(Mekanism.MODID, getName());
    }

    @Nullable
    public FactoryType getFactoryType() {
        if (block instanceof IHasFactoryType) {
            return ((IHasFactoryType) block).getFactoryType();
        }
        return null;
    }

    @Nullable
    public MekanismBlock getUpgradedBlock() {
        //TODO: Have this be dynamic/built up automatically
        if (this == ENERGIZED_SMELTER) {
            return BASIC_SMELTING_FACTORY;
        } else if (this == BASIC_SMELTING_FACTORY) {
            return ADVANCED_SMELTING_FACTORY;
        } else if (this == ADVANCED_SMELTING_FACTORY) {
            return ELITE_SMELTING_FACTORY;
        }
        return null;
    }

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        for (MekanismBlock mekanismBlock : values()) {
            registry.register(mekanismBlock.getBlock());
        }
        registry.register(PortalHelper.BlockPortalOverride.instance);
    }

    public static void registerItemBlocks(IForgeRegistry<Item> registry) {
        for (MekanismBlock mekanismBlock : values()) {
            registry.register(mekanismBlock.getItem());
        }
    }

    /**
     * Retrieves a Factory with a defined tier and recipe type.
     *
     * @param tier - tier to add to the Factory
     * @param type - recipe type to add to the Factory
     *
     * @return factory with defined tier and recipe type
     */
    public static MekanismBlock getFactory(@Nonnull FactoryTier tier, @Nonnull FactoryType type) {
        //TODO: Clean this up and make it more dynamic styled. So that a map gets built at the end of registerBlocks
        switch (tier) {
            case BASIC:
                switch (type) {
                    case SMELTING:
                        return BASIC_SMELTING_FACTORY;
                    case ENRICHING:
                        return BASIC_ENRICHING_FACTORY;
                    case CRUSHING:
                        return BASIC_CRUSHING_FACTORY;
                    case COMPRESSING:
                        return BASIC_COMPRESSING_FACTORY;
                    case COMBINING:
                        return BASIC_COMBINING_FACTORY;
                    case PURIFYING:
                        return BASIC_PURIFYING_FACTORY;
                    case INJECTING:
                        return BASIC_INJECTING_FACTORY;
                    case INFUSING:
                        return BASIC_INFUSING_FACTORY;
                    case SAWING:
                        return BASIC_SAWING_FACTORY;
                }
                break;
            case ADVANCED:
                switch (type) {
                    case SMELTING:
                        return ADVANCED_SMELTING_FACTORY;
                    case ENRICHING:
                        return ADVANCED_ENRICHING_FACTORY;
                    case CRUSHING:
                        return ADVANCED_CRUSHING_FACTORY;
                    case COMPRESSING:
                        return ADVANCED_COMPRESSING_FACTORY;
                    case COMBINING:
                        return ADVANCED_COMBINING_FACTORY;
                    case PURIFYING:
                        return ADVANCED_PURIFYING_FACTORY;
                    case INJECTING:
                        return ADVANCED_INJECTING_FACTORY;
                    case INFUSING:
                        return ADVANCED_INFUSING_FACTORY;
                    case SAWING:
                        return ADVANCED_SAWING_FACTORY;
                }
                break;
            case ELITE:
                switch (type) {
                    case SMELTING:
                        return ELITE_SMELTING_FACTORY;
                    case ENRICHING:
                        return ELITE_ENRICHING_FACTORY;
                    case CRUSHING:
                        return ELITE_CRUSHING_FACTORY;
                    case COMPRESSING:
                        return ELITE_COMPRESSING_FACTORY;
                    case COMBINING:
                        return ELITE_COMBINING_FACTORY;
                    case PURIFYING:
                        return ELITE_PURIFYING_FACTORY;
                    case INJECTING:
                        return ELITE_INJECTING_FACTORY;
                    case INFUSING:
                        return ELITE_INFUSING_FACTORY;
                    case SAWING:
                        return ELITE_SAWING_FACTORY;
                }
                break;
        }
        //It should never be able to reach here
        return BASIC_SMELTING_FACTORY;
    }
}