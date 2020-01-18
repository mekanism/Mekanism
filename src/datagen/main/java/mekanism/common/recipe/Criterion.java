package mekanism.common.recipe;

import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.providers.IItemProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

public class Criterion {

    public static final RecipeCriterion HAS_BRONZE = has("bronze", MekanismTags.Items.INGOTS_BRONZE);
    public static final RecipeCriterion HAS_LAPIS_LAZULI = has("lapis_lazuli", Tags.Items.GEMS_LAPIS);
    public static final RecipeCriterion HAS_OSMIUM = has("osmium", MekanismTags.Items.INGOTS_OSMIUM);
    public static final RecipeCriterion HAS_REFINED_GLOWSTONE = has("refined_glowstone", MekanismTags.Items.INGOTS_REFINED_GLOWSTONE);
    public static final RecipeCriterion HAS_REFINED_OBSIDIAN = has("refined_obsidian", MekanismTags.Items.INGOTS_REFINED_OBSIDIAN);
    public static final RecipeCriterion HAS_STEEL = has("steel", MekanismTags.Items.INGOTS_STEEL);
    public static final RecipeCriterion HAS_BASIC_CIRCUIT = has("basic_circuit", MekanismTags.Items.CIRCUITS_BASIC);
    public static final RecipeCriterion HAS_ADVANCED_CIRCUIT = has("advanced_circuit", MekanismTags.Items.CIRCUITS_ADVANCED);
    public static final RecipeCriterion HAS_ELITE_CIRCUIT = has("elite_circuit", MekanismTags.Items.CIRCUITS_ELITE);
    public static final RecipeCriterion HAS_ULTIMATE_CIRCUIT = has("ultimate_circuit", MekanismTags.Items.CIRCUITS_ULTIMATE);

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

    public static RecipeCriterion has(String name, Tag<Item> tag) {
        return RecipeCriterion.of("has_" + name, hasItem(tag));
    }
}