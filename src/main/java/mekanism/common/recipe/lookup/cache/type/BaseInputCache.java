package mekanism.common.recipe.lookup.cache.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import org.jspecify.annotations.Nullable;

/// Base input cache that implements the backend handling and lookup of a single basic key based input.
public abstract class BaseInputCache<KEY, INPUT extends TypedInstance<KEY>, INGREDIENT extends InputIngredient<KEY, INPUT>, RECIPE extends MekanismRecipe<?>>
      implements IInputCache<KEY, INPUT, INGREDIENT, RECIPE> {

    /// Map of keys representing inputs to a set of the recipes that contain said input. This allows for quick contains checking by checking if a key exists, as well as
    /// quicker recipe lookup.
    private final Map<KEY, List<RECIPE>> inputCache = new HashMap<>();

    @Override
    public void clear() {
        inputCache.clear();
    }

    @Override
    public boolean contains(TypedInstance<KEY> input) {
        return inputCache.containsKey(input.typeHolder().value());
    }

    @Override
    public boolean contains(TypedInstance<KEY> input, Predicate<RECIPE> matchCriteria) {
        for (RECIPE recipe : getRecipes(input)) {
            if (matchCriteria.test(recipe)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public RECIPE findFirstRecipe(TypedInstance<KEY> input, Predicate<RECIPE> matchCriteria) {
        for (RECIPE recipe : getRecipes(input)) {
            if (matchCriteria.test(recipe)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public Iterable<RECIPE> getRecipes(TypedInstance<KEY> input) {
        return inputCache.getOrDefault(input.typeHolder().value(), Collections.emptyList());
    }

    /// Adds a given recipe to the input cache using the corresponding key.
    ///
    /// @param input  Key representing the input.
    /// @param recipe Recipe to add.
    protected void addInputCache(KEY input, RECIPE recipe) {
        if (!inputCache.containsKey(input)) {
            inputCache.put(input, Collections.singletonList(recipe));
        } else {
            List<RECIPE> existing = inputCache.get(input);
            if (existing.size() == 1) {
                List<RECIPE> newList = new ArrayList<>(existing);
                newList.add(recipe);
                inputCache.put(input, newList);
            } else {
                existing.add(recipe);
            }
        }
    }

    /// Adds a given recipe to the input cache using the corresponding key.
    ///
    /// @param input  Key representing the input.
    /// @param recipe Recipe to add.
    protected void addInputCache(Holder<KEY> input, RECIPE recipe) {
        addInputCache(input.value(), recipe);
    }
}