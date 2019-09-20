package mekanism.api.recipes.inputs;

import javax.annotation.Nullable;

public interface IInputHandler<INPUT> {

    //TODO: Note that the returned value should not be modified
    INPUT getInput();

    //TODO: 1.14, make things return an "empty" instance instead of null
    // given in 1.12 a bunch of things like fluids currently, don't have an EMPTY type
    @Nullable
    INPUT getRecipeInput(InputIngredient<INPUT> recipeIngredient);

    void use(INPUT recipeInput, int operations);

    //TODO: Rename
    int operationsCanSupport(InputIngredient<INPUT> recipeIngredient, int currentMax);
}