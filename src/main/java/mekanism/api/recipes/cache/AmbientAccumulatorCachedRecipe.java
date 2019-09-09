package mekanism.api.recipes.cache;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.recipes.AmbientAccumulatorRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class AmbientAccumulatorCachedRecipe extends CachedRecipe<AmbientAccumulatorRecipe> {

    public AmbientAccumulatorCachedRecipe(AmbientAccumulatorRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, recipe::getTicksRequired, setActive, useEnergy, onFinish);

    }

    @Override
    public boolean hasResourcesForTick() {
        //TODO: Implement
        return false;
    }

    @Override
    public boolean hasRoomForOutput() {
        //TODO: implement
        return false;
    }

    @Override
    protected void useResources() {
        super.useResources();
        //TODO: Use any secondary resources or remove this override
    }

    @Override
    protected void finishProcessing() {
        //TODO: add the output to the output slot
    }
}