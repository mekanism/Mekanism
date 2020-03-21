package mekanism.generators.common.loot;

import mekanism.common.loot.table.BaseBlockLootTables;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class GeneratorsBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void addTables() {
        registerDropSelfLootTable(
              GeneratorsBlocks.TURBINE_ROTOR,
              GeneratorsBlocks.ROTATIONAL_COMPLEX,
              GeneratorsBlocks.ELECTROMAGNETIC_COIL,
              GeneratorsBlocks.TURBINE_CASING,
              GeneratorsBlocks.TURBINE_VALVE,
              GeneratorsBlocks.TURBINE_VENT,
              GeneratorsBlocks.SATURATING_CONDENSER,
              GeneratorsBlocks.REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.REACTOR_FRAME,
              GeneratorsBlocks.REACTOR_GLASS,
              GeneratorsBlocks.REACTOR_PORT,
              GeneratorsBlocks.LASER_FOCUS_MATRIX
        );
        registerDropSelfWithContentsLootTable(
              GeneratorsBlocks.HEAT_GENERATOR,
              GeneratorsBlocks.SOLAR_GENERATOR,
              GeneratorsBlocks.GAS_BURNING_GENERATOR,
              GeneratorsBlocks.BIO_GENERATOR,
              GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR,
              GeneratorsBlocks.REACTOR_CONTROLLER
        );
    }
}