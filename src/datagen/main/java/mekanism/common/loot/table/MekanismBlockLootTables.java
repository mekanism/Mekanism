package mekanism.common.loot.table;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.OreType;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.RandomValueRange;

public class MekanismBlockLootTables extends BaseBlockLootTables {

    @Override
    protected void addTables() {
        skip(MekanismBlocks.BOUNDING_BLOCK, MekanismBlocks.ADVANCED_BOUNDING_BLOCK);
        add(block -> createSingleItemTableWithSilkTouch(block, MekanismItems.SALT, ConstantRange.exactly(4)), MekanismBlocks.SALT_BLOCK);
        add(block -> droppingWithFortuneOrRandomly(block, MekanismItems.FLUORITE_GEM, RandomValueRange.between(2, 4)), MekanismBlocks.ORES.get(OreType.FLUORITE));
        //Register the remaining blocks as dropping themselves with any contents they may have stored
        dropSelfWithContents(MekanismBlocks.BLOCKS.getAllBlocks());
    }
}