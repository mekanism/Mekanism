package mekanism.api.recipes.inputs;

public interface ILongInputHandler<INPUT> extends IInputHandler<INPUT> {

    @Override
    default void use(INPUT recipeInput, int operations) {
        //Wrap to the long implementation
        use(recipeInput, (long) operations);
    }

    void use(INPUT recipeInput, long operations);

    @Override
    default int operationsCanSupport(InputIngredient<INPUT> recipeIngredient, int currentMax, int usageMultiplier) {
        //Wrap to the long implementation
        return operationsCanSupport(recipeIngredient, currentMax, (long) usageMultiplier);
    }

    int operationsCanSupport(InputIngredient<INPUT> recipeIngredient, int currentMax, long usageMultiplier);
}