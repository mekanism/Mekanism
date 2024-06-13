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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriPredicate;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation for {@link IInputRecipeCache} for handling recipes with a single input.
 */
public abstract class SingleInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe<?> & Predicate<INPUT>,
      CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>> extends AbstractInputRecipeCache<RECIPE> {

    private final Set<RECIPE> complexRecipes = new HashSet<>();
    private final Function<RECIPE, INGREDIENT> inputExtractor;
    private final CACHE cache;

    protected SingleInputRecipeCache(MekanismRecipeType<?, RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputExtractor, CACHE cache) {
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
        RECIPE recipe = findFirstRecipe(input, cache.getRecipes(input));
        return recipe == null ? findFirstRecipe(input, complexRecipes) : recipe;
    }

    @Nullable
    private RECIPE findFirstRecipe(INPUT input, Iterable<RECIPE> recipes) {
        for (RECIPE recipe : recipes) {
            if (recipe.test(input)) {
                return recipe;
            }
        }
        return null;
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
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        RECIPE recipe = cache.findFirstRecipe(input, ConstantPredicates.alwaysTrue());
        if (recipe == null) {
            for (RECIPE complexRecipe : complexRecipes) {
                if (inputExtractor.apply(complexRecipe).testType(input)) {
                    return complexRecipe;
                }
            }
        }
        return recipe;
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
    public <DATA> RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input, DATA data, TriPredicate<RECIPE, INPUT, DATA> matchCriteria) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        for (RECIPE recipe : cache.getRecipes(input)) {
            if (matchCriteria.test(recipe, input, data)) {
                return recipe;
            }
        }
        for (RECIPE complexRecipe : complexRecipes) {
            if (inputExtractor.apply(complexRecipe).testType(input) && matchCriteria.test(complexRecipe, input, data)) {
                return complexRecipe;
            }
        }
        return null;
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
    public <DATA_1, DATA_2> RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input, DATA_1 data1, DATA_2 data2, CheckRecipeType<INPUT, RECIPE, DATA_1, DATA_2> matchCriteria) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        for (RECIPE recipe : cache.getRecipes(input)) {
            if (matchCriteria.testType(recipe, input, data1, data2)) {
                return recipe;
            }
        }
        for (RECIPE complexRecipe : complexRecipes) {
            if (inputExtractor.apply(complexRecipe).testType(input) && matchCriteria.testType(complexRecipe, input, data1, data2)) {
                return complexRecipe;
            }
        }
        return null;
    }

    @Override
    protected void initCache(List<RecipeHolder<RECIPE>> recipes) {
        for (RecipeHolder<RECIPE> recipeHolder : recipes) {
            RECIPE recipe = recipeHolder.value();
            if (cache.mapInputs(recipe, inputExtractor.apply(recipe))) {
                complexRecipes.add(recipe);
            }
        }
    }

    @FunctionalInterface
    public interface CheckRecipeType<INPUT, RECIPE extends MekanismRecipe<?> & Predicate<INPUT>, DATA_1, DATA_2> {

        boolean testType(RECIPE recipe, INPUT input, DATA_1 data1, DATA_2 data2);
    }
}