package mekanism.common.loot.table;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.storage.loot.ConstantRange;

public class MekanismBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void addTables() {
        skip(MekanismBlocks.BOUNDING_BLOCK, MekanismBlocks.ADVANCED_BOUNDING_BLOCK);
        registerLootTable(block -> droppingWithSilkTouchOrRandomly(block, MekanismItems.SALT, ConstantRange.of(4)), MekanismBlocks.SALT_BLOCK);
        //Register the remaining blocks as dropping themselves with any contents they may have stored
        registerDropSelfWithContentsLootTable(MekanismBlocks.BLOCKS.getAllBlocks());
    }
}