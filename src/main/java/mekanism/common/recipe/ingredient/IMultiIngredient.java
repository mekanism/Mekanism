package mekanism.common.recipe.ingredient;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.recipes.ingredients.InputIngredient;
import org.jetbrains.annotations.NotNull;

public interface IMultiIngredient<TYPE, INGREDIENT extends InputIngredient<@NotNull TYPE>> extends InputIngredient<@NotNull TYPE> {

    /**
     * For use in recipe input caching, checks all ingredients even if some match.
     *
     * @return {@code true} if any ingredient matches.
     */
    boolean forEachIngredient(Predicate<INGREDIENT> checker);

    /**
     * @apiNote For use in flattening multi ingredients, this should return an immutable view.
     */
    List<INGREDIENT> getIngredients();
}