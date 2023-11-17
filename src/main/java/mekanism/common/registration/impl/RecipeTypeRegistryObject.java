package mekanism.common.registration.impl;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RecipeTypeRegistryObject<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> extends
      WrappedRegistryObject<RecipeType<?>, MekanismRecipeType<RECIPE, INPUT_CACHE>> implements IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> {

    public RecipeTypeRegistryObject(DeferredHolder<RecipeType<?>, MekanismRecipeType<RECIPE, INPUT_CACHE>> registryObject) {
        super(registryObject);
    }

    @Override
    public MekanismRecipeType<RECIPE, INPUT_CACHE> getRecipeType() {
        return get();
    }
}