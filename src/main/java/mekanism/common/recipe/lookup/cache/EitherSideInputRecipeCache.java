package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.level.Level;

/**
 * Similar in concept to {@link DoubleInputRecipeCache} except that it requires both input types to be the same and also allows for them to be in any order.
 */
public abstract class EitherSideInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe & BiPredicate<INPUT, INPUT>,
      CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>> extends AbstractInputRecipeCache<RECIPE> {

    private final Set<RECIPE> complexRecipes = new HashSet<>();
    private final Function<RECIPE, INGREDIENT> inputAExtractor;
    private final Function<RECIPE, INGREDIENT> inputBExtractor;
    private final CACHE cache;

    protected EitherSideInputRecipeCache(MekanismRecipeType<RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputAExtractor,
          Function<RECIPE, INGREDIENT> inputBExtractor, CACHE cache) {
        super(recipeType);
        this.inputAExtractor = inputAExtractor;
        this.inputBExtractor = inputBExtractor;
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
        if (cache.isEmpty(input)) {
            //Don't allow empty inputs
            return false;
        }
        initCacheIfNeeded(world);
        return cache.contains(input) || complexRecipes.stream().anyMatch(recipe -> inputAExtractor.apply(recipe).testType(input) ||
                                                                                   inputBExtractor.apply(recipe).testType(input));
    }

    /**
     * Checks is there is a matching recipe with the given inputs. This method exists as a helper for insertion predicates and will return true if inputA is not empty and
     * inputB is empty without doing any extra validation on inputA. This is similar to {@link DoubleInputRecipeCache#containsInputAB(Level, Object, Object)} and {@link
     * DoubleInputRecipeCache#containsInputBA(Level, Object, Object)} except that because {@link EitherSideInputRecipeCache} assumes both inputs are the same type and
     * that the order doesn't matter we just have one method and require the inputs to be passed in the corresponding order instead.
     *
     * @param world  World.
     * @param inputA Recipe input A.
     * @param inputB Recipe input B.
     *
     * @return {@code true} if there is a match or if inputA is not empty and inputB is empty.
     *
     * @apiNote Pass the input you are trying to insert as inputA and the input you already have as inputB.
     */
    public boolean containsInput(@Nullable Level world, INPUT inputA, INPUT inputB) {
        if (cache.isEmpty(inputA)) {
            //Note: We don't bother checking if b is empty here as it will be verified in containsInputB
            return containsInput(world, inputB);
        } else if (cache.isEmpty(inputB)) {
            return true;
        }
        initCacheIfNeeded(world);
        //Note: Even though we know the cache contains input A, we need to check both input A and input B
        // This is because we want to ensure that we allow the inputs being in either order, but in our
        // secondary validation we check inputB first as we know the recipe contains inputA as one of the
        // inputs, but we want to make sure that we only mark it as valid if the same input is on both sides
        // if the recipe combines two of the same type of ingredient
        if (cache.contains(inputA, recipe -> {
            INGREDIENT ingredientA = inputAExtractor.apply(recipe);
            INGREDIENT ingredientB = inputBExtractor.apply(recipe);
            return ingredientB.testType(inputB) && ingredientA.testType(inputA) || ingredientA.testType(inputB) && ingredientB.testType(inputA);
        })) {
            return true;
        }
        //Our quick lookup cache does not contain it, check any recipes where the ingredients are complex
        return complexRecipes.stream().anyMatch(recipe -> {
            INGREDIENT ingredientA = inputAExtractor.apply(recipe);
            INGREDIENT ingredientB = inputBExtractor.apply(recipe);
            return ingredientA.testType(inputA) && ingredientB.testType(inputB) || ingredientB.testType(inputA) && ingredientA.testType(inputB);
        });
    }

    /**
     * Finds the first recipe that matches the given inputs.
     *
     * @param world  World.
     * @param inputA Recipe input A.
     * @param inputB Recipe input B.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     */
    @Nullable
    public RECIPE findFirstRecipe(@Nullable Level world, INPUT inputA, INPUT inputB) {
        if (cache.isEmpty(inputA) || cache.isEmpty(inputB)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        //Note: The recipe's test method checks both directions
        Predicate<RECIPE> matchPredicate = r -> r.test(inputA, inputB);
        //Lookup a recipe from the input map
        RECIPE recipe = cache.findFirstRecipe(inputA, matchPredicate);
        // if there is no recipe, then check if any of our complex recipes match
        return recipe == null ? findFirstRecipe(complexRecipes, matchPredicate) : recipe;
    }

    @Override
    protected void initCache(List<RECIPE> recipes) {
        for (RECIPE recipe : recipes) {
            boolean complexA = cache.mapInputs(recipe, inputAExtractor.apply(recipe));
            boolean complexB = cache.mapInputs(recipe, inputBExtractor.apply(recipe));
            if (complexA || complexB) {
                complexRecipes.add(recipe);
            }
        }
    }
}