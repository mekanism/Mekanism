package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation for {@link IInputRecipeCache} for handling recipes with a single input.
 */
public abstract class SingleInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe & Predicate<INPUT>,
      CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>> extends AbstractInputRecipeCache<RECIPE> {

    private final Set<RECIPE> complexRecipes = new HashSet<>();
    private final Function<RECIPE, INGREDIENT> inputExtractor;
    private final CACHE cache;

    protected SingleInputRecipeCache(MekanismRecipeType<RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputExtractor, CACHE cache) {
        super(recipeType);
        this.inputExtractor = inputExtractor;
        this.cache = cache;
    }

    @Override
    public void clear() {
        super.clear();
        cache.clear();
        complexRecipes.clear();
    }

    /**
     * Checks if there is a matching recipe that has the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public boolean containsInput(@Nullable Level world, INPUT input) {
        return containsInput(world, input, inputExtractor, cache, complexRecipes);
    }

    /**
     * Finds the first recipe that matches the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RECIPE findFirstRecipe(@Nullable Level world, INPUT input) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        Predicate<RECIPE> matchPredicate = recipe -> recipe.test(input);
        RECIPE recipe = cache.findFirstRecipe(input, matchPredicate);
        return recipe == null ? findFirstRecipe(complexRecipes, matchPredicate) : recipe;
    }

    /**
     * Finds the first recipe that matches the given input type ignoring the size requirement.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input) {
        return findTypeBasedRecipe(world, input, ConstantPredicates.alwaysTrue());
    }

    /**
     * Finds the first recipe that matches the given input type ignoring the size requirement and also matches the given recipe predicate.
     *
     * @param world         World.
     * @param input         Recipe input.
     * @param matchCriteria Extra validation criteria to check.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input, Predicate<RECIPE> matchCriteria) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        RECIPE recipe = cache.findFirstRecipe(input, matchCriteria);
        return recipe == null ? findFirstRecipe(complexRecipes, r -> inputExtractor.apply(r).testType(input) && matchCriteria.test(r)) : recipe;
    }

    @Override
    protected void initCache(List<RECIPE> recipes) {
        for (RECIPE recipe : recipes) {
            if (cache.mapInputs(recipe, inputExtractor.apply(recipe))) {
                complexRecipes.add(recipe);
            }
        }
    }
}