package mekanism.common.registries;

import mekanism.api.block.FactoryType;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.MachineType;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPurificationChamber;

public class MekanismMachines {

    public static final MachineType<TileEntityCrusher> CRUSHER = new MachineType<>("crusher", TileEntityCrusher.class, MekanismLang.DESCRIPTION_CRUSHER, MekanismConfig.usage.crusher::get, MekanismConfig.storage.crusher::get, TileEntityCrusher::new, FactoryType.CRUSHING);
    public static final MachineType<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = new MachineType<>("enrichment_chamber", TileEntityEnrichmentChamber.class, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, MekanismConfig.usage.enrichmentChamber::get, MekanismConfig.storage.enrichmentChamber::get, TileEntityEnrichmentChamber::new, FactoryType.ENRICHING);
    public static final MachineType<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = new MachineType<>("energized_smelter", TileEntityEnergizedSmelter.class, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, MekanismConfig.usage.energizedSmelter::get, MekanismConfig.storage.energizedSmelter::get, TileEntityEnergizedSmelter::new, FactoryType.SMELTING);

    public static final MachineType<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = new MachineType<>("osmium_compressor", TileEntityOsmiumCompressor.class, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, MekanismConfig.usage.osmiumCompressor::get, MekanismConfig.storage.osmiumCompressor::get, TileEntityOsmiumCompressor::new, FactoryType.COMPRESSING);
    public static final MachineType<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = new MachineType<>("chemical_injection_chamber", TileEntityChemicalInjectionChamber.class, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, MekanismConfig.usage.chemicalInjectionChamber::get, MekanismConfig.storage.chemicalInjectionChamber::get, TileEntityChemicalInjectionChamber::new, FactoryType.INJECTING).supportsGas();
    public static final MachineType<TileEntityPurificationChamber> PURIFICATION_CHAMBER = new MachineType<>("purification_chamber", TileEntityPurificationChamber.class, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, MekanismConfig.usage.purificationChamber::get, MekanismConfig.storage.purificationChamber::get, TileEntityPurificationChamber::new, FactoryType.PURIFYING).supportsGas();

    public static final MachineType<TileEntityCombiner> COMBINER = new MachineType<>("combiner", TileEntityCombiner.class, MekanismLang.DESCRIPTION_COMBINER, MekanismConfig.usage.combiner::get, MekanismConfig.storage.combiner::get, TileEntityCombiner::new, FactoryType.COMBINING);

    public static void registerAll() {
        //TODO: Remove this, is needed to statically initialize the machine type stuff
    }
}
