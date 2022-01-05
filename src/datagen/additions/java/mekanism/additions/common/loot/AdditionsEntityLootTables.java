package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.loot.table.BaseEntityLootTables;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class AdditionsEntityLootTables extends BaseEntityLootTables {

    @Override
    protected void addTables() {
        //Copy of vanilla's creeper drops
        add(AdditionsEntityTypes.BABY_CREEPER, LootTable.lootTable()
              .withPool(
                    LootPool.lootPool()
                          .name("gunpowder")
                          .setRolls(ConstantValue.exactly(1))
                          .add(LootItem.lootTableItem(Items.GUNPOWDER)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                                .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))
                          )
              ).withPool(LootPool.lootPool()
                    .name("music_discs")
                    .add(TagEntry.expandTag(ItemTags.MUSIC_DISCS))
                    .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.KILLER, EntityPredicate.Builder.entity().of(EntityTypeTags.SKELETONS)))
              )
        );
        //Copy of vanilla's enderman drops
        add(AdditionsEntityTypes.BABY_ENDERMAN, LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("pearls")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ENDER_PEARL)
                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                          .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))
                    )
              )
        );
        //Copy of vanilla's skeleton drops
        add(AdditionsEntityTypes.BABY_SKELETON, skeletonDrops());
        //Copy of vanilla's stray drops
        add(AdditionsEntityTypes.BABY_STRAY, skeletonDrops()
              .withPool(LootPool.lootPool()
                    .name("tipped_arrows")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.TIPPED_ARROW)
                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                          .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))
                                .setLimit(1)
                          ).apply(SetPotionFunction.setPotion(Potions.SLOWNESS))
                    ).when(LootItemKilledByPlayerCondition.killedByPlayer())
              )
        );
        //Copy of vanilla's wither skeleton drops
        add(AdditionsEntityTypes.BABY_WITHER_SKELETON, LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("coal")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.COAL)
                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(-1.0F, 1.0F)))
                          .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("bones")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.BONE)
                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                          .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("skulls")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Blocks.WITHER_SKELETON_SKULL))
                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                    //Double vanilla's skull drop chance due to being "younger and less brittle"
                    .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.05F, 0.01F))
              )
        );
    }

    /**
     * Copy of vanilla's skeleton drops
     */
    private LootTable.Builder skeletonDrops() {
        return LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("arrows")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ARROW)
                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                          .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("bones")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.BONE)
                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                          .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))
                    )
              );
    }
}