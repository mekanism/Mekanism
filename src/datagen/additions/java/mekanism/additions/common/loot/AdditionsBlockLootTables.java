package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.common.loot.table.BaseBlockLootTables;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.TNTBlock;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.BlockStateProperty;

public class AdditionsBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void addTables() {
        //Obsidian TNT
        registerObsidianTNT();
        //Plastic slabs
        add(BaseBlockLootTables::createSlabItemTable, AdditionsBlocks.PLASTIC_SLABS.values());
        add(BaseBlockLootTables::createSlabItemTable, AdditionsBlocks.PLASTIC_GLOW_SLABS.values());
        add(BaseBlockLootTables::createSlabItemTable, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS.values());
        //Register all remaining blocks as just dropping themselves
        dropSelf(AdditionsBlocks.BLOCKS.getAllBlocks());
    }

    private void registerObsidianTNT() {
        Block tnt = AdditionsBlocks.OBSIDIAN_TNT.getBlock();
        add(tnt, LootTable.lootTable().withPool(applyExplosionCondition(tnt, LootPool.lootPool()
                    .name("main")
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(tnt)
                          .when(BlockStateProperty.hasBlockStateProperties(tnt)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TNTBlock.UNSTABLE, false)))
                    )
              ))
        );
    }
}