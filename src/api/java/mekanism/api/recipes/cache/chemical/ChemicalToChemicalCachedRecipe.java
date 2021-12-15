package mekanism.api.recipes.cache.chemical;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;

/**
 * Base class to help implement handling of chemical to chemical recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalToChemicalCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NonNull STACK> outputHandler;
    private final IInputHandler<@NonNull STACK> inputHandler;

    @Nullable//Note: Shouldn't be null in places it is actually used, but we mark it as nullable, so we don't have to initialize it
    private STACK recipeInput;

    /**
     * @param recipe        Recipe.
     * @param inputHandler  Input handler.
     * @param outputHandler Output handler.
     */
    public ChemicalToChemicalCachedRecipe(RECIPE recipe, IInputHandler<@NonNull STACK> inputHandler, IOutputHandler<@NonNull STACK> outputHandler) {
        super(recipe);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        recipeInput = inputHandler.getRecipeInput(recipe.getInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeInput.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the input
        currentMax = inputHandler.operationsCanSupport(recipeInput, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeInput), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(inputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeInput == null || recipeInput.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        inputHandler.use(recipeInput, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeInput), operations);
    }
}