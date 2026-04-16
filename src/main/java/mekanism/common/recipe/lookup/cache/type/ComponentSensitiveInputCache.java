package mekanism.common.recipe.lookup.cache.type;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.util.strategy.BasicStrategy;
import org.jetbrains.annotations.Nullable;

/**
 * Extended input cache that implements the backend handling to allow for both the basic key based input lookup that {@link BaseInputCache} provides, and also a more
 * advanced mapping that is Data Component based.
 */
public abstract class ComponentSensitiveInputCache<KEY, INPUT extends TypedInstance<KEY> & DataComponentHolder, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe<?>>
      extends BaseInputCache<KEY, INPUT, INGREDIENT, RECIPE> {

    /**
     * Map of ResourceKey to Map of components to lists.
     */
    //todo 26.1 can this use a Reference map now that it uses ResourceKey?
    private final Map<ResourceKey<KEY>, Map<DataComponentMap, List<RECIPE>>> componentInputCache = new HashMap<>();

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
        return super.contains(input) || componentCacheContains(input);
    }

    private boolean componentCacheContains(INPUT input) {
        if (componentInputCache.isEmpty()) {
            return false;
        }
        Map<DataComponentMap, List<RECIPE>> holderMatch = componentInputCache.get(input.typeHolder().getKey());
        return holderMatch != null && holderMatch.containsKey(input.getComponents());
    }

    /**
     * @implNote Checks the more specific Data Component based cache before checking the more generic base type.
     */
    @Override
    public Iterable<RECIPE> getRecipes(INPUT input) {
        if (componentInputCache.isEmpty()) {
            return super.getRecipes(input);
        }
        List<RECIPE> nbtRecipes = getComponentMatches(input);
        if (nbtRecipes == null) {
            return super.getRecipes(input);
        }
        Collection<RECIPE> basicRecipes = (Collection<RECIPE>) super.getRecipes(input);
        if (basicRecipes.isEmpty()) {
            return nbtRecipes;
        }
        return Iterables.concat(nbtRecipes, basicRecipes);
    }

    @Nullable
    private List<RECIPE> getComponentMatches(INPUT input) {
        var holderMatches = componentInputCache.get(input.typeHolder().getKey());
        if (holderMatches == null) {
            return null;
        }
        return holderMatches.get(input.getComponents());
    }

    /**
     * Adds a given recipe to the input cache using the corresponding Data Component based key.
     * Works for EXACT matches only.
     *
     * @param inputHolder Holder representing the KEY
     * @param patch       The component patch to apply against inputHolder for storing in the index
     * @param recipe      Recipe to add.
     */
    protected void addNbtInputCache(Holder<KEY> inputHolder, DataComponentPatch patch, RECIPE recipe) {
        ResourceKey<KEY> key = inputHolder.getKey();
        DataComponentMap components = PatchedDataComponentMap.fromPatch(inputHolder.components(), patch);
        var holderMatch = componentInputCache.get(key);
        if (holderMatch == null) {
            componentInputCache.put(key, Collections.singletonMap(components, Collections.singletonList(recipe)));
        } else {
            if (holderMatch.size() == 1) {
                Map<DataComponentMap, List<RECIPE>> newMap = new Object2ObjectOpenCustomHashMap<>(1, Hash.VERY_FAST_LOAD_FACTOR, BasicStrategy.BASIC);
                newMap.putAll(holderMatch);
                holderMatch = newMap;
            }
            List<RECIPE> existing = holderMatch.get(components);
            if (existing == null) {
                holderMatch.put(components, Collections.singletonList(recipe));
            } else if (existing.size() == 1) {
                List<RECIPE> newList = new ArrayList<>(existing);
                newList.add(recipe);
                holderMatch.put(components, newList);
            } else {
                existing.add(recipe);
            }
        }
    }
}