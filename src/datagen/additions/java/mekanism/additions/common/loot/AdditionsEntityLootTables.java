package mekanism.additions.common.loot;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.loot.BaseEntityLootTables;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class AdditionsEntityLootTables extends BaseEntityLootTables {

    public AdditionsEntityLootTables(LootTableSubProvider.Context context) {
        super(context);
    }

    @Override
    public void generate() {
        //Copy of vanilla's bogged drops
        add(AdditionsEntityTypes.BABY_BOGGED, skeletonDrops()
              .withPool(tippedArrow(Potions.POISON))
        );
        //Copy of vanilla's creeper drops
        add(AdditionsEntityTypes.BABY_CREEPER, LootTable.lootTable()
              .withPool(
                    LootPool.lootPool()
                          .name("gunpowder")
                          .setRolls(ContextIntProviders.exactly(1))
                          .add(LootItem.lootTableItem(Items.GUNPOWDER)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0F, 1.0F)))
                          )
              ).withPool(LootPool.lootPool()
                    .name("music_discs")
                    .add(TagEntry.expandTag(this.items.getOrThrow(ItemTags.CREEPER_DROP_MUSIC_DISCS)))
                    .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.ATTACKER, EntityPredicate.Builder.entity().of(this.entityTypes, EntityTypeTags.SKELETONS)))
              )
        );
        //Copy of vanilla's enderman drops
        add(AdditionsEntityTypes.BABY_ENDERMAN, LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("pearls")
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.ENDER_PEARL)
                          .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 1)))
                          .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0F, 1.0F)))
                    )
              )
        );
        //Copy of vanilla's parched drops
        add(AdditionsEntityTypes.BABY_PARCHED, skeletonDrops()
              .withPool(tippedArrow(Potions.WEAKNESS))
        );
        //Copy of vanilla's skeleton drops
        add(AdditionsEntityTypes.BABY_SKELETON, skeletonDrops());
        //Copy of vanilla's stray drops
        add(AdditionsEntityTypes.BABY_STRAY, skeletonDrops()
              .withPool(tippedArrow(Potions.SLOWNESS))
        );
        //Copy of vanilla's wither skeleton drops
        add(AdditionsEntityTypes.BABY_WITHER_SKELETON, LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("coal")
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.COAL)
                          .apply(SetItemCountFunction.setCount(ContextIntProviders.between(-1, 1)))
                          .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("bones")
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.BONE)
                          .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                          .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("skulls")
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.WITHER_SKELETON_SKULL))
                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                    //Double vanilla's skull drop chance due to being "younger and less brittle"
                    .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.enchantments, 0.05F, 0.02F))
              )
        );
    }

    private LootPool.Builder tippedArrow(Holder<Potion> potion) {
        return LootPool.lootPool()
              .name("tipped_arrows")
              .setRolls(ContextIntProviders.exactly(1))
              .add(LootItem.lootTableItem(Items.TIPPED_ARROW)
                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 1)))
                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0F, 1.0F)).setLimit(1))
                    .apply(SetPotionFunction.setPotion(potion))
              ).when(LootItemKilledByPlayerCondition.killedByPlayer());
    }

    /// Copy of vanilla's skeleton drops
    private LootTable.Builder skeletonDrops() {
        return LootTable.lootTable()
              .withPool(LootPool.lootPool()
                    .name("arrows")
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.ARROW)
                          .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                          .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0F, 1.0F))))
              ).withPool(LootPool.lootPool()
                    .name("bones")
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.BONE)
                          .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                          .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0F, 1.0F)))
                    )
              );
    }
}