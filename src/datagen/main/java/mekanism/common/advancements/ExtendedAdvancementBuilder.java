package mekanism.common.advancements;

import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.common.registration.INamedEntry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class ExtendedAdvancementBuilder {

    private final Advancement.Builder internal = Advancement.Builder.advancement();
    private final MekanismAdvancement advancement;

    private ExtendedAdvancementBuilder(MekanismAdvancement advancement) {
        this.advancement = advancement;
        if (this.advancement.parent() != null) {
            internal.parent(this.advancement.parent().name());
        }
    }

    public static ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return new ExtendedAdvancementBuilder(advancement);
    }

    public ExtendedAdvancementBuilder display(ItemStackTemplate stack, @Nullable Identifier background, AdvancementType type, boolean showToast, boolean announceToChat,
          boolean hidden) {
        return display(new DisplayInfo(stack, advancement.translateTitle(), advancement.translateDescription(), Optional.ofNullable(background).map(ResourceTexture::new), type, showToast, announceToChat, hidden));
    }

    public ExtendedAdvancementBuilder display(HolderGetter<Item> items, ResourceKey<Item> id, @Nullable Identifier background, AdvancementType type, boolean showToast,
          boolean announceToChat, boolean hidden) {
        return display(items.getOrThrow(id), background, type, showToast, announceToChat, hidden);
    }

    public ExtendedAdvancementBuilder display(Holder<Item> item, @Nullable Identifier background, AdvancementType type, boolean showToast, boolean announceToChat,
          boolean hidden) {
        return display(new ItemStackTemplate(item), background, type, showToast, announceToChat, hidden);
    }

    public ExtendedAdvancementBuilder display(HolderGetter<Item> items, ResourceKey<Item> id, AdvancementType type, boolean announceToChat) {
        return display(items, id, null, type, true, announceToChat, false);
    }

    public ExtendedAdvancementBuilder display(Holder<Item> item, AdvancementType type, boolean announceToChat) {
        return display(item, null, type, true, announceToChat, false);
    }

    public <ITEM extends ItemLike & INamedEntry> ExtendedAdvancementBuilder displayAndCriterion(ITEM item, AdvancementType type, boolean announceToChat) {
        display(item.asItem().builtInRegistryHolder(), type, announceToChat);
        return addCriterion(item);
    }

    public ExtendedAdvancementBuilder display(DisplayInfo display) {
        return runInternal(builder -> builder.display(display));
    }

    public ExtendedAdvancementBuilder rewards(AdvancementRewards.Builder rewardsBuilder) {
        return runInternal(builder -> builder.rewards(rewardsBuilder));
    }

    public ExtendedAdvancementBuilder rewards(AdvancementRewards rewards) {
        return runInternal(builder -> builder.rewards(rewards));
    }

    public ExtendedAdvancementBuilder orCriteria(String key, HolderGetter<Item> lookup, ItemLike... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("No items specified");
        }
        return addCriterion(key, InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(lookup, items).build()));
    }

    public ExtendedAdvancementBuilder orCriteria(RecipeCriterion... criteria) {
        if (criteria.length > 1) {
            internal.requirements(Strategy.OR);
        }
        return andCriteria(criteria);
    }

    public ExtendedAdvancementBuilder orCriteria(String key, Criterion<?> criterion) {
        internal.requirements(Strategy.OR);
        return addCriterion(key, criterion);
    }

    @SafeVarargs
    public final <ITEM extends ItemLike & INamedEntry> ExtendedAdvancementBuilder andCriteria(ITEM... items) {
        for (ITEM item : items) {
            addCriterion(item);
        }
        return this;
    }

    public ExtendedAdvancementBuilder andCriteria(RecipeCriterion... criteria) {
        if (criteria.length == 0) {
            throw new IllegalArgumentException("No criteria specified");
        }
        for (RecipeCriterion criterion : criteria) {
            internal.addCriterion(criterion.name(), criterion.criterion());
        }
        return this;
    }

    public ExtendedAdvancementBuilder addCriterion(String key, Criterion<?> criterion) {
        return runInternal(builder -> builder.addCriterion(key, criterion));
    }

    public <ITEM extends ItemLike & INamedEntry> ExtendedAdvancementBuilder addCriterion(ITEM item) {
        return addCriterion(item.getName(), InventoryChangeTrigger.TriggerInstance.hasItems(item));
    }

    public ExtendedAdvancementBuilder requirements(AdvancementRequirements requirements) {
        return runInternal(builder -> builder.requirements(requirements));
    }

    private ExtendedAdvancementBuilder runInternal(Consumer<Advancement.Builder> consumer) {
        consumer.accept(internal);
        return this;
    }

    public AdvancementHolder save(Consumer<AdvancementHolder> consumer) {
        return internal.save(consumer, advancement.name());
    }
}