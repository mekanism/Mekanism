package mekanism.generators.common.loot;

import mekanism.common.loot.table.BaseBlockLootTables;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class GeneratorsBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void addTables() {
        registerDropSelfWithContentsLootTable(GeneratorsBlocks.BLOCKS.getAllBlocks());
    }
}