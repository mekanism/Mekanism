package mekanism.common;

import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_1;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_2;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_3;

import java.util.function.Function;
import mekanism.common.block.BlockBasic;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockGasTank;
import mekanism.common.block.BlockGlowPanel;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPlastic;
import mekanism.common.block.BlockPlasticFence;
import mekanism.common.block.BlockSalt;
import mekanism.common.block.BlockTransmitter;
import mekanism.common.block.IBlockMekanism;
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
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockType;
import mekanism.common.item.IItemMekanism;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockGlowPanel;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockMekanism;
import mekanism.common.item.ItemBlockOre;
import mekanism.common.item.ItemBlockPlastic;
import mekanism.common.item.ItemBlockTransmitter;
import mekanism.common.resource.MiscResource;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Ensure all IBlockMekanism's set the required information
public enum MekanismBlock {
    OSMIUM_BLOCK(new BlockResource(Resource.OSMIUM), ItemBlockBasic::new),
    BRONZE_BLOCK(new BlockResource(MiscResource.BRONZE), ItemBlockBasic::new),
    REFINED_OBSIDIAN_BLOCK(new BlockResource(MiscResource.REFINED_OBSIDIAN), ItemBlockBasic::new),
    CHARCOAL_BLOCK(new BlockResource(MiscResource.CHARCOAL), ItemBlockBasic::new),
    REFINED_GLOWSTONE_BLOCK(new BlockResource(MiscResource.REFINED_GLOWSTONE), ItemBlockBasic::new),
    STEEL_BLOCK(new BlockResource(MiscResource.STEEL), ItemBlockBasic::new),

    BASIC_BIN(new BlockBin(BinTier.BASIC), ItemBlockBasic::new),
    ADVANCED_BIN(new BlockBin(BinTier.ADVANCED), ItemBlockBasic::new),
    ELITE_BIN(new BlockBin(BinTier.ELITE), ItemBlockBasic::new),
    ULTIMATE_BIN(new BlockBin(BinTier.ULTIMATE), ItemBlockBasic::new),

    TELEPORTER_FRAME(new BlockTeleporterFrame(), ItemBlockBasic::new),
    STEEL_CASING(new BlockSteelCasing(), ItemBlockBasic::new),
    DYNAMIC_TANK(new BlockDynamicTank(), ItemBlockBasic::new),
    STRUCTURAL_GLASS(new BlockStructuralGlass(), ItemBlockBasic::new),
    DYNAMIC_VALVE(new BlockDynamicValve(), ItemBlockBasic::new),
    COPPER_BLOCK(new BlockResource(Resource.COPPER), ItemBlockBasic::new),
    TIN_BLOCK(new BlockResource(Resource.TIN), ItemBlockBasic::new),
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

    ENRICHMENT_CHAMBER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    OSMIUM_COMPRESSOR(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    COMBINER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    CRUSHER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    DIGITAL_MINER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),

    BASIC_SMELTING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_ENRICHING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_CRUSHING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_COMPRESSING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_COMBINING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_PURIFYING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_INJECTING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_INFUSING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    BASIC_SAWING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),

    ADVANCED_SMELTING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_ENRICHING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_CRUSHING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_COMPRESSING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_COMBINING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_PURIFYING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_INJECTING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_INFUSING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ADVANCED_SAWING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),

    ELITE_SMELTING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_ENRICHING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_CRUSHING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_COMPRESSING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_COMBINING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_PURIFYING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_INJECTING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_INFUSING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELITE_SAWING_FACTORY(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),

    METALLURGIC_INFUSER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    PURIFICATION_CHAMBER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ENERGIZED_SMELTER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    TELEPORTER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ELECTRIC_PUMP(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    PERSONAL_CHEST(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    CHARGEPAD(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    LOGISTICAL_SORTER(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), ItemBlockMachine::new),
    ROTARY_CONDENSENTRATOR(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    CHEMICAL_OXIDIZER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    CHEMICAL_INFUSER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    CHEMICAL_INJECTION_CHAMBER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    ELECTROLYTIC_SEPARATOR(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    PRECISION_SAWMILL(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    CHEMICAL_DISSOLUTION_CHAMBER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    CHEMICAL_WASHER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    CHEMICAL_CRYSTALLIZER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    SEISMIC_VIBRATOR(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    PRESSURIZED_REACTION_CHAMBER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),

    BASIC_FLUID_TANK(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    ADVANCED_FLUID_TANK(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    ELITE_FLUID_TANK(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    ULTIMATE_FLUID_TANK(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    CREATIVE_FLUID_TANK(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),

    FLUIDIC_PLENISHER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    LASER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    LASER_AMPLIFIER(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    LASER_TRACTOR_BEAM(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), ItemBlockMachine::new),
    QUANTUM_ENTANGLOPORTER(BlockMachine.getBlockMachine(MACHINE_BLOCK_3), ItemBlockMachine::new),
    SOLAR_NEUTRON_ACTIVATOR(BlockMachine.getBlockMachine(MACHINE_BLOCK_3), ItemBlockMachine::new),
    OREDICTIONIFICATOR(BlockMachine.getBlockMachine(MACHINE_BLOCK_3), ItemBlockMachine::new),
    RESISTIVE_HEATER(BlockMachine.getBlockMachine(MACHINE_BLOCK_3), ItemBlockMachine::new),
    FORMULAIC_ASSEMBLICATOR(BlockMachine.getBlockMachine(MACHINE_BLOCK_3), ItemBlockMachine::new),
    FUELWOOD_HEATER(BlockMachine.getBlockMachine(MACHINE_BLOCK_3), ItemBlockMachine::new),

    OSMIUM_ORE(new BlockOre(), ItemBlockOre::new),
    COPPER_ORE(new BlockOre(), ItemBlockOre::new),
    TIN_ORE(new BlockOre(), ItemBlockOre::new),

    BASIC_ENERGY_CUBE(new BlockEnergyCube(), ItemBlockEnergyCube::new),
    ADVANCED_ENERGY_CUBE(new BlockEnergyCube(), ItemBlockEnergyCube::new),
    ELITE_ENERGY_CUBE(new BlockEnergyCube(), ItemBlockEnergyCube::new),
    ULTIMATE_ENERGY_CUBE(new BlockEnergyCube(), ItemBlockEnergyCube::new),
    CREATIVE_ENERGY_CUBE(new BlockEnergyCube(), ItemBlockEnergyCube::new),

    BASIC_UNIVERSAL_CABLE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ADVANCED_UNIVERSAL_CABLE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ELITE_UNIVERSAL_CABLE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ULTIMATE_UNIVERSAL_CABLE(new BlockTransmitter(), ItemBlockTransmitter::new),

    BASIC_MECHANICAL_PIPE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ADVANCED_MECHANICAL_PIPE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ELITE_MECHANICAL_PIPE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ULTIMATE_MECHANICAL_PIPE(new BlockTransmitter(), ItemBlockTransmitter::new),

    BASIC_PRESSURIZED_TUBE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ADVANCED_PRESSURIZED_TUBE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ELITE_PRESSURIZED_TUBE(new BlockTransmitter(), ItemBlockTransmitter::new),
    ULTIMATE_PRESSURIZED_TUBE(new BlockTransmitter(), ItemBlockTransmitter::new),

    BASIC_LOGISTICAL_TRANSPORTER(new BlockTransmitter(), ItemBlockTransmitter::new),
    ADVANCED_LOGISTICAL_TRANSPORTER(new BlockTransmitter(), ItemBlockTransmitter::new),
    ELITE_LOGISTICAL_TRANSPORTER(new BlockTransmitter(), ItemBlockTransmitter::new),
    ULTIMATE_LOGISTICAL_TRANSPORTER(new BlockTransmitter(), ItemBlockTransmitter::new),

    RESTRICTIVE_TRANSPORTER(new BlockTransmitter(), ItemBlockTransmitter::new),
    DIVERSION_TRANSPORTER(new BlockTransmitter(), ItemBlockTransmitter::new),

    BASIC_THERMODYNAMIC_CONDUCTOR(new BlockTransmitter(), ItemBlockTransmitter::new),
    ADVANCED_THERMODYNAMIC_CONDUCTOR(new BlockTransmitter(), ItemBlockTransmitter::new),
    ELITE_THERMODYNAMIC_CONDUCTOR(new BlockTransmitter(), ItemBlockTransmitter::new),
    ULTIMATE_THERMODYNAMIC_CONDUCTOR(new BlockTransmitter(), ItemBlockTransmitter::new),

    OBSIDIAN_TNT(new BlockBounding()),
    BOUNDING_BLOCK(new BlockBounding()),

    BASIC_GAS_TANK(new BlockGasTank(), ItemBlockGasTank::new),
    ADVANCED_GAS_TANK(new BlockGasTank(), ItemBlockGasTank::new),
    ELITE_GAS_TANK(new BlockGasTank(), ItemBlockGasTank::new),
    ULTIMATE_GAS_TANK(new BlockGasTank(), ItemBlockGasTank::new),
    CREATIVE_GAS_TANK(new BlockGasTank(), ItemBlockGasTank::new),

    CARDBOARD_BOX(new BlockCardboardBox(), ItemBlockCardboardBox::new),

    BLACK_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    RED_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    GREEN_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    BROWN_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    BLUE_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    PURPLE_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    CYAN_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    LIGHT_GRAY_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    GRAY_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    PINK_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    LIME_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    YELLOW_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    LIGHT_BLUE_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    MAGENTA_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    ORANGE_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),
    WHITE_GLOW_PANEL(new BlockGlowPanel(), ItemBlockGlowPanel::new),

    BLACK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    RED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    GREEN_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    BROWN_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    BLUE_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    PURPLE_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    CYAN_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    GRAY_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    PINK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    LIME_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    YELLOW_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    ORANGE_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),
    WHITE_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.PLASTIC), ItemBlockPlastic::new),

    BLACK_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    RED_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    GREEN_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    BROWN_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    BLUE_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    PURPLE_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    CYAN_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    LIGHT_GRAY_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    GRAY_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    PINK_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    LIME_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    YELLOW_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    LIGHT_BLUE_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    MAGENTA_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    ORANGE_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),
    WHITE_SLICK_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.SLICK), ItemBlockPlastic::new),

    BLACK_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    RED_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    GREEN_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    BROWN_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    BLUE_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    PURPLE_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    CYAN_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    GRAY_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    PINK_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    LIME_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    YELLOW_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    ORANGE_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),
    WHITE_PLASTIC_GLOW_BLOCK(new BlockPlastic(PlasticBlockType.GLOW), ItemBlockPlastic::new),

    BLACK_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    RED_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    GREEN_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    BROWN_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    PURPLE_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    CYAN_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    PINK_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    LIME_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    YELLOW_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    MAGENTA_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    ORANGE_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),
    WHITE_REINFORCED_PLASTIC_BLOCK(new BlockPlastic(PlasticBlockType.REINFORCED), ItemBlockPlastic::new),

    BLACK_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    RED_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    GREEN_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    BROWN_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    BLUE_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    PURPLE_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    CYAN_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    GRAY_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    PINK_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    LIME_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    YELLOW_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    ORANGE_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),
    WHITE_PLASTIC_ROAD(new BlockPlastic(PlasticBlockType.ROAD), ItemBlockPlastic::new),

    BLACK_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    RED_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    GREEN_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    BROWN_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    BLUE_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    PURPLE_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    CYAN_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    LIGHT_GRAY_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    GRAY_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    PINK_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    LIME_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    YELLOW_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    LIGHT_BLUE_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    MAGENTA_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    ORANGE_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),
    WHITE_PLASTIC_BARRIER(new BlockPlasticFence(), ItemBlockPlastic::new),

    SALT_BLOCK(new BlockSalt());


    private final ItemBlock item;
    private final Block block;

    <BLOCK extends Block & IBlockMekanism> MekanismBlock(BLOCK block) {
        this(block, ItemBlockMekanism::new);
    }

    <ITEM extends ItemBlock & IItemMekanism, BLOCK extends Block & IBlockMekanism> MekanismBlock(BLOCK block, Function<BLOCK, ITEM> itemCreator) {
        this.block = block;
        this.item = itemCreator.apply(block);
    }

    public Block getBlock() {
        return block;
    }

    public ItemBlock getItem() {
        return item;
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
    }

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        for (MekanismBlock mekanismBlock : values()) {
            registry.register(mekanismBlock.getBlock());
        }
        registry.register(BlockBasic.BlockPortalOverride.instance);
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