package mekanism.defense.common.loot;

import mekanism.common.loot.table.BaseBlockLootTables;
import mekanism.defense.common.registries.DefenseBlocks;
import net.minecraft.core.HolderLookup;

public class DefenseBlockLootTables extends BaseBlockLootTables {

    public DefenseBlockLootTables(HolderLookup.Provider provider) {
        super(provider);
    }

    @Override
    protected void generate() {
        //Register all remaining blocks as just dropping themselves
        dropSelf(DefenseBlocks.BLOCKS.getPrimaryEntries());
    }
}