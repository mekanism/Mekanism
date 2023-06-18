package mekanism.defense.common.loot;

import mekanism.common.loot.table.BaseBlockLootTables;
import mekanism.defense.common.registries.DefenseBlocks;

public class DefenseBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void generate() {
        //Register all remaining blocks as just dropping themselves
        dropSelf(DefenseBlocks.BLOCKS.getAllBlocks());
    }
}