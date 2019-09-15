package mekanism.api.recipes.cache;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.function.BooleanConsumer;
import mekanism.api.function.IntToIntFunction;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class GasToGasCachedRecipe extends CachedRecipe<GasToGasRecipe> {

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final Supplier<@NonNull GasTank> inputTank;
    //TODO: Rename, and shift up to the CachedRecipe class level once we move things to having setters for "optional" params
    private final IntToIntFunction operationCalculator;

    public GasToGasCachedRecipe(GasToGasRecipe recipe, BooleanSupplier canTileFunction, BooleanConsumer setActive, Runnable onFinish,
          Supplier<@NonNull GasTank> inputTank, IntToIntFunction operationCalculator, IOutputHandler<@NonNull GasStack> outputHandler) {
        this(recipe, canTileFunction, () -> 0, () -> 0, () -> 1, setActive, energy -> {}, onFinish, inputTank, operationCalculator, outputHandler);
    }

    public GasToGasCachedRecipe(GasToGasRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, BooleanConsumer setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull GasTank> inputTank,
          IntToIntFunction operationCalculator, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputTank = inputTank;
        this.operationCalculator = operationCalculator;
        this.outputHandler = outputHandler;
    }

    @Nonnull
    private GasTank getGasTank() {
        return inputTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            return 0;
        }
        GasStack recipeInput = recipe.getInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeInput == null || recipeInput.amount == 0) {
            //TODO: 1.14 make this check about being empty instead
            return 0;
        }
        //Calculate the current max based on how much input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputGas.amount / recipeInput.amount, currentMax);
        //Calculate the max based on the space in the output
        currentMax = outputHandler.operationsRoomFor(recipe.getOutput(recipeInput), currentMax);

        //Do any extra processing for the max amount
        return operationCalculator.apply(currentMax);
    }

    @Override
    public boolean hasResourcesForTick() {
        GasStack gasInput = getGasTank().getGas();
        return gasInput != null && recipe.test(gasInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        GasStack inputGas = getGasTank().getGas();
        if (inputGas == null || inputGas.amount == 0) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        GasStack recipeInput = recipe.getInput().getMatchingInstance(inputGas);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeInput == null || recipeInput.amount == 0) {
            //TODO: 1.14 make this check about being empty instead
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        outputHandler.handleOutput(recipe.getOutput(recipeInput), operations);
    }
}