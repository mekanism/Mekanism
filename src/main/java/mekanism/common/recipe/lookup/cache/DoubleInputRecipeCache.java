package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation for {@link IInputRecipeCache} for handling recipes with two inputs.
 */
public abstract class DoubleInputRecipeCache<INPUT_A, INGREDIENT_A extends InputIngredient<INPUT_A>, INPUT_B, INGREDIENT_B extends InputIngredient<INPUT_B>,
      RECIPE extends MekanismRecipe<?> & BiPredicate<INPUT_A, INPUT_B>, CACHE_A extends IInputCache<INPUT_A, INGREDIENT_A, RECIPE>,
      CACHE_B extends IInputCache<INPUT_B, INGREDIENT_B, RECIPE>> extends AbstractInputRecipeCache<RECIPE> {

    private final Set<RECIPE> complexIngredientA = new HashSet<>();
    private final Set<RECIPE> complexIngredientB = new HashSet<>();
    private final Set<RECIPE> complexRecipes = new HashSet<>();
    private final Function<RECIPE, INGREDIENT_A> inputAExtractor;
    private final Function<RECIPE, INGREDIENT_B> inputBExtractor;
    private final CACHE_A cacheA;
    private final CACHE_B cacheB;

    protected DoubleInputRecipeCache(MekanismRecipeType<?, RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT_A> inputAExtractor, CACHE_A cacheA,
          Function<RECIPE, INGREDIENT_B> inputBExtractor, CACHE_B cacheB) {
        super(recipeType);
        this.inputAExtractor = inputAExtractor;
        this.inputBExtractor = inputBExtractor;
        this.cacheA = cacheA;
        this.cacheB = cacheB;
    }

    @Override
    public void clear() {
        super.clear();
        cacheA.clear();
        cacheB.clear();
        complexIngredientA.clear();
        complexIngredientB.clear();
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
    public boolean containsInputA(@Nullable Level world, INPUT_A input) {
        return containsInput(world, input, inputAExtractor, cacheA, complexIngredientA);
    }

    /**
     * Checks if there is a matching recipe that has the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public boolean containsInputB(@Nullable Level world, INPUT_B input) {
        return containsInput(world, input, inputBExtractor, cacheB, complexIngredientB);
    }

    /**
     * Checks is there is a matching recipe with the given inputs. This method exists as a helper for insertion predicates and will return true if inputA is not empty and
     * inputB is empty without doing any extra validation on inputA.
     *
     * @param world  World.
     * @param inputA Recipe input A.
     * @param inputB Recipe input B.
     *
     * @return {@code true} if there is a match or if inputA is not empty and inputB is empty.
     *
     * @apiNote If you are trying to insert inputA and already have inputB in the machine call this method, otherwise call
     * {@link #containsInputBA(Level, Object, Object)}.
     */
    public boolean containsInputAB(@Nullable Level world, INPUT_A inputA, INPUT_B inputB) {
        return containsPairing(world, inputA, inputAExtractor, cacheA, complexIngredientA, inputB, inputBExtractor, cacheB, complexIngredientB);
    }

    /**
     * Checks is there is a matching recipe with the given inputs. This method exists as a helper for insertion predicates and will return true if inputB is not empty and
     * inputA is empty without doing any extra validation on inputA.
     *
     * @param world  World.
     * @param inputA Recipe input A.
     * @param inputB Recipe input B.
     *
     * @return {@code true} if there is a match or if inputB is not empty and inputA is empty.
     *
     * @apiNote If you are trying to insert inputA and already have inputA in the machine call this method, otherwise call
     * {@link #containsInputAB(Level, Object, Object)}.
     */
    public boolean containsInputBA(@Nullable Level world, INPUT_A inputA, INPUT_B inputB) {
        return containsPairing(world, inputB, inputBExtractor, cacheB, complexIngredientB, inputA, inputAExtractor, cacheA, complexIngredientA);
    }

    /**
     * Finds the first recipe that matches the given inputs.
     *
     * @param world  World.
     * @param inputA Recipe input A.
     * @param inputB Recipe input B.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     *
     * @implNote Lookups up the recipe first from the A input map (the fact that it is A is arbitrary and just as well could be B).
     * @apiNote To force using the B input map instead for recipe lookup use {@link #findFirstRecipe(Level, Object, Object, boolean)}.
     */
    @Nullable
    public RECIPE findFirstRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB) {
        return findFirstRecipe(world, inputA, inputB, true);
    }

    /**
     * Finds the first recipe that matches the given inputs.
     *
     * @param world     World.
     * @param inputA    Recipe input A.
     * @param inputB    Recipe input B.
     * @param useCacheA {@code true} to use the A input map, {@code false} to use the B input map.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     *
     * @apiNote This should be used for recipe lookup in cases where we expect that the B input map will be a lot larger so the map based lookup will be more efficient
     * than looking up based on the A input map and then having to iterate all the recipes. For example the chemical washer recipes.
     */
    @Nullable
    public RECIPE findFirstRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, boolean useCacheA) {
        if (cacheA.isEmpty(inputA) || cacheB.isEmpty(inputB)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        //Lookup a recipe from the specified input map
        RECIPE recipe;
        if (useCacheA) {
            recipe = findFirstRecipe(inputA, inputB, cacheA.getRecipes(inputA));
        } else {
            recipe = findFirstRecipe(inputA, inputB, cacheB.getRecipes(inputB));
        }
        // if there is no recipe, then check if any of our complex recipes (either a or b being complex) match
        return recipe == null ? findFirstRecipe(inputA, inputB, complexRecipes) : recipe;
    }

    @Nullable
    private RECIPE findFirstRecipe(INPUT_A inputA, INPUT_B inputB, Iterable<RECIPE> recipes) {
        for (RECIPE recipe : recipes) {
            if (recipe.test(inputA, inputB)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Finds the first recipe that matches the given input type ignoring the size requirement and also matches the given recipe predicate.
     *
     * @param world         World.
     * @param inputA        Recipe input A.
     * @param inputB        Recipe input B.
     * @param matchCriteria Extra validation criteria to check.
     *
     * @return Recipe matching the given input type, or {@code null} if no recipe matches.
     *
     * @apiNote This is mainly meant as a helper for factories so makes the assumption that if inputB is empty it doesn't factor it into the check at all.
     */
    @Nullable
    public <DATA> RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, DATA data, CheckRecipeType<INPUT_A, INPUT_B, RECIPE, DATA> matchCriteria) {
        if (cacheA.isEmpty(inputA)) {
            //Don't allow empty primary inputs
            return null;
        }
        initCacheIfNeeded(world);
        if (cacheB.isEmpty(inputB)) {
            //If b is empty, lookup by A and our match criteria
            for (RECIPE recipe : cacheA.getRecipes(inputA)) {
                if (matchCriteria.testType(recipe, inputA, inputB, data)) {
                    return recipe;
                }
            }
            for (RECIPE complexRecipe : complexRecipes) {
                if (inputAExtractor.apply(complexRecipe).testType(inputA) && matchCriteria.testType(complexRecipe, inputA, inputB, data)) {
                    return complexRecipe;
                }
            }
        } else {
            for (RECIPE recipe : cacheA.getRecipes(inputA)) {
                if (inputBExtractor.apply(recipe).testType(inputB) && matchCriteria.testType(recipe, inputA, inputB, data)) {
                    return recipe;
                }
            }
            for (RECIPE complexRecipe : complexRecipes) {
                if (inputAExtractor.apply(complexRecipe).testType(inputA)) {
                    if (inputBExtractor.apply(complexRecipe).testType(inputB) && matchCriteria.testType(complexRecipe, inputA, inputB, data)) {
                        return complexRecipe;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void initCache(List<RecipeHolder<RECIPE>> recipes) {
        for (RecipeHolder<RECIPE> recipeHolder : recipes) {
            RECIPE recipe = recipeHolder.value();
            boolean complexA = cacheA.mapInputs(recipe, inputAExtractor.apply(recipe));
            boolean complexB = cacheB.mapInputs(recipe, inputBExtractor.apply(recipe));
            if (complexA) {
                complexIngredientA.add(recipe);
            }
            if (complexB) {
                complexIngredientB.add(recipe);
            }
            if (complexA || complexB) {
                complexRecipes.add(recipe);
            }
        }
    }

    /**
     * Helper expansion class for {@link DoubleInputRecipeCache} to simplify the generics when both inputs are of the same type.
     */
    public abstract static class DoubleSameInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe<?> & BiPredicate<INPUT, INPUT>,
          CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>> extends DoubleInputRecipeCache<INPUT, INGREDIENT, INPUT, INGREDIENT, RECIPE, CACHE, CACHE> {

        protected DoubleSameInputRecipeCache(MekanismRecipeType<?, RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputAExtractor,
              Function<RECIPE, INGREDIENT> inputBExtractor, Supplier<CACHE> cacheSupplier) {
            super(recipeType, inputAExtractor, cacheSupplier.get(), inputBExtractor, cacheSupplier.get());
        }
    }

    @FunctionalInterface
    public interface CheckRecipeType<INPUT_A, INPUT_B, RECIPE extends MekanismRecipe<?> & BiPredicate<INPUT_A, INPUT_B>, DATA> {

        boolean testType(RECIPE recipe, INPUT_A inputA, INPUT_B inputB, DATA data);
    }
}