package mekanism.api.recipes.cache;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.function.BooleanConsumer;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
//TODO: JavaDocs
public abstract class CachedRecipe<RECIPE extends IMekanismRecipe> {

    private final BooleanSupplier canTileFunction;
    private final DoubleSupplier perTickEnergy;
    private final DoubleSupplier storedEnergy;
    private final BooleanConsumer setActive;
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
          BooleanConsumer setActive, DoubleConsumer useEnergy, Runnable onFinish) {
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
        //TODO: Given we are going to probably have ALL recipes check the getOperationsThisTick(), we are going to
        // want some way to check things so that by default it doesn't do the max operations and instead does a single
        // run for the majority of recipes
        //TODO: Should this be passing Integer.MAX_VALUE or get the value from somewhere else. Some sort of thing the tile passes as a supplier
        int operations = canTileFunction() ? getOperationsThisTick(Integer.MAX_VALUE) : 0;
        if (operations > 0) {
            setActive.accept(true);
            useResources(operations);
            operatingTicks++;
            if (operatingTicks >= getTicksRequired()) {
                operatingTicks = 0;
                finishProcessing(operations);
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
    protected void useResources(int operations) {
        //TODO: Multiple energy used by operations
        useEnergy.accept(getEnergyPerTick());
    }

    //TODO: Is there a better name for this, basically is how many times this can function this tick
    protected int getOperationsThisTick(int currentMax) {
        if (currentMax == 0) {
            //Short circuit that if we already can't perform any outputs, just return
            return 0;
        }
        double energyPerTick = getEnergyPerTick();
        if (energyPerTick == 0) {
            //If we don't have an energy requirement return what we were told the max is
            return currentMax;
        }
        double operations = getStoredElectricity() / energyPerTick;
        //Make sure we don't have any integer overflow in calculating how much we have room for
        return Math.min(operations > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) operations, currentMax);
    }

    public boolean canFunction() {
        //TODO: Decide if we should be passing 1 as the current max or Integer.MAX_VALUE
        // Currently is passing 1, as if anything has something that is based off current operations
        // and short circuits because of it then going to a fractional amount
        return canTileFunction() && getOperationsThisTick(1) > 0;
    }

    //TODO: Find some alternative for checking input validity for the cached recipe refresh
    // as we ONLY care about the inputs in that case
    public abstract boolean hasResourcesForTick();

    //TODO: None of the implementations are currently actually removing the inputs from the machines here, and are only adding the output
    protected abstract void finishProcessing(int operations);

    public RECIPE getRecipe() {
        return recipe;
    }
}