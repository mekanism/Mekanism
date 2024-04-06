package mekanism.common.recipe.lookup.cache.type;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;

/**
 * Extended input cache that implements the backend handling to allow for both the basic key based input lookup that {@link BaseInputCache} provides, and also a more
 * advanced mapping that is NBT based.
 */
public abstract class NBTSensitiveInputCache<KEY, NBT_KEY, INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe>
      extends BaseInputCache<KEY, INPUT, INGREDIENT, RECIPE> {

    /**
     * Map of NBT based keys representing inputs to a set of the recipes that contain said input. This allows for quick contains checking by checking if a key exists, as
     * well as quicker recipe lookup.
     */
    private final Map<NBT_KEY, Set<RECIPE>> nbtInputCache = new HashMap<>();

    @Override
    public void clear() {
        super.clear();
        nbtInputCache.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Checks the more specific NBT based cache before checking the more generic base type.
     */
    @Override
    public boolean contains(INPUT input) {
        return nbtInputCache.containsKey(createNbtKey(input)) || super.contains(input);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Checks the more specific NBT based cache before checking the more generic base type.
     */
    @Override
    public Iterable<RECIPE> getRecipes(INPUT input) {
        Set<RECIPE> nbtRecipes = nbtInputCache.getOrDefault(createNbtKey(input), Collections.emptySet());
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
     * Creates a key for the given input including NBT for use in querying our input cache.
     *
     * @param input Input to convert into an NBT based key.
     *
     * @return Key representing the given input including any NBT data.
     *
     * @apiNote This key can be "raw" to cut down on any copying as it only used for querying and will not be stored.
     */
    protected abstract NBT_KEY createNbtKey(INPUT input);

    /**
     * Adds a given recipe to the input cache using the corresponding NBT based key.
     *
     * @param input  Key representing the input including any NBT data. Must not be a "raw" key as we are persisting it in our input cache.
     * @param recipe Recipe to add.
     */
    protected void addNbtInputCache(NBT_KEY input, RECIPE recipe) {
        nbtInputCache.computeIfAbsent(input, i -> new HashSet<>()).add(recipe);
    }
}