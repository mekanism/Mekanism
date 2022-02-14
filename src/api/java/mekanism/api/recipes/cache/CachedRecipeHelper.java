package mekanism.api.recipes.cache;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;

/**
 * Helper class for implementing simple variants of calculateOperationsThisTick
 */
public class CachedRecipeHelper {

    private CachedRecipeHelper() {
    }

    /**
     * Calculates the operations that would occur this tick for recipes with one input.
     *
     * @param tracker         Tracker of current errors and max operations.
     * @param inputHandler    Input handler.
     * @param inputIngredient Supplier of the recipe's input ingredient.
     * @param inputSetter     Consumer to set a cached value of the input to not have to recalculate it again.
     * @param outputHandler   Output handler.
     * @param outputGetter    Gets the recipe's output when given the corresponding input.
     * @param outputSetter    Consumer to set the cached value of the output to not have to recalculate it again.
     * @param emptyCheck      Checks if the input is empty.
     */
    public static <INPUT, OUTPUT> void oneInputCalculateOperationsThisTick(OperationTracker tracker, IInputHandler<INPUT> inputHandler,
          Supplier<? extends InputIngredient<INPUT>> inputIngredient, Consumer<INPUT> inputSetter, IOutputHandler<OUTPUT> outputHandler,
          Function<INPUT, OUTPUT> outputGetter, Consumer<OUTPUT> outputSetter, Predicate<INPUT> emptyCheck) {
        if (tracker.shouldContinueChecking()) {
            INPUT input = inputHandler.getRecipeInput(inputIngredient.get());
            inputSetter.accept(input);
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(input)
            if (emptyCheck.test(input)) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                //Calculate the current max based on the input
                inputHandler.calculateOperationsCanSupport(tracker, input);
                if (tracker.shouldContinueChecking()) {
                    OUTPUT output = outputGetter.apply(input);
                    outputSetter.accept(output);
                    //Calculate the max based on the space in the output
                    outputHandler.calculateOperationsCanSupport(tracker, output);
                }
            }
        }
    }

    /**
     * Calculates the operations that would occur this tick for recipes with two inputs.
     *
     * @param tracker          Tracker of current errors and max operations.
     * @param inputAHandler    Primary input handler.
     * @param inputAIngredient Supplier of the recipe's primary input ingredient.
     * @param inputBHandler    Secondary input handler.
     * @param inputBIngredient Supplier of the recipe's secondary input ingredient.
     * @param inputsSetter     Consumer to set the cached values of the inputs to not have to recalculate them again.
     * @param outputHandler    Output handler.
     * @param outputGetter     Gets the recipe's output when given the corresponding inputs.
     * @param outputSetter     Consumer to set the cached value of the output to not have to recalculate it again.
     * @param emptyCheckA      Checks if the primary input is empty.
     * @param emptyCheckB      Checks if the secondary input is empty.
     */
    public static <INPUT_A, INPUT_B, OUTPUT> void twoInputCalculateOperationsThisTick(OperationTracker tracker, IInputHandler<INPUT_A> inputAHandler,
          Supplier<? extends InputIngredient<INPUT_A>> inputAIngredient, IInputHandler<INPUT_B> inputBHandler,
          Supplier<? extends InputIngredient<INPUT_B>> inputBIngredient, BiConsumer<INPUT_A, INPUT_B> inputsSetter, IOutputHandler<OUTPUT> outputHandler,
          BiFunction<INPUT_A, INPUT_B, OUTPUT> outputGetter, Consumer<OUTPUT> outputSetter, Predicate<INPUT_A> emptyCheckA, Predicate<INPUT_B> emptyCheckB) {
        if (tracker.shouldContinueChecking()) {
            INPUT_A inputA = inputAHandler.getRecipeInput(inputAIngredient.get());
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputA)
            if (emptyCheckA.test(inputA)) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                INPUT_B inputB = inputBHandler.getRecipeInput(inputBIngredient.get());
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputB)
                if (emptyCheckB.test(inputB)) {
                    //No input, we don't know if the recipe matches or not so treat it as not matching
                    tracker.mismatchedRecipe();
                } else {
                    inputsSetter.accept(inputA, inputB);
                    //Calculate the current max based on the primary input
                    inputAHandler.calculateOperationsCanSupport(tracker, inputA);
                    if (tracker.shouldContinueChecking()) {
                        //Calculate the current max based on the secondary input
                        inputBHandler.calculateOperationsCanSupport(tracker, inputB);
                        if (tracker.shouldContinueChecking()) {
                            OUTPUT output = outputGetter.apply(inputA, inputB);
                            outputSetter.accept(output);
                            //Calculate the max based on the space in the output
                            outputHandler.calculateOperationsCanSupport(tracker, output);
                        }
                    }
                }
            }
        }
    }
}