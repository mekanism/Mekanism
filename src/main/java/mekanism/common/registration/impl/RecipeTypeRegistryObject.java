package mekanism.common.registration.impl;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeRegistryObject<VANILLA_INPUT extends RecipeInput, BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends MekanismRecipe<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO>, INPUT_CACHE extends IInputRecipeCache> extends
      MekanismDeferredHolder<RecipeType<?>, MekanismRecipeType<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE>>
      implements IMekanismRecipeTypeProvider<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE> {

    public RecipeTypeRegistryObject(ResourceKey<RecipeType<?>> key) {
        super(key);
    }

    @Override
    public MekanismRecipeType<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE> getRecipeType() {
        return value();
    }
}