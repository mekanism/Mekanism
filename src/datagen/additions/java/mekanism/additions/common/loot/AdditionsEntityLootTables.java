package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.loot.table.BaseEntityLootTables;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.TagLootEntry;
import net.minecraft.world.storage.loot.conditions.EntityHasProperty;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetNBT;

public class AdditionsEntityLootTables extends BaseEntityLootTables {

    @Override
    protected void addTables() {
        //Copy of vanilla's creeper drops
        registerLootTable(AdditionsEntityTypes.BABY_CREEPER, LootTable.builder()
              .addLootPool(
                    LootPool.builder()
                          .rolls(ConstantRange.of(1))
                          .addEntry(ItemLootEntry.builder(Items.GUNPOWDER)
                                .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                                .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F)))
                          )
              ).addLootPool(LootPool.builder()
                    .addEntry(TagLootEntry.func_216176_b(ItemTags.MUSIC_DISCS))
                    .acceptCondition(EntityHasProperty.builder(LootContext.EntityTarget.KILLER, EntityPredicate.Builder.create().type(EntityTypeTags.SKELETONS)))
              )
        );
        //Copy of vanilla's enderman drops
        registerLootTable(AdditionsEntityTypes.BABY_ENDERMAN, LootTable.builder()
              .addLootPool(LootPool.builder()
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
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.COAL)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(-1.0F, 1.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
              ).addLootPool(LootPool.builder()
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.BONE)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
              ).addLootPool(LootPool.builder()
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
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.ARROW)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
              ).addLootPool(LootPool.builder()
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(Items.BONE)
                          .acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 2.0F)))
                          .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F)))
                    )
              );
    }
}