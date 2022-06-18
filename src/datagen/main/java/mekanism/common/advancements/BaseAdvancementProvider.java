package mekanism.common.advancements;

import java.util.Arrays;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BaseAdvancementProvider extends AdvancementProvider {

    private final String modid;

    public BaseAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper, String modid) {
        super(generator, existingFileHelper);
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    @Override
    protected abstract void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper);

    protected ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return ExtendedAdvancementBuilder.advancement(advancement, fileHelper);
    }

    public static InventoryChangeTrigger.TriggerInstance hasItems(ItemPredicate... predicates) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(predicates);
    }

    protected static ItemPredicate predicate(ItemLike... items) {
        return ItemPredicate.Builder.item().of(items).build();
    }

    protected static CriterionTriggerInstance hasAllItems(ItemLike... items) {
        return hasItems(predicate(items));
    }

    @SafeVarargs
    protected static CriterionTriggerInstance hasItems(TagKey<Item>... tags) {
        return hasItems(Arrays.stream(tags)
              .map(tag -> ItemPredicate.Builder.item().of(tag).build())
              .toArray(ItemPredicate[]::new));
    }
}