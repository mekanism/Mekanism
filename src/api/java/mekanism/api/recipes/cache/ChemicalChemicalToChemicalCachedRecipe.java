package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class to help implement handling of chemical chemical to chemical recipes.
 */
@NothingNullByDefault
public class ChemicalChemicalToChemicalCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NotNull STACK> outputHandler;
    private final IInputHandler<@NotNull STACK> leftInputHandler;
    private final IInputHandler<@NotNull STACK> rightInputHandler;

    //Note: These shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private STACK leftRecipeInput;
    @Nullable
    private STACK rightRecipeInput;
    @Nullable
    private STACK output;

    /**
     * @param recipe            Recipe.
     * @param recheckAllErrors  Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                          do this every tick or if there is no one viewing recipes.
     * @param leftInputHandler  Left input handler.
     * @param rightInputHandler Right input handler.
     * @param outputHandler     Output handler.
     */
    public ChemicalChemicalToChemicalCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull STACK> leftInputHandler,
          IInputHandler<@NotNull STACK> rightInputHandler, IOutputHandler<@NotNull STACK> outputHandler) {
        super(recipe, recheckAllErrors);
        this.leftInputHandler = Objects.requireNonNull(leftInputHandler, "Left input handler cannot be null.");
        this.rightInputHandler = Objects.requireNonNull(rightInputHandler, "Right input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            STACK leftInputChemical = leftInputHandler.getInput();
            if (leftInputChemical.isEmpty()) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                STACK rightInputChemical = rightInputHandler.getInput();
                if (rightInputChemical.isEmpty()) {
                    //No input, we don't know if the recipe matches or not so treat it as not matching
                    tracker.mismatchedRecipe();
                } else {
                    Supplier<INGREDIENT> leftIngredient;
                    Supplier<INGREDIENT> rightIngredient;
                    INGREDIENT leftInput = recipe.getLeftInput();
                    INGREDIENT rightInput = recipe.getRightInput();
                    if (!leftInput.test(leftInputChemical) || !rightInput.test(rightInputChemical)) {
                        //If one of our inputs is invalid for the side it is on, switch them so that we can check
                        // if they are just reversed which side they are on and there is a valid recipe for them
                        // if they are on the other side
                        leftIngredient = () -> rightInput;
                        rightIngredient = () -> leftInput;
                    } else {
                        leftIngredient = () -> leftInput;
                        rightIngredient = () -> rightInput;
                    }
                    CachedRecipeHelper.twoInputCalculateOperationsThisTick(tracker, leftInputHandler, leftIngredient, rightInputHandler, rightIngredient, (left, right) -> {
                        leftRecipeInput = left;
                        rightRecipeInput = right;
                    }, outputHandler, recipe::getOutput, output -> this.output = output, ChemicalStack::isEmpty, ChemicalStack::isEmpty);
                }
            }
        }
    }

    @Override
    public boolean isInputValid() {
        STACK leftInput = leftInputHandler.getInput();
        if (leftInput.isEmpty()) {
            return false;
        }
        STACK rightInput = rightInputHandler.getInput();
        return !rightInput.isEmpty() && recipe.test(leftInput, rightInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (leftRecipeInput != null && rightRecipeInput != null && output != null && !leftRecipeInput.isEmpty() && !rightRecipeInput.isEmpty() && !output.isEmpty()) {
            leftInputHandler.use(leftRecipeInput, operations);
            rightInputHandler.use(rightRecipeInput, operations);
            outputHandler.handleOutput(output, operations);
        }
    }
}