package mekanism.common.loot.table;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.RandomValueRange;

public class MekanismBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void addTables() {
        registerLootTable((block) -> droppingWithSilkTouchOrRandomly(block, MekanismItems.SALT, ConstantRange.of(4)), MekanismBlocks.SALT_BLOCK);
        registerLootTable((block) -> droppingWithSilkTouchOrRandomly(block, MekanismItems.FLUORITE_GEM, RandomValueRange.of(2, 4)), MekanismBlocks.FLUORITE_ORE);
        registerDropSelfLootTable(
              //Ores
              MekanismBlocks.OSMIUM_ORE,
              MekanismBlocks.COPPER_ORE,
              MekanismBlocks.TIN_ORE,
              MekanismBlocks.URANIUM_ORE,
              //Storage blocks
              MekanismBlocks.OSMIUM_BLOCK,
              MekanismBlocks.BRONZE_BLOCK,
              MekanismBlocks.REFINED_OBSIDIAN_BLOCK,
              MekanismBlocks.CHARCOAL_BLOCK,
              MekanismBlocks.REFINED_GLOWSTONE_BLOCK,
              MekanismBlocks.STEEL_BLOCK,
              MekanismBlocks.COPPER_BLOCK,
              MekanismBlocks.TIN_BLOCK,
              //Other things
              MekanismBlocks.BOILER_CASING,
              MekanismBlocks.BOILER_VALVE,
              MekanismBlocks.DYNAMIC_TANK,
              MekanismBlocks.DYNAMIC_VALVE,
              MekanismBlocks.INDUCTION_CASING,
              MekanismBlocks.INDUCTION_PORT,
              MekanismBlocks.PRESSURE_DISPERSER,
              MekanismBlocks.STEEL_CASING,
              MekanismBlocks.STRUCTURAL_GLASS,
              MekanismBlocks.SUPERHEATING_ELEMENT,
              MekanismBlocks.TELEPORTER_FRAME,
              MekanismBlocks.THERMAL_EVAPORATION_BLOCK,
              MekanismBlocks.THERMAL_EVAPORATION_VALVE,
              MekanismBlocks.RADIOACTIVE_WASTE_BARREL,
              MekanismBlocks.BASIC_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER,
              //Transmitters
              MekanismBlocks.RESTRICTIVE_TRANSPORTER,
              MekanismBlocks.DIVERSION_TRANSPORTER,
              MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE,
              MekanismBlocks.BASIC_MECHANICAL_PIPE, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismBlocks.ULTIMATE_MECHANICAL_PIPE,
              MekanismBlocks.BASIC_PRESSURIZED_TUBE, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE,
              MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER,
              MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR
        );
        registerDropSelfWithContentsLootTable(
              MekanismBlocks.CARDBOARD_BOX,
              MekanismBlocks.CHARGEPAD,
              MekanismBlocks.CHEMICAL_CRYSTALLIZER,
              MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER,
              MekanismBlocks.CHEMICAL_INFUSER,
              MekanismBlocks.CHEMICAL_INJECTION_CHAMBER,
              MekanismBlocks.CHEMICAL_OXIDIZER,
              MekanismBlocks.CHEMICAL_WASHER,
              MekanismBlocks.COMBINER,
              MekanismBlocks.CRUSHER,
              MekanismBlocks.DIGITAL_MINER,
              MekanismBlocks.ELECTRIC_PUMP,
              MekanismBlocks.ELECTROLYTIC_SEPARATOR,
              MekanismBlocks.ENERGIZED_SMELTER,
              MekanismBlocks.ENRICHMENT_CHAMBER,
              MekanismBlocks.FLUIDIC_PLENISHER,
              MekanismBlocks.FORMULAIC_ASSEMBLICATOR,
              MekanismBlocks.FUELWOOD_HEATER,
              MekanismBlocks.LASER,
              MekanismBlocks.LASER_AMPLIFIER,
              MekanismBlocks.LASER_TRACTOR_BEAM,
              MekanismBlocks.LOGISTICAL_SORTER,
              MekanismBlocks.METALLURGIC_INFUSER,
              MekanismBlocks.OREDICTIONIFICATOR,
              MekanismBlocks.OSMIUM_COMPRESSOR,
              MekanismBlocks.PERSONAL_CHEST,
              MekanismBlocks.PRECISION_SAWMILL,
              MekanismBlocks.PRESSURIZED_REACTION_CHAMBER,
              MekanismBlocks.PURIFICATION_CHAMBER,
              MekanismBlocks.QUANTUM_ENTANGLOPORTER,
              MekanismBlocks.RESISTIVE_HEATER,
              MekanismBlocks.ROTARY_CONDENSENTRATOR,
              MekanismBlocks.SECURITY_DESK,
              MekanismBlocks.SEISMIC_VIBRATOR,
              MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR,
              MekanismBlocks.TELEPORTER,
              MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER,
              MekanismBlocks.MODIFICATION_STATION,
              MekanismBlocks.ISOTOPIC_CENTRIFUGE,
              MekanismBlocks.NUTRITIONAL_LIQUIFIER,
              //Tiered things
              MekanismBlocks.BASIC_INDUCTION_CELL, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ULTIMATE_INDUCTION_CELL,
              MekanismBlocks.BASIC_BIN, MekanismBlocks.ADVANCED_BIN, MekanismBlocks.ELITE_BIN, MekanismBlocks.ULTIMATE_BIN, MekanismBlocks.CREATIVE_BIN,
              MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismBlocks.CREATIVE_ENERGY_CUBE,
              MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.CREATIVE_FLUID_TANK,
              MekanismBlocks.BASIC_GAS_TANK, MekanismBlocks.ADVANCED_GAS_TANK, MekanismBlocks.ELITE_GAS_TANK, MekanismBlocks.ULTIMATE_GAS_TANK, MekanismBlocks.CREATIVE_GAS_TANK
        );
        registerDropSelfWithContentsLootTable(MekanismBlocks.getFactoryBlocks());
    }
}