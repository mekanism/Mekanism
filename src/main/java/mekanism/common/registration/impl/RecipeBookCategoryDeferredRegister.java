package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public class RecipeBookCategoryDeferredRegister extends MekanismDeferredRegister<RecipeBookCategory> {

    private final List<MekanismRecipeBookTypes> types = new ArrayList<>();

    public RecipeBookCategoryDeferredRegister(String modid) {
        super(Registries.RECIPE_BOOK_CATEGORY, modid);
    }

    public MekanismRecipeBookTypes registerAll(Identifier recipeType, ILangEntry toggleButton, MekanismBookCategory[] bookCategories, Predicate<RecipeDisplay> displayMatches) {
        for (MekanismBookCategory bookCategory : bookCategories) {
            register(recipeType.getPath(), bookCategory);
        }
        return constructTypes(toggleButton, bookCategories, displayMatches);
    }

    public MekanismRecipeBookTypes constructTypes(ILangEntry toggleButton, MekanismBookCategory[] bookCategories, Predicate<RecipeDisplay> displayMatches) {
        MekanismRecipeBookTypes recipeBookTypes = new MekanismRecipeBookTypes(List.of(bookCategories), toggleButton.translate(), displayMatches);
        types.add(recipeBookTypes);
        return recipeBookTypes;
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

    public List<MekanismRecipeBookTypes> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public record MekanismRecipeBookTypes(List<MekanismBookCategory> categories, Component filterName, Predicate<RecipeDisplay> displayMatches) implements ExtendedRecipeBookCategory {
    }
}