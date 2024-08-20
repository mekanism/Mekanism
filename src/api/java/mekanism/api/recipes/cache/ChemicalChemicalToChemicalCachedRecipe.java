package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Base class to help implement handling of chemical chemical to chemical recipes.
 */
@NothingNullByDefault
public class ChemicalChemicalToChemicalCachedRecipe<RECIPE extends ChemicalChemicalToChemicalRecipe> extends CachedRecipe<RECIPE> {

    private final IOutputHandler<ChemicalStack> outputHandler;
    private final IInputHandler<ChemicalStack> leftInputHandler;
    private final IInputHandler<ChemicalStack> rightInputHandler;
    private final BiConsumer<ChemicalStack, ChemicalStack> inputsSetter;
    private final Consumer<ChemicalStack> outputSetter;
    private final Supplier<ChemicalStackIngredient> leftInput;
    private final Supplier<ChemicalStackIngredient> rightInput;
    private final BinaryOperator<ChemicalStack> outputGetter;

    //Note: These shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private ChemicalStack leftRecipeInput;
    @Nullable
    private ChemicalStack rightRecipeInput;
    @Nullable
    private ChemicalStack output;

    /**
     * @param recipe            Recipe.
     * @param recheckAllErrors  Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                          do this every tick or if there is no one viewing recipes.
     * @param leftInputHandler  Left input handler.
     * @param rightInputHandler Right input handler.
     * @param outputHandler     Output handler.
     */
    public ChemicalChemicalToChemicalCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<ChemicalStack> leftInputHandler,
          IInputHandler<ChemicalStack> rightInputHandler, IOutputHandler<ChemicalStack> outputHandler) {
        super(recipe, recheckAllErrors);
        this.leftInputHandler = Objects.requireNonNull(leftInputHandler, "Left input handler cannot be null.");
        this.rightInputHandler = Objects.requireNonNull(rightInputHandler, "Right input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.leftInput = this.recipe::getLeftInput;
        this.rightInput = this.recipe::getRightInput;
        this.inputsSetter = (left, right) -> {
            leftRecipeInput = left;
            rightRecipeInput = right;
        };
        this.outputSetter = output -> this.output = output;
        this.outputGetter = this.recipe::getOutput;
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            ChemicalStack leftInputChemical = leftInputHandler.getInput();
            if (leftInputChemical.isEmpty()) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                ChemicalStack rightInputChemical = rightInputHandler.getInput();
                if (rightInputChemical.isEmpty()) {
                    //No input, we don't know if the recipe matches or not so treat it as not matching
                    tracker.mismatchedRecipe();
                } else {
                    Supplier<ChemicalStackIngredient> leftIngredient;
                    Supplier<ChemicalStackIngredient> rightIngredient;
                    if (!recipe.getLeftInput().test(leftInputChemical) || !recipe.getRightInput().test(rightInputChemical)) {
                        //If one of our inputs is invalid for the side it is on, switch them so that we can check
                        // if they are just reversed which side they are on and there is a valid recipe for them
                        // if they are on the other side
                        leftIngredient = rightInput;
                        rightIngredient = leftInput;
                    } else {
                        leftIngredient = leftInput;
                        rightIngredient = rightInput;
                    }
                    CachedRecipeHelper.twoInputCalculateOperationsThisTick(tracker, leftInputHandler, leftIngredient, rightInputHandler, rightIngredient, inputsSetter,
                          outputHandler, outputGetter, outputSetter, ConstantPredicates.CHEMICAL_EMPTY, ConstantPredicates.CHEMICAL_EMPTY);
                }
            }
        }
    }

    @Override
    public boolean isInputValid() {
        ChemicalStack leftInput = leftInputHandler.getInput();
        if (leftInput.isEmpty()) {
            return false;
        }
        ChemicalStack rightInput = rightInputHandler.getInput();
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