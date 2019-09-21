package mekanism.api.recipes.cache;

import java.util.Random;
import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.AmbientAccumulatorRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.annotations.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
//TODO: Decide if this should be removed, or if I should restore all tiles and things for the ambient accumulator
public class AmbientAccumulatorCachedRecipe extends CachedRecipe<AmbientAccumulatorRecipe> {

    private static final Random gasRand = new Random();

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    //TODO: 1.14 DimensionType instead of id
    //This can be a supplier in case someone implementing this ends up having their collector be able to go from one dimension to another
    private final IntSupplier currentDimension;

    public AmbientAccumulatorCachedRecipe(AmbientAccumulatorRecipe recipe, IntSupplier currentDimension, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe);
        this.currentDimension = currentDimension;
        this.outputHandler = outputHandler;
        setRequiredTicks(recipe::getTicksRequired);
    }

    private int getDimension() {
        return currentDimension.getAsInt();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        if (getDimension() != recipe.getDimension()) {
            return 0;
        }
        return outputHandler.operationsRoomFor(recipe.getOutput(), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return getDimension() == recipe.getDimension();
    }

    @Override
    protected void finishProcessing(int operations) {
        if (operations > 0 && gasRand.nextDouble() < 0.05) {
            outputHandler.handleOutput(recipe.getOutput(), operations);
        }
    }
}