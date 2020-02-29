package mekanism.common.registries;

import java.util.EnumSet;
import mekanism.api.Upgrade;
import mekanism.api.block.FactoryType;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.machines.Machine;
import mekanism.common.content.machines.Machine.MachineBuilder;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.TileEntityPurificationChamber;

public class MekanismMachines {
    // Enrichment Chamber
    public static final Machine<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.ENRICHMENT_CHAMBER, MekanismContainerTypes.ENRICHMENT_CHAMBER, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, MekanismSounds.ENRICHMENT_CHAMBER, FactoryType.ENRICHING)
        .withConfig(MekanismConfig.usage.enrichmentChamber, MekanismConfig.storage.enrichmentChamber)
        .withFactoryHierarchy(MekanismBlocks.BASIC_ENRICHING_FACTORY, MekanismBlocks.ADVANCED_ENRICHING_FACTORY, MekanismBlocks.ELITE_ENRICHING_FACTORY, MekanismBlocks.ULTIMATE_ENRICHING_FACTORY)
        .build();
    // Crusher
    public static final Machine<TileEntityCrusher> CRUSHER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.CRUSHER, MekanismContainerTypes.CRUSHER, MekanismLang.DESCRIPTION_CRUSHER, MekanismSounds.CRUSHER, FactoryType.CRUSHING)
        .withConfig(MekanismConfig.usage.crusher, MekanismConfig.storage.crusher)
        .withFactoryHierarchy(MekanismBlocks.BASIC_CRUSHING_FACTORY, MekanismBlocks.ADVANCED_CRUSHING_FACTORY, MekanismBlocks.ELITE_CRUSHING_FACTORY, MekanismBlocks.ULTIMATE_CRUSHING_FACTORY).build();
    // Energized Smelter
    public static final Machine<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.ENERGIZED_SMELTER, MekanismContainerTypes.ENERGIZED_SMELTER, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, MekanismSounds.ENERGIZED_SMELTER, FactoryType.SMELTING)
        .withConfig(MekanismConfig.usage.energizedSmelter, MekanismConfig.storage.energizedSmelter)
        .withFactoryHierarchy(MekanismBlocks.BASIC_SMELTING_FACTORY, MekanismBlocks.ADVANCED_SMELTING_FACTORY, MekanismBlocks.ELITE_SMELTING_FACTORY, MekanismBlocks.ULTIMATE_SMELTING_FACTORY).build();
    // Precision Sawmill
    public static final Machine<TileEntityPrecisionSawmill> PRECISION_SAWMILL = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.PRECISION_SAWMILL, MekanismContainerTypes.PRECISION_SAWMILL, MekanismLang.DESCRIPTION_PRECISION_SAWMILL, MekanismSounds.PRECISION_SAWMILL, FactoryType.SAWING)
        .withConfig(MekanismConfig.usage.precisionSawmill, MekanismConfig.storage.precisionSawmill)
        .withFactoryHierarchy(MekanismBlocks.BASIC_SAWING_FACTORY, MekanismBlocks.ADVANCED_SAWING_FACTORY, MekanismBlocks.ELITE_SAWING_FACTORY, MekanismBlocks.ULTIMATE_SAWING_FACTORY).build();
    // Osmium Compressor
    public static final Machine<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.OSMIUM_COMPRESSOR, MekanismContainerTypes.OSMIUM_COMPRESSOR, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, MekanismSounds.OSMIUM_COMPRESSOR, FactoryType.COMPRESSING)
        .withConfig(MekanismConfig.usage.osmiumCompressor, MekanismConfig.storage.osmiumCompressor)
        .withFactoryHierarchy(MekanismBlocks.BASIC_COMPRESSING_FACTORY, MekanismBlocks.ADVANCED_COMPRESSING_FACTORY, MekanismBlocks.ELITE_COMPRESSING_FACTORY, MekanismBlocks.ULTIMATE_COMPRESSING_FACTORY)
        .build();
    // Combiner
    public static final Machine<TileEntityCombiner> COMBINER = MachineBuilder.createFactoryMachine(MekanismTileEntityTypes.COMBINER, MekanismContainerTypes.COMBINER, MekanismLang.DESCRIPTION_COMBINER, MekanismSounds.COMBINER, FactoryType.COMBINING)
        .withConfig(MekanismConfig.usage.combiner, MekanismConfig.storage.combiner)
        .withFactoryHierarchy(MekanismBlocks.BASIC_COMBINING_FACTORY, MekanismBlocks.ADVANCED_COMBINING_FACTORY, MekanismBlocks.ELITE_COMBINING_FACTORY, MekanismBlocks.ULTIMATE_COMBINING_FACTORY)
        .build();
    // Metallurgic Infuser
    public static final Machine<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.METALLURGIC_INFUSER, MekanismContainerTypes.METALLURGIC_INFUSER, MekanismLang.DESCRIPTION_METALLURGIC_INFUSER, MekanismSounds.METALLURGIC_INFUSER, FactoryType.INFUSING)
        .withConfig(MekanismConfig.usage.metallurgicInfuser, MekanismConfig.storage.metallurgicInfuser)
        .withFactoryHierarchy(MekanismBlocks.BASIC_INFUSING_FACTORY, MekanismBlocks.ADVANCED_INFUSING_FACTORY, MekanismBlocks.ELITE_INFUSING_FACTORY, MekanismBlocks.ULTIMATE_INFUSING_FACTORY).build();
    // Purification Chamber
    public static final Machine<TileEntityPurificationChamber> PURIFICATION_CHAMBER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.PURIFICATION_CHAMBER, MekanismContainerTypes.PURIFICATION_CHAMBER, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, MekanismSounds.PURIFICATION_CHAMBER, FactoryType.PURIFYING)
        .withConfig(MekanismConfig.usage.purificationChamber, MekanismConfig.storage.purificationChamber)
        .withFactoryHierarchy(MekanismBlocks.BASIC_PURIFYING_FACTORY, MekanismBlocks.ADVANCED_PURIFYING_FACTORY, MekanismBlocks.ELITE_PURIFYING_FACTORY, MekanismBlocks.ULTIMATE_PURIFYING_FACTORY)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS)).build();
    // Chemical Injection Chamber
    public static final Machine<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = MachineBuilder
        .createFactoryMachine(MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, MekanismSounds.CHEMICAL_INJECTION_CHAMBER, FactoryType.INJECTING)
        .withConfig(MekanismConfig.usage.chemicalInjectionChamber, MekanismConfig.storage.chemicalInjectionChamber)
        .withFactoryHierarchy(MekanismBlocks.BASIC_INJECTING_FACTORY, MekanismBlocks.ADVANCED_INJECTING_FACTORY, MekanismBlocks.ELITE_INJECTING_FACTORY, MekanismBlocks.ULTIMATE_INJECTING_FACTORY)
        .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS)).build();

    // Pressurized Reaction Chamber
    public static final Machine<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = MachineBuilder
        .createMachine(MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, MekanismLang.DESCRIPTION_PRESSURIZED_REACTION_CHAMBER, MekanismSounds.PRESSURIZED_REACTION_CHAMBER)
        .withConfig(MekanismConfig.usage.pressurizedReactionBase, MekanismConfig.storage.pressurizedReactionBase).build();
    // Chemical Crystallizer
    public static final Machine<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = MachineBuilder
        .createMachine(MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER, MekanismSounds.CHEMICAL_CRYSTALLIZER)
        .withConfig(MekanismConfig.usage.chemicalCrystallizer, MekanismConfig.storage.chemicalCrystallizer).build();
    // Electrolytic Separator
    public static final Machine<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = MachineBuilder
        .createMachine(MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR, MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR, MekanismSounds.ELECTROLYTIC_SEPARATOR)
        .withConfig(() -> MekanismConfig.general.FROM_H2.get() * 2, MekanismConfig.storage.electrolyticSeparator::get).build();
}
