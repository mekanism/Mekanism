package mekanism.api.recipes.cache;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
//TODO: JavaDocs
public abstract class CachedRecipe<RECIPE extends IMekanismRecipe> {

    private final BooleanSupplier canTileFunction;
    private final DoubleSupplier perTickEnergy;
    private final DoubleSupplier storedEnergy;
    private final Consumer<Boolean> setActive;
    private final DoubleConsumer useEnergy;
    private final IntSupplier requiredTicks;
    private final Runnable onFinish;
    protected final RECIPE recipe;

    /**
     * Ticks the machine has spent processing so far
     */
    private int operatingTicks;

    //TODO: If this gets converted almost to more of a builder pattern with some reasonable defaults that may be useful
    // Especially when it comes to some of the recipes having "optional" but currently unused in our machines params
    protected CachedRecipe(RECIPE recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy, IntSupplier requiredTicks,
          Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish) {
        this.recipe = recipe;
        this.canTileFunction = canTileFunction;
        this.requiredTicks = requiredTicks;
        this.perTickEnergy = perTickEnergy;
        this.storedEnergy = storedEnergy;
        this.setActive = setActive;
        this.useEnergy = useEnergy;
        this.onFinish = onFinish;
    }


    public void process() {
        boolean hasResourcesForTick = hasResourcesForTick();
        if (hasResourcesForTick && canTileFunction() && getStoredElectricity() >= getEnergyPerTick() && hasRoomForOutput()) {
            setActive.accept(true);
            useResources();
            operatingTicks++;
            if (operatingTicks >= getTicksRequired()) {
                operatingTicks = 0;
                finishProcessing();
                onFinish.run();
            }
        } else {
            //TODO: Check performance, previously this only would set it to inactive if the energy the machine had last tick is less than
            // the energy we have now. Due to the performance improvements that were made to handling the active states, I believe that
            // using the more accurate "disabling" of machines makes more sense
            setActive.accept(false);
        }
        //TODO: Should this be moved into the else branch
        if (!hasResourcesForTick) {
            //Note: We don't have to recalculate hasResourcesForTick after we finish processing
            // as operating ticks will be set to zero in that case anyways
            operatingTicks = 0;
        }
    }

    private boolean canTileFunction() {
        return canTileFunction.getAsBoolean();
    }

    protected double getStoredElectricity() {
        return storedEnergy.getAsDouble();
    }

    protected double getEnergyPerTick() {
        return perTickEnergy.getAsDouble();
    }

    private int getTicksRequired() {
        return requiredTicks.getAsInt();
    }

    //TODO: JavaDoc to mention that super should be called to ensure that the per tick energy is used
    protected void useResources() {
        useEnergy.accept(getEnergyPerTick());
    }

    public abstract boolean hasResourcesForTick();

    public abstract boolean hasRoomForOutput();

    //TODO: None of the implementations are currently actually removing the inputs from the machines here, and are only adding the output
    protected abstract void finishProcessing();

    public RECIPE getRecipe() {
        return recipe;
    }
}