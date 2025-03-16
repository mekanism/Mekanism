package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.common.loot.table.BaseBlockLootTables;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class AdditionsBlockLootTables extends BaseBlockLootTables {

    public AdditionsBlockLootTables(HolderLookup.Provider provider) {
        super(provider);
    }

    @Override
    protected void generate() {
        //Obsidian TNT
        registerObsidianTNT();
        //Plastic slabs
        add(this::createSlabItemTable, AdditionsBlocks.PLASTIC_SLABS.values());
        add(this::createSlabItemTable, AdditionsBlocks.PLASTIC_GLOW_SLABS.values());
        add(this::createSlabItemTable, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS.values());
        //Register all remaining blocks as just dropping themselves
        dropSelf(AdditionsBlocks.BLOCKS.getPrimaryEntries());
    }

    private void registerObsidianTNT() {
        add(AdditionsBlocks.OBSIDIAN_TNT, tnt -> LootTable.lootTable().withPool(applyExplosionCondition(tnt, LootPool.lootPool()
              .name("main")
              .setRolls(ConstantValue.exactly(1))
              .add(LootItem.lootTableItem(tnt)
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(tnt)
                          .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TntBlock.UNSTABLE, false)))
              )
        )));
    }
}