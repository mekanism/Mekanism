package mekanism.common.registries;

import mekanism.api.block.FactoryType;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.MachineType;
import mekanism.common.tile.*;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MekanismMachines {
    private static final List<MachineType<?>> machines = new ArrayList<>();

    public static final MachineType<TileEntityCrusher> CRUSHER = register("crusher", TileEntityCrusher.class, MekanismLang.DESCRIPTION_CRUSHER, MekanismConfig.usage.crusher::get, MekanismConfig.storage.crusher::get, FactoryType.CRUSHING);
    public static final MachineType<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = register("enrichment_chamber", TileEntityEnrichmentChamber.class, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, MekanismConfig.usage.enrichmentChamber::get, MekanismConfig.storage.enrichmentChamber::get, FactoryType.ENRICHING);
    public static final MachineType<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = register("energized_smelter", TileEntityEnergizedSmelter.class, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, MekanismConfig.usage.energizedSmelter::get, MekanismConfig.storage.energizedSmelter::get, FactoryType.SMELTING);

    public static final MachineType<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = register("osmium_compressor", TileEntityOsmiumCompressor.class, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, MekanismConfig.usage.osmiumCompressor::get, MekanismConfig.storage.osmiumCompressor::get, FactoryType.COMPRESSING);
    public static final MachineType<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = register("chemical_injection_chamber", TileEntityChemicalInjectionChamber.class, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, MekanismConfig.usage.chemicalInjectionChamber::get, MekanismConfig.storage.chemicalInjectionChamber::get, FactoryType.INJECTING).supportsGas();
    public static final MachineType<TileEntityPurificationChamber> PURIFICATION_CHAMBER = register("purification_chamber", TileEntityPurificationChamber.class, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, MekanismConfig.usage.purificationChamber::get, MekanismConfig.storage.purificationChamber::get, FactoryType.PURIFYING).supportsGas();

    public static final MachineType<TileEntityCombiner> COMBINER = register("combiner", TileEntityCombiner.class, MekanismLang.DESCRIPTION_COMBINER, MekanismConfig.usage.combiner::get, MekanismConfig.storage.combiner::get, FactoryType.COMBINING);

    private static <TILE extends TileEntityOperationalMachine<?>> MachineType<TILE> register(String name, Class<TILE> tileClass, ILangEntry description, Supplier<Double> usageSupplier, Supplier<Double> storageSupplier, FactoryType factoryType) {
        MachineType<TILE> machine = new MachineType<>(name, tileClass, description, usageSupplier, storageSupplier, factoryType);
        machines.add(machine);
        return machine;
    }

    public static void registerAll() {
        for(MachineType<?> machine : machines) {
            machine.register();
        }
    }
}
