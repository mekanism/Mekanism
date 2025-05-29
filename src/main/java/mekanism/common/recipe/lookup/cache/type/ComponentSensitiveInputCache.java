package mekanism.common.recipe.lookup.cache.type;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;

/**
 * Extended input cache that implements the backend handling to allow for both the basic key based input lookup that {@link BaseInputCache} provides, and also a more
 * advanced mapping that is Data Component based.
 */
public abstract class ComponentSensitiveInputCache<KEY, INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe<?>>
      extends BaseInputCache<KEY, INPUT, INGREDIENT, RECIPE> {

    /**
     * Map of NBT based keys representing inputs to a set of the recipes that contain said input. This allows for quick contains checking by checking if a key exists, as
     * well as quicker recipe lookup.
     */
    private final Map<INPUT, List<RECIPE>> componentInputCache;

    protected ComponentSensitiveInputCache(Hash.Strategy<? super INPUT> componentHashStrategy) {
        this.componentInputCache = new Object2ObjectOpenCustomHashMap<>(0, Hash.VERY_FAST_LOAD_FACTOR, componentHashStrategy);
    }

    @Override
    public void clear() {
        super.clear();
        componentInputCache.clear();
    }

    /**
     * @implNote Checks the more specific Data Component based cache before checking the more generic base type.
     */
    @Override
    public boolean contains(INPUT input) {
        return super.contains(input) || (!componentInputCache.isEmpty() && componentInputCache.containsKey(input));
    }

    /**
     * @implNote Checks the more specific Data Component based cache before checking the more generic base type.
     */
    @Override
    public Iterable<RECIPE> getRecipes(INPUT input) {
        if (componentInputCache.isEmpty()) {
            return super.getRecipes(input);
        }
        List<RECIPE> nbtRecipes = componentInputCache.getOrDefault(input, Collections.emptyList());
        if (nbtRecipes.isEmpty()) {
            return super.getRecipes(input);
        }
        Collection<RECIPE> basicRecipes = (Collection<RECIPE>) super.getRecipes(input);
        if (basicRecipes.isEmpty()) {
            return nbtRecipes;
        }
        return Iterables.concat(nbtRecipes, basicRecipes);
    }

    /**
     * Adds a given recipe to the input cache using the corresponding Data Component based key.
     *
     * @param input  Key representing the input including any Data Component data. Must not be a "raw" key as we are persisting it in our input cache.
     * @param recipe Recipe to add.
     */
    protected void addNbtInputCache(INPUT input, RECIPE recipe) {
        if (!componentInputCache.containsKey(input)) {
            componentInputCache.put(input, Collections.singletonList(recipe));
        } else {
            List<RECIPE> existing = componentInputCache.get(input);
            if (existing.size() == 1) {
                List<RECIPE> newList = new ArrayList<>(existing);
                newList.add(recipe);
                componentInputCache.put(input, newList);
            } else {
                existing.add(recipe);
            }
        }
    }
}