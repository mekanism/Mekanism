package mekanism.common.recipe.lookup.cache.type;

import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import org.jetbrains.annotations.Nullable;

/**
 * Base interface describing how a specific input type is cached to allow for quick lookup of recipes by input both for finding the recipes and checking if any even exist
 * with the given input.
 */
public interface IInputCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe<?>> {

    /**
     * Checks if this {@link IInputCache} knows about the given input.
     *
     * @param input Input to check.
     *
     * @return {@code true} if this cache does have the given input, {@code false} if there isn't.
     */
    boolean contains(INPUT input);

    /**
     * Checks if this {@link IInputCache} knows about the given input, and if it does, checks if any of the recipes that match that input type match the given recipe
     * predicate.
     *
     * @param input         Input to check.
     * @param matchCriteria Predicate to further validate recipes with.
     *
     * @return {@code true} if this cache does have the given input and a recipe that matches, {@code false} if there isn't.
     */
    boolean contains(INPUT input, Predicate<RECIPE> matchCriteria);

    /**
     * Gets the recipe for the given input. Note: that no validation is done here about the input matching the recipe's criteria in regard to required amounts, all that
     * is done regarding the input is that the type is used in the recipe.
     *
     * @param input Input to check.
     *
     * @return Recipes for the given input that matches the given criteria, or empty if no recipe matches.
     */
    Iterable<RECIPE> getRecipes(INPUT input);

    /**
     * Finds the first recipe for the given input that matches the given match criteria. Note: that no validation is done here about the input matching the recipe's
     * criteria in regard to required amounts, all that is done regarding the input is that the type is used in the recipe.
     *
     * @param input         Input to check.
     * @param matchCriteria Predicate to further validate recipes with.
     *
     * @return Recipe for the given input that matches the given criteria, or {@code null} if no recipe matches.
     */
    @Nullable
    RECIPE findFirstRecipe(INPUT input, Predicate<RECIPE> matchCriteria);

    /**
     * Maps the given ingredient and adds it into this {@link IInputCache} as a quicker lookup for the given recipe.
     *
     * @param recipe          Recipe the given ingredient is an input of.
     * @param inputIngredient Ingredient to map and cache.
     *
     * @return {@code true} if any part of the ingredient is complex and the {@link mekanism.common.recipe.lookup.cache.IInputRecipeCache} will need to do extra handling,
     * or {@code false} if we were able to fully cache the ingredient's components.
     */
    boolean mapInputs(RECIPE recipe, INGREDIENT inputIngredient);

    /**
     * Clears this {@link IInputCache}
     */
    void clear();

    /**
     * Helper method that is not actually used by the caches themselves, but allows for the broader {@link mekanism.common.recipe.lookup.cache.IInputRecipeCache} to
     * easily check if an input is empty without requiring a bunch of extra dummy classes implementing empty checks based on the given generics.
     *
     * @param input Input to check
     *
     * @return {@code true} if the input is empty.
     */
    boolean isEmpty(INPUT input);
}