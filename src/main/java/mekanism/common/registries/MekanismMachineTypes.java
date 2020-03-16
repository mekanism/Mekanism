package mekanism.common.registries;

import java.util.EnumSet;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import mekanism.api.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockShapes;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.Factory.FactoryBuilder;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.FactoryMachine;
import mekanism.common.content.blocktype.Machine.MachineBuilder;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.tier.FactoryTier;
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
import mekanism.common.util.text.TextComponentUtil;

public class MekanismMachineTypes {

    private static final Table<FactoryTier, FactoryType, Factory<?>> FACTORIES = HashBasedTable.create();

    // Enrichment Chamber
    public static final FactoryMachine<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.ENRICHMENT_CHAMBER, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, FactoryType.ENRICHING)
          .withGui(() -> MekanismContainerTypes.ENRICHMENT_CHAMBER)
          .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
          .withConfig(MekanismConfig.usage.enrichmentChamber, MekanismConfig.storage.enrichmentChamber)
          .build();
    // Crusher
    public static final FactoryMachine<TileEntityCrusher> CRUSHER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.CRUSHER, MekanismLang.DESCRIPTION_CRUSHER, FactoryType.CRUSHING)
          .withGui(() -> MekanismContainerTypes.CRUSHER)
          .withSound(MekanismSounds.CRUSHER)
          .withConfig(MekanismConfig.usage.crusher, MekanismConfig.storage.crusher)
          .build();
    // Energized Smelter
    public static final FactoryMachine<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.ENERGIZED_SMELTER, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, FactoryType.SMELTING)
          .withGui(() -> MekanismContainerTypes.ENERGIZED_SMELTER)
          .withSound(MekanismSounds.ENERGIZED_SMELTER)
          .withConfig(MekanismConfig.usage.energizedSmelter, MekanismConfig.storage.energizedSmelter)
          .build();
    // Precision Sawmill
    public static final FactoryMachine<TileEntityPrecisionSawmill> PRECISION_SAWMILL = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.PRECISION_SAWMILL, MekanismLang.DESCRIPTION_PRECISION_SAWMILL, FactoryType.SAWING)
          .withGui(() -> MekanismContainerTypes.PRECISION_SAWMILL)
          .withSound(MekanismSounds.PRECISION_SAWMILL)
          .withConfig(MekanismConfig.usage.precisionSawmill, MekanismConfig.storage.precisionSawmill)
          .build();
    // Osmium Compressor
    public static final FactoryMachine<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.OSMIUM_COMPRESSOR, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, FactoryType.COMPRESSING)
          .withGui(() -> MekanismContainerTypes.OSMIUM_COMPRESSOR)
          .withSound(MekanismSounds.OSMIUM_COMPRESSOR)
          .withConfig(MekanismConfig.usage.osmiumCompressor, MekanismConfig.storage.osmiumCompressor)
          .build();
    // Combiner
    public static final FactoryMachine<TileEntityCombiner> COMBINER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.COMBINER, MekanismLang.DESCRIPTION_COMBINER, FactoryType.COMBINING)
          .withGui(() -> MekanismContainerTypes.COMBINER)
          .withSound(MekanismSounds.COMBINER)
          .withConfig(MekanismConfig.usage.combiner, MekanismConfig.storage.combiner)
          .build();
    // Metallurgic Infuser
    public static final FactoryMachine<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.METALLURGIC_INFUSER, MekanismLang.DESCRIPTION_METALLURGIC_INFUSER, FactoryType.INFUSING)
          .withGui(() -> MekanismContainerTypes.METALLURGIC_INFUSER)
          .withSound(MekanismSounds.METALLURGIC_INFUSER)
          .withConfig(MekanismConfig.usage.metallurgicInfuser, MekanismConfig.storage.metallurgicInfuser)
          .withCustomShape(BlockShapes.METALLURGIC_INFUSER)
          .build();
    // Purification Chamber
    public static final FactoryMachine<TileEntityPurificationChamber> PURIFICATION_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.PURIFICATION_CHAMBER, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, FactoryType.PURIFYING)
          .withGui(() -> MekanismContainerTypes.PURIFICATION_CHAMBER)
          .withSound(MekanismSounds.PURIFICATION_CHAMBER)
          .withConfig(MekanismConfig.usage.purificationChamber, MekanismConfig.storage.purificationChamber)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
          .build();
    // Chemical Injection Chamber
    public static final FactoryMachine<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = MachineBuilder
          .createFactoryMachine(() -> MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, FactoryType.INJECTING)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER)
          .withSound(MekanismSounds.CHEMICAL_INJECTION_CHAMBER)
          .withConfig(MekanismConfig.usage.chemicalInjectionChamber, MekanismConfig.storage.chemicalInjectionChamber)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
          .build();

    // Pressurized Reaction Chamber
    public static final Machine<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, MekanismLang.DESCRIPTION_PRESSURIZED_REACTION_CHAMBER)
          .withGui(() -> MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER)
          .withSound(MekanismSounds.PRESSURIZED_REACTION_CHAMBER)
          .withConfig(MekanismConfig.usage.pressurizedReactionBase, MekanismConfig.storage.pressurizedReactionBase)
          .withCustomShape(BlockShapes.PRESSURIZED_REACTION_CHAMBER)
          .build();
    // Chemical Crystallizer
    public static final Machine<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_CRYSTALLIZER)
          .withSound(MekanismSounds.CHEMICAL_CRYSTALLIZER)
          .withConfig(MekanismConfig.usage.chemicalCrystallizer, MekanismConfig.storage.chemicalCrystallizer)
          .withCustomShape(BlockShapes.CHEMICAL_CRYSTALLIZER)
          .build();
    // Chemical Dissolution Chamber
    public static final Machine<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_DISSOLUTION_CHAMBER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER)
          .withSound(MekanismSounds.CHEMICAL_DISSOLUTION_CHAMBER)
          .withConfig(MekanismConfig.usage.chemicalDissolutionChamber, MekanismConfig.storage.chemicalDissolutionChamber)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
          .withCustomShape(BlockShapes.CHEMICAL_DISSOLUTION_CHAMBER)
          .build();
    // Chemical Infuser
    public static final Machine<TileEntityChemicalInfuser> CHEMICAL_INFUSER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_INFUSER, MekanismLang.DESCRIPTION_CHEMICAL_INFUSER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_INFUSER)
          .withSound(MekanismSounds.CHEMICAL_INFUSER)
          .withConfig(MekanismConfig.usage.chemicalInfuser, MekanismConfig.storage.chemicalInfuser)
          .withCustomShape(BlockShapes.CHEMICAL_INFUSER)
          .build();
    // Chemical Oxidizer
    public static final Machine<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_OXIDIZER, MekanismLang.DESCRIPTION_CHEMICAL_OXIDIZER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_OXIDIZER)
          .withSound(MekanismSounds.CHEMICAL_OXIDIZER)
          .withConfig(MekanismConfig.usage.oxidationChamber, MekanismConfig.storage.oxidationChamber)
          .withCustomShape(BlockShapes.CHEMICAL_OXIDIZER)
          .build();
    // Chemical Washer
    public static final Machine<TileEntityChemicalWasher> CHEMICAL_WASHER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.CHEMICAL_WASHER, MekanismLang.DESCRIPTION_CHEMICAL_WASHER)
          .withGui(() -> MekanismContainerTypes.CHEMICAL_WASHER)
          .withSound(MekanismSounds.CHEMICAL_WASHER)
          .withConfig(MekanismConfig.usage.chemicalWasher, MekanismConfig.storage.chemicalWasher)
          .withCustomShape(BlockShapes.CHEMICAL_WASHER)
          .build();
    // Rotary Condensentrator
    public static final Machine<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ROTARY_CONDENSENTRATOR, MekanismLang.DESCRIPTION_ROTARY_CONDENSENTRATOR)
          .withGui(() -> MekanismContainerTypes.ROTARY_CONDENSENTRATOR)
          .withSound(MekanismSounds.ROTARY_CONDENSENTRATOR)
          .withConfig(MekanismConfig.usage.rotaryCondensentrator, MekanismConfig.storage.rotaryCondensentrator)
          .withCustomShape(BlockShapes.ROTARY_CONDENSENTRATOR)
          .build();
    // Electrolytic Separator
    public static final Machine<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR, MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR)
          .withGui(() -> MekanismContainerTypes.ELECTROLYTIC_SEPARATOR)
          .withSound(MekanismSounds.ELECTROLYTIC_SEPARATOR)
          .withConfig(() -> MekanismConfig.general.FROM_H2.get() * 2, MekanismConfig.storage.electrolyticSeparator::get)
          .withCustomShape(BlockShapes.ELECTROLYTIC_SEPARATOR)
          .build();
    // Digital Miner
    public static final Machine<TileEntityDigitalMiner> DIGITAL_MINER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.DIGITAL_MINER, MekanismLang.DESCRIPTION_DIGITAL_MINER)
          .withGui(() -> MekanismContainerTypes.DIGITAL_MINER)
          .withConfig(MekanismConfig.usage.digitalMiner::get, MekanismConfig.storage.digitalMiner::get)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.ANCHOR))
          .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new DigitalMinerContainer(i, inv, tile)))
          .withCustomShape(BlockShapes.DIGITAL_MINER)
          .build();
    // Formulaic Assemblicator
    public static final Machine<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR, MekanismLang.DESCRIPTION_FORMULAIC_ASSEMBLICATOR)
          .withGui(() -> MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR)
          .withConfig(MekanismConfig.usage.formulaicAssemblicator, MekanismConfig.storage.formulaicAssemblicator)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
          .withCustomContainer((tile) -> new ContainerProvider(TextComponentUtil.translate(tile.getBlockType().getTranslationKey()), (i, inv, player) -> new FormulaicAssemblicatorContainer(i, inv, tile)))
          .build();
    // Electric Pump
    public static final Machine<TileEntityElectricPump> ELECTRIC_PUMP = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.ELECTRIC_PUMP, MekanismLang.DESCRIPTION_ELECTRIC_PUMP)
          .withGui(() -> MekanismContainerTypes.ELECTRIC_PUMP)
          .withConfig(MekanismConfig.usage.electricPump, MekanismConfig.storage.electricPump)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.FILTER))
          .withCustomShape(BlockShapes.ELECTRIC_PUMP)
          .build();
    // Fluidic Plenisher
    public static final Machine<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = MachineBuilder
          .createMachine(() -> MekanismTileEntityTypes.FLUIDIC_PLENISHER, MekanismLang.DESCRIPTION_FLUIDIC_PLENISHER)
          .withGui(() -> MekanismContainerTypes.FLUIDIC_PLENISHER)
          .withConfig(MekanismConfig.usage.fluidicPlenisher, MekanismConfig.storage.fluidicPlenisher)
          .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
          .withCustomShape(BlockShapes.FLUIDIC_PLENISHER)
          .build();

    static {
        for (FactoryTier tier : FactoryTier.values()) {
            for (FactoryType type : FactoryType.values()) {
                FACTORIES.put(tier, type, FactoryBuilder.createFactory(() -> MekanismTileEntityTypes.getFactoryTile(tier, type), () -> MekanismContainerTypes.FACTORY, type.getBaseMachine(), tier).build());
            }
        }
    }

    public static Factory<?> getFactory(FactoryTier tier, FactoryType type) {
        return FACTORIES.get(tier, type);
    }
}
