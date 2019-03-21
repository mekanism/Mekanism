package mekanism.common.integration.crafttweaker.util;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import mekanism.common.integration.crafttweaker.helpers.RecipeInfoHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;

public abstract class RecipeMapModification<INPUT extends MachineInput<INPUT>, RECIPE extends MachineRecipe<INPUT, ?, RECIPE>> implements
      IAction {

    protected final HashMap<INPUT, RECIPE> recipes;
    protected final Map<INPUT, RECIPE> map;
    protected final String name;
    protected boolean add;

    protected RecipeMapModification(String name, boolean add, Recipe<INPUT, ?, RECIPE> recipeType) {
        this.name = name;
        this.map = recipeType.get();
        this.add = add;
        this.recipes = new HashMap<>();
    }

    @Override
    public void apply() {
        if (!recipes.isEmpty()) {
            if (add) {
                for (Entry<INPUT, RECIPE> entry : recipes.entrySet()) {
                    INPUT key = entry.getKey();
                    RECIPE value = entry.getValue();
                    if (map.put(key, value) != null) {
                        CraftTweakerAPI.logInfo(String.format("Overwritten %s Recipe for %s", name,
                              RecipeInfoHelper.getRecipeInfo(new AbstractMap.SimpleEntry<>(entry.getKey(), value))));
                    }
                }
            } else {
                for (INPUT key : recipes.keySet()) {
                    if (map.remove(key) == null) {
                        CraftTweakerAPI.logError(String.format("Error removing %s Recipe : null object", name));
                    }
                }
            }
        }
    }

    private String getRecipeInfo() {
        if (!recipes.isEmpty()) {
            return recipes.entrySet().stream().filter(Objects::nonNull).map(RecipeInfoHelper::getRecipeInfo)
                  .collect(Collectors.joining(", "));
        }
        return "Unknown item";
    }

    @Override
    public String describe() {
        return String.format("Removing %d %s Recipe(s) for %s", recipes.size(), name, getRecipeInfo());
    }
}