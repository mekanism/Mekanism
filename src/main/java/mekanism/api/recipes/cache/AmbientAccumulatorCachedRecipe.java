package mekanism.api.recipes.cache;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.AmbientAccumulatorRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class AmbientAccumulatorCachedRecipe extends CachedRecipe<AmbientAccumulatorRecipe> {

    private static final Random gasRand = new Random();

    private final BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput;
    //TODO: 1.14 DimensionType instead of id
    //This can be a supplier in case someone implementing this ends up having their collector be able to go from one dimension to another
    private final IntSupplier currentDimension;

    public AmbientAccumulatorCachedRecipe(AmbientAccumulatorRecipe recipe, BooleanSupplier canTileFunction, Runnable onFinish,
          IntSupplier currentDimension, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, () -> 0, () -> 0, recipe::getTicksRequired, active -> {
        }, energy -> {
        }, onFinish);
        this.currentDimension = currentDimension;
        this.addToOutput = addToOutput;
    }

    private int getDimension() {
        return currentDimension.getAsInt();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
        return 1;
    }

    @Override
    public boolean hasResourcesForTick() {
        return getDimension() == recipe.getDimension();
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(), true);
    }

    @Override
    protected void finishProcessing(int operations) {
        if (gasRand.nextDouble() < 0.05) {
            addToOutput.apply(recipe.getOutput(), false);
        }
    }
}