package mekanism.common.recipe.ingredients;

import java.util.List;
import javax.annotation.Nonnull;

//TODO: Maybe eventually get rid of this and use the built in Ingredient.
// The reason for this existing for now is to not have to implement all the
// things that Ingredient needs AND to not verify that they properly exist
public interface IMekanismIngredient<TYPE> {

    @Nonnull
    List<TYPE> getMatching();

    boolean contains(@Nonnull TYPE stack);
}