package mekanism.common.registration.impl;

import java.util.function.Function;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeDeferredRegister extends MekanismDeferredRegister<RecipeType<?>> {

    public RecipeTypeDeferredRegister(String modid) {
        super(Registries.RECIPE_TYPE, modid, RecipeTypeRegistryObject::new);
    }

    public <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache>
    RecipeTypeRegistryObject<VANILLA_INPUT, RECIPE, INPUT_CACHE> registerMek(String name, Function<ResourceLocation, ? extends MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>> func) {
        return (RecipeTypeRegistryObject<VANILLA_INPUT, RECIPE, INPUT_CACHE>) super.register(name, func);
    }
}