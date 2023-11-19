package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeDeferredRegister extends MekanismDeferredRegister<RecipeType<?>> {

    private final List<IMekanismRecipeTypeProvider<?, ?>> recipeTypes = new ArrayList<>();

    public RecipeTypeDeferredRegister(String modid) {
        super(Registries.RECIPE_TYPE, modid, RecipeTypeRegistryObject::new);
    }

    public <RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> registerMek(String name,
          Function<ResourceLocation, ? extends MekanismRecipeType<RECIPE, INPUT_CACHE>> func) {
        RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> registeredRecipeType = (RecipeTypeRegistryObject<RECIPE, INPUT_CACHE>) super.register(name, func);
        recipeTypes.add(registeredRecipeType);
        return registeredRecipeType;
    }

    public List<IMekanismRecipeTypeProvider<?, ?>> getAllRecipeTypes() {
        return Collections.unmodifiableList(recipeTypes);
    }
}