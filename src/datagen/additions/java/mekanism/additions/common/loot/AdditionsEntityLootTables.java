package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.loot.table.BaseEntityLootTable;
import net.minecraft.item.Items;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;

public class AdditionsEntityLootTables extends BaseEntityLootTable {

    @Override
    protected void addTables() {
        //Copy of the normal skeleton's loot table for our baby skeleton
        registerLootTable(AdditionsEntityTypes.BABY_SKELETON, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
              .addEntry(ItemLootEntry.builder(Items.ARROW).acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                    .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
              .addEntry(ItemLootEntry.builder(Items.BONE).acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                    .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))));
    }
}