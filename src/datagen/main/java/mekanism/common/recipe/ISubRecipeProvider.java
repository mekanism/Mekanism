package mekanism.common.recipe;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.RecipeCriterion;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/// Interface for helping split the recipe provider over multiple classes to make it a bit easier to interact with
public interface ISubRecipeProvider {

    void addRecipes(RecipeOutput output);

    HolderGetter<Item> items();

    HolderGetter<Chemical> chemicals();

    HolderGetter<Fluid> fluids();

    default ItemStackTemplate template(ResourceKey<Item> item) {
        return RecipeProviderUtil.template(items(), item);
    }

    default ItemStackTemplate template(ResourceKey<Item> item, int amount) {
        return RecipeProviderUtil.template(items(), item, amount);
    }

    default ItemStackTemplate template(BlockItemId item) {
        return RecipeProviderUtil.template(items(), item);
    }

    default ItemStackTemplate template(BlockItemId item, int amount) {
        return RecipeProviderUtil.template(items(), item, amount);
    }

    default ChemicalStackTemplate chemicalTemplate(ResourceKey<Chemical> chemical, int amount) {
        return RecipeProviderUtil.chemicalTemplate(chemicals(), chemical, amount);
    }

    default FluidStackTemplate fluidTemplate(ResourceKey<Fluid> fluid, int amount) {
        return RecipeProviderUtil.fluidTemplate(fluids(), fluid, amount);
    }

    default RecipeCriterion hasCriterion(BlockItemId item) {
        return hasCriterion(item.item());
    }

    default RecipeCriterion hasCriterion(ResourceKey<Item> item) {
        return hasCriterion(items().getOrThrow(item));
    }

    default RecipeCriterion hasCriterion(Holder<? extends ItemLike> item) {
        return new RecipeCriterion(RecipeProvider.getHasName(item.value()), has(item));
    }

    default RecipeCriterion hasCriterion(Item item) {
        return new RecipeCriterion(RecipeProvider.getHasName(item), has(item));
    }

    default Criterion<TriggerInstance> has(Item item) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(items(), item));
    }

    default Criterion<TriggerInstance> has(BlockItemId item) {
        return has(item.item());
    }

    default Criterion<TriggerInstance> has(ResourceKey<Item> item) {
        return has(items().getOrThrow(item));
    }

    default Criterion<TriggerInstance> has(Holder<? extends ItemLike> item) {
        return has(item.value().asItem());
    }

    default Criterion<InventoryChangeTrigger.TriggerInstance> has(BlockItemTagId tag) {
        return has(tag.item());
    }

    /// Copied from [net.minecraft.data.recipes.RecipeProvider]
    default Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(items(), tag));
    }
}