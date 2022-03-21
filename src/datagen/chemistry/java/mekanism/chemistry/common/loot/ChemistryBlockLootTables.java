package mekanism.chemistry.common.loot;

import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.common.loot.table.BaseBlockLootTables;

public class ChemistryBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void addTables() {
        dropSelfWithContents(ChemistryBlocks.BLOCKS.getAllBlocks());
    }
}
