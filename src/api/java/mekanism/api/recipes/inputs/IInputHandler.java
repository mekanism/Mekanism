package mekanism.api.recipes.inputs;

public interface IInputHandler<INPUT> {

    //TODO: Note that the returned value should not be modified
    INPUT getInput();

    INPUT getRecipeInput(InputIngredient<INPUT> recipeIngredient);

    void use(INPUT recipeInput, int operations);

    //TODO: Rename
    int operationsCanSupport(InputIngredient<INPUT> recipeIngredient, int currentMax);
}