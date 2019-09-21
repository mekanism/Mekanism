package mekanism.common.integration.crafttweaker.util;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.actions.IAction;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.common.integration.crafttweaker.helpers.RecipeInfoHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;

public abstract class RecipeMapModification<RECIPE extends IMekanismRecipe> implements IAction {

    protected final Set<RECIPE> recipes;
    protected final Recipe<RECIPE> recipeType;
    protected final String name;
    protected boolean add;

    protected RecipeMapModification(String name, boolean add, Recipe<RECIPE> recipeType) {
        this.name = name;
        this.recipeType = recipeType;
        this.add = add;
        this.recipes = new HashSet<>();
    }

    @Override
    public void apply() {
        if (!recipes.isEmpty()) {
            if (add) {
                for (RECIPE recipe : recipes) {
                    if (!recipeType.put(recipe)) {
                        //TODO: Failed to add warning
                        /*CraftTweakerAPI.logInfo(String.format("Overwritten %s Recipe for %s", name,
                              RecipeInfoHelper.getRecipeInfo(new AbstractMap.SimpleEntry<>(entry.getKey(), value))));*/
                    }
                }
            } else {
                for (RECIPE recipe : recipes) {
                    if (!recipeType.remove(r -> r.equals(recipe))) {
                        //TODO: It is null if an exception is thrown so catch it so fix this error message
                        CraftTweakerAPI.logError(String.format("Error removing %s Recipe : null object", name));
                    }
                }
            }
        }
    }

    private String getRecipeInfo() {
        if (!recipes.isEmpty()) {
            return recipes.stream().filter(Objects::nonNull).map(RecipeInfoHelper::getRecipeInfo).collect(Collectors.joining(", "));
        }
        return "Unknown item";
    }

    @Override
    public String describe() {
        return String.format("%s %d %s Recipe(s) for %s", add ? "Adding" : "Removing", recipes.size(), name, getRecipeInfo());
    }
}