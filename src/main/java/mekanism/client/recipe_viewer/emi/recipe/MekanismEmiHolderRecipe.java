package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public abstract class MekanismEmiHolderRecipe<RECIPE extends Recipe<?>> extends MekanismEmiRecipe<RECIPE> {

    private final RecipeHolder<RECIPE> recipeHolder;

    public MekanismEmiHolderRecipe(MekanismEmiRecipeCategory category, RecipeHolder<RECIPE> recipeHolder) {
        super(category, recipeHolder.id(), recipeHolder.value());
        this.recipeHolder = recipeHolder;
    }

    public MekanismEmiHolderRecipe(EmiRecipeCategory category, RecipeHolder<RECIPE> recipeHolder, int xOffset, int yOffset, int width, int height) {
        super(category, recipeHolder.id(), recipeHolder.value(), xOffset, yOffset, width, height);
        this.recipeHolder = recipeHolder;
    }

    @Nullable
    @Override
    public RecipeHolder<RECIPE> getBackingRecipe() {
        return recipeHolder;
    }
}