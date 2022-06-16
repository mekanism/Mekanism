package mekanism.common.advancements;

import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ExtendedAdvancementBuilder {

    private final Advancement.Builder internal = Advancement.Builder.advancement();
    private final MekanismAdvancement advancement;

    private ExtendedAdvancementBuilder(MekanismAdvancement advancement) {
        this.advancement = advancement;
        if (this.advancement.parent() != null) {
            parent(this.advancement.parent().path());
        }
    }

    public static ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return new ExtendedAdvancementBuilder(advancement);
    }

    public ExtendedAdvancementBuilder parent(Advancement parent) {
        internal.parent(parent);
        return this;
    }

    public ExtendedAdvancementBuilder parent(ResourceLocation parentId) {
        internal.parent(parentId);
        return this;
    }

    public ExtendedAdvancementBuilder display(ItemStack stack, @Nullable ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat,
          boolean hidden) {
        internal.display(stack, advancement.translateTitle(), advancement.translateDescription(), background, frame, showToast, announceToChat, hidden);
        return this;
    }

    public ExtendedAdvancementBuilder display(ItemLike item, @Nullable ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat,
          boolean hidden) {
        return display(new ItemStack(item), background, frame, showToast, announceToChat, hidden);
    }

    public ExtendedAdvancementBuilder display(ItemLike item, FrameType frame) {
        return display(item, null, frame, true, true, false);
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

    public ExtendedAdvancementBuilder orCriteria(Map<String, CriterionTriggerInstance> criteria) {
        internal.requirements(RequirementsStrategy.OR);
        return andCriteria(criteria);
    }

    public ExtendedAdvancementBuilder andCriteria(Map<String, CriterionTriggerInstance> criteria) {
        criteria.forEach(internal::addCriterion);
        return this;
    }

    public ExtendedAdvancementBuilder addCriterion(String key, CriterionTriggerInstance criterion) {
        return runInternal(builder -> builder.addCriterion(key, criterion));
    }

    public ExtendedAdvancementBuilder requirements(String[][] requirements) {
        return runInternal(builder -> builder.requirements(requirements));
    }

    private ExtendedAdvancementBuilder runInternal(Consumer<Advancement.Builder> consumer) {
        consumer.accept(internal);
        return this;
    }

    public Advancement save(Consumer<Advancement> consumer, ExistingFileHelper existingFileHelper) {
        return internal.save(consumer, advancement.path(), existingFileHelper);
    }
}