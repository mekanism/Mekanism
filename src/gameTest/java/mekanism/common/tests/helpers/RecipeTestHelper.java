package mekanism.common.tests.helpers;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class RecipeTestHelper extends MissingElementTestHelper {

    public RecipeTestHelper(GameTestInfo info) {
        super(info);
    }

    public <RECIPE extends MekanismRecipe<?>> List<RecipeHolder<RECIPE>> getRecipes(IMekanismRecipeTypeProvider<?, RECIPE, ?> recipeType) {
        return recipeType.getRecipes(getLevel());
    }

    public ContextMap recipeContext() {
        return SlotDisplayContext.fromLevel(getLevel());
    }

    public void collectInputs(Set<ResourceKey<Item>> inputs, ItemStackIngredient input, TagKey<Item> knownMissingTag, String type) {
        for (ItemStack representation : input.display().resolveForStacks(recipeContext())) {
            if (representation.is(knownMissingTag)) {
                fail("Item " + representation.getItem() + " is marked as being known to be missing, but has a " + type + " recipe.");
            }
            ResourceKey<Item> key = representation.typeHolder().getKey();
            if (key != null) {
                inputs.add(key);
            }
        }
    }

    public <TYPE> Set<ResourceKey<Item>> collectMissingRecipes(DataComponentType<TYPE> component, Set<ResourceKey<Item>> recipeInputs) {
        Set<ResourceKey<Item>> missingRecipes = new ReferenceLinkedOpenHashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            TYPE value = item.components().get(component);
            if (value != null) {
                ResourceKey<Item> compostable = BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
                if (!recipeInputs.contains(compostable)) {
                    missingRecipes.add(compostable);
                }
            }
        }
        return missingRecipes;
    }
}