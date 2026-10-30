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
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

/// Base input cache that implements the backend handling and lookup of a single basic key based input.
public abstract class BaseInputCache<KEY, INPUT extends TypedInstance<KEY>, INGREDIENT extends InputIngredient<KEY, INPUT>, RECIPE extends MekanismRecipe<?, ?, ?>>
      implements IInputCache<KEY, INPUT, INGREDIENT, RECIPE> {

    /// Map of keys representing inputs to a set of the recipes that contain said input. This allows for quick contains checking by checking if a key exists, as well as
    /// quicker recipe lookup.
    private final Map<KEY, List<RecipeHolder<RECIPE>>> inputCache = new HashMap<>();

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
        for (RecipeHolder<RECIPE> recipeHolder : getRecipes(input)) {
            if (matchCriteria.test(recipeHolder.value())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public RecipeHolder<RECIPE> findFirstRecipe(TypedInstance<KEY> input, Predicate<RECIPE> matchCriteria) {
        for (RecipeHolder<RECIPE> recipeHolder : getRecipes(input)) {
            if (matchCriteria.test(recipeHolder.value())) {
                return recipeHolder;
            }
        }
        return null;
    }

    @Override
    public Iterable<RecipeHolder<RECIPE>> getRecipes(TypedInstance<KEY> input) {
        return inputCache.getOrDefault(input.typeHolder().value(), Collections.emptyList());
    }

    /// Adds a given recipe to the input cache using the corresponding key.
    ///
    /// @param input        Key representing the input.
    /// @param recipeHolder Recipe to add.
    protected void addInputCache(KEY input, RecipeHolder<RECIPE> recipeHolder) {
        if (!inputCache.containsKey(input)) {
            inputCache.put(input, Collections.singletonList(recipeHolder));
        } else {
            List<RecipeHolder<RECIPE>> existing = inputCache.get(input);
            if (existing.size() == 1) {
                List<RecipeHolder<RECIPE>> newList = new ArrayList<>(existing);
                newList.add(recipeHolder);
                inputCache.put(input, newList);
            } else {
                existing.add(recipeHolder);
            }
        }
    }

    /// Adds a given recipe to the input cache using the corresponding key.
    ///
    /// @param input        Key representing the input.
    /// @param recipeHolder Recipe to add.
    protected void addInputCache(Holder<KEY> input, RecipeHolder<RECIPE> recipeHolder) {
        addInputCache(input.value(), recipeHolder);
    }
}