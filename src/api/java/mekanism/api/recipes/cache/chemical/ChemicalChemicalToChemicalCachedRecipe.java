package mekanism.api.recipes.cache.chemical;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Base class to help implement handling of chemical chemical to chemical recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalChemicalToChemicalCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NonNull STACK> outputHandler;
    private final IInputHandler<@NonNull STACK> leftInputHandler;
    private final IInputHandler<@NonNull STACK> rightInputHandler;

    //Note: These shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private STACK leftRecipeInput;
    @Nullable
    private STACK rightRecipeInput;

    /**
     * @param recipe            Recipe.
     * @param leftInputHandler  Left input handler.
     * @param rightInputHandler Right input handler.
     * @param outputHandler     Output handler.
     */
    public ChemicalChemicalToChemicalCachedRecipe(RECIPE recipe, IInputHandler<@NonNull STACK> leftInputHandler, IInputHandler<@NonNull STACK> rightInputHandler,
          IOutputHandler<@NonNull STACK> outputHandler) {
        super(recipe);
        this.leftInputHandler = Objects.requireNonNull(leftInputHandler, "Left input handler cannot be null.");
        this.rightInputHandler = Objects.requireNonNull(rightInputHandler, "Right input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Nullable
    protected Pair<INGREDIENT, INGREDIENT> getIngredients() {
        STACK leftInputChemical = leftInputHandler.getInput();
        if (leftInputChemical.isEmpty()) {
            return null;
        }
        STACK rightInputChemical = rightInputHandler.getInput();
        if (rightInputChemical.isEmpty()) {
            return null;
        }
        INGREDIENT leftInput = recipe.getLeftInput();
        INGREDIENT rightInput = recipe.getRightInput();
        if (!leftInput.test(leftInputChemical) || !rightInput.test(rightInputChemical)) {
            //If one of our inputs is invalid for the side it is on, switch them so that we can check
            // if they are just reversed which side they are on and there is a valid recipe for them
            // if they are on the other side
            return Pair.of(rightInput, leftInput);
        }
        return Pair.of(leftInput, rightInput);
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        Pair<INGREDIENT, INGREDIENT> ingredients = getIngredients();
        if (ingredients == null) {
            //If either inputs are empty then we are unable to operate
            return -1;
        }
        leftRecipeInput = leftInputHandler.getRecipeInput(ingredients.getLeft());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(leftRecipeInput)
        if (leftRecipeInput.isEmpty()) {
            return -1;
        }
        rightRecipeInput = rightInputHandler.getRecipeInput(ingredients.getRight());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(rightRecipeInput)
        if (rightRecipeInput.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the left input
        currentMax = leftInputHandler.operationsCanSupport(leftRecipeInput, currentMax);
        //Calculate the current max based on the right input
        currentMax = rightInputHandler.operationsCanSupport(rightRecipeInput, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(leftRecipeInput, rightRecipeInput), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(leftInputHandler.getInput(), rightInputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        if (leftRecipeInput == null || rightRecipeInput == null || leftRecipeInput.isEmpty() || rightRecipeInput.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        leftInputHandler.use(leftRecipeInput, operations);
        rightInputHandler.use(rightRecipeInput, operations);
        outputHandler.handleOutput(recipe.getOutput(leftRecipeInput, rightRecipeInput), operations);
    }
}