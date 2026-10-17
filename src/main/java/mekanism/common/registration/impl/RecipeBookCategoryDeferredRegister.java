package mekanism.common.registration.impl;

import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class RecipeBookCategoryDeferredRegister extends MekanismDeferredRegister<RecipeBookCategory> {

    public RecipeBookCategoryDeferredRegister(String modid) {
        super(Registries.RECIPE_BOOK_CATEGORY, modid);
    }

    public MekanismDeferredHolder<RecipeBookCategory, RecipeBookCategory> register(Identifier recipeType, MekanismBookCategory bookCategory) {
        return register(recipeType.getPath(), bookCategory);
    }

    public MekanismDeferredHolder<RecipeBookCategory, RecipeBookCategory> register(String recipeType, MekanismBookCategory bookCategory) {
        return register(recipeType + "_" + bookCategory.getSerializedName());
    }

    public MekanismDeferredHolder<RecipeBookCategory, RecipeBookCategory> register(String name) {
        return register(name, RecipeBookCategory::new);
    }
}