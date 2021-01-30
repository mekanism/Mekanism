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
        registerLootTable(AdditionsEntityTypes.BABY_CREEPER, LootTable.builder()
              .addLootPool(
                    LootPool.builder()
                          .name("gunpowder")
                          .rolls(ConstantRange.of(1))
                          .addEntry(ItemLootEntry.builder(Items.GUNPOWDER)
                                .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                                .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F)))
                          )
              ).addLootPool(LootPool.builder()
                    .name("music_discs")
                    .addEntry(TagLootEntry.getBuilder(ItemTags.MUSIC_DISCS))
                    .acceptCondition(EntityHasProperty.builder(LootContext.EntityTarget.KILLER, EntityPredicate.Builder.create().type(EntityTypeTags.SKELETONS)))
              )
        );
        //Copy of vanilla's enderman drops
        registerLootTable(AdditionsEntityTypes.BABY_ENDERMAN, LootTable.builder()
              .addLootPool(LootPool.builder()
                    .name("pearls")
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.ENDER_PEARL)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 1.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F)))
                    )
              )
        );
        //Copy of vanilla's skeleton drops
        registerLootTable(AdditionsEntityTypes.BABY_SKELETON, skeletonDrops());
        //Copy of vanilla's stray drops
        CompoundNBT slownessPotion = new CompoundNBT();
        slownessPotion.putString("Potion", "minecraft:slowness");
        registerLootTable(AdditionsEntityTypes.BABY_STRAY, skeletonDrops()
              .addLootPool(LootPool.builder()
                    .name("tipped_arrows")
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.TIPPED_ARROW)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 1.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))
                                .func_216072_a(1)
                          ).acceptFunction(SetNBT.builder(slownessPotion))
                    ).acceptCondition(KilledByPlayer.builder())
              )
        );
        //Copy of vanilla's wither skeleton drops
        registerLootTable(AdditionsEntityTypes.BABY_WITHER_SKELETON, LootTable.builder()
              .addLootPool(LootPool.builder()
                    .name("coal")
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.COAL)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(-1.0F, 1.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
              ).addLootPool(LootPool.builder()
                    .name("bones")
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.BONE)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
              ).addLootPool(LootPool.builder()
                    .name("skulls")
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Blocks.WITHER_SKELETON_SKULL))
                    .acceptCondition(KilledByPlayer.builder())
                    //Double vanilla's skull drop chance due to being "younger and less brittle"
                    .acceptCondition(RandomChanceWithLooting.builder(0.05F, 0.01F))
              )
        );
    }

    /**
     * Copy of vanilla's skeleton drops
     */
    private LootTable.Builder skeletonDrops() {
        return LootTable.builder()
              .addLootPool(LootPool.builder()
                    .name("arrows")
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.ARROW)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
              ).addLootPool(LootPool.builder()
                    .name("bones")
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.BONE)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F)))
                    )
              );
    }
}