package mekanism.api.recipes.cache;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;

/**
 * Base class to help implement handling of Mekanism recipes.
 */
@NothingNullByDefault
public abstract class CachedRecipe<RECIPE extends MekanismRecipe<?>> {

    /**
     * Internal recipe object this cached recipe acts on.
     */
    protected final RECIPE recipe;
    /**
     * Set of all the errors from when this {@link CachedRecipe} last calculated all the errors.
     */
    private Set<RecipeError> errors = Collections.emptySet();
    /**
     * Tracks whether recipe processing is currently paused due to errors.
     *
     * @since 10.5.15
     */
    private boolean pausedForErrors = false;
    /**
     * Used to check if the {@link CachedRecipe} should recheck for all errors.
     */
    private final BooleanSupplier recheckAllErrors;

    /**
     * Used to check if the {@link CachedRecipe} holder can function.
     *
     * @implNote Defaults to returning {@code true}.
     */
    private BooleanSupplier canHolderFunction = ConstantPredicates.ALWAYS_TRUE;
    /**
     * Called to set the active state of the holder.
     *
     * @implNote Defaults to doing nothing.
     */
    private BooleanConsumer setActive = active -> {
    };

    /**
     * Gets the number of ticks required to complete the recipe.
     *
     * @implNote Defaults to finishing after a single tick, or in other words the recipe finishing every tick.
     */
    private IntSupplier requiredTicks = () -> 1;
    /**
     * Allows for running any extra methods that need to be done when the recipe finishes processing. For example marking the tile dirty.
     *
     * @implNote Defaults to doing nothing.
     */
    private Runnable onFinish = () -> {
    };

    /**
     * Gets the per tick energy consumption of this recipe.
     *
     * @implNote Defaults to not requiring any energy.
     */
    private LongSupplier perTickEnergy = ConstantPredicates.ZERO_LONG;
    /**
     * Gets the energy currently stored in the machine/object executing this {@link CachedRecipe}.
     *
     * @implNote Defaults to returning no energy stored.
     */
    private LongSupplier storedEnergy = ConstantPredicates.ZERO_LONG;
    /**
     * Called to consume energy.
     *
     * @implNote Defaults to doing nothing.
     */
    private LongConsumer useEnergy = energy -> {
    };

    /**
     * Gets the baseline maximum number of operations that can be performed if everything is working properly. The returned value should be at least one.
     *
     * @implNote Defaults to only performing one "operation" each tick.
     */
    private IntSupplier baselineMaxOperations = () -> 1;
    /**
     * Function to allow for post-processing {@link #calculateOperationsThisTick(OperationTracker)}.
     *
     * @implNote Defaults to doing nothing.
     */
    private Consumer<OperationTracker> postProcessOperations = tracker -> {
    };
    /**
     * Called when the errors for this cached recipe changes.
     *
     * @implNote Defaults to doing nothing.
     */
    private Consumer<Set<RecipeError>> onErrorsChange = errors -> {
    };

    /**
     * Ticks the machine has spent processing so far.
     */
    private int operatingTicks;
    /**
     * Allows for cached recipe holders to have handling for when the operating ticks changed, for example to allow the machine to know that the number of operating ticks
     * has changed and sync the progress in containers to clients.
     *
     * @implNote Defaults to doing nothing.
     */
    private IntConsumer operatingTicksChanged = ticks -> {
    };

    /**
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     */
    protected CachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors) {
        this.recipe = Objects.requireNonNull(recipe, "Recipe cannot be null.");
        this.recheckAllErrors = Objects.requireNonNull(recheckAllErrors, "Recheck all errors supplier cannot be null.");
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
    public CachedRecipe<RECIPE> setEnergyRequirements(LongSupplier perTickEnergy, IEnergyContainer energyContainer) {
        //TODO: Re-evaluate if we want to change this to a system similar to the InputHandler, so that we can simulate extracting energy
        // from our container, it likely is not worth it as if we make the assumption we can extract all stored energy it cuts down on
        // processing. If we move the energy requirement checks to after checking about inputs it may become worthwhile
        this.perTickEnergy = Objects.requireNonNull(perTickEnergy, "The per tick energy cannot be null.");
        Objects.requireNonNull(energyContainer, "Energy container cannot be null.");
        this.storedEnergy = energyContainer::getEnergy;
        this.useEnergy = energy -> energyContainer.extract(energy, Action.EXECUTE, AutomationType.INTERNAL);
        return this;
    }

    /**
     * Sets the supplier that supplies the number of ticks required to complete the recipe.
     *
     * @param requiredTicks Supplies the number of ticks required to complete this recipe.
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
     * Sets the callback that is run when this {@link CachedRecipe} completes processing of a recipe. This allows the {@link CachedRecipe} holder to do any extra handling
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
     * Sets the supplier that supplies the baseline maximum number of operations that can be performed each tick if everything is working properly.
     *
     * @param baselineMaxOperations Supplies the baseline max number of operations per tick.
     *
     * @apiNote {@code baselineMaxOperations} should return a value of at least one.
     * <br>
     * If this method is not used, the {@code baselineMaxOperations} of this {@link CachedRecipe} defaults to returning one.
     */
    public CachedRecipe<RECIPE> setBaselineMaxOperations(IntSupplier baselineMaxOperations) {
        this.baselineMaxOperations = Objects.requireNonNull(baselineMaxOperations, "Baseline max operations cannot be null.");
        return this;
    }

    /**
     * Sets the callback used to post-process the number of operations this {@link CachedRecipe} calculated it can perform in
     * {@link #calculateOperationsThisTick(OperationTracker)} and to add any {@link RecipeError}s that occur during post-processing.
     * <p>
     * It can be assumed that when {@code postProcessOperations} is called {@link OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param postProcessOperations Function that applies post-processing to the result of {@link #calculateOperationsThisTick(OperationTracker)}.
     *
     * @apiNote If this method is not used, {@code postProcessOperations} for this {@link CachedRecipe} defaults to doing nothing as the baseline already has the maximum
     * amount of operations we can actually perform.
     */
    public CachedRecipe<RECIPE> setPostProcessOperations(Consumer<OperationTracker> postProcessOperations) {
        this.postProcessOperations = Objects.requireNonNull(postProcessOperations, "Post processing of the operation count cannot be null.");
        return this;
    }

    /**
     * Sets the callback that run when the set of known {@link RecipeError}s of this {@link CachedRecipe} changes.
     *
     * @param onErrorsChange Consumer to call with the set of {@link RecipeError}s this {@link CachedRecipe} when they change.
     *
     * @apiNote If this method is not used, this {@link CachedRecipe} defaults to not notifying any holder when the errors change.
     */
    public CachedRecipe<RECIPE> setErrorsChanged(Consumer<Set<RecipeError>> onErrorsChange) {
        this.onErrorsChange = Objects.requireNonNull(onErrorsChange, "On errors change consumer cannot be null.");
        return this;
    }

    /**
     * Updates the known errors to the given set, and calls {@link #onErrorsChange} if the errors actually changed.
     */
    private void updateErrors(Set<RecipeError> errors) {
        //Validate the errors actually changed as they potentially are just the same ones we found last go around
        if (!this.errors.equals(errors)) {
            this.errors = errors;
            if (this.errors.size() > 1) {
                pausedForErrors = true;
            } else {
                pausedForErrors = !this.errors.contains(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE);
            }
            onErrorsChange.accept(errors);
        }
    }

    /**
     * Unpauses any recipes that got paused due to errors that cause processing to stop.
     *
     * @since 10.5.15
     */
    public void unpauseErrors() {
        pausedForErrors = false;
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
        // as once we start processing the cached recipe should basically be immutable
        if (pausedForErrors) {
            //Note: We just set active as false if we are paused for errors and then don't do any other processing
            setActive.accept(false);
            return;
        }
        int operations;
        if (canHolderFunction.getAsBoolean()) {
            setupVariableValues();
            OperationTracker tracker = new OperationTracker(errors, recheckAllErrors.getAsBoolean(), baselineMaxOperations.getAsInt());
            calculateOperationsThisTick(tracker);
            if (tracker.shouldContinueChecking()) {
                postProcessOperations.accept(tracker);
                //If we should continue checking try to cap the max at the max amount we have for energy that we didn't cap it at earlier
                // Note: We don't have to always try and cap it as if we shouldn't continue checking that means we are already stopped.
                if (tracker.shouldContinueChecking() && tracker.capAtMaxForEnergy()) {
                    //If we lowered the maximum number of operations due to our available energy, then we add an error that we don't have
                    // enough energy to run at our maximum rate
                    tracker.addError(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE);
                }
            }
            operations = tracker.currentMax;
            if (tracker.hasErrorsToCopy()) {
                updateErrors(tracker.errors);
            }
        } else {
            operations = 0;
            if (!errors.isEmpty()) {
                updateErrors(Collections.emptySet());
            }
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
    protected int getOperatingTicks() {
        return operatingTicks;
    }

    /**
     * Called each tick to allow for {@link CachedRecipe}s to consume any per tick resources.
     *
     * @param operations Number of operations being performed.
     *
     * @implNote It is safe to assume that {@link #calculateOperationsThisTick(OperationTracker)} will have been called before this method and there will be at least one
     * operation being performed. This means that caching of types can be done inside of {@link #calculateOperationsThisTick(OperationTracker)} and safely used here.
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
        long energy = perTickEnergy.getAsLong();
        if (operations == 1) {
            //While floating long will short circuit any calculations if multiplied by one given we require making a copy to ensure we don't
            // modify the source value, if we do the check here manually as well, then we can skip creating unnecessary objects
            useEnergy.accept(energy);
        } else {
            useEnergy.accept(energy * operations);
        }
    }

    /**
     * Calculates the number of operations this {@link CachedRecipe} can perform this tick based on the various inputs available to the recipe and space left in the
     * outputs and updates the given operation tracker. If this {@link CachedRecipe} runs into any problems while processing it should also track the corresponding
     * {@link RecipeError} using {@link OperationTracker#addError(RecipeError)}.
     *
     * @param tracker Tracker of current errors and max operations.
     *
     * @implNote While max operations per energy is tracked here, it only is incorporated into the max number of operations if none can be performed. Otherwise, it gets
     * incorporated after this method is called.
     */
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        if (tracker.shouldContinueChecking()) {
            long energyPerTick = perTickEnergy.getAsLong();
            //If we don't have an energy requirement return what we were told the max is
            if (energyPerTick != 0L) {
                //Make sure we don't have any integer overflow in calculating how much we have room for
                int operations = MathUtils.clampToInt(storedEnergy.getAsLong() / energyPerTick);
                //Update the max amount we can perform from our energy (we apply this at the end so that we can see if we have a reduced
                // operation count due to energy
                tracker.maxForEnergy = operations;
                if (operations == 0) {
                    //If we have no operations we can perform however, just update it immediately
                    tracker.updateOperations(operations);
                    tracker.addError(RecipeError.NOT_ENOUGH_ENERGY);
                }
            }
        }
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
     * @implNote It is safe to assume that {@link #calculateOperationsThisTick(OperationTracker)} will have been called before this method and there will be at least one
     * operation being performed. This means that caching of types and outputs can be done inside of {@link #calculateOperationsThisTick(OperationTracker)} and safely
     * used here.
     */
    protected abstract void finishProcessing(int operations);

    /**
     * Gets the actual recipe object this {@link CachedRecipe} has cached and operates on.
     */
    public RECIPE getRecipe() {
        return recipe;
    }

    /**
     * Class for keeping track of the number of max operations that can be performed by the {@link CachedRecipe}, and any errors that it has run into while calculating
     * how many operations it can perform at once.
     *
     * @see CachedRecipe#calculateOperationsThisTick(OperationTracker)
     */
    public static final class OperationTracker {

        /**
         * Magic number for when a recipe should reset its progress.
         */
        private static final int RESET_PROGRESS = -1;
        /**
         * Magic number for when a recipe does not match at all, and the progress should be reset and errors cleared.
         */
        private static final int MISMATCHED_RECIPE = -2;

        /**
         * Set of all the errors from when the {@link CachedRecipe} last calculated the complete set of errors.
         */
        private final Set<RecipeError> lastErrors;
        /**
         * Set of errors that the {@link CachedRecipe} has run into so far while calculating how many operations it can perform at once.
         */
        private Set<RecipeError> errors = Collections.emptySet();
        /**
         * Used to determine if this tracker should try and check for all existing errors ({@code true}) or short circuit as soon as it knows no processing can be done.
         */
        private boolean checkAll;
        /**
         * Caches whether we have checked if {@link #lastErrors} contains all {@link #errors}.
         *
         * @implNote Starts at {@code true} as {@link #lastErrors} will always contain the contents of an empty set (the default for new {@link #errors}).
         */
        private boolean checkedErrors = true;
        /**
         * The current maximum number of operations that the {@link CachedRecipe} can perform this tick.
         */
        private int currentMax;
        /**
         * The current maximum number of operations that the {@link CachedRecipe} can perform this tick due to available energy.
         */
        private int maxForEnergy;

        /**
         * @param lastErrors  Set of the last errors the {@link CachedRecipe} had.
         * @param checkAll    {@code true} if this tracker should try and check for all existing errors.
         * @param startingMax Starting maximum number of operations that the {@link CachedRecipe} can perform this tick.
         */
        private OperationTracker(Set<RecipeError> lastErrors, boolean checkAll, int startingMax) {
            this.lastErrors = lastErrors;
            this.checkAll = checkAll;
            this.currentMax = startingMax;
            this.maxForEnergy = currentMax;
        }

        /**
         * @return {@code true} if the errors from this {@link OperationTracker} should be copied to the {@link ICachedRecipeHolder}.
         */
        private boolean hasErrorsToCopy() {
            if (currentMax == MISMATCHED_RECIPE) {
                errors = Collections.emptySet();
                //Return true as if we have a mismatched recipe we want to clear any errors that exist
                return true;
            } else if (checkAll || currentMax > 0) {
                //Return true if we were checking everything, or we have operations to perform as that means
                // we did a full check so we either have no errors left, or we have some and are just running
                // at a reduced rate.
                return true;
            }
            //If we haven't checked for errors, and we have errors that we didn't have the last time we checked
            // mark that we have errors we want to copy over.
            // Note: We don't need to check if errors is not empty as checkedErrors will be true if we don't
            // have any errors
            return !checkedErrors && !lastErrors.containsAll(errors);
        }

        /**
         * @return {@code true} if the {@link CachedRecipe} should continue calculating how many operations can be performed this tick.
         *
         * @see CachedRecipe#calculateOperationsThisTick(OperationTracker)
         */
        public boolean shouldContinueChecking() {
            if (currentMax > 0) {
                //If we can still perform at least one operation, that means we need to continue checking if the cached recipe can process
                return true;
            } else if (currentMax == 0) {
                //If we are going to perform zero operations (paused but not resetting), we want to check if we should continue
                // checking for errors or not.
                if (checkAll) {
                    //If we are wanting to check all the errors because of it being a while since we last checked continue checking.
                    return true;
                } else if (!checkedErrors) {
                    //If we haven't compared our current errors since we have added some to the last set of errors the recipe had
                    // check if the last set of errors contains all of our current errors.
                    if (!lastErrors.containsAll(errors)) {
                        // if it doesn't we set checkAll to true as we know we have some new errors and want to be able to collect
                        // them all, and this way we will be able to shortcut any further checks to if our last errors has all our
                        // current errors as we know it will be true.
                        checkAll = true;
                        return true;
                    }
                    // if our last set of errors does contain all our current errors, just mark that we compared the errors so that
                    // we can short circuit past checking them on the next shouldContinueChecking call as we want this method to be
                    // cheap to call as it is called a lot.
                    checkedErrors = true;
                }
            }
            return false;
        }

        /**
         * Updates the maximum number of operations the {@link CachedRecipe} can currently perform.
         *
         * @param max Maximum number of operations. A value of zero means that no operations will take place, while a value less than zero means that the current recipe
         *            progress will be reset.
         *
         * @return {@code true} if the maximum number of operations decreased as a result of this method call.
         *
         * @apiNote This method can only reduce the maximum number of operations, it cannot increase it.
         * <br>
         * It is recommended to use one of the specific reset methods ({@link #mismatchedRecipe()}, {@link #resetProgress(RecipeError)}) rather than using this method to
         * reset the recipe's progress.
         */
        public boolean updateOperations(int max) {
            if (max < currentMax) {
                currentMax = max;
                return true;
            }
            return false;
        }

        /**
         * Updates the maximum number of operations the {@link CachedRecipe} can currently perform to the maximum number that was calculated based on available energy.
         *
         * @return {@code true} if the maximum number of operations decreased as a result of this method call.
         *
         * @apiNote This method can only reduce the maximum number of operations, it cannot increase it.
         */
        private boolean capAtMaxForEnergy() {
            return updateOperations(maxForEnergy);
        }

        /**
         * Marks that the recipe doesn't match and that both progress and all errors should be reset.
         */
        public void mismatchedRecipe() {
            //TODO - 1.18: Should mismatched recipe even exist (beyond use in rotary condensentrator) given otherwise it may be input doesn't produce output
            // or no matching recipe though actually that maybe only should be done if there is something in the output
            updateOperations(MISMATCHED_RECIPE);
        }

        /**
         * Marks that the recipe's progress should be reset and adds the given {@link RecipeError} to the set of errors that caused the recipe to reset.
         *
         * @param error Error to add.
         */
        public void resetProgress(RecipeError error) {
            //Use update operations so that we don't override mismatched recipe with reset progress
            updateOperations(RESET_PROGRESS);
            addError(error);
        }

        /**
         * Adds a {@link RecipeError} to the set of errors stopping this recipe from operating at peak efficiency (or potentially at all).
         *
         * @param error Error to add.
         */
        public void addError(RecipeError error) {
            Objects.requireNonNull(error, "Error cannot be null.");
            if (errors.isEmpty()) {
                //If our set of errors is empty, then it is the default empty set, and we need to initialize it
                // Note: As we expect to have a small number of errors, we use an array set as it should be more efficient
                errors = new ObjectArraySet<>();
            }
            //Add the error to our known errors
            if (errors.add(error)) {
                // and if we didn't already know about that error, mark that we haven't compared our current errors
                // to the previous set of errors the cached recipe had
                checkedErrors = false;
            }
        }

        /**
         * Marker class for errors {@link CachedRecipe}s may run into when processing a recipe.
         */
        public static final class RecipeError {

            /**
             * Common representation for when a recipe errors due to the inputs not producing what is currently in the output.
             */
            public static final RecipeError INPUT_DOESNT_PRODUCE_OUTPUT = create();
            /**
             * Common representation for when a recipe errors due to not having energy.
             */
            public static final RecipeError NOT_ENOUGH_ENERGY = create();
            /**
             * Common representation for when a recipe does not have enough energy to run at full speed but is still able to process at a reduced rate.
             */
            public static final RecipeError NOT_ENOUGH_ENERGY_REDUCED_RATE = create();
            /**
             * Common representation for when a recipe errors due to not having enough of the input.
             */
            public static final RecipeError NOT_ENOUGH_INPUT = create();
            /**
             * Common representation for when a recipe errors due to not having enough of the secondary input.
             */
            public static final RecipeError NOT_ENOUGH_SECONDARY_INPUT = create();
            /**
             * Common representation for when a recipe errors due to not having enough of the left input.
             */
            public static final RecipeError NOT_ENOUGH_LEFT_INPUT = create();
            /**
             * Common representation for when a recipe errors due to not having enough of the right input.
             */
            public static final RecipeError NOT_ENOUGH_RIGHT_INPUT = create();
            /**
             * Common representation for when a recipe errors due to not having enough space in the output.
             */
            public static final RecipeError NOT_ENOUGH_OUTPUT_SPACE = create();

            /**
             * Creates a new marker for tracking an error.
             */
            public static RecipeError create() {
                //noinspection InstantiationOfUtilityClass
                return new RecipeError();
            }

            private RecipeError() {
            }
        }
    }
}