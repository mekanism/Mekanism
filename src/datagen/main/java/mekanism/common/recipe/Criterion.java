package mekanism.common.recipe;

import mekanism.api.providers.IItemProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

public class Criterion {

    public static final RecipeCriterion HAS_BRONZE = RecipeCriterion.of("has_bronze", hasItem(MekanismTags.Items.INGOTS_BRONZE));
    public static final RecipeCriterion HAS_LAPIS_LAZULI = RecipeCriterion.of("has_lapis_lazuli", hasItem(Tags.Items.GEMS_LAPIS));
    public static final RecipeCriterion HAS_OSMIUM = RecipeCriterion.of("has_osmium", hasItem(MekanismTags.Items.INGOTS_OSMIUM));
    public static final RecipeCriterion HAS_REFINED_GLOWSTONE = RecipeCriterion.of("has_refined_glowstone", hasItem(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE));
    public static final RecipeCriterion HAS_REFINED_OBSIDIAN = RecipeCriterion.of("has_refined_obsidian", hasItem(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN));
    public static final RecipeCriterion HAS_STEEL = RecipeCriterion.of("has_steel", hasItem(MekanismTags.Items.INGOTS_STEEL));

    private static InventoryChangeTrigger.Instance hasItem(net.minecraft.util.IItemProvider itemIn) {
        return hasItem(ItemPredicate.Builder.create().item(itemIn).build());
    }

    private static InventoryChangeTrigger.Instance hasItem(Tag<Item> tagIn) {
        return hasItem(ItemPredicate.Builder.create().tag(tagIn).build());
    }

    private static InventoryChangeTrigger.Instance hasItem(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.Instance(MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, predicates);
    }

    public static RecipeCriterion has(IItemProvider item) {
        return RecipeCriterion.of("has_" + item.getName(), hasItem(item));
    }

    public static RecipeCriterion has(Item item) {
        return RecipeCriterion.of("has_" + item.getRegistryName().getPath(), hasItem(item));
    }
}