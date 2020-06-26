package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.common.loot.table.BaseBlockLootTables;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.TNTBlock;
import net.minecraft.data.loot.BlockLootTables;
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
        registerLootTable(BlockLootTables::droppingSlab, AdditionsBlocks.BLACK_PLASTIC_SLAB, AdditionsBlocks.RED_PLASTIC_SLAB, AdditionsBlocks.GREEN_PLASTIC_SLAB,
              AdditionsBlocks.BROWN_PLASTIC_SLAB, AdditionsBlocks.BLUE_PLASTIC_SLAB, AdditionsBlocks.PURPLE_PLASTIC_SLAB, AdditionsBlocks.CYAN_PLASTIC_SLAB,
              AdditionsBlocks.LIGHT_GRAY_PLASTIC_SLAB, AdditionsBlocks.GRAY_PLASTIC_SLAB, AdditionsBlocks.PINK_PLASTIC_SLAB, AdditionsBlocks.LIME_PLASTIC_SLAB,
              AdditionsBlocks.YELLOW_PLASTIC_SLAB, AdditionsBlocks.LIGHT_BLUE_PLASTIC_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_SLAB, AdditionsBlocks.ORANGE_PLASTIC_SLAB,
              AdditionsBlocks.WHITE_PLASTIC_SLAB);
        //Register all remaining blocks as just dropping themselves
        registerDropSelfLootTable(AdditionsBlocks.BLOCKS.getAllBlocks());
    }

    private void registerObsidianTNT() {
        Block tnt = AdditionsBlocks.OBSIDIAN_TNT.getBlock();
        registerLootTable(tnt, LootTable.builder().addLootPool(withSurvivesExplosion(tnt, LootPool.builder().rolls(ConstantRange.of(1))
              .addEntry(ItemLootEntry.builder(tnt).acceptCondition(BlockStateProperty.builder(tnt)
                    .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withBoolProp(TNTBlock.UNSTABLE, false)))))));
    }
}