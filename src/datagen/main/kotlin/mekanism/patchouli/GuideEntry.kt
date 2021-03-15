package mekanism.patchouli

import mekanism.patchouli.dsl.IGuideEntry
import java.util.HashSet
import java.util.Locale

private val UNIQUE_CACHE: MutableSet<String> = HashSet()

enum class GuideEntry(folder: String?, name: String): IGuideEntry {
    PIPES_LOGISTICAL("pipes", "logistical"),
    PIPES_MECHANICAL("pipes", "mechanical"),
    PIPES_GAS("pipes", "gas"),
    PIPES_HEAT("pipes", "heat"),
    PIPES_POWER("pipes", "power"),
    TANKS_LIQUID("tanks", "liquid"),
    TANKS_GAS("tanks", "gas"),
    BINS("bins"),
    ENERGY_CUBES("energy_cubes"),
    INDUCTION_CELL("induction", "cell"),
    INDUCTION_PROVIDER("induction", "provider"),
    ALLOYS("items", "alloys"),
    INSTALLERS("items", "installers"),
    CIRCUITS("items", "circuits"),
    THERMAL_EVAP("multiblocks", "thermal_evaporation"),
    DYNAMIC_TANK("multiblocks", "dynamic_tank"),
    TELEPORTER("multiblocks", "teleporter"),
    INDUCTION("multiblocks", "induction_matrix"),
    BOILER("multiblocks", "boiler"),
    ORE_DOUBLING("ore_processing", "doubling"),
    ORE_TRIPLING("ore_processing", "tripling"),
    ORE_QUADRUPLING("ore_processing", "quadrupling"),
    ORE_QUINTUPLING("ore_processing", "quintupling"),

    //fixme?
    /* Disassembler entries are really pages, but have title and text */
    DISASSEMBLER_NORMAL("items", "atomic_disassembler.normal"),
    DISASSEMBLER_SLOW("items", "atomic_disassembler.slow"),
    DISASSEMBLER_FAST("items", "atomic_disassembler.fast"),
    DISASSEMBLER_VEIN("items", "atomic_disassembler.vein"),
    DISASSEMBLER_EXTENDED_VEIN("items", "atomic_disassembler.extended_vein"),
    DISASSEMBLER_OFF("items", "atomic_disassembler.off"),

    //fixme?
    GENERATORS_TURBINE("multiblocks", "industrial_turbine"),
    GENERATORS_FUSION("multiblocks", "fusion_reactor"),

    CHEMICAL_DEUTERIUM("chemicals", "deuterium"),
    CHEMICAL_TRITIUM("chemicals", "tritium"),
    CHEMICAL_DT_FUEL("chemicals", "dt_fuel"),

    LIQUID_HEAVY_WATER("liquids", "heavy_water")

    ;

    override val entryId: String = IGuideEntry.generate(folder, name)

    init {
        require(UNIQUE_CACHE.add(entryId)) { "Duplicate page id: $entryId" }
    }

    constructor(name: String) : this(null, name)
}