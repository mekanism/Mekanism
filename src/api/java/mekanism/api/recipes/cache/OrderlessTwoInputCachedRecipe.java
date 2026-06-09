package mekanism.api.recipes.cache;

import java.util.function.BooleanSupplier;
import mekanism.api.recipes.OrderlessTwoInputRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.core.TypedInstance;

/**
 * Base class to help implement handling of either side two input recipes.
 *
 * @since 10.8.0
 */
public class OrderlessTwoInputCachedRecipe<HOLDER, INPUT extends TypedInstance<HOLDER>, INGREDIENT extends InputIngredient<HOLDER, INPUT>, OUTPUT,
      RECIPE extends OrderlessTwoInputRecipe<HOLDER, INPUT, INGREDIENT, ?, OUTPUT>> extends TwoInputCachedRecipe<HOLDER, INPUT, INGREDIENT, HOLDER, INPUT, INGREDIENT, OUTPUT, RECIPE> {

    /**
     * @param recipe            Recipe.
     * @param recheckAllErrors  Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                          do this every tick or if there is no one viewing recipes.
     * @param leftInputHandler  Left input handler.
     * @param rightInputHandler Right input handler.
     * @param outputHandler     Output handler.
     */
    public OrderlessTwoInputCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<HOLDER, INPUT> leftInputHandler,
          IInputHandler<HOLDER, INPUT> rightInputHandler, IOutputHandler<OUTPUT> outputHandler) {
        super(recipe, recheckAllErrors, leftInputHandler, rightInputHandler, outputHandler);
    }

    @Override
    protected void calculateOperations(OperationTracker tracker) {
        if (tracker.shouldContinueChecking()) {
            INPUT inputA = inputHandler.getInput();
            if (inputHandler.isEmpty(inputA)) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                INPUT inputB = secondaryInputHandler.getInput();
                if (inputHandler.isEmpty(inputB)) {
                    //No input, we don't know if the recipe matches or not so treat it as not matching
                    tracker.mismatchedRecipe();
                } else if (!recipe.getInputA().test(inputA) || !recipe.getRightInput().test(inputB)) {
                    //If one of our inputs is invalid for the side it is on, switch them so that we can check
                    // if they are just reversed which side they are on and there is a valid recipe for them
                    // if they are on the other side
                    CachedRecipeHelper.twoInputCalculateOperationsThisTick(tracker, inputHandler, inputBSupplier, secondaryInputHandler, inputASupplier, inputsSetter,
                          outputHandler, outputGetter, outputSetter);
                } else {
                    //Otherwise just call super as the things are already in the correct slots
                    super.calculateOperations(tracker);
                }
            }
        }
    }
}