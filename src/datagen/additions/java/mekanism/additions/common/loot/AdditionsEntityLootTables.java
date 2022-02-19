package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.loot.table.BaseEntityLootTables;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.TagLootEntry;
import net.minecraft.loot.conditions.EntityHasProperty;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.conditions.RandomChanceWithLooting;
import net.minecraft.loot.functions.LootingEnchantBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.loot.functions.SetNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;

public class AdditionsEntityLootTables extends BaseEntityLootTables {

    @Override
    protected void addTables() {
        //Copy of vanilla's creeper drops
        add(AdditionsEntityTypes.BABY_CREEPER, LootTable.lootTable()
              .withPool(
                    LootPool.lootPool()
                          .name("gunpowder")
                          .setRolls(ConstantRange.exactly(1))
                          .add(ItemLootEntry.lootTableItem(Items.GUNPOWDER)
                                .apply(SetCount.setCount(RandomValueRange.between(0.0F, 2.0F)))
                                .apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F)))
                          )
              ).withPool(LootPool.lootPool()
                    .name("music_discs")
                    .add(TagLootEntry.expandTag(ItemTags.MUSIC_DISCS))
                    .when(EntityHasProperty.hasProperties(LootContext.EntityTarget.KILLER, EntityPredicate.Builder.entity().of(EntityTypeTags.SKELETONS)))
              )
        );
        //Copy of vanilla's enderman drops
        add(AdditionsEntityTypes.BABY_ENDERMAN, LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("pearls")
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(Items.ENDER_PEARL)
                          .apply(SetCount.setCount(RandomValueRange.between(0.0F, 1.0F)))
                          .apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F)))
                    )
              )
        );
        //Copy of vanilla's skeleton drops
        add(AdditionsEntityTypes.BABY_SKELETON, skeletonDrops());
        //Copy of vanilla's stray drops
        CompoundNBT slownessPotion = new CompoundNBT();
        slownessPotion.putString("Potion", "minecraft:slowness");
        add(AdditionsEntityTypes.BABY_STRAY, skeletonDrops()
              .withPool(LootPool.lootPool()
                    .name("tipped_arrows")
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(Items.TIPPED_ARROW)
                          .apply(SetCount.setCount(RandomValueRange.between(0.0F, 1.0F)))
                          .apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F))
                                .setLimit(1)
                          ).apply(SetNBT.setTag(slownessPotion))
                    ).when(KilledByPlayer.killedByPlayer())
              )
        );
        //Copy of vanilla's wither skeleton drops
        add(AdditionsEntityTypes.BABY_WITHER_SKELETON, LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("coal")
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(Items.COAL)
                          .apply(SetCount.setCount(RandomValueRange.between(-1.0F, 1.0F)))
                          .apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("bones")
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(Items.BONE)
                          .apply(SetCount.setCount(RandomValueRange.between(0.0F, 2.0F)))
                          .apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("skulls")
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(Blocks.WITHER_SKELETON_SKULL))
                    .when(KilledByPlayer.killedByPlayer())
                    //Double vanilla's skull drop chance due to being "younger and less brittle"
                    .when(RandomChanceWithLooting.randomChanceAndLootingBoost(0.05F, 0.01F))
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
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(Items.ARROW)
                          .apply(SetCount.setCount(RandomValueRange.between(0.0F, 2.0F)))
                          .apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("bones")
                    .setRolls(ConstantRange.exactly(1))
                    .add(ItemLootEntry.lootTableItem(Items.BONE)
                          .apply(SetCount.setCount(RandomValueRange.between(0.0F, 2.0F)))
                          .apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F)))
                    )
              );
    }
}