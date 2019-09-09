package mekanism.api.recipes.cache;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class SawmillCachedRecipe extends CachedRecipe<SawmillRecipe> {

    public SawmillCachedRecipe(SawmillRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);

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