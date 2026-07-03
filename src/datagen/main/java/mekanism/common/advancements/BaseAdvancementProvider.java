package mekanism.common.advancements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.component.predicate.UpgradeTypeComponentPredicate;
import mekanism.common.registries.MekanismDataComponentPredicates;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public abstract class BaseAdvancementProvider implements AdvancementSubProvider {

    protected ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return ExtendedAdvancementBuilder.advancement(advancement);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate... predicates) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(predicates);
    }

    @SafeVarargs
    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasAllItems(Holder<Item>... items) {
        //return InventoryChangeTrigger.TriggerInstance.hasItems(items);
        return hasItems(Arrays.stream(items).map(BaseAdvancementProvider::predicate).toArray(ItemPredicate[]::new));
    }

    @SafeVarargs
    protected static ItemPredicate predicate(Holder<Item>... items) {
        //return ItemPredicate.Builder.item().of(items).build();
        return new ItemPredicate(Optional.of(HolderSet.direct(items)), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
    }

    @SafeVarargs
    protected static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(HolderGetter<Item> lookup, TagKey<Item>... tags) {
        List<ItemPredicate> list = new ArrayList<>();
        for (TagKey<Item> tag : tags) {
            list.add(ItemPredicate.Builder.item().of(lookup, tag).build());
        }
        return hasItems(list.toArray(new ItemPredicate[0]));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> hasUpgrade(ResourceKey<Upgrade> upgrade) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().withComponents(
              DataComponentMatchers.Builder.components().partial(MekanismDataComponentPredicates.UPGRADES.get(), new UpgradeTypeComponentPredicate(upgrade)).build()
        ));
    }

    protected static Item[] getItems(Collection<? extends Holder<Item>> items, Predicate<Item> matcher) {
        return items.stream()
              .map(Holder::value)
              .filter(matcher)
              .toArray(Item[]::new);
    }
}