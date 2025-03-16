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
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ExtendedAdvancementBuilder {

    private final Advancement.Builder internal = Advancement.Builder.advancement();
    private final MekanismAdvancement advancement;
    private final ExistingFileHelper existingFileHelper;

    private ExtendedAdvancementBuilder(MekanismAdvancement advancement, ExistingFileHelper existingFileHelper) {
        this.advancement = advancement;
        this.existingFileHelper = existingFileHelper;
        if (this.advancement.parent() != null) {
            internal.parent(this.advancement.parent().name());
        }
    }

    public static ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement, ExistingFileHelper existingFileHelper) {
        return new ExtendedAdvancementBuilder(advancement, existingFileHelper);
    }

    public ExtendedAdvancementBuilder display(ItemStack stack, @Nullable ResourceLocation background, AdvancementType type, boolean showToast, boolean announceToChat,
          boolean hidden) {
        return display(new DisplayInfo(stack, advancement.translateTitle(), advancement.translateDescription(), Optional.ofNullable(background), type, showToast, announceToChat, hidden));
    }

    public ExtendedAdvancementBuilder display(ItemLike item, @Nullable ResourceLocation background, AdvancementType type, boolean showToast, boolean announceToChat,
          boolean hidden) {
        return display(new ItemStack(item), background, type, showToast, announceToChat, hidden);
    }

    public ExtendedAdvancementBuilder display(ItemLike item, AdvancementType type, boolean announceToChat) {
        return display(item, null, type, true, announceToChat, false);
    }

    public <ITEM extends ItemLike & INamedEntry> ExtendedAdvancementBuilder displayAndCriterion(ITEM item, AdvancementType type, boolean announceToChat) {
        display(item, type, announceToChat);
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

    public ExtendedAdvancementBuilder orCriteria(String key, ItemLike... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("No items specified");
        }
        return addCriterion(key, InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items).build()));
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
        return internal.save(consumer, advancement.name(), existingFileHelper);
    }
}