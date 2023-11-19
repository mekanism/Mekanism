package mekanism.common.registration.impl;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeRegistryObject<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> extends
      MekanismDeferredHolder<RecipeType<?>, MekanismRecipeType<RECIPE, INPUT_CACHE>> implements IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> {

    public RecipeTypeRegistryObject(ResourceKey<RecipeType<?>> key) {
        super(key);
    }

    @Override
    public MekanismRecipeType<RECIPE, INPUT_CACHE> getRecipeType() {
        return value();
    }
}