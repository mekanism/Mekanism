package mekanism.generators.common.loot;

import mekanism.common.loot.BaseBlockLootTables;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.data.loot.LootTableSubProvider;

public class GeneratorsBlockLootTables extends BaseBlockLootTables {

    public GeneratorsBlockLootTables(LootTableSubProvider.Context context) {
        super(context);
    }

    @Override
    protected void generate() {
        dropSelfWithContents(GeneratorsBlocks.BLOCKS.getPrimaryEntries());
    }
}