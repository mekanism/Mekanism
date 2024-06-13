package mekanism.generators.common.loot;

import mekanism.common.loot.table.BaseBlockLootTables;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.HolderLookup;

public class GeneratorsBlockLootTables extends BaseBlockLootTables {

    public GeneratorsBlockLootTables(HolderLookup.Provider provider) {
        super(provider);
    }

    @Override
    protected void generate() {
        dropSelfWithContents(GeneratorsBlocks.BLOCKS.getPrimaryEntries());
    }
}