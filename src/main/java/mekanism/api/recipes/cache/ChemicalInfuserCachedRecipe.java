package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalInfuserCachedRecipe extends CachedRecipe<ChemicalInfuserRecipe> {

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final Supplier<@NonNull GasTank> leftTank;
    private final Supplier<@NonNull GasTank> rightTank;

    public ChemicalInfuserCachedRecipe(ChemicalInfuserRecipe recipe, Supplier<@NonNull GasTank> leftTank, Supplier<@NonNull GasTank> rightTank,
          IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe);
        this.leftTank = leftTank;
        this.rightTank = rightTank;
        this.outputHandler = outputHandler;
    }

    @Nonnull
    private GasTank getLeftTank() {
        return leftTank.get();
    }

    @Nonnull
    private GasTank getRightTank() {
        return rightTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        GasStack leftInputGas = getLeftTank().getGas();
        if (leftInputGas == null || leftInputGas.amount == 0) {
            return 0;
        }
        GasStack rightInputGas = getRightTank().getGas();
        if (rightInputGas == null || rightInputGas.amount == 0) {
            return 0;
        }

        GasStackIngredient leftInput = recipe.getLeftInput();
        GasStackIngredient rightInput = recipe.getRightInput();
        if (!leftInput.test(leftInputGas) || !rightInput.test(rightInputGas)) {
            //If one of our inputs is invalid for the side it is on, switch them so that we can check
            // if they are just reversed which side they are on and there is a valid recipe for them
            // if they are on the other side
            GasStack temp = leftInputGas;
            leftInputGas = rightInputGas;
            rightInputGas = temp;
        }

        GasStack leftRecipeInput = leftInput.getMatchingInstance(leftInputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(leftInputGas)
        if (leftRecipeInput == null || leftRecipeInput.amount == 0) {
            return 0;
        }

        GasStack rightRecipeInput = rightInput.getMatchingInstance(rightInputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(rightInputGas)
        if (rightRecipeInput == null || rightRecipeInput.amount == 0) {
            return 0;
        }

        //Calculate the current max based on how much input we have in the left tank to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(rightInputGas.amount / leftRecipeInput.amount, currentMax);

        //Calculate the current max based on how much input we have in the right tank to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(leftInputGas.amount / rightRecipeInput.amount, currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(leftRecipeInput, rightRecipeInput), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasStack leftInput = getLeftTank().getGas();
        GasStack rightInput = getRightTank().getGas();
        return leftInput != null && rightInput != null && recipe.test(leftInput, rightInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        GasStack leftInputGas = getLeftTank().getGas();
        if (leftInputGas == null || leftInputGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        GasStack rightInputGas = getRightTank().getGas();
        if (rightInputGas == null || rightInputGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        GasStackIngredient leftInput = recipe.getLeftInput();
        GasStackIngredient rightInput = recipe.getRightInput();
        if (!leftInput.test(leftInputGas) || !rightInput.test(rightInputGas)) {
            //If one of our inputs is invalid for the side it is on, switch them so that we can check
            // if they are just reversed which side they are on and there is a valid recipe for them
            // if they are on the other side
            //Note: At this point, given we are processing, if we get into this if statement THEN
            // we "know" they are in the opposite slots.
            //TODO: Caching this stuff will make the finishProcessing more efficient
            GasStack temp = leftInputGas;
            leftInputGas = rightInputGas;
            rightInputGas = temp;
        }

        GasStack leftRecipeInput = leftInput.getMatchingInstance(leftInputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(leftInputGas)
        if (leftRecipeInput == null || leftRecipeInput.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        GasStack rightRecipeInput = rightInput.getMatchingInstance(rightInputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(rightInputGas)
        if (rightRecipeInput == null || rightRecipeInput.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        getLeftTank().draw(leftRecipeInput.amount * operations, true);
        getRightTank().draw(rightRecipeInput.amount * operations, true);
        outputHandler.handleOutput(recipe.getOutput(leftRecipeInput, rightRecipeInput), operations);
    }
}