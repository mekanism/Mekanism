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
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriPredicate;
import org.jspecify.annotations.Nullable;

/// Basic implementation for [IInputRecipeCache] for handling recipes with a single input.
public abstract class SingleInputRecipeCache<TYPE, STACK extends TypedInstance<TYPE>, INGREDIENT extends InputIngredient<TYPE, STACK>,
      RECIPE extends MekanismRecipe<?, ?, ?> & Predicate<STACK>, CACHE extends IInputCache<TYPE, STACK, INGREDIENT, RECIPE>> extends AbstractInputRecipeCache<RECIPE> {

    private final Set<RecipeHolder<RECIPE>> complexRecipes = new HashSet<>();
    private final Function<RECIPE, INGREDIENT> inputExtractor;
    private final CACHE cache;

    protected SingleInputRecipeCache(MekanismRecipeType<?, ?, ?, RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputExtractor, CACHE cache) {
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

    /// Checks if there is a matching recipe that has the given input.
    ///
    /// @param world World.
    /// @param input Recipe input.
    ///
    /// @return `true` if there is a match, `false` if there isn't.
    public boolean containsInput(@Nullable Level world, TypedInstance<TYPE> input) {
        return containsInput(world, input, inputExtractor, cache, complexRecipes);
    }

    /// Finds the first recipe that matches the given input.
    ///
    /// @param world World.
    /// @param input Recipe input.
    ///
    /// @return Recipe matching the given input, or `null` if no recipe matches.
    @Nullable
    public RecipeHolder<RECIPE> findFirstRecipe(@Nullable Level world, STACK input) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        RecipeHolder<RECIPE> recipe = findFirstRecipe(input, cache.getRecipes(input));
        return recipe == null ? findFirstRecipe(input, complexRecipes) : recipe;
    }

    @Nullable
    private RecipeHolder<RECIPE> findFirstRecipe(STACK input, Iterable<RecipeHolder<RECIPE>> recipes) {
        for (RecipeHolder<RECIPE> recipeHolder : recipes) {
            if (recipeHolder.value().test(input)) {
                return recipeHolder;
            }
        }
        return null;
    }

    /// Finds the first recipe that matches the given input type ignoring the size requirement.
    ///
    /// @param world World.
    /// @param input Recipe input.
    ///
    /// @return Recipe matching the given input, or `null` if no recipe matches.
    @Nullable
    public RECIPE findTypeBasedRecipe(@Nullable Level world, TypedInstance<TYPE> input) {
        RecipeHolder<RECIPE> recipeHolder = findTypeBasedRecipeHolder(world, input);
        return recipeHolder == null ? null : recipeHolder.value();
    }

    /// Finds the first recipe that matches the given input type ignoring the size requirement.
    ///
    /// @param world World.
    /// @param input Recipe input.
    ///
    /// @return Recipe matching the given input, or `null` if no recipe matches.
    @Nullable
    public RecipeHolder<RECIPE> findTypeBasedRecipeHolder(@Nullable Level world, TypedInstance<TYPE> input) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        RecipeHolder<RECIPE> recipeHolder = cache.findFirstRecipe(input, ConstantPredicates.alwaysTrue());
        if (recipeHolder == null) {
            for (RecipeHolder<RECIPE> complexRecipeHolder : complexRecipes) {
                RECIPE complexRecipe = complexRecipeHolder.value();
                if (inputExtractor.apply(complexRecipe).testType(input)) {
                    return complexRecipeHolder;
                }
            }
        }
        return recipeHolder;
    }

    /// Finds the first recipe that matches the given input type ignoring the size requirement and also matches the given recipe predicate.
    ///
    /// @param world         World.
    /// @param input         Recipe input.
    /// @param matchCriteria Extra validation criteria to check.
    ///
    /// @return Recipe matching the given input, or `null` if no recipe matches.
    @Nullable
    public <INPUT extends TypedInstance<TYPE>, DATA> RecipeHolder<RECIPE> findTypeBasedRecipe(@Nullable Level world, INPUT input, DATA data, TriPredicate<? super RECIPE, INPUT, DATA> matchCriteria) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        for (RecipeHolder<RECIPE> recipeHolder : cache.getRecipes(input)) {
            RECIPE recipe = recipeHolder.value();
            if (matchCriteria.test(recipe, input, data)) {
                return recipeHolder;
            }
        }
        for (RecipeHolder<RECIPE> complexRecipeHolder : complexRecipes) {
            RECIPE complexRecipe = complexRecipeHolder.value();
            if (inputExtractor.apply(complexRecipe).testType(input) && matchCriteria.test(complexRecipe, input, data)) {
                return complexRecipeHolder;
            }
        }
        return null;
    }

    /// Finds the first recipe that matches the given input type ignoring the size requirement and also matches the given recipe predicate.
    ///
    /// @param world         World.
    /// @param input         Recipe input.
    /// @param matchCriteria Extra validation criteria to check.
    ///
    /// @return Recipe matching the given input, or `null` if no recipe matches.
    @Nullable
    public <INPUT extends TypedInstance<TYPE>, DATA_1, DATA_2> RecipeHolder<RECIPE> findTypeBasedRecipe(@Nullable Level world, INPUT input, DATA_1 data1, DATA_2 data2,
          CheckRecipeType<TYPE, INPUT, RECIPE, DATA_1, DATA_2> matchCriteria) {
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        for (RecipeHolder<RECIPE> recipeHolder : cache.getRecipes(input)) {
            if (matchCriteria.testType(recipeHolder.value(), input, data1, data2)) {
                return recipeHolder;
            }
        }
        for (RecipeHolder<RECIPE> complexRecipeHolder : complexRecipes) {
            RECIPE complexRecipe = complexRecipeHolder.value();
            if (inputExtractor.apply(complexRecipe).testType(input) && matchCriteria.testType(complexRecipe, input, data1, data2)) {
                return complexRecipeHolder;
            }
        }
        return null;
    }

    @Override
    protected void initCache(List<RecipeHolder<RECIPE>> recipes) {
        for (RecipeHolder<RECIPE> recipeHolder : recipes) {
            RECIPE recipe = recipeHolder.value();
            if (cache.mapInputs(recipeHolder, inputExtractor.apply(recipe))) {
                complexRecipes.add(recipeHolder);
            }
        }
    }

    @FunctionalInterface
    public interface CheckRecipeType<TYPE, INPUT extends TypedInstance<TYPE>, RECIPE extends MekanismRecipe<?, ?, ?> & Predicate<? extends TypedInstance<TYPE>>, DATA_1, DATA_2> {

        boolean testType(RECIPE recipe, INPUT input, DATA_1 data1, DATA_2 data2);
    }
}