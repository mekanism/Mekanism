package mekanism.common.registries;

import static net.minecraft.block.Block.makeCuboidShape;
import java.util.EnumSet;
import mekanism.api.Upgrade;
import mekanism.api.block.FactoryType;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.machines.Machine;
import mekanism.common.content.machines.Machine.FactoryMachine;
import mekanism.common.content.machines.Machine.MachineBuilder;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.text.TextComponentUtil;

public class MekanismMachines {
    // Enrichment Chamber
    public static final FactoryMachine<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.ENRICHMENT_CHAMBER, MekanismContainerTypes.ENRICHMENT_CHAMBER, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, FactoryType.ENRICHING)
        .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
        .withConfig(MekanismConfig.usage.enrichmentChamber, MekanismConfig.storage.enrichmentChamber)
        .withFactoryHierarchy(MekanismBlocks.BASIC_ENRICHING_FACTORY, MekanismBlocks.ADVANCED_ENRICHING_FACTORY, MekanismBlocks.ELITE_ENRICHING_FACTORY, MekanismBlocks.ULTIMATE_ENRICHING_FACTORY)
        .build();
    // Crusher
    public static final FactoryMachine<TileEntityCrusher> CRUSHER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.CRUSHER, MekanismContainerTypes.CRUSHER, MekanismLang.DESCRIPTION_CRUSHER, FactoryType.CRUSHING)
        .withSound(MekanismSounds.CRUSHER)
        .withConfig(MekanismConfig.usage.crusher, MekanismConfig.storage.crusher)
        .withFactoryHierarchy(MekanismBlocks.BASIC_CRUSHING_FACTORY, MekanismBlocks.ADVANCED_CRUSHING_FACTORY, MekanismBlocks.ELITE_CRUSHING_FACTORY, MekanismBlocks.ULTIMATE_CRUSHING_FACTORY)
        .build();
    // Energized Smelter
    public static final FactoryMachine<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.ENERGIZED_SMELTER, MekanismContainerTypes.ENERGIZED_SMELTER, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, FactoryType.SMELTING)
        .withSound(MekanismSounds.ENERGIZED_SMELTER)
        .withConfig(MekanismConfig.usage.energizedSmelter, MekanismConfig.storage.energizedSmelter)
        .withFactoryHierarchy(MekanismBlocks.BASIC_SMELTING_FACTORY, MekanismBlocks.ADVANCED_SMELTING_FACTORY, MekanismBlocks.ELITE_SMELTING_FACTORY, MekanismBlocks.ULTIMATE_SMELTING_FACTORY)
        .build();
    // Precision Sawmill
    public static final FactoryMachine<TileEntityPrecisionSawmill> PRECISION_SAWMILL = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.PRECISION_SAWMILL, MekanismContainerTypes.PRECISION_SAWMILL, MekanismLang.DESCRIPTION_PRECISION_SAWMILL, FactoryType.SAWING)
        .withSound(MekanismSounds.PRECISION_SAWMILL)
        .withConfig(MekanismConfig.usage.precisionSawmill, MekanismConfig.storage.precisionSawmill)
        .withFactoryHierarchy(MekanismBlocks.BASIC_SAWING_FACTORY, MekanismBlocks.ADVANCED_SAWING_FACTORY, MekanismBlocks.ELITE_SAWING_FACTORY, MekanismBlocks.ULTIMATE_SAWING_FACTORY)
        .build();
    // Osmium Compressor
    public static final FactoryMachine<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.OSMIUM_COMPRESSOR, MekanismContainerTypes.OSMIUM_COMPRESSOR, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, FactoryType.COMPRESSING)
        .withSound(MekanismSounds.OSMIUM_COMPRESSOR)
        .withConfig(MekanismConfig.usage.osmiumCompressor, MekanismConfig.storage.osmiumCompressor)
        .withFactoryHierarchy(MekanismBlocks.BASIC_COMPRESSING_FACTORY, MekanismBlocks.ADVANCED_COMPRESSING_FACTORY, MekanismBlocks.ELITE_COMPRESSING_FACTORY, MekanismBlocks.ULTIMATE_COMPRESSING_FACTORY)
        .build();
    // Combiner
    public static final FactoryMachine<TileEntityCombiner> COMBINER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.COMBINER, MekanismContainerTypes.COMBINER, MekanismLang.DESCRIPTION_COMBINER, FactoryType.COMBINING)
        .withSound(MekanismSounds.COMBINER)
        .withConfig(MekanismConfig.usage.combiner, MekanismConfig.storage.combiner)
        .withFactoryHierarchy(MekanismBlocks.BASIC_COMBINING_FACTORY, MekanismBlocks.ADVANCED_COMBINING_FACTORY, MekanismBlocks.ELITE_COMBINING_FACTORY, MekanismBlocks.ULTIMATE_COMBINING_FACTORY)
        .build();
    // Metallurgic Infuser
    public static final FactoryMachine<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.METALLURGIC_INFUSER, MekanismContainerTypes.METALLURGIC_INFUSER, MekanismLang.DESCRIPTION_METALLURGIC_INFUSER, FactoryType.INFUSING)
        .withSound(MekanismSounds.METALLURGIC_INFUSER)
        .withConfig(MekanismConfig.usage.metallurgicInfuser, MekanismConfig.storage.metallurgicInfuser)
        .withFactoryHierarchy(MekanismBlocks.BASIC_INFUSING_FACTORY, MekanismBlocks.ADVANCED_INFUSING_FACTORY, MekanismBlocks.ELITE_INFUSING_FACTORY, MekanismBlocks.ULTIMATE_INFUSING_FACTORY)
        .withCustomShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16),//base
              makeCuboidShape(0, 4, 15, 16, 16, 16),//back
              makeCuboidShape(0, 15, 8, 16, 16, 15),//top
              makeCuboidShape(1.5, 7, 1.5, 14.5, 8, 15.5),//divider
              makeCuboidShape(0, 4, 8, 1, 15, 15),//sideRight
              makeCuboidShape(15, 4, 8, 16, 15, 15),//sideLeft
              makeCuboidShape(13.5, 11, 1.5, 14.5, 12, 2.5),//bar1
              makeCuboidShape(1.5, 11, 1.5, 2.5, 12, 2.5),//bar2
              makeCuboidShape(11, 10.5, 5, 12, 15.5, 8),//connector1
              makeCuboidShape(4, 10.5, 5, 5, 15.5, 8),//connector2
              makeCuboidShape(10.5, 10.5, 13, 12.5, 11.5, 15),//tapBase1
              makeCuboidShape(3.5, 10.5, 13, 5.5, 11.5, 15),//tapBase2
              makeCuboidShape(10.5, 11.5, 4, 12.5, 12.5, 15),//tap1
              makeCuboidShape(3.5, 11.5, 4, 5.5, 12.5, 15),//tap2
              makeCuboidShape(1, 12, 1, 15, 15, 15),//plate1
              makeCuboidShape(1, 8, 1, 15, 11, 15),//plate2
              makeCuboidShape(1, 4, 1, 15, 7, 15)//plate3
        )).build();
    // Purification Chamber
    public static final FactoryMachine<TileEntityPurificationChamber> PURIFICATION_CHAMBER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.PURIFICATION_CHAMBER, MekanismContainerTypes.PURIFICATION_CHAMBER, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, FactoryType.PURIFYING)
        .withSound(MekanismSounds.PURIFICATION_CHAMBER)
        .withConfig(MekanismConfig.usage.purificationChamber, MekanismConfig.storage.purificationChamber)
        .withFactoryHierarchy(MekanismBlocks.BASIC_PURIFYING_FACTORY, MekanismBlocks.ADVANCED_PURIFYING_FACTORY, MekanismBlocks.ELITE_PURIFYING_FACTORY, MekanismBlocks.ULTIMATE_PURIFYING_FACTORY)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
        .build();
    // Chemical Injection Chamber
    public static final FactoryMachine<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, FactoryType.INJECTING)
        .withSound(MekanismSounds.CHEMICAL_INJECTION_CHAMBER)
        .withConfig(MekanismConfig.usage.chemicalInjectionChamber, MekanismConfig.storage.chemicalInjectionChamber)
        .withFactoryHierarchy(MekanismBlocks.BASIC_INJECTING_FACTORY, MekanismBlocks.ADVANCED_INJECTING_FACTORY, MekanismBlocks.ELITE_INJECTING_FACTORY, MekanismBlocks.ULTIMATE_INJECTING_FACTORY)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
        .build();

    // Pressurized Reaction Chamber
    public static final Machine<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = MachineBuilder
        .createMachine(MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, MekanismLang.DESCRIPTION_PRESSURIZED_REACTION_CHAMBER)
        .withSound(MekanismSounds.PRESSURIZED_REACTION_CHAMBER)
        .withConfig(MekanismConfig.usage.pressurizedReactionBase, MekanismConfig.storage.pressurizedReactionBase)
        .withCustomShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16),//base
              makeCuboidShape(1, 4, 1, 10, 15, 6),//front
              makeCuboidShape(0, 4, 6, 16, 16, 16),//body
              makeCuboidShape(13, 3.5, 0.5, 15, 15.5, 6.5),//frontDivider1
              makeCuboidShape(10, 3.5, 0.5, 12, 15.5, 6.5),//frontDivider2
              makeCuboidShape(12, 5, 1, 13, 6, 6),//bar1
              makeCuboidShape(12, 7, 1, 13, 8, 6),//bar2
              makeCuboidShape(12, 9, 1, 13, 10, 6),//bar3
              makeCuboidShape(12, 11, 1, 13, 12, 6),//bar4
              makeCuboidShape(12, 13, 1, 13, 14, 6)//bar5
        )).build();
    // Chemical Crystallizer
    public static final Machine<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = MachineBuilder
        .createMachine(MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER)
        .withSound(MekanismSounds.CHEMICAL_CRYSTALLIZER)
        .withConfig(MekanismConfig.usage.chemicalCrystallizer, MekanismConfig.storage.chemicalCrystallizer)
        .withCustomShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 5, 16),//base
              makeCuboidShape(0, 11, 0, 16, 16, 16),//tank
              makeCuboidShape(3, 4.5, 3, 13, 5.5, 13),//tray
              makeCuboidShape(1, 7, 1, 15, 11, 15),//Shape1
              makeCuboidShape(0, 3, 3, 1, 13, 13),//portRight
              makeCuboidShape(15, 4, 4, 16, 12, 12),//portLeft
              makeCuboidShape(0, 5, 0, 16, 7, 2),//rimBack
              makeCuboidShape(0, 5, 2, 2, 7, 14),//rimRight
              makeCuboidShape(14, 5, 2, 16, 7, 14),//rimLeft
              makeCuboidShape(0, 5, 14, 16, 7, 16),//rimFront
              makeCuboidShape(14.5, 6, 14.5, 15.5, 11, 15.5),//support1
              makeCuboidShape(0.5, 6, 14.5, 1.5, 11, 15.5),//support2
              makeCuboidShape(14.5, 6, 0.5, 15.5, 11, 1.5),//support3
              makeCuboidShape(0.5, 6, 0.5, 1.5, 11, 1.5)//support4
        )).build();
    
    public static final Machine<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = MachineBuilder
        .createMachine(MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR, MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR)
        .withSound(MekanismSounds.ELECTROLYTIC_SEPARATOR)
        .withConfig(() -> MekanismConfig.general.FROM_H2.get() * 2, MekanismConfig.storage.electrolyticSeparator::get)
        .withCustomShape(VoxelShapeUtils.combine(
            makeCuboidShape(0, 0, 0, 16, 4, 16),//base
            makeCuboidShape(15, 3, 3, 16, 13, 13),//portToggle1
            makeCuboidShape(0, 4, 4, 1, 12, 12),//portToggle2a
            makeCuboidShape(4, 4, 0, 12, 12, 1),//portToggle3a
            makeCuboidShape(4, 4, 15, 12, 12, 16),//portToggle4a
            makeCuboidShape(1, 4, 7, 3, 11, 9),//portToggle2b
            makeCuboidShape(7, 4, 1, 8, 11, 3),//portToggle3b
            makeCuboidShape(7, 4, 13, 8, 11, 15),//portToggle4b
            makeCuboidShape(8, 4, 0, 16, 16, 16),//tank1
            makeCuboidShape(0, 4, 9, 7, 14, 16),//tank2
            makeCuboidShape(0, 4, 0, 7, 14, 7),//tank3
            makeCuboidShape(6.5, 10, 7.5, 9.5, 11, 8.5),//tube1
            makeCuboidShape(3, 12, 7.5, 7, 13, 8.5),//tube2
            makeCuboidShape(3, 12, 7.5, 4, 15, 8.5),//tube3
            makeCuboidShape(3, 15, 3, 4, 16, 13),//tube4
            makeCuboidShape(3, 14, 3, 4, 15, 4),//tube5
            makeCuboidShape(3, 14, 12, 4, 15, 13)//tube6
        )).build();
    public static final Machine<TileEntityDigitalMiner> DIGITAL_MINER = MachineBuilder
        .createMachine(MekanismTileEntityTypes.DIGITAL_MINER, MekanismContainerTypes.DIGITAL_MINER, MekanismLang.DESCRIPTION_DIGITAL_MINER)
        .withConfig(MekanismConfig.usage.digitalMiner::get, MekanismConfig.storage.digitalMiner::get)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.ANCHOR))
        .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new DigitalMinerContainer(i, inv, tile)))
        .withCustomShape(VoxelShapeUtils.combine(
            makeCuboidShape(5, 9, -14, 6, 10, -13),
            makeCuboidShape(10, 9, -14, 11, 10, -13),
            makeCuboidShape(10, 9, -13, 11, 11, -9),
            makeCuboidShape(5, 9, -13, 6, 11, -9),
            makeCuboidShape(10, 20, -11, 12, 22, -9),
            makeCuboidShape(4, 20, -11, 6, 22, -9),
            makeCuboidShape(-8, 3, -9, 24, 32, 3),
            makeCuboidShape(-8, 3, 20, 24, 32, 32),
            makeCuboidShape(-8, 3, 4, 24, 8, 19),
            makeCuboidShape(24, 24, -8, 29, 29, -6),
            makeCuboidShape(24, 24, 0, 29, 29, 2),
            makeCuboidShape(24, 24, 21, 29, 29, 23),
            makeCuboidShape(24, 24, 29, 29, 29, 31),
            makeCuboidShape(-13, 24, -8, -8, 29, -6),
            makeCuboidShape(-13, 24, 0, -8, 29, 2),
            makeCuboidShape(-13, 24, 21, -8, 29, 23),
            makeCuboidShape(-13, 24, 29, -8, 29, 31),
            makeCuboidShape(24, 24, -6, 25, 29, 0),
            makeCuboidShape(24, 24, 23, 25, 29, 29),
            makeCuboidShape(-9, 24, -6, -8, 29, 0),
            makeCuboidShape(-9, 24, 23, -8, 29, 29),
            makeCuboidShape(26, 2, -7, 30, 30, 1),
            makeCuboidShape(26, 2, 22, 30, 30, 30),
            makeCuboidShape(-14, 2, -7, -10, 30, 1),
            makeCuboidShape(-14, 2, 22, -10, 30, 30),
            makeCuboidShape(24, 0, -8, 31, 2, 2),
            makeCuboidShape(24, 0, 21, 31, 2, 31),
            makeCuboidShape(-15, 0, 21, -8, 2, 31),
            makeCuboidShape(-15, 0, -8, -8, 2, 2),
            makeCuboidShape(-7, 4, 3, 23, 31, 20),
            makeCuboidShape(5, 2, -6, 11, 4, 5),
            makeCuboidShape(5, 1, 5, 11, 4, 11),
            makeCuboidShape(-15, 5, 5, -6, 11, 11),
            makeCuboidShape(22, 5, 5, 31, 11, 11),
            makeCuboidShape(4, 0, 4, 12, 1, 12),
            makeCuboidShape(-16, 4, 4, -15, 12, 12),
            makeCuboidShape(-9, 4, 4, -8, 12, 12),
            makeCuboidShape(31, 4, 4, 32, 12, 12),
            makeCuboidShape(24, 4, 4, 25, 12, 12),
            makeCuboidShape(-8, 27, 4, 24, 32, 19),
            makeCuboidShape(-8, 21, 4, 24, 26, 19),
            makeCuboidShape(-8, 15, 4, 24, 20, 19),
            makeCuboidShape(-8, 9, 4, 24, 14, 19),
            //Keyboard
            makeCuboidShape(3, 11, -10.5, 13, 12.5, -11.75),
            makeCuboidShape(3, 10, -11.75, 13, 11.5, -13),
            makeCuboidShape(3, 9.5, -13, 13, 11, -14.25),
            makeCuboidShape(3, 9, -14.25, 13, 10.5, -15.25),
            makeCuboidShape(4, 9.5, -12, 12, 10, -13),
            makeCuboidShape(4, 8.5, -13, 12, 9.5, -14.25),
            //Center monitor
            makeCuboidShape(2, 18, -10.5, 14, 24, -11.5),
            makeCuboidShape(1, 16, -11.5, 15, 26, -13.5),
            //Left monitor
            makeCuboidShape(17, 17.75, -10, 18.5, 24.25, -11.5),
            makeCuboidShape(18.5, 17.75, -10.5, 22, 24.25, -12),
            makeCuboidShape(22, 17.75, -11.5, 25.5, 24.25, -13),
            makeCuboidShape(25.5, 17.75, -12.5, 29, 24.25, -14),
            makeCuboidShape(15.5, 16, -11.5, 19.5, 26, -13.5),
            makeCuboidShape(18.5, 16, -12, 23, 26, -14),
            makeCuboidShape(22, 16, -13, 26.5, 26, -15),
            makeCuboidShape(25.5, 16, -14, 30, 26, -16),
            //Right Monitor
            makeCuboidShape(-3 + 2.5, 17.75, -10, -6.5 + 2.5, 24.25, -11.5),
            makeCuboidShape(-6.5 + 2.5, 17.75, -10.5, -10 + 2.5, 24.25, -12),
            makeCuboidShape(-10 + 2.5, 17.75, -11.5, -13.5 + 2.5, 24.25, -13),
            makeCuboidShape(-13.5 + 2.5, 17.75, -12.5, -15 + 2.5, 24.25, -14),
            makeCuboidShape(-6.5 + 2.5, 16, -11.5, -2 + 2.5, 26, -13.5),
            makeCuboidShape(-10 + 2.5, 16, -12, -5.5 + 2.5, 26, -14),
            makeCuboidShape(-13.5 + 2.5, 16, -13, -9 + 2.5, 26, -15),
            makeCuboidShape(-16.5 + 2.5, 16, -14, -12.5 + 2.5, 26, -16)
        )).build();
}
