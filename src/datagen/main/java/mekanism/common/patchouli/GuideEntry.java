package mekanism.common.patchouli;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Thiakil on 19/05/2020.
 */
public enum GuideEntry {
    PIPES_LOGISTICAL("pipes", "logistical"),
    PIPES_MECHANICAL("pipes","mechanical"),
    PIPES_GAS("pipes","gas"),
    PIPES_HEAT("pipes","heat"),
    PIPES_POWER("pipes","power"),
    TANKS_LIQUID("tanks","liquid"),
    TANKS_GAS("tanks", "gas"),
    BINS("bins"),
    ENERGY_CUBES("energy_cubes"),
    INDUCTION_CELL("induction","cell"),
    INDUCTION_PROVIDER("induction","provider"),
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

    /* Disassembler entries are really pages, but have title and text */
    DISASSEMBLER_NORMAL("items", "atomic_disassembler.normal"),
    DISASSEMBLER_SLOW("items", "atomic_disassembler.slow"),
    DISASSEMBLER_FAST("items", "atomic_disassembler.fast"),
    DISASSEMBLER_VEIN("items", "atomic_disassembler.vein"),
    DISASSEMBLER_EXTENDED_VEIN("items", "atomic_disassembler.extended_vein"),
    DISASSEMBLER_OFF("items", "atomic_disassembler.off"),

    GENERATORS_TURBINE("multiblocks", "industrial_turbine"),
    GENERATORS_FUSION("multiblocks", "fusion_reactor"),

    CHEMICAL_HYDROGEN("chemicals", "hydrogen"),
    CHEMICAL_OXYGEN("chemicals", "oxygen"),
    CHEMICAL_CHLORINE("chemicals", "chlorine"),
    CHEMICAL_HYDROGEN_CHLORIDE("chemicals", "hydrogen_chloride"),
    CHEMICAL_SULFUR_DIOXIDE("chemicals", "sulfur_dioxide"),
    CHEMICAL_SULFUR_TRIOXIDE("chemicals", "sulfur_trioxide"),
    CHEMICAL_BRINE("chemicals", "gaseous_brine"),
    CHEMICAL_WATER_VAPOR("chemicals", "water_vapor"),
    CHEMICAL_SULFURIC_ACID("chemicals", "sulfuric_acid"),
    CHEMICAL_ETHYLENE("chemicals", "ethylene"),
    CHEMICAL_DEUTERIUM("chemicals", "deuterium"),
    CHEMICAL_TRITIUM("chemicals", "tritium"),
    CHEMICAL_DT_FUEL("chemicals", "dt_fuel"),
    CHEMICAL_LITHIUM("chemicals", "lithium"),
    CHEMICAL_SODIUM("chemicals", "sodium")
    ;

    static {
        Set<String> UNIQUE_CACHE = new HashSet<>();
        for (GuideEntry guidePage : values()) {
            if (!UNIQUE_CACHE.add(guidePage.entryId)) {
                throw new IllegalArgumentException("Duplicate page id: "+guidePage.entryId);
            }
        }
    }

    private final String entryId;

    GuideEntry(String name) {
        this(null, name);
    }
    GuideEntry(String folder, String name) {
        this.entryId = (folder != null ? folder + "/" : "") + name.toLowerCase(Locale.ROOT);
    }

    public String getEntryId() {
        return entryId;
    }

}
