package mekanism.common;

import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IFactory.RecipeType;
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
import mekanism.common.item.block.ItemBlockBasic;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.ItemBlockGasTank;
import mekanism.common.item.block.ItemBlockGlowPanel;
import mekanism.common.item.block.ItemBlockMachine;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.item.block.ItemBlockOre;
import mekanism.common.item.block.ItemBlockPlastic;
import mekanism.common.item.block.ItemBlockResource;
import mekanism.common.item.block.transmitter.ItemBlockLogisticalTransporter;
import mekanism.common.item.block.transmitter.ItemBlockMechanicalPipe;
import mekanism.common.item.block.transmitter.ItemBlockPressurizedTube;
import mekanism.common.item.block.transmitter.ItemBlockRestrictiveTransporter;
import mekanism.common.item.block.transmitter.ItemBlockThermodynamicConductor;
import mekanism.common.item.block.transmitter.ItemBlockDiversionTransporter;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Ensure all IBlockMekanism's set the required information
public enum MekanismBlock implements IBlockProvider {
    OSMIUM_BLOCK(new BlockResource(BlockResourceInfo.OSMIUM), ItemBlockResource::new),
    BRONZE_BLOCK(new BlockResource(BlockResourceInfo.BRONZE), ItemBlockResource::new),
    REFINED_OBSIDIAN_BLOCK(new BlockResource(BlockResourceInfo.REFINED_OBSIDIAN), ItemBlockResource::new),
    CHARCOAL_BLOCK(new BlockResource(BlockResourceInfo.CHARCOAL), ItemBlockResource::new),
    REFINED_GLOWSTONE_BLOCK(new BlockResource(BlockResourceInfo.REFINED_GLOWSTONE), ItemBlockResource::new),
    STEEL_BLOCK(new BlockResource(BlockResourceInfo.STEEL), ItemBlockResource::new),

    BASIC_BIN(new BlockBin(BinTier.BASIC), ItemBlockBasic::new),
    ADVANCED_BIN(new BlockBin(BinTier.ADVANCED), ItemBlockBasic::new),
    ELITE_BIN(new BlockBin(BinTier.ELITE), ItemBlockBasic::new),
    ULTIMATE_BIN(new BlockBin(BinTier.ULTIMATE), ItemBlockBasic::new),
    CREATIVE_BIN(new BlockBin(BinTier.CREATIVE), ItemBlockBasic::new),

    TELEPORTER_FRAME(new BlockTeleporterFrame(), ItemBlockBasic::new),
    STEEL_CASING(new BlockSteelCasing(), ItemBlockBasic::new),
    DYNAMIC_TANK(new BlockDynamicTank(), ItemBlockBasic::new),
    STRUCTURAL_GLASS(new BlockStructuralGlass(), ItemBlockBasic::new),
    DYNAMIC_VALVE(new BlockDynamicValve(), ItemBlockBasic::new),
    COPPER_BLOCK(new BlockResource(BlockResourceInfo.COPPER), ItemBlockResource::new),
    TIN_BLOCK(new BlockResource(BlockResourceInfo.TIN), ItemBlockResource::new),
    THERMAL_EVAPORATION_CONTROLLER(new BlockThermalEvaporationController(), ItemBlockBasic::new),
    THERMAL_EVAPORATION_VALVE(new BlockThermalEvaporationValve(), ItemBlockBasic::new),

    THERMAL_EVAPORATION_BLOCK(new BlockThermalEvaporation(), ItemBlockBasic::new),
    INDUCTION_CASING(new BlockInductionCasing(), ItemBlockBasic::new),
    INDUCTION_PORT(new BlockInductionPort(), ItemBlockBasic::new),

    BASIC_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.BASIC), ItemBlockBasic::new),
    ADVANCED_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.ADVANCED), ItemBlockBasic::new),
    ELITE_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.ELITE), ItemBlockBasic::new),
    ULTIMATE_INDUCTION_CELL(new BlockInductionCell(InductionCellTier.ULTIMATE), ItemBlockBasic::new),

    BASIC_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.BASIC), ItemBlockBasic::new),
    ADVANCED_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.ADVANCED), ItemBlockBasic::new),
    ELITE_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.ELITE), ItemBlockBasic::new),
    ULTIMATE_INDUCTION_PROVIDER(new BlockInductionProvider(InductionProviderTier.ULTIMATE), ItemBlockBasic::new),

    SUPERHEATING_ELEMENT(new BlockSuperheatingElement(), ItemBlockBasic::new),
    PRESSURE_DISPERSER(new BlockPressureDisperser(), ItemBlockBasic::new),
    BOILER_CASING(new BlockBoilerCasing(), ItemBlockBasic::new),
    BOILER_VALVE(new BlockBoilerValve(), ItemBlockBasic::new),
    SECURITY_DESK(new BlockSecurityDesk(), ItemBlockBasic::new),

    ENRICHMENT_CHAMBER(new BlockEnrichmentChamber(), ItemBlockMachine::new),
    OSMIUM_COMPRESSOR(new BlockOsmiumCompressor(), ItemBlockMachine::new),
    COMBINER(new BlockCombiner(), ItemBlockMachine::new),
    CRUSHER(new BlockCrusher(), ItemBlockMachine::new),
    DIGITAL_MINER(new BlockDigitalMiner(), ItemBlockMachine::new),

    BASIC_SMELTING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.SMELTING), ItemBlockMachine::new),
    BASIC_ENRICHING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.ENRICHING), ItemBlockMachine::new),
    BASIC_CRUSHING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.CRUSHING), ItemBlockMachine::new),
    BASIC_COMPRESSING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.COMPRESSING), ItemBlockMachine::new),
    BASIC_COMBINING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.COMBINING), ItemBlockMachine::new),
    BASIC_PURIFYING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.PURIFYING), ItemBlockMachine::new),
    BASIC_INJECTING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.INJECTING), ItemBlockMachine::new),
    BASIC_INFUSING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.INFUSING), ItemBlockMachine::new),
    BASIC_SAWING_FACTORY(new BlockFactory(FactoryTier.BASIC, RecipeType.SAWING), ItemBlockMachine::new),

    ADVANCED_SMELTING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.SMELTING), ItemBlockMachine::new),
    ADVANCED_ENRICHING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.ENRICHING), ItemBlockMachine::new),
    ADVANCED_CRUSHING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.CRUSHING), ItemBlockMachine::new),
    ADVANCED_COMPRESSING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.COMPRESSING), ItemBlockMachine::new),
    ADVANCED_COMBINING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.COMBINING), ItemBlockMachine::new),
    ADVANCED_PURIFYING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.PURIFYING), ItemBlockMachine::new),
    ADVANCED_INJECTING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.INJECTING), ItemBlockMachine::new),
    ADVANCED_INFUSING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.INFUSING), ItemBlockMachine::new),
    ADVANCED_SAWING_FACTORY(new BlockFactory(FactoryTier.ADVANCED, RecipeType.SAWING), ItemBlockMachine::new),

    ELITE_SMELTING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.SMELTING), ItemBlockMachine::new),
    ELITE_ENRICHING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.ENRICHING), ItemBlockMachine::new),
    ELITE_CRUSHING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.CRUSHING), ItemBlockMachine::new),
    ELITE_COMPRESSING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.COMPRESSING), ItemBlockMachine::new),
    ELITE_COMBINING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.COMBINING), ItemBlockMachine::new),
    ELITE_PURIFYING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.PURIFYING), ItemBlockMachine::new),
    ELITE_INJECTING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.INJECTING), ItemBlockMachine::new),
    ELITE_INFUSING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.INFUSING), ItemBlockMachine::new),
    ELITE_SAWING_FACTORY(new BlockFactory(FactoryTier.ELITE, RecipeType.SAWING), ItemBlockMachine::new),

    METALLURGIC_INFUSER(new BlockMetallurgicInfuser(), ItemBlockMachine::new),
    PURIFICATION_CHAMBER(new BlockPurificationChamber(), ItemBlockMachine::new),
    ENERGIZED_SMELTER(new BlockEnergizedSmelter(), ItemBlockMachine::new),
    TELEPORTER(new BlockTeleporter(), ItemBlockMachine::new),
    ELECTRIC_PUMP(new BlockElectricPump(), ItemBlockMachine::new),
    PERSONAL_CHEST(new BlockPersonalChest(), ItemBlockMachine::new),
    CHARGEPAD(new BlockChargepad(), ItemBlockMachine::new),
    LOGISTICAL_SORTER(new BlockLogisticalSorter(), ItemBlockMachine::new),
    ROTARY_CONDENSENTRATOR(new BlockRotaryCondensentrator(), ItemBlockMachine::new),
    CHEMICAL_OXIDIZER(new BlockChemicalOxidizer(), ItemBlockMachine::new),
    CHEMICAL_INFUSER(new BlockChemicalInfuser(), ItemBlockMachine::new),
    CHEMICAL_INJECTION_CHAMBER(new BlockChemicalInjectionChamber(), ItemBlockMachine::new),
    ELECTROLYTIC_SEPARATOR(new BlockElectrolyticSeparator(), ItemBlockMachine::new),
    PRECISION_SAWMILL(new BlockPrecisionSawmill(), ItemBlockMachine::new),
    CHEMICAL_DISSOLUTION_CHAMBER(new BlockChemicalDissolutionChamber(), ItemBlockMachine::new),
    CHEMICAL_WASHER(new BlockChemicalWasher(), ItemBlockMachine::new),
    CHEMICAL_CRYSTALLIZER(new BlockChemicalCrystallizer(), ItemBlockMachine::new),
    SEISMIC_VIBRATOR(new BlockSeismicVibrator(), ItemBlockMachine::new),
    PRESSURIZED_REACTION_CHAMBER(new BlockPressurizedReactionChamber(), ItemBlockMachine::new),

    BASIC_FLUID_TANK(new BlockFluidTank(FluidTankTier.BASIC), ItemBlockMachine::new),
    ADVANCED_FLUID_TANK(new BlockFluidTank(FluidTankTier.ADVANCED), ItemBlockMachine::new),
    ELITE_FLUID_TANK(new BlockFluidTank(FluidTankTier.ELITE), ItemBlockMachine::new),
    ULTIMATE_FLUID_TANK(new BlockFluidTank(FluidTankTier.ULTIMATE), ItemBlockMachine::new),
    CREATIVE_FLUID_TANK(new BlockFluidTank(FluidTankTier.CREATIVE), ItemBlockMachine::new),

    FLUIDIC_PLENISHER(new BlockFluidicPlenisher(), ItemBlockMachine::new),
    LASER(new BlockLaser(), ItemBlockMachine::new),
    LASER_AMPLIFIER(new BlockLaserAmplifier(), ItemBlockMachine::new),
    LASER_TRACTOR_BEAM(new BlockLaserTractorBeam(), ItemBlockMachine::new),
    QUANTUM_ENTANGLOPORTER(new BlockQuantumEntangloporter(), ItemBlockMachine::new),
    SOLAR_NEUTRON_ACTIVATOR(new BlockSolarNeutronActivator(), ItemBlockMachine::new),
    OREDICTIONIFICATOR(new BlockOredictionificator(), ItemBlockMachine::new),
    RESISTIVE_HEATER(new BlockResistiveHeater(), ItemBlockMachine::new),
    FORMULAIC_ASSEMBLICATOR(new BlockFormulaicAssemblicator(), ItemBlockMachine::new),
    FUELWOOD_HEATER(new BlockFuelwoodHeater(), ItemBlockMachine::new),

    OSMIUM_ORE(new BlockOre(Resource.OSMIUM), ItemBlockOre::new),
    COPPER_ORE(new BlockOre(Resource.COPPER), ItemBlockOre::new),
    TIN_ORE(new BlockOre(Resource.TIN), ItemBlockOre::new),

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
    BOUNDING_BLOCK(new BlockBounding()),

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
    LIGHT_GRAY_GLOW_PANEL(new BlockGlowPanel(EnumColor.GREY), ItemBlockGlowPanel::new),
    GRAY_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_GREY), ItemBlockGlowPanel::new),
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
    LIGHT_GRAY_PLASTIC_BLOCK(new BlockPlastic(EnumColor.GREY), ItemBlockPlastic::new),
    GRAY_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_GREY), ItemBlockPlastic::new),
    PINK_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BRIGHT_PINK), ItemBlockPlastic::new),
    LIME_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BRIGHT_GREEN), ItemBlockPlastic::new),
    YELLOW_PLASTIC_BLOCK(new BlockPlastic(EnumColor.YELLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.INDIGO), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_BLOCK(new BlockPlastic(EnumColor.PINK), ItemBlockPlastic::new),
    ORANGE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.ORANGE), ItemBlockPlastic::new),
    WHITE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.WHITE), ItemBlockPlastic::new),

    BLACK_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BLACK), ItemBlockPlastic::new),
    RED_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.RED), ItemBlockPlastic::new),
    GREEN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_GREEN), ItemBlockPlastic::new),
    BROWN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BROWN), ItemBlockPlastic::new),
    BLUE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_BLUE), ItemBlockPlastic::new),
    PURPLE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.PURPLE), ItemBlockPlastic::new),
    CYAN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_AQUA), ItemBlockPlastic::new),
    LIGHT_GRAY_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.GREY), ItemBlockPlastic::new),
    GRAY_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_GREY), ItemBlockPlastic::new),
    PINK_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BRIGHT_PINK), ItemBlockPlastic::new),
    LIME_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BRIGHT_GREEN), ItemBlockPlastic::new),
    YELLOW_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.YELLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.INDIGO), ItemBlockPlastic::new),
    MAGENTA_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.PINK), ItemBlockPlastic::new),
    ORANGE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.ORANGE), ItemBlockPlastic::new),
    WHITE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.WHITE), ItemBlockPlastic::new),

    BLACK_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BLACK), ItemBlockPlastic::new),
    RED_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.RED), ItemBlockPlastic::new),
    GREEN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_GREEN), ItemBlockPlastic::new),
    BROWN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BROWN), ItemBlockPlastic::new),
    BLUE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_BLUE), ItemBlockPlastic::new),
    PURPLE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.PURPLE), ItemBlockPlastic::new),
    CYAN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_AQUA), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.GREY), ItemBlockPlastic::new),
    GRAY_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_GREY), ItemBlockPlastic::new),
    PINK_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BRIGHT_PINK), ItemBlockPlastic::new),
    LIME_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BRIGHT_GREEN), ItemBlockPlastic::new),
    YELLOW_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.YELLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.INDIGO), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.PINK), ItemBlockPlastic::new),
    ORANGE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.ORANGE), ItemBlockPlastic::new),
    WHITE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.WHITE), ItemBlockPlastic::new),

    BLACK_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BLACK), ItemBlockPlastic::new),
    RED_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.RED), ItemBlockPlastic::new),
    GREEN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_GREEN), ItemBlockPlastic::new),
    BROWN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BROWN), ItemBlockPlastic::new),
    BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_BLUE), ItemBlockPlastic::new),
    PURPLE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.PURPLE), ItemBlockPlastic::new),
    CYAN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_AQUA), ItemBlockPlastic::new),
    LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.GREY), ItemBlockPlastic::new),
    GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_GREY), ItemBlockPlastic::new),
    PINK_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BRIGHT_PINK), ItemBlockPlastic::new),
    LIME_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BRIGHT_GREEN), ItemBlockPlastic::new),
    YELLOW_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.YELLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.INDIGO), ItemBlockPlastic::new),
    MAGENTA_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.PINK), ItemBlockPlastic::new),
    ORANGE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.ORANGE), ItemBlockPlastic::new),
    WHITE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.WHITE), ItemBlockPlastic::new),

    BLACK_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BLACK), ItemBlockPlastic::new),
    RED_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.RED), ItemBlockPlastic::new),
    GREEN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_GREEN), ItemBlockPlastic::new),
    BROWN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BROWN), ItemBlockPlastic::new),
    BLUE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_BLUE), ItemBlockPlastic::new),
    PURPLE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.PURPLE), ItemBlockPlastic::new),
    CYAN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_AQUA), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.GREY), ItemBlockPlastic::new),
    GRAY_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_GREY), ItemBlockPlastic::new),
    PINK_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BRIGHT_PINK), ItemBlockPlastic::new),
    LIME_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BRIGHT_GREEN), ItemBlockPlastic::new),
    YELLOW_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.YELLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.INDIGO), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.PINK), ItemBlockPlastic::new),
    ORANGE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.ORANGE), ItemBlockPlastic::new),
    WHITE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.WHITE), ItemBlockPlastic::new),

    BLACK_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BLACK), ItemBlockPlastic::new),
    RED_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.RED), ItemBlockPlastic::new),
    GREEN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_GREEN), ItemBlockPlastic::new),
    BROWN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BROWN), ItemBlockPlastic::new),
    BLUE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_BLUE), ItemBlockPlastic::new),
    PURPLE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.PURPLE), ItemBlockPlastic::new),
    CYAN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_AQUA), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.GREY), ItemBlockPlastic::new),
    GRAY_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_GREY), ItemBlockPlastic::new),
    PINK_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BRIGHT_PINK), ItemBlockPlastic::new),
    LIME_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BRIGHT_GREEN), ItemBlockPlastic::new),
    YELLOW_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.YELLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.INDIGO), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.PINK), ItemBlockPlastic::new),
    ORANGE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.ORANGE), ItemBlockPlastic::new),
    WHITE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.WHITE), ItemBlockPlastic::new),

    SALT_BLOCK(new BlockSalt());

    @Nonnull
    private final ItemBlock item;
    @Nonnull
    private final Block block;

    MekanismBlock(@Nonnull Block block) {
        this(block, ItemBlockMekanism::new);
    }

    <ITEM extends ItemBlock & IItemMekanism, BLOCK extends Block> MekanismBlock(@Nonnull BLOCK block, Function<BLOCK, ITEM> itemCreator) {
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
    public ItemBlock getItem() {
        return item;
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
            Item item = mekanismBlock.getItem();
            registry.register(item);
            if (item instanceof IItemMekanism) {
                ((IItemMekanism) item).registerOreDict();
            }
        }
    }
}