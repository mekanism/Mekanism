package mekanism.api.recipes.cache;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongConsumer;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.recipes.MekanismRecipe;

/**
 * Base class to help implement handling of Mekanism recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CachedRecipe<RECIPE extends MekanismRecipe> {

    /**
     * Internal recipe object this cached recipe acts on.
     */
    protected final RECIPE recipe;

    /**
     * Used to check if the {@link CachedRecipe} holder can function (Defaults to returning true)
     */
    private BooleanSupplier canHolderFunction = () -> true;
    /**
     * Called to set the active state of the holder (Defaults to doing nothing)
     */
    private BooleanConsumer setActive = active -> {
    };

    /**
     * Gets the number of ticks required to complete the recipe (Default to finishing after a single tick, or in other words the recipe finishing every tick)
     */
    private IntSupplier requiredTicks = () -> 1;
    /**
     * Allows for running any extra methods that need to be done when the recipe finishes processing. For example marking the tile dirty. (Defaults to doing nothing)
     */
    private Runnable onFinish = () -> {
    };

    /**
     * Gets the per tick energy consumption of this recipe. (Defaults to not requiring any energy)
     */
    private FloatingLongSupplier perTickEnergy = () -> FloatingLong.ZERO;
    /**
     * Gets the energy currently stored in the machine/object executing this {@link CachedRecipe}. (Defaults to returning no energy stored)
     */
    private FloatingLongSupplier storedEnergy = () -> FloatingLong.ZERO;
    /**
     * Called to consume energy. (Defaults to not doing anything)
     */
    private FloatingLongConsumer useEnergy = energy -> {
    };

    /**
     * Function to allow for post-processing {@link #getOperationsThisTick(int)}. (Defaults to capping the number of operations to one operation per tick)
     */
    private IntUnaryOperator postProcessOperations = currentMax -> Math.min(1, currentMax);

    /**
     * Ticks the machine has spent processing so far.
     */
    private int operatingTicks;
    /**
     * Allows for cached recipe holders to have handling for when the operating ticks changed, for example to allow the machine to know that the number of operating ticks
     * has changed and sync the progress in containers to clients. (Defaults to not doing anything)
     */
    private IntConsumer operatingTicksChanged = ticks -> {
    };

    /**
     * @param recipe Recipe.
     */
    protected CachedRecipe(RECIPE recipe) {
        this.recipe = Objects.requireNonNull(recipe, "Recipe cannot be null.");
    }

    /**
     * Sets the callback that is used to check if the {@link CachedRecipe} holder is currently able to function. If the holder is not able to function, it will be set to
     * inactive but the current recipe progress will not be reset. This method is used for things like respecting redstone control modes.
     *
     * @param canHolderFunction Returns {@code true} If the holder can function, or {@code false} if it can't.
     *
     * @apiNote If this method is not used, {@code canHolderFunction} for this {@link CachedRecipe} defaults to returning true.
     */
    public CachedRecipe<RECIPE> setCanHolderFunction(BooleanSupplier canHolderFunction) {
        this.canHolderFunction = Objects.requireNonNull(canHolderFunction, "Can holder function cannot be null.");
        return this;
    }

    /**
     * Sets the callback that is used to change the active state of the {@link CachedRecipe} holder.
     *
     * @param setActive Consumer that handles changing the active state of the holder.
     *
     * @apiNote If this method is not used, the {@code setActive} consumer of this {@link CachedRecipe} defaults to doing nothing.
     */
    public CachedRecipe<RECIPE> setActive(BooleanConsumer setActive) {
        this.setActive = Objects.requireNonNull(setActive, "Set active consumer cannot be null.");
        return this;
    }

    /**
     * Sets the various energy requirements of this {@link CachedRecipe} and allows for gathering enough information to allow for calculating and using the stored
     * energy.
     *
     * @param perTickEnergy   Per tick energy consumption required to process the recipe.
     * @param energyContainer Energy container that will be used for looking up the current stored energy and have energy extracted from it.
     *
     * @apiNote If this method is not used, this {@link CachedRecipe} defaults to not requiring or using any energy.
     */
    public CachedRecipe<RECIPE> setEnergyRequirements(FloatingLongSupplier perTickEnergy, IEnergyContainer energyContainer) {
        //TODO - 1.18: Re-evaluate if we want to change this to a system similar to the InputHandler,
        // so that we can simulate extracting energy from our container
        this.perTickEnergy = Objects.requireNonNull(perTickEnergy, "The per tick energy cannot be null.");
        Objects.requireNonNull(energyContainer, "Energy container cannot be null.");
        this.storedEnergy = energyContainer::getEnergy;
        this.useEnergy = energy -> energyContainer.extract(energy, Action.EXECUTE, AutomationType.INTERNAL);
        return this;
    }

    /**
     * Sets the supplier that supplies the number of ticks required to complete the recipe.
     *
     * @param requiredTicks Supplies the number of ticks this recipe requires to complete.
     *
     * @apiNote {@code requiredTicks} should return a value of at least one, though any lower values will be treated as if the recipe takes a single tick to complete.
     * <br>
     * If this method is not used, the {@code requiredTicks} of this {@link CachedRecipe} defaults to returning one.
     */
    public CachedRecipe<RECIPE> setRequiredTicks(IntSupplier requiredTicks) {
        this.requiredTicks = Objects.requireNonNull(requiredTicks, "Required ticks cannot be null.");
        return this;
    }

    /**
     * Sets the callback used for handling and keeping track of when the number of operating ticks for this {@link CachedRecipe} changes. This can be used to allow the
     * {@link CachedRecipe} holder to persist the recipe's progress between sessions and to ensure the proper value gets synced to the client in GUIs.
     *
     * @param operatingTicksChanged Called when the number of operating ticks changes with the new operating tick number.
     *
     * @apiNote If this method is not used, the {@code operatingTicksChanged} consumer of this {@link CachedRecipe} defaults to doing nothing.
     */
    public CachedRecipe<RECIPE> setOperatingTicksChanged(IntConsumer operatingTicksChanged) {
        this.operatingTicksChanged = Objects.requireNonNull(operatingTicksChanged, "Operating ticks changed handler cannot be null.");
        return this;
    }

    /**
     * Sets the callback that is ran when this {@link CachedRecipe} completes processing of a recipe. This allows the {@link CachedRecipe} holder to do any extra handling
     * it needs to do such as marking the tile dirty and in need of saving.
     *
     * @param onFinish Runnable to execute when this {@link CachedRecipe} completes processing of a recipe.
     *
     * @apiNote If this method is not used, the {@code onFinish} handler of this {@link CachedRecipe} defaults to doing nothing.
     */
    public CachedRecipe<RECIPE> setOnFinish(Runnable onFinish) {
        this.onFinish = Objects.requireNonNull(onFinish, "On finish handling cannot be null.");
        return this;
    }

    /**
     * Sets the callback used to post process the number of operations this {@link CachedRecipe} calculated it can perform in {@link #getOperationsThisTick(int)}. The
     * function {@code postProcessOperations} should not return a value higher than it is passed in or various issues may occur.
     *
     * @param postProcessOperations Function that applies post-processing to the result of {@link #getOperationsThisTick(int)}.
     *
     * @apiNote If this method is not used, {@code postProcessOperations} for this {@link CachedRecipe} defaults to capping the number of operations at one.
     */
    public CachedRecipe<RECIPE> setPostProcessOperations(IntUnaryOperator postProcessOperations) {
        this.postProcessOperations = Objects.requireNonNull(postProcessOperations, "Post processing of the operation count cannot be null.");
        return this;
    }

    /**
     * Sets the number of operating ticks that have passed so far. This is used to allow {@link CachedRecipe} holders to persist and load recipe progress.
     *
     * @param operatingTicks Number of operating ticks that have passed.
     */
    public void loadSavedOperatingTicks(int operatingTicks) {
        if (operatingTicks > 0 && operatingTicks < requiredTicks.getAsInt()) {
            this.operatingTicks = operatingTicks;
        }
    }

    /**
     * Called by the holder of this {@link CachedRecipe} to attempt to process/handle the internal recipe.
     */
    public void process() {
        //TODO: Evaluate adding in some marker that gets set to true here that then denies the various callbacks/builders from being used
        int operations;
        if (canHolderFunction.getAsBoolean()) {
            setupVariableValues();
            //TODO: Given we are going to probably have ALL recipes check the getOperationsThisTick(), we are going to
            // want some way to check things so that by default it doesn't do the max operations and instead does a single
            // run for the majority of recipes
            //TODO: Should this be passing Integer.MAX_VALUE or get the value from somewhere else. Some sort of thing the tile passes as a supplier
            operations = postProcessOperations.applyAsInt(getOperationsThisTick(Integer.MAX_VALUE));
        } else {
            operations = 0;
        }
        if (operations > 0) {
            setActive.accept(true);
            //Always use energy, as that is a constant thing we can check
            useEnergy(operations);
            operatingTicks++;
            int ticksRequired = requiredTicks.getAsInt();
            if (operatingTicks >= ticksRequired) {
                operatingTicks = 0;
                finishProcessing(operations);
                onFinish.run();
                resetCache();
            } else {
                //If we still have ticks left required to operate, use the contents
                useResources(operations);
            }
            if (ticksRequired > 1) {
                //If no ticks are required don't bother marking it as changed as it resets itself back to the same value
                operatingTicksChanged.accept(operatingTicks);
            }
        } else {
            setActive.accept(false);
            if (operations < 0) {
                //Reset the progress
                operatingTicks = 0;
                operatingTicksChanged.accept(operatingTicks);
                resetCache();
            }
        }
    }

    /**
     * Called to allow recipes to set up any variables they will need this tick to allow them to reduce the number of places they have to recalculate/retrieve a passed in
     * value.
     */
    protected void setupVariableValues() {
    }

    /**
     * @return Gets the current number of operating ticks that have happened so far.
     */
    //TODO - 1.18: Make this protected
    public int getOperatingTicks() {
        return operatingTicks;
    }

    @Deprecated//TODO - 1.18: Remove this helper
    protected FloatingLong getStoredElectricity() {
        return storedEnergy.get();
    }

    @Deprecated//TODO - 1.18: Remove this helper
    protected FloatingLong getEnergyPerTick() {
        return perTickEnergy.get();
    }

    @Deprecated//TODO - 1.18: Remove this helper
    protected int getTicksRequired() {
        return requiredTicks.getAsInt();
    }

    /**
     * Called each tick to allow for {@link CachedRecipe}s to consume any per tick resources.
     *
     * @param operations Number of operations being performed.
     *
     * @implNote It is safe to assume that {@link #getOperationsThisTick(int)} will have been called before this method and there will be at least one operation. This
     * means that caching of types can be done inside of {@link #getOperationsThisTick(int)} and safely used here.
     */
    protected void useResources(int operations) {
    }

    /**
     * Called when the recipe finishes processing or gets reset so that any values the implementation may be holding onto can be properly reset.
     */
    protected void resetCache() {
    }

    /**
     * Called each tick to consume energy for a given number of operations.
     *
     * @param operations Number of operations being performed.
     */
    protected void useEnergy(int operations) {
        useEnergy.accept(perTickEnergy.get().multiply(operations));
    }

    /**
     * Gets the number of operations this {@link CachedRecipe} can perform this tick based on the various inputs available to the recipe and space left in the outputs.
     *
     * @param currentMax The current maximum number of operations.
     *
     * @return The number of operations that can be performed. A value of zero means that no operations will take place, while a value less than zero means that the
     * current recipe progress will be reset.
     */
    protected int getOperationsThisTick(int currentMax) {
        //TODO - WARNING SYSTEM: Probably use this to hookup some handling related to warnings, and make use of records for returning multiple types
        //TODO: Try to deduplicate the code in the implementations as there is a good bit of duplication for calculating the max
        // of the different types that recipe uses
        if (currentMax <= 0) {
            //Short circuit that if we already can't perform any outputs, just return
            return currentMax;
        }
        FloatingLong energyPerTick = perTickEnergy.get();
        if (energyPerTick.isZero()) {
            //If we don't have an energy requirement return what we were told the max is
            return currentMax;
        }
        //Make sure we don't have any integer overflow in calculating how much we have room for
        return Math.min(storedEnergy.get().divideToInt(energyPerTick), currentMax);
    }

    @Deprecated//TODO - 1.18: Remove this
    public boolean canFunction() {
        return canHolderFunction.getAsBoolean() && postProcessOperations.applyAsInt(getOperationsThisTick(1)) > 0;
    }

    /**
     * Checks if the resources in the inputs is valid for this {@link CachedRecipe}.
     *
     * @return {@code true} if the resources are valid.
     */
    public abstract boolean isInputValid();

    /**
     * Called when a recipe finishes processing. This method consumes any recipe inputs and produces the recipe outputs.
     *
     * @param operations Number of operations being performed.
     *
     * @implNote It is safe to assume that {@link #getOperationsThisTick(int)} will have been called before this method and there will be at least one operation. This
     * means that caching of types can be done inside of {@link #getOperationsThisTick(int)} and safely used here.
     */
    protected abstract void finishProcessing(int operations);

    /**
     * Gets the actual recipe object this {@link CachedRecipe} has cached and operates on.
     */
    public RECIPE getRecipe() {
        return recipe;
    }
}