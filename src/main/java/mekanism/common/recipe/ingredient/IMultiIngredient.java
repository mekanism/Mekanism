package mekanism.common.recipe.ingredient;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.ingredients.InputIngredient;

public interface IMultiIngredient<TYPE, INGREDIENT extends InputIngredient<@NonNull TYPE>> extends InputIngredient<@NonNull TYPE> {

    /**
     * For use in recipe input caching, checks all ingredients even if some match.
     *
     * @return {@code true} if any ingredient matches.
     */
    boolean forEachIngredient(Predicate<INGREDIENT> checker);
}