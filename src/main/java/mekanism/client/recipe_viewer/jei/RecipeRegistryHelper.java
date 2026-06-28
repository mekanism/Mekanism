package mekanism.client.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeRegistryHelper {

    private RecipeRegistryHelper() {
    }

    public static void registerCondensentrator(IRecipeRegistration registry) {
        List<RecipeHolder<RotaryRecipe>> condensentratorRecipes = new ArrayList<>();
        List<RecipeHolder<RotaryRecipe>> decondensentratorRecipes = new ArrayList<>();
        for (RecipeHolder<RotaryRecipe> recipeHolder : MekanismRecipeType.ROTARY.getRecipes()) {
            RotaryRecipe recipe = recipeHolder.value();
            if (recipe.hasChemicalToFluid()) {
                condensentratorRecipes.add(recipeHolder);
            }
            if (recipe.hasFluidToChemical()) {
                decondensentratorRecipes.add(recipeHolder);
            }
        }
        registry.addRecipes(MekanismJEI.holderRecipeType(RecipeViewerRecipeType.CONDENSENTRATING), condensentratorRecipes);
        registry.addRecipes(MekanismJEI.holderRecipeType(RecipeViewerRecipeType.DECONDENSENTRATING), decondensentratorRecipes);
    }

    public static <RECIPE extends MekanismRecipe<?>> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType,
          IMekanismRecipeTypeProvider<?, RECIPE, ?> type) {
        registry.addRecipes(MekanismJEI.holderRecipeType(recipeType), type.getRecipes());
    }

    public static <RECIPE> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType, Map<Identifier, RECIPE> recipes) {
        register(registry, recipeType, List.copyOf(recipes.values()));
    }

    public static <RECIPE> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType, List<RECIPE> recipes) {
        registry.addRecipes(MekanismJEI.recipeType(recipeType), recipes);
    }
}