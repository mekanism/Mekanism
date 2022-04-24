package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeDeferredRegister extends WrappedDeferredRegister<RecipeType<?>> {

    private final List<IMekanismRecipeTypeProvider<?, ?>> recipeTypes = new ArrayList<>();

    public RecipeTypeDeferredRegister(String modid) {
        super(modid, Registry.RECIPE_TYPE_REGISTRY);
    }

    public <RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> register(String name,
          Supplier<? extends MekanismRecipeType<RECIPE, INPUT_CACHE>> sup) {
        RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> registeredRecipeType = register(name, sup, RecipeTypeRegistryObject::new);
        recipeTypes.add(registeredRecipeType);
        return registeredRecipeType;
    }

    public List<IMekanismRecipeTypeProvider<?, ?>> getAllRecipeTypes() {
        return Collections.unmodifiableList(recipeTypes);
    }
}